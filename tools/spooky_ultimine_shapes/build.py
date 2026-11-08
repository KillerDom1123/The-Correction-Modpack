"""Build spooky_ultimine_shapes with the local JDK and the jars already on this machine.

Usage:  python tools/spooky_ultimine_shapes/build.py [--install]
  --install  also copy the jar into the instance's mods/ and local/ directories (older versions are removed).

No Gradle, no network (shared steps: tools/_javabuild.py). Compiles against the CurseForge install's libraries
and the FTB Ultimine jar (its shape classes). Source is written against SRG names (m_xxxx_). No mixins.
The version lives in resources/META-INF/mods.toml and MANIFEST.MF.
"""
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
from _javabuild import FORGE, MC_SRG, build, lib, mod  # noqa: E402

build(__file__, [
    lib(f"net/minecraft/client/{MC_SRG}/client-{MC_SRG}-srg.jar"),
    lib(f"net/minecraftforge/forge/{FORGE}/forge-{FORGE}-universal.jar"),
    lib("net/minecraftforge/eventbus/*/eventbus-*.jar"),
    lib("net/minecraftforge/forgespi/*/forgespi-*.jar"),
    lib(f"net/minecraftforge/fmlcore/{FORGE}/fmlcore-{FORGE}.jar"),
    lib(f"net/minecraftforge/fmlloader/{FORGE}/fmlloader-{FORGE}.jar"),
    lib(f"net/minecraftforge/javafmllanguage/{FORGE}/javafmllanguage-{FORGE}.jar"),
    lib("org/apache/logging/log4j/log4j-api/*/log4j-api-*.jar"),
    lib("com/mojang/datafixerupper/*/datafixerupper-*.jar"),
    mod("ftb-ultimine-forge-2001.1.8.jar"),
])
