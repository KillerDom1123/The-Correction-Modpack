"""Build immptl_dimthread_compat with the local JDK and the jars already on this machine.

Usage:  python tools/immptl_dimthread_compat/build.py [--install]
  --install  also copy the jar into the instance's mods/ and local/ directories (older versions are removed).

No Gradle, no network (shared steps: tools/_javabuild.py). Compiles against:
  - Mixin 0.8.5, the Forge 1.20.1 SRG client jar, fmlcore/fmlloader/javafmllanguage, log4j-api
    (all from the CurseForge install's libraries folder)
  - the DimThreads jar from the instance's mods/ folder
The Immersive Portals class is referenced by name only, so its jar is not needed.
"""
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
from _javabuild import FORGE, MC_SRG, build, lib, mod  # noqa: E402

build(__file__, [
    lib("org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar"),
    lib(f"net/minecraft/client/{MC_SRG}/client-{MC_SRG}-srg.jar"),
    lib(f"net/minecraftforge/fmlcore/{FORGE}/fmlcore-{FORGE}.jar"),
    lib(f"net/minecraftforge/javafmllanguage/{FORGE}/javafmllanguage-{FORGE}.jar"),
    lib("org/apache/logging/log4j/log4j-api/*/log4j-api-*.jar"),
    lib(f"net/minecraftforge/fmlloader/{FORGE}/fmlloader-{FORGE}.jar"),  # Dist enum referenced by FML annotations
    mod("dimthread-FORGE-mc1.20.1-v1.2.1.jar"),
])
