package spookydookie.tickfixes;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * config/spooky_tick_fixes-common.toml. Every patch has its own switch, so a misbehaving patch can be
 * turned off without a rebuild. Hot paths read the volatile copies below, refreshed on load/reload.
 */
public final class TickFixesConfig {
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue MIMIC_SLEEP;
    private static final ForgeConfigSpec.DoubleValue MIMIC_WAKE_RANGE;
    private static final ForgeConfigSpec.BooleanValue WATCHER_FOLLOW_RANGE;
    private static final ForgeConfigSpec.DoubleValue WATCHER_FOLLOW_RANGE_CAP;
    private static final ForgeConfigSpec.BooleanValue OBSESSED_SYNC;
    private static final ForgeConfigSpec.BooleanValue SPIRIT_LOOK_GATE;
    private static final ForgeConfigSpec.BooleanValue FRAGILE_TORCHES;
    private static final ForgeConfigSpec.BooleanValue FARM_CHARM_SYNC;
    private static final ForgeConfigSpec.IntValue FARM_CHARM_REFRESH;
    private static final ForgeConfigSpec.BooleanValue ANIMATION_GATES;
    private static final ForgeConfigSpec.BooleanValue ADASTRAEXTRA_GATE;

    public static volatile boolean mimicSleep = true;
    public static volatile double mimicWakeRange = 24.0D;
    public static volatile boolean watcherFollowRange = true;
    public static volatile double watcherFollowRangeCap = 32.0D;
    public static volatile boolean obsessedSync = true;
    public static volatile boolean spiritLookGate = true;
    public static volatile boolean fragileTorches = true;
    public static volatile boolean farmCharmSync = true;
    public static volatile int farmCharmRefreshTicks = 100;
    public static volatile boolean animationGates = true;
    public static volatile boolean adAstraExtraGate = true;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Spooky Dookie tick fixes. Each patch skips work that cannot change the outcome;",
                  "see docs/SERVER_TICK_AUDIT_2026-11-06.md section 5.3. Set a switch to false to restore the mod's own behaviour.")
         .push("patches");
        MIMIC_SLEEP = b.comment("Artifacts: a dormant mimic skips its tick while no player is within mimicWakeRange (it can only wake by targeting a player within 16 blocks, being hurt, or being in water).")
                .define("mimicSleep", true);
        MIMIC_WAKE_RANGE = b.comment("Blocks. Must stay above the mimic's 16-block follow range.")
                .defineInRange("mimicWakeRange", 24.0D, 17.0D, 128.0D);
        WATCHER_FOLLOW_RANGE = b.comment("The One Who Watches: cap the follow range of the stationary watchers (hanging, staring, crawling, hallucination, delusion, looked_at). The mod hardcodes 2048, which makes each path search visit up to 32768 nodes.")
                .define("watcherFollowRange", true);
        WATCHER_FOLLOW_RANGE_CAP = b.comment("Follow range for those watchers. Their look-at-player goal has its own 1000-block range, so they still watch.")
                .defineInRange("watcherFollowRangeCap", 32.0D, 8.0D, 2048.0D);
        OBSESSED_SYNC = b.comment("The Obsessed: send the player-variable sync once per player per tick (end of tick) instead of once per variable write (up to 102 per tick).")
                .define("obsessedSyncCoalesce", true);
        SPIRIT_LOOK_GATE = b.comment("Whispering Spirits: skip the per-player look check (100 ray steps x 3 raycasts per tick) when no spirit is loaded in that dimension.")
                .define("spiritLookGate", true);
        FRAGILE_TORCHES = b.comment("Fragile Torches: check for a fragile torch at the mob's feet/head before the per-mob config list parse.")
                .define("fragileTorchesPrecheck", true);
        FARM_CHARM_SYNC = b.comment("Farm & Charm: send an animal's saturation sync only when it changes (plus a periodic refresh and when a player starts tracking the animal), instead of every tick to every player.")
                .define("farmCharmSyncOnChange", true);
        FARM_CHARM_REFRESH = b.comment("Ticks between forced refreshes of an unchanged saturation value.")
                .defineInRange("farmCharmRefreshTicks", 100, 20, 1200);
        ANIMATION_GATES = b.comment("MCreator animation-sync handlers (Born in Chaos, The Obsessed, Creeping Woods, Foes, TOWW, Rake, Antlers): skip mobs from other mods (each handler only acts on its own mod's entity classes).")
                .define("animationGates", true);
        ADASTRAEXTRA_GATE = b.comment("Ad Astra Extra: skip its per-mob procedure for mobs that are neither 'gassed' nor in adastraextra:jupiter (its only two branches).")
                .define("adAstraExtraGate", true);
        b.pop();
        SPEC = b.build();
    }

    static void refresh() {
        mimicSleep = MIMIC_SLEEP.get();
        mimicWakeRange = MIMIC_WAKE_RANGE.get();
        watcherFollowRange = WATCHER_FOLLOW_RANGE.get();
        watcherFollowRangeCap = WATCHER_FOLLOW_RANGE_CAP.get();
        obsessedSync = OBSESSED_SYNC.get();
        spiritLookGate = SPIRIT_LOOK_GATE.get();
        fragileTorches = FRAGILE_TORCHES.get();
        farmCharmSync = FARM_CHARM_SYNC.get();
        farmCharmRefreshTicks = FARM_CHARM_REFRESH.get();
        animationGates = ANIMATION_GATES.get();
        adAstraExtraGate = ADASTRAEXTRA_GATE.get();
    }

    static String summary() {
        return "mimicSleep=" + mimicSleep + "(" + mimicWakeRange + ") watcherFollowRange=" + watcherFollowRange + "(" + watcherFollowRangeCap
                + ") obsessedSync=" + obsessedSync + " spiritLookGate=" + spiritLookGate + " fragileTorches=" + fragileTorches
                + " farmCharmSync=" + farmCharmSync + "(" + farmCharmRefreshTicks + "t) animationGates=" + animationGates
                + " adAstraExtraGate=" + adAstraExtraGate;
    }

    private TickFixesConfig() {}
}
