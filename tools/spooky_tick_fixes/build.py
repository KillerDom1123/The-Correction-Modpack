"""Build spooky_tick_fixes with the local JDK and the jars already on this machine.

Usage:  python tools/spooky_tick_fixes/build.py [--install]
  --install  also copy the jar into the instance's mods/ and local/ directories (older versions are removed).

No Gradle, no network (shared steps: tools/_javabuild.py). Compiles against the CurseForge install's libraries
(Mixin, the Forge 1.20.1 patched client classes, the vanilla SRG client jar, Forge/FML/eventbus/ASM) and the
handful of target mod jars whose types the patches reference directly. All other targets are named by string.
Source is written against SRG names (m_xxxx_), so no refmap is needed (every mixin uses remap = false).
The version lives in resources/META-INF/mods.toml and MANIFEST.MF.
"""
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
from _javabuild import FORGE, MC_SRG, build, lib, mod  # noqa: E402

build(__file__, [
    lib("org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar"),
    # Forge-patched vanilla classes first (they add e.g. Entity.persistentData), then plain vanilla SRG.
    lib(f"net/minecraftforge/forge/{FORGE}/forge-{FORGE}-client.jar"),
    lib(f"net/minecraft/client/{MC_SRG}/client-{MC_SRG}-srg.jar"),
    lib(f"net/minecraftforge/forge/{FORGE}/forge-{FORGE}-universal.jar"),
    lib("net/minecraftforge/eventbus/*/eventbus-*.jar"),
    lib("net/minecraftforge/forgespi/*/forgespi-*.jar"),
    lib(f"net/minecraftforge/fmlcore/{FORGE}/fmlcore-{FORGE}.jar"),
    lib(f"net/minecraftforge/fmlloader/{FORGE}/fmlloader-{FORGE}.jar"),
    lib(f"net/minecraftforge/javafmllanguage/{FORGE}/javafmllanguage-{FORGE}.jar"),
    lib("org/apache/logging/log4j/log4j-api/*/log4j-api-*.jar"),
    lib("org/ow2/asm/asm/9.8/asm-9.8.jar"),
    lib("org/ow2/asm/asm-tree/9.8/asm-tree-9.8.jar"),
    lib("com/electronwill/night-config/core/*/core-*.jar"),
    lib("com/google/guava/guava/*/guava-*.jar"),
    lib("com/mojang/brigadier/*/brigadier-*.jar"),
    # Target mods whose types are referenced directly.
    mod("artifacts-forge-*.jar"),
    mod("the_obsessed-*.jar"),
    mod("WhisperingSpirits-forge-*.jar"),
    mod("letsdo-farm_and_charm-forge-*.jar"),
    mod("citadel-*.jar"),
], lint="all,-options,-processing,-rawtypes,-unchecked,-serial")
