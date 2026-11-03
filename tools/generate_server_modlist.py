#!/usr/bin/env python3
"""Generate docs/SERVER_MODS.md from a dedicated server's actual mods/ folder.

Reuses the parser and CATEGORY_RULES of tools/generate_modlist.py and the client-only
prefix list of scripts/sync_server_mods.py, so the three stay consistent.

Usage:
    python tools/generate_server_modlist.py                     # current server (Server v2)
    python tools/generate_server_modlist.py --server "<path>"   # another server folder
Run it after scripts/sync_server_mods.py.
"""
import argparse
import importlib.util
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

# Grouping of the client-only exclusions, in the order the sync script lists them.
EXCLUSION_GROUPS = [
    ("Rendering / shaders / visual FX — hard-crash a dedicated server",
     ["embeddium-", "oculus-", "sodiumdynamiclights-", "sodiumoptionsapi-", "immediatelyfast-", "cavedust-", "voidfog-"]),
    ("Client HUD / GUI / input — no server function",
     ["betterf3-", "borderlesswindow-", "blur-", "controlling-", "mousetweaks-", "legendarytooltips-", "moreoverlays-", "chat_heads-"]),
    ("Client animation / audio",
     ["mobends-", "notenoughanimations-", "ambientsounds_", "sound-physics-remastered-", "presencefootsteps-"]),
    ("Client-only utilities",
     ["jei-", "xaerominimap-", "farsight_immptl_compat-"]),
]
NOTES = {
    "cavedust-": " — confirmed server crash 2026-07-12 (loads `client.gui.screens.Screen`)",
    "voidfog-": " — confirmed server crash 2026-07-12 (loads `client.renderer.FogRenderer`)",
    "jei-": " (recipe viewer)",
    "xaerominimap-": " (minimap; the world map's server component is kept)",
    "farsight_immptl_compat-": " (pack-built; source in `tools/farsight_immptl_compat/`, restores Farsight's chunk keeping under Immersive Portals)",
}


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--server", type=Path, default=sync.DEFAULT_SERVER)
    args = ap.parse_args()
    server = args.server
    mods_dir = server / "mods"
    if not mods_dir.is_dir():
        print(f"mods/ not found at {mods_dir}", file=sys.stderr)
        return 1

    jars = [gm.parse_jar(p) for p in sorted(mods_dir.glob("*.jar"), key=lambda p: p.name.lower())]
    client = sorted((ROOT / "mods").glob("*.jar"), key=lambda p: p.name.lower())
    excluded = [p.name for p in client if sync.is_client_only(p.name)]
    client_disabled = sorted(p.name for p in (ROOT / "mods").glob("*.jar.disabled"))

    groups: dict[str, list[dict]] = {}
    for j in jars:
        groups.setdefault(gm.categorise(j["mods"][0]["modid"], j["file"]), []).append(j)

    rel = server.relative_to(sync.SERVERS_ROOT.parent) if server.is_relative_to(sync.SERVERS_ROOT.parent) else server
    out: list[str] = []
    w = out.append
    w("# Server Mod List — Spooky Dookie\n")
    w(f"**Server:** `{rel.as_posix()}`  ")
    w("**Minecraft:** 1.20.1 · **Loader:** Forge 47.4.10 · **Runtime:** Java 17 (pinned in `run.bat`/`run.sh`)  ")
    w(f"**Active mods:** {len(jars)} · **Disabled:** 0  ")
    w(f"**Generated:** {date.today().isoformat()} by `tools/generate_server_modlist.py` from the server's own `mods/` folder — do not edit by hand.\n")
    w("> The server is the client instance's `mods/` **minus the client-only list** below, kept in step by")
    w("> `scripts/sync_server_mods.py` (default target: `Servers/Server v2`). Every jar here must match the client")
    w("> exactly, or players are refused at login.")
    w(">")
    w("> **Pinned on both sides — never sync newer versions:** `chunksending-1.20.1-2.8` (4.1 never delivers chunks")
    w("> under DimThreads), `farsight-1.20.1-5.1`, `hardcoretruedarkness-4.0.0` (v6 blacks out the view under shaders and is")
    w("> a hard client/server protocol gate). The newer jars are parked in the client's `local/mods-rolled-back-2026-11-01/`.")
    w(">")
    w("> **Pack-built:** `immptl_dimthread_compat-1.0.0.jar` (source in `tools/immptl_dimthread_compat/`) lets Immersive")
    w("> Portals and DimThreads run together; without it the server log grows by ~100 MB a minute.")
    w(">")
    w("> **Previous server:** `Servers/spookydookie` (July 2026, 264 mods, TFC-era) is kept unchanged for reference and is")
    w("> no longer synced.\n")
    w("---\n")
    for cat in [c for c, _ in gm.CATEGORY_RULES] + [gm.FALLBACK_CATEGORY]:
        items = groups.get(cat)
        if not items:
            continue
        w(f"## {cat} ({len(items)})\n")
        for j in sorted(items, key=lambda x: x["mods"][0]["name"].lower()):
            names = " + ".join(m["name"] for m in j["mods"])
            w(f"- {names} — `{j['file']}`")
        w("")

    w("---\n")
    w(f"## Client-only mods excluded from the server ({len(excluded)})")
    w("Present in the client instance but deliberately **not** copied to the server. They set")
    w("`IGNORE_SERVER_VERSION`, so clients keep them and are not refused for the server lacking them.\n")
    for title, prefixes in EXCLUSION_GROUPS:
        hits = [(n, next(p for p in prefixes if n.lower().startswith(p))) for n in excluded
                if any(n.lower().startswith(p) for p in prefixes)]
        if not hits:
            continue
        w(f"**{title}:**")
        for n, p in hits:
            w(f"- {n}{NOTES.get(p, '')}")
        w("")
    w("> **Kept despite being client-leaning** (harmless idling on a server, and dropping them risked a dependency or")
    w("> client refusal): `Camerapture`, `farsight`, `sounds_of_the_forest`, `clientcrafting`, plus client-side libraries")
    w("> (`athena`, `fusion`, `Iceberg`, `Prism`, `CreativeCore`). A dedicated server built from this list started cleanly")
    w("> on 2026-11-02 (281 jars).\n")
    w("## Disabled mods")
    w("The server has no disabled jars. The **client instance** keeps these as `.jar.disabled` — loaded on neither side:\n")
    for n in client_disabled:
        w(f"- {n}")
    w("")

    target = ROOT / "docs" / "SERVER_MODS.md"
    target.write_text("\n".join(out), encoding="utf-8")
    print(f"Wrote {target} — {len(jars)} server jars, {len(excluded)} client-only excluded, "
          f"{len(client_disabled)} disabled on the client.")
    uncat = groups.get(gm.FALLBACK_CATEGORY, [])
    if uncat:
        print("Uncategorised:", ", ".join(j["mods"][0]["modid"] for j in uncat))
    return 0


if __name__ == "__main__":
    sys.exit(main())
