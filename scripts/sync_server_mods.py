#!/usr/bin/env python3
"""Sync instance mods -> dedicated server mods, excluding client-only mods.

Copies every active .jar from the client instance's mods/ folder to the
server's mods/ folder, EXCEPT known client-only mods (rendering/shaders,
HUD/GUI/input, client audio/animation, minimap, JEI). Also prunes server
jars that are no longer wanted (removed from the instance, or client-only),
so the server always equals "instance minus exclusions".

.disabled files are ignored on both sides.

Usage:
    python scripts/sync_server_mods.py            # do the sync
    python scripts/sync_server_mods.py --dry-run  # show what would change

Exclusions are matched by lowercase filename PREFIX so version bumps stay
excluded (e.g. "oculus-" matches oculus-mc1.20.1-1.8.0.jar and any update).
See docs/SERVER_MODS.md for the rationale per mod.
"""

import argparse
import shutil
import sys
from pathlib import Path

# Instance root = parent of this script's folder
INSTANCE_MODS = Path(__file__).resolve().parent.parent / "mods"
SERVER_MODS = Path(r"E:\Program Files (x86)\CurseForge\mc\Servers\spookydookie\mods")

# Client-only mods (lowercase filename prefixes). Two categories:
#   crash  = loads client classes on a dedicated server -> boot crash
#   inert  = no server function (HUD/GUI/input/audio/display)
CLIENT_ONLY_PREFIXES = [
    # rendering / shaders / visual FX — crash a dedicated server
    "embeddium-",
    "oculus-",
    "sodiumdynamiclights-",
    "sodiumoptionsapi-",
    "immediatelyfast-",
    "cavedust-",          # confirmed crash 2026-07-12 (client.gui Screen)
    "voidfog-",           # confirmed crash 2026-07-12 (client FogRenderer)
    # client HUD / GUI / input
    "betterf3-",
    "borderlesswindow-",
    "blur-",
    "controlling-",
    "mousetweaks-",
    "legendarytooltips-",
    "moreoverlays-",
    "chat_heads-",
    # client animation / audio
    "mobends-",
    "notenoughanimations-",
    "ambientsounds_",
    "sound-physics-remastered-",
    "presencefootsteps-",
    # client-only utilities
    "jei-",               # recipe viewer
    "xaerominimap-",      # minimap client; world map stays server-side
]


def is_client_only(jar_name: str) -> bool:
    name = jar_name.lower()
    return any(name.startswith(p) for p in CLIENT_ONLY_PREFIXES)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--dry-run", action="store_true",
                        help="report changes without copying/deleting")
    args = parser.parse_args()

    for d, label in ((INSTANCE_MODS, "instance"), (SERVER_MODS, "server")):
        if not d.is_dir():
            print(f"ERROR: {label} mods dir not found: {d}")
            return 1

    instance_jars = {p.name: p for p in INSTANCE_MODS.glob("*.jar")}
    server_jars = {p.name: p for p in SERVER_MODS.glob("*.jar")}

    wanted = {n: p for n, p in instance_jars.items() if not is_client_only(n)}
    excluded = sorted(n for n in instance_jars if is_client_only(n))

    to_copy = sorted(n for n in wanted if n not in server_jars)
    # re-copy if size differs (jar replaced in-place with same name)
    to_update = sorted(
        n for n in wanted
        if n in server_jars and wanted[n].stat().st_size != server_jars[n].stat().st_size
    )
    to_prune = sorted(n for n in server_jars if n not in wanted)

    verb = "would " if args.dry_run else ""
    print(f"instance active jars : {len(instance_jars)}")
    print(f"client-only excluded : {len(excluded)}")
    print(f"server target set    : {len(wanted)}")
    print(f"server currently has : {len(server_jars)}")
    print()

    for n in to_copy:
        print(f"  {verb}copy   + {n}")
        if not args.dry_run:
            shutil.copy2(wanted[n], SERVER_MODS / n)
    for n in to_update:
        print(f"  {verb}update ~ {n}")
        if not args.dry_run:
            shutil.copy2(wanted[n], SERVER_MODS / n)
    for n in to_prune:
        reason = "client-only" if is_client_only(n) else "not in instance"
        print(f"  {verb}prune  - {n}  ({reason})")
        if not args.dry_run:
            (SERVER_MODS / n).unlink()

    if not (to_copy or to_update or to_prune):
        print("Nothing to do — server already in sync.")
    else:
        print(f"\n{len(to_copy)} copied, {len(to_update)} updated, "
              f"{len(to_prune)} pruned" + (" (dry run)" if args.dry_run else ""))

    # post-check: server should now exactly equal the wanted set
    if not args.dry_run:
        final = {p.name for p in SERVER_MODS.glob("*.jar")}
        if final == set(wanted):
            print(f"VERIFIED: server mods == instance minus exclusions ({len(final)} jars)")
        else:
            print("WARNING: mismatch after sync!")
            for n in sorted(set(wanted) - final):
                print(f"  missing on server: {n}")
            for n in sorted(final - set(wanted)):
                print(f"  unexpected on server: {n}")
            return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
