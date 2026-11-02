"""Build immptl_dimthread_compat with the local JDK and the jars already on this machine.

Usage:  python tools/immptl_dimthread_compat/build.py [--install]
  --install  also copy the jar into the instance's mods/ and local/ directories.

No Gradle, no network. Compiles against:
  - Mixin 0.8.5, the Forge 1.20.1 SRG client jar, fmlcore/javafmllanguage, log4j-api
    (all from the CurseForge install's libraries folder)
  - the DimThreads jar from the instance's mods/ folder
The Immersive Portals class is referenced by name only, so its jar is not needed.
"""
import glob, os, shutil, subprocess, sys

HERE = os.path.dirname(os.path.abspath(__file__))
INSTANCE = os.path.abspath(os.path.join(HERE, "..", ".."))
LIB = r"E:/Program Files (x86)/CurseForge/mc/Install/libraries"
JDK = r"E:/Program Files/Java/jdk-17/bin"
VERSION = "1.0.0"
JAR_NAME = f"immptl_dimthread_compat-{VERSION}.jar"

def one(pattern):
    hits = glob.glob(pattern)
    if len(hits) != 1:
        sys.exit(f"expected exactly one match for {pattern}, got {hits}")
    return hits[0]

classpath = [
    one(os.path.join(LIB, "org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar")),
    one(os.path.join(LIB, "net/minecraft/client/1.20.1-20230612.114412/client-1.20.1-20230612.114412-srg.jar")),
    one(os.path.join(LIB, "net/minecraftforge/fmlcore/1.20.1-47.4.10/fmlcore-1.20.1-47.4.10.jar")),
    one(os.path.join(LIB, "net/minecraftforge/javafmllanguage/1.20.1-47.4.10/javafmllanguage-1.20.1-47.4.10.jar")),
    one(os.path.join(LIB, "org/apache/logging/log4j/log4j-api/*/log4j-api-*.jar")),
    one(os.path.join(LIB, "net/minecraftforge/fmlloader/1.20.1-47.4.10/fmlloader-1.20.1-47.4.10.jar")),  # Dist enum referenced by FML annotations
    one(os.path.join(INSTANCE, "mods", "dimthread-FORGE-mc1.20.1-v1.2.1.jar")),
]

src = [os.path.join(dp, f) for dp, _, fs in os.walk(os.path.join(HERE, "src")) for f in fs if f.endswith(".java")]
out_classes = os.path.join(HERE, "build", "classes")
out_jar = os.path.join(HERE, "build", JAR_NAME)
shutil.rmtree(os.path.join(HERE, "build"), ignore_errors=True)
os.makedirs(out_classes)

javac = [os.path.join(JDK, "javac.exe"), "--release", "17", "-proc:none", "-Xlint:all,-options,-processing",
         "-encoding", "UTF-8", "-cp", os.pathsep.join(classpath), "-d", out_classes] + src
print("javac", *[os.path.basename(s) for s in src])
subprocess.run(javac, check=True)

jar = [os.path.join(JDK, "jar.exe"), "--create", "--file", out_jar, "--manifest", os.path.join(HERE, "MANIFEST.MF"),
       "-C", out_classes, ".", "-C", os.path.join(HERE, "resources"), "."]
subprocess.run(jar, check=True)
print("built", out_jar, os.path.getsize(out_jar), "bytes")

if "--install" in sys.argv:
    for dest in (os.path.join(INSTANCE, "mods"), os.path.join(INSTANCE, "local")):
        os.makedirs(dest, exist_ok=True)
        shutil.copy2(out_jar, os.path.join(dest, JAR_NAME))
        print("installed ->", os.path.join(dest, JAR_NAME))
