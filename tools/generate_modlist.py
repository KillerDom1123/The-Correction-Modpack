#!/usr/bin/env python3
"""
generate_modlist.py — auto-generate modlist.md from the mods/ folder.

Reads every jar's metadata (META-INF/mods.toml, with MANIFEST.MF / filename
fallbacks for versions) and writes a categorised, authoritative modlist.md.

Usage:
    python tools/generate_modlist.py

Run it whenever mods are added/removed/updated. The flat "Full manifest" table
is always accurate; the grouped view is best-effort (heuristic) — edit
CATEGORY_RULES below to re-bucket anything that lands in "Uncategorised".
"""

from __future__ import annotations
import io, json, re, sys, zipfile
from pathlib import Path

try:
    import tomllib  # Python 3.11+
except ModuleNotFoundError:
    print("Python 3.11+ required (needs tomllib).", file=sys.stderr)
    sys.exit(1)

# ── Settings ──────────────────────────────────────────────────────────────
PACK_NAME = "The World After The Correction"
ROOT = Path(__file__).resolve().parent.parent   # instance root (tools/..)
MODS_DIR = ROOT / "mods"
OUTPUT = ROOT / "modlist.md"
OUTPUT_JSON = ROOT / "modlist.json"
README = ROOT / "README.md"
# The README's auto-updated block sits between these markers (everything else is left alone).
README_START = "<!-- MODLIST:START -->"
README_END = "<!-- MODLIST:END -->"

# Grouped view: first matching rule wins; matched against "<modid> <filename>"
# (lowercased). Order matters — put specific/library rules before generic ones.
CATEGORY_RULES = [
    ("Core libraries & performance", [
        "architectury", "balm", "botarium", "citadel", "cloth", "creativecore",
        "cupboard", "curios", "geckolib", "glitchcore", "guideme", "integrated_api",
        "irons_lib", "moonlight", "player-animation", "player_animation",
        "resourcefulconfig", "resourcefullib", "rhino", "searchables", "smartbrainlib",
        "supermartijn642", "valhelsia", "voidlessframework", "zeta", "athena",
        "kuma", "flib", "cristellib", "embeddium", "oculus", "immediatelyfast",
        "entityculling", "modernfix", "createbetterfps", "ferritecore", "clumps", "spark",
        "bookshelf", "caelus", "collective", "framework", "fzzy_config", "iceberg",
        "placebo", "prism", "puzzleslib", "bcc", "sodiumoptionsapi", "deimos", "aso", "c4p",
        "kotlin", "fastbench", "mixinextras", "mixinsquared",
        "canary", "aiimprovements", "ai-improvements", "betterchunkloading",
        "chunksending", "chunky", "connectivity", "coroutil", "dataanchor",
        "dimthread", "fastasyncworldsave", "limitedchunks", "mobtimizations",
        "recipeessentials", "smoothchunk", "structureessentials",
    ]),
    ("Create & addons", ["create", "ponder", "compressedcreativity", "waystoneandcreate", "sliceanddice"]),
    ("Tech — energy, automation, industry", [
        "mekanism", "appliedenergistics", "ae2", "immersiveengineering",
        "immersivepetroleum", "pneumaticcraft", "cc-tweaked", "cctweaked",
        "computercraft", "ad_astra", "adastra", "structure ad astra", "steam_rails",
        "railways", "terrafirmacraft", "tfc", "appmek",
        "bigreactors", "extremereactors", "zerocore",
    ]),
    ("Magic", ["ars_nouveau", "occultism", "hexerei", "forbidden_arcanus",
               "irons_spellbooks", "theforgotten", "bloodmagic", "mahoutsukai"]),
    ("Colony / civilisation", ["minecolonies", "structurize", "blockui", "domum",
                               "multipiston", "smallcolonies", "towntalk"]),
    ("Horror & atmosphere", [
        "cave_dweller", "themimic", "weeping_angels", "from-the-fog", "fromthefog",
        "man_from", "man-from", "manfromthefog", "serversidehorror", "horror_element",
        "whisperingspirits", "born_in_chaos", "borninchaos", "foes", "john_mod",
        "john mod", "creepingwoods", "voidfog", "hardcoretruedarkness", "cant_breathe",
        "cavedust", "ambientsounds", "sound-physics", "soundphysics",
        "presencefootsteps", "sounds_of_the_forest", "pale_hound", "the_one_who_watches",
        "the_obsessed", "door_knocker", "rake_the_arrival", "the_rake", "antlers", "wendigo",
    ]),
    ("Mobs & world creatures", ["alexscaves", "alexsmobs", "goblintraders", "guardvillagers"]),
    ("Worldgen & structures", [
        "terralith", "incendium", "amplified_nether", "deeperdarker", "deeper_darker",
        "immersive_weathering", "dungeonsarise", "towns-and-towers", "townsandtowers",
        "structory", "repurposed_structures", "more_undrground", "more_underground",
        "watchtower", "lostarchitects", "yung", "idas", "big_lost_city",
        "spooky_campsite",
    ]),
    ("Food & farming", ["farmersdelight", "candlelight", "farm_and_charm", "farmandcharm",
                        "cookingforblockheads", "soldiersdelight", "appleskin", "rationcraft"]),
    ("Decoration & building", ["chipped", "rechiseled", "framedblocks", "copycats",
                               "decorative_blocks", "another_furniture", "handcrafted",
                               "interiors", "beautify", "cluttered", "supplementaries",
                               "suppsquared", "amendments", "fragiletorches", "fusion",
                               "connectedglass", "refurbished_furniture", "furniture"]),
    ("Storage, QoL & utility", ["storagedrawers", "sophisticated", "carryon", "corpse",
                                "lootr", "mousetweaks", "controlling", "jade",
                                "naturescompass", "explorerscompass", "waystones",
                                "coldsweat", "camerapture", "chat_heads", "chatheads",
                                "notenoughanimations", "mobends", "artifacts",
                                "simpledaylengthextender", "openloader", "quark",
                                "xaero", "cosmeticarmor", "elytraslot", "doubledoors",
                                "fallingtree", "fastleafdecay", "visualworkbench",
                                "clientcrafting", "moreoverlays", "simplebackups",
                                "farsight", "nochatreports", "betterf3", "enchdesc",
                                "legendarytooltips", "subtle_effects", "sodiumdynamiclights",
                                "toofast", "observable", "crash_assistant", "comforts",
                                "dummmmmmy", "carry_on_back", "phantom_remover", "blur",
                                "borderlesswindow", "mobdismemberment"]),
    ("Security & protection", ["securitycraft", "moreprotectables", "scfilters", "sc_cheiders"]),
    ("Quests, loot, data & guides", ["ftb", "kubejs", "lootjs", "lootintegration",
                                     "wda-questline", "wda_questline", "jei", "patchouli",
                                     "modonomicon", "ycurrenci"]),
]
FALLBACK_CATEGORY = "Uncategorised (edit CATEGORY_RULES)"

VERSION_PLACEHOLDER = re.compile(r"\$\{.*\}")


def read_manifest_version(zf: zipfile.ZipFile) -> str | None:
    try:
        text = zf.read("META-INF/MANIFEST.MF").decode("utf-8", "replace")
    except KeyError:
        return None
    m = re.search(r"(?im)^Implementation-Version:\s*(.+?)\s*$", text)
    return m.group(1) if m else None


def version_from_filename(fname: str) -> str:
    stem = re.sub(r"\.jar(\.disabled)?$", "", fname)
    # grab the longest version-looking token
    cands = re.findall(r"\d[\w.+-]*\d|\d", stem)
    return max(cands, key=len) if cands else "unknown"


def parse_jar(path: Path) -> dict:
    """Return {file, mods:[{modid,name,version}], mc}."""
    info = {"file": path.name, "mods": [], "mc": None}
    try:
        zf = zipfile.ZipFile(path)
    except zipfile.BadZipFile:
        info["mods"] = [{"modid": "?", "name": path.stem, "version": "unknown"}]
        return info

    toml_data = None
    for cand in ("META-INF/mods.toml", "META-INF/neoforge.mods.toml"):
        try:
            toml_data = tomllib.load(io.BytesIO(zf.read(cand)))
            break
        except KeyError:
            continue
        except Exception:
            toml_data = None
            break

    manifest_ver = None
    mods = (toml_data or {}).get("mods") or []
    for m in mods:
        ver = str(m.get("version", "")).strip()
        if not ver or VERSION_PLACEHOLDER.search(ver):
            if manifest_ver is None:
                manifest_ver = read_manifest_version(zf) or ""
            ver = manifest_ver or version_from_filename(path.name)
        info["mods"].append({
            "modid": m.get("modId", "?"),
            "name": (m.get("displayName") or m.get("modId") or path.stem).strip(),
            "version": ver,
        })
    # detect MC version from dependencies (best-effort)
    for deps in (toml_data or {}).get("dependencies", {}).values() if toml_data else []:
        if isinstance(deps, list):
            for d in deps:
                if d.get("modId") == "minecraft":
                    mm = re.search(r"\d+\.\d+(\.\d+)?", str(d.get("versionRange", "")))
                    if mm:
                        info["mc"] = mm.group(0)
    if not info["mods"]:  # no mods.toml (library-ish jar)
        info["mods"] = [{"modid": "?", "name": path.stem,
                         "version": read_manifest_version(zf) or version_from_filename(path.name)}]
    return info


def categorise(modid: str, filename: str) -> str:
    hay = f"{modid} {filename}".lower()
    for cat, keys in CATEGORY_RULES:
        if any(k in hay for k in keys):
            return cat
    return FALLBACK_CATEGORY


def collect(pattern: str):
    return sorted(MODS_DIR.glob(pattern), key=lambda p: p.name.lower())


def update_readme(mc: str, total: int, present_cats: list[tuple[str, int]]) -> None:
    """Refresh the auto-generated 'at a glance' block in README.md, if markers exist."""
    if not README.exists():
        return
    text = README.read_text(encoding="utf-8")
    if README_START not in text or README_END not in text:
        print(f"  README: no {README_START} / {README_END} markers found — skipped.")
        return
    cats = " · ".join(f"{c} ({n})" for c, n in present_cats)
    block = (
        f"{README_START}\n"
        f"> **At a glance** — Minecraft {mc} · Forge · **{total} mods** · "
        f"full list in [modlist.md](modlist.md)\n>\n"
        f"> {cats}\n"
        f"{README_END}"
    )
    new = re.sub(re.escape(README_START) + r".*?" + re.escape(README_END),
                 lambda _m: block, text, flags=re.S)
    if new != text:
        README.write_text(new, encoding="utf-8")
        print("  README: at-a-glance block updated.")
    else:
        print("  README: already up to date.")


def main() -> None:
    if not MODS_DIR.is_dir():
        print(f"mods/ not found at {MODS_DIR}", file=sys.stderr)
        sys.exit(1)

    enabled = [parse_jar(p) for p in collect("*.jar")]
    disabled = [parse_jar(p) for p in collect("*.jar.disabled")]

    mc_versions = [j["mc"] for j in enabled if j["mc"]]
    # prefer a specific x.y.z reading over a bare x.y
    mc = next((v for v in mc_versions if v.count(".") == 2),
              mc_versions[0] if mc_versions else "1.20.1")

    # group enabled by category
    groups: dict[str, list[dict]] = {}
    for j in enabled:
        primary = j["mods"][0]
        cat = categorise(primary["modid"], j["file"])
        groups.setdefault(cat, []).append(j)

    out: list[str] = []
    w = out.append
    w(f"# Mod List — {PACK_NAME}\n")
    w(f"**Minecraft:** {mc} · **Loader:** Forge · **{len(enabled)} enabled, {len(disabled)} disabled**\n")
    w("> Auto-generated by `tools/generate_modlist.py` — do not edit by hand; re-run the script instead.\n")
    w("> Every mod is the property of its respective author; this pack is a curated configuration over their work.\n")
    w("---\n")

    # ordered categories (rule order), then fallback last
    cat_order = [c for c, _ in CATEGORY_RULES] + [FALLBACK_CATEGORY]
    for cat in cat_order:
        items = groups.get(cat)
        if not items:
            continue
        w(f"## {cat}  ({len(items)})\n")
        names = []
        for j in sorted(items, key=lambda x: x["mods"][0]["name"].lower()):
            names.append(" + ".join(m["name"] for m in j["mods"]))
        w(" · ".join(names) + "\n")

    w("---\n")
    w(f"## Full manifest — enabled ({len(enabled)})\n")
    w("| Mod | Version | Mod ID | File |")
    w("|-----|---------|--------|------|")
    rows = sorted(enabled, key=lambda j: j["mods"][0]["name"].lower())
    for j in rows:
        for i, m in enumerate(j["mods"]):
            f = j["file"] if i == 0 else "↳ (same jar)"
            w(f"| {m['name']} | `{m['version']}` | `{m['modid']}` | `{f}` |")
    w("")

    if disabled:
        w(f"## Full manifest — disabled ({len(disabled)})\n")
        w("| Mod | Version | File |")
        w("|-----|---------|------|")
        for j in sorted(disabled, key=lambda x: x["mods"][0]["name"].lower()):
            m = j["mods"][0]
            w(f"| {m['name']} | `{m['version']}` | `{j['file']}` |")
        w("")

    OUTPUT.write_text("\n".join(out), encoding="utf-8")
    print(f"Wrote {OUTPUT.relative_to(ROOT)} — {len(enabled)} enabled, {len(disabled)} disabled.")

    # ── machine-readable modlist.json ──
    present = [(c, len(groups[c])) for c in cat_order if groups.get(c)]

    def entries(jars: list[dict]) -> list[dict]:
        rows = []
        for j in jars:
            cat = categorise(j["mods"][0]["modid"], j["file"])
            for m in j["mods"]:
                rows.append({"name": m["name"], "modid": m["modid"],
                             "version": m["version"], "file": j["file"], "category": cat})
        return rows

    payload = {
        "pack": PACK_NAME,
        "minecraft": mc,
        "loader": "forge",
        "counts": {"enabled": len(enabled), "disabled": len(disabled)},
        "categories": {c: n for c, n in present},
        "mods": entries(enabled),
        "disabled": entries(disabled),
    }
    OUTPUT_JSON.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Wrote {OUTPUT_JSON.relative_to(ROOT)}.")

    # ── keep the README's at-a-glance block in sync ──
    update_readme(mc, len(enabled), present)

    uncat = groups.get(FALLBACK_CATEGORY)
    if uncat:
        print(f"  {len(uncat)} uncategorised: " +
              ", ".join(j["mods"][0]["modid"] for j in uncat))


if __name__ == "__main__":
    main()
