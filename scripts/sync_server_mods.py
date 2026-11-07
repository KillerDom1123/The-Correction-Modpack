#!/usr/bin/env python3
"""Sync the client instance's mods to the dedicated server: the live VM (over FTP) or a local server folder.

The server's mods/ must equal the client's active jars minus the client-only list below, or players are
refused at login. The script lists the changes, then:
  - copies (uploads) jars the server lacks, or holds with different content under the same name;
  - takes jars the server should no longer load out of mods/ without deleting them: renamed to
    <name>.disabled when the client has that jar disabled too, otherwise moved to mods-removed/ beside mods/.
Files already ending in .disabled are left alone on both sides.

Usage:
    python scripts/sync_server_mods.py              # the live VM: show the changes, ask, then apply
    python scripts/sync_server_mods.py --dry-run    # show the changes only
    python scripts/sync_server_mods.py --yes        # apply without asking
    python scripts/sync_server_mods.py --server "E:/.../Servers/Server v2"   # a local server folder (test bed)

The VM: the FTP login comes from ~/.spooky_ftp_netrc (netrc format; another file via SPOOKY_FTP_NETRC). The host
is that file's only machine entry, or SPOOKY_FTP_HOST. The server lives in files/server under the FTP login.
Changes take effect when the server restarts. `Servers/spookydookie` (July 2026) is never synced.

Content check: a local folder compares SHA-256. The VM compares sizes, because FTP has no checksum command, so
a jar rebuilt under the same name and size would be missed there. The pack's build scripts name each jar
after its version: bump the version when the code changes.

Client-only jars are matched by lowercase filename PREFIX, so version bumps stay excluded.
tools/generate_server_modlist.py prints the groups and notes below in docs/SERVER_MODS.md.
"""

import argparse
import ftplib
import hashlib
import netrc
import os
import shutil
import sys
from pathlib import Path

INSTANCE_MODS = Path(__file__).resolve().parent.parent / "mods"
SERVERS_ROOT = Path(r"E:\Program Files (x86)\CurseForge\mc\Servers")
NETRC = Path(os.environ.get("SPOOKY_FTP_NETRC") or Path.home() / ".spooky_ftp_netrc")
VM_ROOT = "files/server"      # relative to the FTP login directory
REMOVED = "mods-removed"      # beside mods/, on the server

# Client-only jars: (group, [(lowercase filename prefix, note)]). Two kinds:
#   crash = loads client classes on a dedicated server -> boot crash
#   inert = no server function (HUD/GUI/input/audio/display)
CLIENT_ONLY_GROUPS = [
    ("Rendering / shaders / visual FX — hard-crash a dedicated server", [
        ("embeddium-", ""),
        ("oculus-", ""),
        ("sodiumdynamiclights-", ""),
        ("sodiumoptionsapi-", ""),
        ("immediatelyfast-", ""),
        ("cavedust-", "confirmed server crash 2026-07-12 (loads `client.gui.screens.Screen`)"),
        ("voidfog-", "confirmed server crash 2026-07-12 (loads `client.renderer.FogRenderer`)"),
    ]),
    ("Client HUD / GUI / input — no server function", [
        ("betterf3-", ""),
        ("borderlesswindow-", ""),
        ("blur-", ""),
        ("controlling-", ""),
        ("mousetweaks-", ""),
        ("legendarytooltips-", ""),
        ("moreoverlays-", ""),
        ("chat_heads-", ""),
        ("simplezoom-", "hold-to-zoom key (C); client mixins only"),
        ("configured-", "in-game config screens (JEI's wrench); kept off the server so server configs "
                        "can't be edited from the client; its channel accepts a missing peer"),
    ]),
    ("Client animation / audio", [
        ("mobends-", ""),
        ("notenoughanimations-", ""),
        ("ambientsounds_", ""),
        ("sound-physics-remastered-", ""),
        ("presencefootsteps-", ""),
    ]),
    # JEI is NOT client-only: its server half moves the items for the recipe "+" button (and gives items to
    # ops/creative). Without it on the server the button fails with "Server must have JEI installed".
    ("Client-only utilities", [
        ("xaerominimap-", "minimap; the world map's server component is kept"),
        ("farsight_immptl_compat-", "pack-built; source in `tools/farsight_immptl_compat/`, restores Farsight's "
                                    "chunk keeping under Immersive Portals"),
    ]),
]
CLIENT_ONLY_PREFIXES = [p for _, items in CLIENT_ONLY_GROUPS for p, _ in items]


def client_only_entry(jar_name: str) -> tuple[str, str] | None:
    """(group, note) for a client-only jar, else None."""
    name = jar_name.lower()
    for group, items in CLIENT_ONLY_GROUPS:
        for prefix, note in items:
            if name.startswith(prefix):
                return group, note
    return None


def is_client_only(jar_name: str) -> bool:
    return client_only_entry(jar_name) is not None


def _sha256(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


class LocalServer:
    """A server folder on this machine."""

    def __init__(self, root: Path):
        self.root = root
        self.label = str(root)
        self.is_vm = False

    def exists(self, sub: str = "mods") -> bool:
        return (self.root / sub).is_dir()

    def listing(self, sub: str = "mods") -> dict[str, tuple[str, int]]:
        """name -> (kind 'd' or '-', size) for the folder's entries."""
        d = self.root / sub
        if not d.is_dir():
            return {}
        return {p.name: ("d", 0) if p.is_dir() else ("-", p.stat().st_size) for p in d.iterdir()}

    def local_path(self, name: str) -> Path | None:
        return self.root / "mods" / name

    def same(self, name: str, src: Path, size: int) -> bool:
        return size == src.stat().st_size and _sha256(self.root / "mods" / name) == _sha256(src)

    def put(self, src: Path, name: str) -> None:
        part = self.root / "mods" / f"{name}.part"
        shutil.copy2(src, part)
        os.replace(part, self.root / "mods" / name)

    def move(self, src_rel: str, dst_rel: str) -> None:
        (self.root / dst_rel).parent.mkdir(parents=True, exist_ok=True)
        os.replace(self.root / src_rel, self.root / dst_rel)

    def close(self) -> None:
        pass


class VmServer:
    """The live server on the Proxmox VM, over FTP."""

    def __init__(self):
        try:
            rc = netrc.netrc(NETRC)
        except FileNotFoundError:
            sys.exit(f"No FTP login file at {NETRC} (netrc format; or set SPOOKY_FTP_NETRC).")
        host = os.environ.get("SPOOKY_FTP_HOST") or (next(iter(rc.hosts)) if len(rc.hosts) == 1 else None)
        auth = rc.authenticators(host) if host else None
        if not auth:
            sys.exit(f"{NETRC} must hold exactly one machine entry, or set SPOOKY_FTP_HOST.")
        self.ftp = ftplib.FTP(host, timeout=120)
        self.ftp.encoding = "utf-8"
        self.ftp.login(auth[0], auth[2])
        self.home = self.ftp.pwd()
        self.label = f"VM ftp://{host}/{VM_ROOT}"
        self.is_vm = True

    def exists(self, sub: str = "mods") -> bool:
        try:
            self.ftp.cwd(f"{VM_ROOT}/{sub}")
            return True
        except ftplib.error_perm:
            return False
        finally:
            self.ftp.cwd(self.home)

    def listing(self, sub: str = "mods") -> dict[str, tuple[str, int]]:
        lines: list[str] = []
        self.ftp.retrlines(f"LIST -a {VM_ROOT}/{sub}", lines.append)
        out = {}
        for line in lines:
            parts = line.split(None, 8)   # perms links owner group size month day time/year name
            if len(parts) < 9 or parts[8] in (".", ".."):
                continue
            out[parts[8]] = (parts[0][0], int(parts[4]))
        return out

    def local_path(self, name: str) -> Path | None:
        return None

    def same(self, name: str, src: Path, size: int) -> bool:
        return size == src.stat().st_size

    def put(self, src: Path, name: str) -> None:
        part = f"{VM_ROOT}/mods/{name}.part"   # Forge ignores *.part, so a broken upload never loads
        with open(src, "rb") as f:
            self.ftp.storbinary(f"STOR {part}", f)
        self.ftp.rename(part, f"{VM_ROOT}/mods/{name}")

    def move(self, src_rel: str, dst_rel: str) -> None:
        parent = dst_rel.rpartition("/")[0]
        if parent and not self.exists(parent):
            self.ftp.mkd(f"{VM_ROOT}/{parent}")
        self.ftp.rename(f"{VM_ROOT}/{src_rel}", f"{VM_ROOT}/{dst_rel}")

    def close(self) -> None:
        try:
            self.ftp.quit()
        except (ftplib.all_errors):
            pass


def open_server(path: Path | None):
    """The server to work on: a local folder when a path is given, else the VM."""
    return LocalServer(path) if path else VmServer()


def add_target_args(parser: argparse.ArgumentParser) -> None:
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--vm", action="store_true", help="the live VM over FTP (the default)")
    group.add_argument("--server", type=Path, help="a local server folder instead, e.g. Servers/Server v2")


def jar_files(listing: dict[str, tuple[str, int]], suffix: str = ".jar") -> dict[str, int]:
    return {n: s for n, (k, s) in listing.items() if k == "-" and n.lower().endswith(suffix)}


def sync(server, dry_run: bool, yes: bool) -> int:
    print(f"server target        : {server.label}")
    if not INSTANCE_MODS.is_dir():
        print(f"ERROR: instance mods dir not found: {INSTANCE_MODS}")
        return 1
    if not server.exists("mods"):
        print("ERROR: the server has no mods/ folder")
        return 1

    instance = {p.name: p for p in INSTANCE_MODS.glob("*.jar")}
    client_disabled = {p.name[: -len(".disabled")] for p in INSTANCE_MODS.glob("*.jar.disabled")}
    listing = server.listing("mods")
    server_jars = jar_files(listing)

    wanted = {n: p for n, p in instance.items() if not is_client_only(n)}
    excluded = sorted(n for n in instance if is_client_only(n))
    to_copy = sorted(n for n in wanted if n not in server_jars)
    to_update = sorted(n for n in wanted if n in server_jars and not server.same(n, wanted[n], server_jars[n]))
    to_remove = sorted(n for n in server_jars if n not in wanted)

    removed_names = set(server.listing(REMOVED)) if server.exists(REMOVED) else set()
    moves = []   # (name, destination relative to the server root, reason)
    for n in to_remove:
        reason = "client-only" if is_client_only(n) else (
            "disabled in the client" if n in client_disabled else "not in the client")
        if n in client_disabled and f"{n}.disabled" not in listing:
            moves.append((n, f"mods/{n}.disabled", reason))
        else:
            dest = n
            i = 1
            while dest in removed_names:
                dest = f"{n}.{i}"
                i += 1
            removed_names.add(dest)
            moves.append((n, f"{REMOVED}/{dest}", reason))

    print(f"instance active jars : {len(instance)}")
    print(f"client-only excluded : {len(excluded)}")
    print(f"server target set    : {len(wanted)}")
    print(f"server currently has : {len(server_jars)}")
    print()
    for n in to_copy:
        print(f"  copy    + {n}")
    for n in to_update:
        print(f"  update  ~ {n}")
    for n, dest, reason in moves:
        print(f"  remove  - {n}  ({reason}) -> {dest}")

    changes = len(to_copy) + len(to_update) + len(moves)
    if not changes:
        print("Nothing to do: the server already matches the client.")
        return 0
    print(f"\n{len(to_copy)} to copy, {len(to_update)} to update, {len(moves)} to take out of mods/")
    if dry_run:
        print("(dry run: nothing changed)")
        return 0
    if server.is_vm and not yes:
        if not sys.stdin.isatty():
            print("Not applied: no terminal to ask in. Re-run with --yes to apply.")
            return 1
        if input(f"Apply these {changes} changes to the VM? [y/N] ").strip().lower() not in ("y", "yes"):
            print("Not applied.")
            return 1

    for n in to_copy + to_update:
        print(f"  {'uploading' if server.is_vm else 'copying'} {n}")
        server.put(wanted[n], n)
    for n, dest, _ in moves:
        server.move(f"mods/{n}", dest)
        print(f"  moved {n} -> {dest}")

    final = jar_files(server.listing("mods"))
    bad_size = sorted(n for n in wanted if n in final and final[n] != wanted[n].stat().st_size)
    if set(final) == set(wanted) and not bad_size:
        print(f"VERIFIED: server mods == instance minus exclusions ({len(final)} jars)")
    else:
        print("WARNING: mismatch after sync!")
        for n in sorted(set(wanted) - set(final)):
            print(f"  missing on server: {n}")
        for n in sorted(set(final) - set(wanted)):
            print(f"  unexpected on server: {n}")
        for n in bad_size:
            print(f"  size differs: {n}")
        return 1
    if server.is_vm:
        print("Restart the server for the changes to take effect.")
    print("Then regenerate docs/SERVER_MODS.md: python tools/generate_server_modlist.py")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    add_target_args(parser)
    parser.add_argument("--dry-run", action="store_true", help="show the changes without making them")
    parser.add_argument("--yes", "-y", action="store_true", help="apply to the VM without asking")
    args = parser.parse_args()
    server = open_server(args.server)
    try:
        return sync(server, args.dry_run, args.yes)
    finally:
        server.close()


if __name__ == "__main__":
    sys.exit(main())
