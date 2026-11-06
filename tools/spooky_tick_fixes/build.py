"""Build spooky_tick_fixes with the local JDK and the jars already on this machine.

Usage:  python tools/spooky_tick_fixes/build.py [--install]
  --install  also copy the jar into the instance's mods/ and local/ directories.

No Gradle, no network. Compiles against the CurseForge install's libraries (Mixin, the Forge 1.20.1
patched client classes, the vanilla SRG client jar, Forge/FML/eventbus/ASM) and the handful of target
mod jars whose types the patches reference directly. All other targets are named by string.
Source is written against SRG names (m_xxxx_), so no refmap is needed (every mixin uses remap = false).
"""
import glob, os, shutil, subprocess, sys

HERE = os.path.dirname(os.path.abspath(__file__))
INSTANCE = os.path.abspath(os.path.join(HERE, "..", ".."))
LIB = r"E:/Program Files (x86)/CurseForge/mc/Install/libraries"
JDK = r"E:/Program Files/Java/jdk-17/bin"
VERSION = "1.1.0"
JAR_NAME = f"spooky_tick_fixes-{VERSION}.jar"


def one(pattern):
    hits = sorted(glob.glob(pattern))
    if not hits:
        sys.exit(f"no match for {pattern}")
    return hits[-1]  # highest version when several are installed


classpath = [
    one(os.path.join(LIB, "org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar")),
    # Forge-patched vanilla classes first (they add e.g. Entity.persistentData), then plain vanilla SRG.
    one(os.path.join(LIB, "net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-client.jar")),
    one(os.path.join(LIB, "net/minecraft/client/1.20.1-20230612.114412/client-1.20.1-20230612.114412-srg.jar")),
    one(os.path.join(LIB, "net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-universal.jar")),
    one(os.path.join(LIB, "net/minecraftforge/eventbus/*/eventbus-*.jar")),
    one(os.path.join(LIB, "net/minecraftforge/forgespi/*/forgespi-*.jar")),
    one(os.path.join(LIB, "net/minecraftforge/fmlcore/1.20.1-47.4.10/fmlcore-1.20.1-47.4.10.jar")),
    one(os.path.join(LIB, "net/minecraftforge/fmlloader/1.20.1-47.4.10/fmlloader-1.20.1-47.4.10.jar")),
    one(os.path.join(LIB, "net/minecraftforge/javafmllanguage/1.20.1-47.4.10/javafmllanguage-1.20.1-47.4.10.jar")),
    one(os.path.join(LIB, "org/apache/logging/log4j/log4j-api/*/log4j-api-*.jar")),
    one(os.path.join(LIB, "org/ow2/asm/asm/9.8/asm-9.8.jar")),
    one(os.path.join(LIB, "org/ow2/asm/asm-tree/9.8/asm-tree-9.8.jar")),
    one(os.path.join(LIB, "com/electronwill/night-config/core/*/core-*.jar")),
    one(os.path.join(LIB, "com/google/guava/guava/*/guava-*.jar")),
    one(os.path.join(LIB, "com/mojang/brigadier/*/brigadier-*.jar")),
    # Target mods whose types are referenced directly.
    one(os.path.join(INSTANCE, "mods", "artifacts-forge-*.jar")),
    one(os.path.join(INSTANCE, "mods", "the_obsessed-*.jar")),
    one(os.path.join(INSTANCE, "mods", "WhisperingSpirits-forge-*.jar")),
    one(os.path.join(INSTANCE, "mods", "letsdo-farm_and_charm-forge-*.jar")),
    one(os.path.join(INSTANCE, "mods", "citadel-*.jar")),
]

src = [os.path.join(dp, f) for dp, _, fs in os.walk(os.path.join(HERE, "src")) for f in fs if f.endswith(".java")]
out_classes = os.path.join(HERE, "build", "classes")
out_jar = os.path.join(HERE, "build", JAR_NAME)
shutil.rmtree(os.path.join(HERE, "build"), ignore_errors=True)
os.makedirs(out_classes)

javac = [os.path.join(JDK, "javac.exe"), "--release", "17", "-proc:none", "-Xlint:all,-options,-processing,-rawtypes,-unchecked,-serial",
         "-encoding", "UTF-8", "-cp", os.pathsep.join(classpath), "-d", out_classes] + src
print("javac", len(src), "sources")
subprocess.run(javac, check=True)

jar = [os.path.join(JDK, "jar.exe"), "--create", "--file", out_jar, "--manifest", os.path.join(HERE, "MANIFEST.MF"),
       "-C", out_classes, ".", "-C", os.path.join(HERE, "resources"), "."]
subprocess.run(jar, check=True)
print("built", out_jar, os.path.getsize(out_jar), "bytes")

if "--install" in sys.argv:
    for dest in (os.path.join(INSTANCE, "mods"), os.path.join(INSTANCE, "local")):
        os.makedirs(dest, exist_ok=True)
        for old in glob.glob(os.path.join(dest, "spooky_tick_fixes-*.jar")):
            if os.path.basename(old) != JAR_NAME:
                os.remove(old)
                print("removed old", old)
        shutil.copy2(out_jar, os.path.join(dest, JAR_NAME))
        print("installed ->", os.path.join(dest, JAR_NAME))
