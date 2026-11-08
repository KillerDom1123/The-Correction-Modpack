#!/usr/bin/env python3
"""Generate docs/SERVER_MODS.md from the dedicated server's actual mods/ folder.

Reuses the parser and CATEGORY_RULES of tools/generate_modlist.py, and the server access and client-only
groups of scripts/sync_server_mods.py, so the three stay consistent.

Usage:
    python tools/generate_server_modlist.py                     # the live VM, over FTP (the default)
    python tools/generate_server_modlist.py --server "<path>"   # a local server folder, e.g. Servers/Server v2
Run it after scripts/sync_server_mods.py.

On the VM the jars are listed over FTP and their names and versions are read from the client instance's copy of
the same file (parity means they are identical); a jar the client lacks is listed from its filename.
Everything except NOTES below is read from the server and the client instance.
"""
import argparse
import importlib.util
import re
import sys
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.dont_write_bytecode = True


def load(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


gm = load("generate_modlist", ROOT / "tools" / "generate_modlist.py")
sync = load("sync_server_mods", ROOT / "scripts" / "sync_server_mods.py")


def natural(s: str):
    # "47.4.10" > "47.4.9": digit runs compare as numbers.
    return [int(t) if i % 2 else t for i, t in enumerate(re.split(r"(\d+)", s))]

# Hand-maintained: printed as-is. Update when the facts change.
NOTES = """\
> **Notes** (hand-maintained in `tools/generate_server_modlist.py`; everything else here is generated):
>
> - **Live server:** the Proxmox VM since 2026-11-06, deployed over FTP by `scripts/sync_server_mods.py`.
>   `Servers/Server v2` (built 2026-11-02) is a local test bed (`--server`), and `Servers/spookydookie` (July 2026,
>   TFC-era) is kept unchanged for reference; neither is synced by default.
> - **Runtime:** the VM runs Java 21 (Ubuntu OpenJDK 21.0.12, `-Xms8G -Xmx32G`, G1). Forge 1.20.1 targets Java 17.
> - **Pinned on both sides — never sync newer versions:** `chunksending-1.20.1-2.8` (4.1 never delivers chunks
>   under DimThreads), `farsight-1.20.1-5.1`, `hardcoretruedarkness-4.0.0` (v6 blacks out the view under shaders and
>   is a hard client/server protocol gate). The newer jars are parked in the client's `local/mods-rolled-back-2026-11-01/`.
> - **Pack-built** (category *Local Fixes*, source in `tools/`): `spooky_tick_fixes` holds the server-tick fixes, the
>   activation range, the chunk guards and chunk-safe ticking, and (1.4.0) the provoke-only Wendigo (config
>   `config/spooky_tick_fixes-common.toml`), on clients and the server. `spooky_pregen` runs Chunky while the server
>   is empty (config `config/spooky_pregen.json`, `/pregen`); it is inert on clients. `spooky_ultimine_shapes` adds
>   FTB Ultimine shapes and must match on both sides. `farsight_immptl_compat` is client-only.
>   `immptl_dimthread_compat` is disabled with DimThreads.
> - **DimThreads disabled 2026-11-06:** version 1.2.1 ticks every dimension under one global lock (no parallelism), and
>   a 46-minute profile with it off was no slower. Its jar and `immptl_dimthread_compat` are `.jar.disabled` on both sides.
> - **Server-side helpers added 2026-11-06:** Let Me Despawn (+ its library Almanac) lets mobs that picked up items
>   despawn normally; Dynamic View (`dynview`) lowers view/simulation distance when ticks run long.
> - **Removed / disabled 2026-11-06:** Data Anchor (an orphan library with a strict network channel, so it came off
>   every client too) and The Man From The Fog (disabled in the client and on the VM).
> - **JEI is on the server too (2026-11-07):** its "+" button, which moves a recipe's ingredients into a crafting grid,
>   needs JEI's server half. It used to be on the client-only list, and the button failed with "Server must have JEI
>   installed".
> - **Kept despite being client-leaning** (harmless idling on a server, and dropping them risked a dependency or
>   client refusal): `Camerapture`, `farsight`, `sounds_of_the_forest`, `clientcrafting`, plus client-side libraries
>   (`athena`, `fusion`, `Iceberg`, `Prism`, `CreativeCore`).
"""


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    sync.add_target_args(ap)
    args = ap.parse_args()
    server = sync.open_server(args.server)
    try:
        return generate(server, args.server)
    finally:
        server.close()


def generate(server, server_path: Path | None) -> int:
    if not server.exists("mods"):
        print(f"no mods/ folder on {server.label}", file=sys.stderr)
        return 1
    listing = server.listing("mods")
    names = sorted(sync.jar_files(listing), key=str.lower)
    server_disabled = sorted(sync.jar_files(listing, ".jar.disabled"), key=str.lower)

    jars, from_filename = [], []
    for n in names:
        src = server.local_path(n) or ROOT / "mods" / n
        if src.is_file():
            jars.append(gm.parse_jar(src))
        else:
            from_filename.append(n)
            jars.append({"file": n, "mc": None, "mods": [
                {"modid": "?", "name": Path(n).stem, "version": gm.version_from_filename(n)}]})

    # Installed Forge: libraries/net/minecraftforge/forge/<mc>-<forge>/
    forge_lib = "libraries/net/minecraftforge/forge"
    forge_dirs = [n for n, (k, _) in server.listing(forge_lib).items() if k == "d" and "-" in n] \
        if server.exists(forge_lib) else []
    mc, forge = max(forge_dirs, key=natural).split("-", 1) if forge_dirs else ("1.20.1", "unknown")

    client = sorted((ROOT / "mods").glob("*.jar"), key=lambda p: p.name.lower())
    excluded = [p.name for p in client if sync.is_client_only(p.name)]
    expected = {p.name for p in client if not sync.is_client_only(p.name)}
    client_disabled = sorted(p.name for p in (ROOT / "mods").glob("*.jar.disabled"))
    missing = sorted(expected - set(names), key=str.lower)
    extra = sorted(set(names) - expected, key=str.lower)

    groups: dict[str, list[dict]] = {}
    for j in jars:
        groups.setdefault(gm.categorise(j["mods"][0]["modid"], j["file"]), []).append(j)

    if server_path is None:
        where = "the live VM (`files/server` under the FTP login)"
    else:
        rel = server_path.relative_to(sync.SERVERS_ROOT.parent) \
            if server_path.is_relative_to(sync.SERVERS_ROOT.parent) else server_path
        where = f"`{rel.as_posix()}` (local folder)"

    out: list[str] = []
    w = out.append
    w("# Server Mod List — Spooky Dookie\n")
    w(f"**Server:** {where}  ")
    w(f"**Minecraft:** {mc} · **Loader:** Forge {forge}  ")
    w(f"**Active jars:** {len(names)} · **Disabled on the server:** {len(server_disabled)}  ")
    w(f"**Generated:** {date.today().isoformat()} by `tools/generate_server_modlist.py` from the server's own "
      "`mods/` folder — do not edit by hand.\n")
    w("> The server is the client instance's `mods/` **minus the client-only list** below, kept in step by")
    w("> `scripts/sync_server_mods.py`. Every jar here must match the client exactly, or players are refused at login.\n")
    if not (missing or extra):
        w(f"**Parity:** matches the client instance ({len(client)} active jars − {len(excluded)} client-only "
          f"= {len(expected)}).\n")
    else:
        w("**Parity: ⚠ differs from the client instance** — run `scripts/sync_server_mods.py`:\n")
        for n in missing:
            w(f"- missing on the server: `{n}`")
        for n in extra:
            w(f"- on the server but not wanted: `{n}`")
        w("")
    w(NOTES)
    w("---\n")
    for cat in [c for c, _ in gm.CATEGORY_RULES] + [gm.FALLBACK_CATEGORY]:
        items = groups.get(cat)
        if not items:
            continue
        w(f"## {cat} ({len(items)})\n")
        for j in sorted(items, key=lambda x: x["mods"][0]["name"].lower()):
            names_ = " + ".join(m["name"] for m in j["mods"])
            flag = " — *not in the client instance; name from the filename*" if j["file"] in from_filename else ""
            w(f"- {names_} — `{j['file']}`{flag}")
        w("")

    w("---\n")
    w(f"## Client-only mods excluded from the server ({len(excluded)})")
    w("Present in the client instance but deliberately **not** copied to the server. They set")
    w("`IGNORE_SERVER_VERSION`, so clients keep them and are not refused for the server lacking them.")
    w("The groups and notes come from `CLIENT_ONLY_GROUPS` in `scripts/sync_server_mods.py`.\n")
    by_group: dict[str, list[str]] = {}
    for n in excluded:
        group, note = sync.client_only_entry(n)
        by_group.setdefault(group, []).append(f"- {n}" + (f" — {note}" if note else ""))
    for group, _ in sync.CLIENT_ONLY_GROUPS:
        if by_group.get(group):
            w(f"**{group}:**")
            out.extend(by_group[group])
            w("")

    w("## Disabled jars")
    if server_disabled:
        w(f"**On the server** ({len(server_disabled)}) — left in `mods/` as `.jar.disabled`, not loaded:\n")
        out.extend(f"- {n}" for n in server_disabled)
        w("")
    else:
        w("**On the server:** none.\n")
    w(f"**In the client instance** ({len(client_disabled)}) — kept as `.jar.disabled`; none may be active on the server:\n")
    out.extend(f"- {n}" for n in client_disabled)
    w("")

    target = ROOT / "docs" / "SERVER_MODS.md"
    target.write_text("\n".join(out), encoding="utf-8")
    print(f"Wrote {target}: {len(names)} server jars ({where}), {len(excluded)} client-only excluded, "
          f"{len(server_disabled)} disabled on the server, {len(client_disabled)} disabled in the client.")
    if missing or extra:
        print(f"PARITY: {len(missing)} missing on the server, {len(extra)} unwanted on the server; see the document.")
    if from_filename:
        print("Not in the client instance (listed from filename):", ", ".join(from_filename))
    uncat = groups.get(gm.FALLBACK_CATEGORY, [])
    if uncat:
        print("Uncategorised:", ", ".join(j["mods"][0]["modid"] for j in uncat))
    return 0


if __name__ == "__main__":
    sys.exit(main())
