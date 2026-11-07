package spookydookie.tickfixes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.fml.loading.LoadingModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Applies each patch only when its target mod is installed, so removing a mod later never breaks startup.
 * Logs one line per patch. Applied patches keep "defaultRequire: 1", so a target that changed shape still
 * fails loudly at startup instead of silently losing the fix.
 */
public final class TickFixesMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("spooky_tick_fixes");
    private static final String PACKAGE = "spookydookie.tickfixes.mixin.";

    private static final Map<String, String> REQUIRED_MOD = Map.ofEntries(
            Map.entry("MimicEntityMixin", "artifacts"),
            Map.entry("ObsessedPlayerVariablesMixin", "the_obsessed"),
            Map.entry("WhisperingSpiritLookMixin", "whispering_spirits"),
            Map.entry("FragileTorchesMixin", "fragiletorches"),
            Map.entry("FarmCharmSaturationMixin", "farm_and_charm"),
            Map.entry("AdAstraExtraEntityTickMixin", "adastraextra"),
            Map.entry("MobFlagsMixin", "immersive_portals_full_immersion"),
            Map.entry("FullImmersionAiMixin", "immersive_portals_full_immersion"),
            Map.entry("FullImmersionDespawnMixin", "immersive_portals_full_immersion"),
            Map.entry("AlexsCavesMagnetMixin", "alexscaves"),
            Map.entry("CitadelTrackerCacheMixin", "citadel"),
            Map.entry("AntlersDayCounterMixin", "antlers"),
            Map.entry("TowwWorldSyncMixin", "the_one_who_watches"),
            Map.entry("ray.CreepingWoodsRayMixin", "creepingwoods"),
            Map.entry("ray.RakeRayMixin", "rake_the_arrival"),
            Map.entry("ray.SpiritRayMixin", "whispering_spirits"),
            Map.entry("ray.JohnRayMixin", "john_mod_reborn"),
            Map.entry("ray.ObsessedRayMixin", "the_obsessed"),
            Map.entry("ray.ObsessedChunkWaitMixin", "the_obsessed"),
            Map.entry("ray.ObsessedBlockReadMixin", "the_obsessed"),
            Map.entry("anim.BornInChaosAnimationGateMixin", "born_in_chaos_v1"),
            Map.entry("anim.ObsessedAnimationGateMixin", "the_obsessed"),
            Map.entry("anim.CreepingWoodsAnimationGateMixin", "creepingwoods"),
            Map.entry("anim.FoesAnimationGateMixin", "foes"),
            Map.entry("anim.TowwAnimationGateMixin", "the_one_who_watches"),
            Map.entry("anim.RakeAnimationGateMixin", "rake_the_arrival"),
            Map.entry("anim.AntlersAnimationGateMixin", "antlers"));

    /** Patches on vanilla classes: always applied, but logged like the others. */
    private static final Set<String> VANILLA_PATCHES = Set.of("VillagerSleepChunkMixin", "OffThreadChunkLogMixin");

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String name = mixinClassName.startsWith(PACKAGE) ? mixinClassName.substring(PACKAGE.length()) : mixinClassName;
        String mod = REQUIRED_MOD.get(name);
        if (VANILLA_PATCHES.contains(name)) {
            LOGGER.info("[spooky_tick_fixes] {} -> applying (vanilla)", name);
            return true;
        }
        if (mod == null) return true; // the accessors and the activation-range hook
        LoadingModList mods = LoadingModList.get();
        boolean present = mods != null && mods.getModFileById(mod) != null;
        LOGGER.info("[spooky_tick_fixes] {} -> {} ({})", name, present ? "applying" : "skipped, mod not installed", mod);
        return present;
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
