"""Shared build steps for the pack's own small Forge mods (tools/<mod>/build.py).

No Gradle, no network: javac and jar from a local JDK 17+, compiled against the CurseForge install's
libraries and jars from the instance's mods/ folder. Each build.py lists its classpath and calls build().

The mod id and version come from the project's resources/META-INF/mods.toml, and MANIFEST.MF must carry the
same Implementation-Version, so the version is changed in those two files only. The jar is named
<modId>-<version>.jar.

Machine-specific paths, overridable with environment variables:
  SPOOKY_JDK      JDK home (default: E:/Program Files/Java/jdk-17, then JAVA_HOME)
  SPOOKY_MC_LIBS  CurseForge libraries folder (default: E:/Program Files (x86)/CurseForge/mc/Install/libraries)
"""
import argparse
import glob
import os
import re
import shutil
import subprocess
import sys
import tomllib
from pathlib import Path

TOOLS = Path(__file__).resolve().parent
INSTANCE = TOOLS.parent
DEFAULT_JDK = r"E:/Program Files/Java/jdk-17"
LIB = Path(os.environ.get("SPOOKY_MC_LIBS") or r"E:/Program Files (x86)/CurseForge/mc/Install/libraries")

# Pack versions, for classpath entries.
MC_SRG = "1.20.1-20230612.114412"
FORGE = "1.20.1-47.4.10"

EXE = ".exe" if os.name == "nt" else ""


def _natural(s: str):
    # "2.9" < "2.19": digit runs compare as numbers. re.split keeps text at even and numbers at odd positions.
    return [int(t) if i % 2 else t for i, t in enumerate(re.split(r"(\d+)", s))]


def lib(pattern: str) -> str:
    """A jar from the CurseForge libraries folder. When a wildcard matches several versions, the highest wins."""
    hits = glob.glob(str(LIB / pattern))
    if not hits:
        sys.exit(f"library not found: {LIB / pattern}")
    return max(hits, key=_natural)


def mod(pattern: str) -> str:
    """A jar from the instance's mods/ folder. Exactly one must match."""
    hits = glob.glob(str(INSTANCE / "mods" / pattern))
    if len(hits) != 1:
        found = ", ".join(os.path.basename(h) for h in hits) or "none"
        sys.exit(f"expected exactly one mods/{pattern}, found: {found}\n"
                 "If the mod was updated, check the patches still fit it, then adjust the pattern in build.py.")
    return hits[0]


def _jdk_bin() -> Path:
    for home in (os.environ.get("SPOOKY_JDK"), DEFAULT_JDK, os.environ.get("JAVA_HOME")):
        if home and (Path(home) / "bin" / f"javac{EXE}").is_file():
            return Path(home) / "bin"
    sys.exit("No JDK found. Set SPOOKY_JDK to a JDK 17+ folder.")


def _identity(project: Path) -> tuple[str, str]:
    with open(project / "resources" / "META-INF" / "mods.toml", "rb") as f:
        meta = tomllib.load(f)["mods"][0]
    mod_id, version = meta["modId"], meta["version"]
    manifest = (project / "MANIFEST.MF").read_text(encoding="utf-8")
    m = re.search(r"(?im)^Implementation-Version:\s*(\S+)", manifest)
    if not m or m.group(1) != version:
        sys.exit(f"version mismatch: mods.toml says {version}, MANIFEST.MF says {m.group(1) if m else 'nothing'}")
    return mod_id, version


def build(build_py: str, classpath: list[str], lint: str = "all,-options,-processing") -> None:
    project = Path(build_py).resolve().parent
    mod_id, version = _identity(project)
    jar_name = f"{mod_id}-{version}.jar"

    ap = argparse.ArgumentParser(description=f"Build {jar_name}")
    ap.add_argument("--install", action="store_true",
                    help="also copy the jar into the instance's mods/ and local/, removing older versions there")
    args = ap.parse_args()

    jdk = _jdk_bin()
    out = project / "build"
    classes = out / "classes"
    shutil.rmtree(out, ignore_errors=True)
    if out.exists():
        sys.exit(f"could not clear {out} (is a file in it open?)")
    classes.mkdir(parents=True)

    src = sorted(str(p) for p in (project / "src").rglob("*.java"))
    print(f"{jar_name}: javac {len(src)} sources")
    subprocess.run([str(jdk / f"javac{EXE}"), "--release", "17", "-proc:none", f"-Xlint:{lint}",
                    "-encoding", "UTF-8", "-cp", os.pathsep.join(classpath), "-d", str(classes), *src], check=True)

    out_jar = out / jar_name
    subprocess.run([str(jdk / f"jar{EXE}"), "--create", "--file", str(out_jar),
                    "--manifest", str(project / "MANIFEST.MF"),
                    "-C", str(classes), ".", "-C", str(project / "resources"), "."], check=True)
    print("built", out_jar, out_jar.stat().st_size, "bytes")

    if args.install:
        for dest in (INSTANCE / "mods", INSTANCE / "local"):
            dest.mkdir(exist_ok=True)
            # Two versions in mods/ would stop Forge with a duplicate-mod error.
            for old in dest.glob(f"{mod_id}-*.jar"):
                if old.name != jar_name:
                    old.unlink()
                    print("removed old", old)
            shutil.copy2(out_jar, dest / jar_name)
            print("installed ->", dest / jar_name)
        print("Next: python scripts/sync_server_mods.py (the server copy; client-only jars are skipped)")
