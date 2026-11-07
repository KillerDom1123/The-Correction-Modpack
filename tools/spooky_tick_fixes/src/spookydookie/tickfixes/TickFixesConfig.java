package spookydookie.tickfixes;

import java.util.List;
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
    private static final ForgeConfigSpec.BooleanValue FULL_IMMERSION_GATE;
    private static final ForgeConfigSpec.BooleanValue ALEXSCAVES_GATE;
    private static final ForgeConfigSpec.BooleanValue CITADEL_CACHE;
    private static final ForgeConfigSpec.BooleanValue ANTLERS_DAY_COUNTER;
    private static final ForgeConfigSpec.BooleanValue TOWW_SYNC_GATE;
    private static final ForgeConfigSpec.BooleanValue RAY_LOADED_ONLY;
    private static final ForgeConfigSpec.BooleanValue RAY_DEDUPE;
    private static final ForgeConfigSpec.BooleanValue OBSESSED_CHUNK_WAIT;
    private static final ForgeConfigSpec.IntValue OBSESSED_CHUNK_RADIUS;
    private static final ForgeConfigSpec.BooleanValue OBSESSED_READS_LOADED_ONLY;
    private static final ForgeConfigSpec.BooleanValue VILLAGER_SLEEP_LOADED_ONLY;
    private static final ForgeConfigSpec.BooleanValue OFF_THREAD_CHUNK_LOG;
    private static final ForgeConfigSpec.BooleanValue CS_ENABLED;
    private static final ForgeConfigSpec.BooleanValue CS_DISK_LOADS;
    private static final ForgeConfigSpec.IntValue CS_DISK_MS;
    private static final ForgeConfigSpec.BooleanValue CS_LOG;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CS_EXEMPT;

    private static final ForgeConfigSpec.BooleanValue WENDIGO_ENABLED;
    private static final ForgeConfigSpec.DoubleValue WENDIGO_PROVOKE_RANGE;
    private static final ForgeConfigSpec.DoubleValue WENDIGO_PROVOKE_RANGE_SNEAKING;
    private static final ForgeConfigSpec.DoubleValue WENDIGO_STARE_RANGE;
    private static final ForgeConfigSpec.DoubleValue WENDIGO_STARE_SECONDS;
    private static final ForgeConfigSpec.DoubleValue WENDIGO_CALM_RANGE;
    private static final ForgeConfigSpec.BooleanValue WENDIGO_VANISH_AFTER_CALM;
    private static final ForgeConfigSpec.DoubleValue WENDIGO_KEEP_AWAY_RANGE;
    private static final ForgeConfigSpec.DoubleValue WENDIGO_SOUND_RANGE;
    private static final ForgeConfigSpec.BooleanValue WENDIGO_DESPAWN;

    private static final ForgeConfigSpec.BooleanValue AR_ENABLED;
    private static final ForgeConfigSpec.DoubleValue AR_RANGE;
    private static final ForgeConfigSpec.IntValue AR_INTERVAL;
    private static final ForgeConfigSpec.BooleanValue AR_ADAPTIVE;
    private static final ForgeConfigSpec.DoubleValue AR_ENABLE_ABOVE;
    private static final ForgeConfigSpec.DoubleValue AR_DISABLE_BELOW;
    private static final ForgeConfigSpec.BooleanValue AR_AGE_CATCH_UP;
    private static final ForgeConfigSpec.BooleanValue AR_DESPAWN_CHECKS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> AR_ALWAYS_ENTITIES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> AR_ALWAYS_NAMESPACES;

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
    public static volatile boolean fullImmersionGate = true;
    public static volatile boolean alexsCavesGate = true;
    public static volatile boolean citadelCache = true;
    public static volatile boolean antlersDayCounter = true;
    public static volatile boolean towwSyncGate = true;
    public static volatile boolean rayLoadedOnly = true;
    public static volatile boolean rayDedupe = true;
    public static volatile boolean obsessedChunkWait = true;
    public static volatile int obsessedChunkRadius = 6;
    public static volatile boolean obsessedReadsLoadedOnly = true;
    public static volatile boolean villagerSleepLoadedOnly = true;
    public static volatile boolean offThreadChunkLog = true;
    public static volatile boolean chunkSafeTicking = true;
    public static volatile boolean chunkSafeAllowDiskLoads = true;
    public static volatile int chunkSafeDiskCheckMs = 50;
    public static volatile boolean chunkSafeLog = true;
    public static volatile List<String> chunkSafeExempt = List.of();

    public static volatile boolean wendigoProvokedOnly = true;
    public static volatile double wendigoProvokeRange = 8.0D;
    public static volatile double wendigoProvokeRangeSneaking = 4.0D;
    public static volatile double wendigoStareRange = 24.0D;
    public static volatile double wendigoStareSeconds = 5.0D;
    public static volatile double wendigoCalmRange = 32.0D;
    public static volatile boolean wendigoVanishAfterCalm = true;
    public static volatile double wendigoKeepAwayRange = 16.0D;
    public static volatile double wendigoSoundRange = 128.0D;
    public static volatile boolean wendigoDespawnNormally = true;

    public static volatile boolean activationEnabled = true;
    public static volatile double activationRangeBlocks = 48.0D;
    public static volatile int activationInterval = 4;
    public static volatile boolean activationAdaptive = true;
    public static volatile double activationEnableAboveMspt = 50.0D;
    public static volatile double activationDisableBelowMspt = 30.0D;
    public static volatile boolean activationAgeCatchUp = true;
    public static volatile boolean activationDespawnChecks = true;

    private static final List<String> DEFAULT_ALWAYS_ENTITIES = List.of(
            "minecraft:ender_dragon", "minecraft:wither", "minecraft:warden", "minecraft:elder_guardian");
    private static final List<String> DEFAULT_ALWAYS_NAMESPACES = List.of(
            // colonies and guards keep working while their owner is away
            "minecolonies", "guardvillagers",
            // scripted horror (stalking/approach logic and timers)
            "the_one_who_watches", "rake_the_arrival", "the_obsessed", "cave_dweller", "weeping_angels", "pale_hound",
            "cant_breathe", "themimic_er", "whispering_spirits", "antlers", "john_mod_reborn", "creepingwoods", "man",
            // familiars and summons
            "mahoutsukai");

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
        FULL_IMMERSION_GATE = b.comment("Immersive Portals Full Immersion: skip its per-mob portal-AI wrapper and portal despawn check in dimensions that contain no portal.")
                .define("fullImmersionGate", true);
        ALEXSCAVES_GATE = b.comment("Alex's Caves: skip the per-movement search for moving magnetic blocks in dimensions that contain none.")
                .define("alexsCavesMovingBlockGate", true);
        CITADEL_CACHE = b.comment("Citadel: cache the tick-rate tracker per server instead of a saved-data lookup on every entity tick.")
                .define("citadelTrackerCache", true);
        ANTLERS_DAY_COUNTER = b.comment("Antlers: run its day counter on the overworld's tick only. It runs on every level's tick but the counter is global,",
                                        "so it counted ~45x too fast and sent a full sync to every player each time. Changes behaviour: days count at the real pace.")
                .define("antlersDayCounterOverworldOnly", true);
        TOWW_SYNC_GATE = b.comment("The One Who Watches: skip its per-tick world-variable sync for levels with no players (the packet goes nowhere); the data is still marked for saving.")
                .define("towwEmptyLevelSyncSkip", true);
        RAY_LOADED_ONLY = b.comment("Horror look-checks (Creeping Woods, Rake, Whispering Spirits, John, The Obsessed): a ray cast that would touch a chunk",
                                    "that is not loaded returns 'nothing hit' instead of loading (or generating) that chunk on the server thread.",
                                    "Their creatures only exist in loaded chunks, so the result is the same. Stops the multi-second ticks seen while sailing.")
                .define("horrorRayCastLoadedOnly", true);
        RAY_DEDUPE = b.comment("Same procedures: reuse the result of an identical ray cast made just before in the same procedure run (they cast each ray three times).")
                .define("horrorRayCastDedupe", true);
        OBSESSED_CHUNK_WAIT = b.comment("The Obsessed: skip its per-player tick while any chunk within obsessedChunkRadius of the player is still loading or",
                                        "generating. Its block reads around the player would otherwise make the server thread wait for those chunks.")
                .define("obsessedWaitForChunks", true);
        OBSESSED_CHUNK_RADIUS = b.comment("Chunks around the player that must be loaded. Its stalker spawn search reads blocks up to about 80 blocks from the player; 6 chunks = 96 blocks.")
                .defineInRange("obsessedChunkRadius", 6, 1, 8);
        OBSESSED_READS_LOADED_ONLY = b.comment("The Obsessed: a block read (isEmptyBlock / getBlockState) in an unloaded chunk answers 'not empty' / air instead of loading the chunk.",
                                               "Covers its spawn-search scan point wherever it is (near another player, or left behind after a teleport). It then takes its own 'no space here' path.")
                .define("obsessedReadsLoadedOnly", true);
        VILLAGER_SLEEP_LOADED_ONLY = b.comment("Vanilla villagers: the sleep check reads the home bed's block before checking that the bed is within 2 blocks.",
                                               "A bed in an unloaded chunk is always further than that, so answer air instead of loading (or generating) the chunk. Crash 2026-11-07 01:44.")
                .define("villagerSleepLoadedOnly", true);
        OFF_THREAD_CHUNK_LOG = b.comment("Diagnostic: log the first chunk request from each caller on a thread other than the server thread, with its stack.",
                                         "Vanilla runs those requests on the server thread between ticks, so they show up as 'Can't keep up' without a long tick.",
                                         "Only observes. Counts are in /tickfixes stats.")
                .define("offThreadChunkLog", true);
        b.pop();

        b.comment("Chunk-safe ticking (1.3.4): while a dimension ticks, a block or fluid read on the server thread never makes it generate terrain.",
                  "Loaded chunks, and chunks already generated on disk, behave as in vanilla. A read of terrain that doesn't exist yet (or is still",
                  "being generated) answers with a barrier block and no fluid instead of waiting. This was the cause of every watchdog crash so far.").push("chunkSafeTicking");
        CS_ENABLED = b.comment("Master switch.").define("enabled", true);
        CS_DISK_LOADS = b.comment("Load chunks that are fully generated on disk, as vanilla would (milliseconds). False = barrier for every unloaded chunk.")
                .define("allowDiskLoads", true);
        CS_DISK_MS = b.comment("Longest wait for the on-disk status check before answering with a barrier (the answer is cached for 5 s).")
                .defineInRange("diskCheckMillis", 50, 5, 1000);
        CS_LOG = b.comment("Log the first barrier read from each caller, with its stack. Counts are in /tickfixes stats.")
                .define("logCallers", true);
        CS_EXEMPT = b.comment("Callers that may still generate terrain synchronously: class-name prefixes, matched against the first frame",
                              "outside Minecraft/Forge, e.g. com.example.mod. or com.example.mod.SomeClass.someMethod")
                .defineListAllowEmpty(List.of("exemptCallers"), List::of, o -> o instanceof String);
        b.pop();

        b.comment("Antlers' Wendigo (1.4.0): keeps away from people and attacks only a player who provoked it, instead of hunting the",
                  "nearest player within ~300 blocks. Changes behaviour. Provoking it: hitting it (any time), or at night being too close where it",
                  "can see you (surprising or cornering it), or staring at it. It then chases only that player, until that player leaves, dies,",
                  "changes dimension or gets further than calmRange; then it retreats and vanishes (vanishAfterCalm).").push("antlersWendigo");
        WENDIGO_ENABLED = b.comment("Master switch. false = the mod's own hunting, stalking roar (1,600 blocks) and scream range (640 blocks).")
                .define("enabled", true);
        WENDIGO_KEEP_AWAY_RANGE = b.comment("Blocks. While calm it moves away from any player it can see within this range. 0 = off (it wanders as the mod made it).")
                .defineInRange("keepAwayRange", 16.0D, 0.0D, 32.0D);
        WENDIGO_PROVOKE_RANGE = b.comment("Blocks. At night, a survival/adventure player this close, in its line of sight, provokes it. 0 = off.",
                                          "Keep it below keepAwayRange: it then only happens when a player surprises it, corners it or runs it down.")
                .defineInRange("provokeRange", 8.0D, 0.0D, 32.0D);
        WENDIGO_PROVOKE_RANGE_SNEAKING = b.comment("Blocks. The same while sneaking. 0 = sneaking players never provoke it by coming close.")
                .defineInRange("provokeRangeSneaking", 4.0D, 0.0D, 32.0D);
        WENDIGO_STARE_RANGE = b.comment("Blocks. At night, staring at it from this far or closer for stareSeconds provokes it: your crosshair on its body,",
                                        "a clear line of sight both ways, and it is not walking away from someone at the time. 0 = off.")
                .defineInRange("stareRange", 24.0D, 0.0D, 64.0D);
        WENDIGO_STARE_SECONDS = b.comment("Seconds of continuous staring. Looking away resets it.")
                .defineInRange("stareSeconds", 5.0D, 0.5D, 20.0D);
        WENDIGO_CALM_RANGE = b.comment("Blocks. It calms down when its provoker gets further away than this.")
                .defineInRange("calmRange", 32.0D, 16.0D, 256.0D);
        WENDIGO_VANISH_AFTER_CALM = b.comment("After calming down it retreats from every player within 48 blocks (only a hit provokes it again) and vanishes once",
                                              "no player is within 24 blocks and none within 64 can see it, or after 60 s once none is within 16. false = it stays, calm.")
                .define("vanishAfterCalm", true);
        WENDIGO_SOUND_RANGE = b.comment("Blocks. Range of its roar (only while provoked, every 15-30 s) and of its 'I see you' scream (was 640, on the Master channel).")
                .defineInRange("soundRange", 128.0D, 16.0D, 640.0D);
        WENDIGO_DESPAWN = b.comment("Let the Wendigo and its daytime form despawn like other monsters (the mod never despawns them). Works without 'enabled'.")
                .define("despawnNormally", true);
        b.pop();

        b.comment("Activation range (audit section 5.5.1): mobs far from every player tick less often. Changes behaviour of far mobs",
                  "(slow motion); MineColonies, guards, scripted horror and bosses are excluded by default.").push("activationRange");
        AR_ENABLED = b.comment("Master switch.").define("enabled", true);
        AR_RANGE = b.comment("Blocks from the nearest player in the same dimension beyond which a mob is 'far'.")
                .defineInRange("range", 48.0D, 16.0D, 256.0D);
        AR_INTERVAL = b.comment("A far mob ticks once every this many ticks (staggered by entity id).")
                .defineInRange("interval", 4, 2, 20);
        AR_ADAPTIVE = b.comment("Only throttle while the server is behind: switch on when the average tick exceeds enableAboveMspt, off again below disableBelowMspt.")
                .define("adaptive", true);
        AR_ENABLE_ABOVE = b.defineInRange("enableAboveMspt", 50.0D, 1.0D, 1000.0D);
        AR_DISABLE_BELOW = b.defineInRange("disableBelowMspt", 30.0D, 0.0D, 1000.0D);
        AR_AGE_CATCH_UP = b.comment("Advance AgeableMob age on skipped ticks, so growth and breeding cooldowns keep normal pace.")
                .define("ageCatchUp", true);
        AR_DESPAWN_CHECKS = b.comment("Run the vanilla despawn check on skipped ticks, so despawn rates are unchanged.")
                .define("despawnChecks", true);
        AR_ALWAYS_ENTITIES = b.comment("Entity ids that always tick at full rate.")
                .defineListAllowEmpty(List.of("alwaysTickEntities"), () -> DEFAULT_ALWAYS_ENTITIES, o -> o instanceof String);
        AR_ALWAYS_NAMESPACES = b.comment("Mod namespaces whose entities always tick at full rate.")
                .defineListAllowEmpty(List.of("alwaysTickNamespaces"), () -> DEFAULT_ALWAYS_NAMESPACES, o -> o instanceof String);
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
        fullImmersionGate = FULL_IMMERSION_GATE.get();
        alexsCavesGate = ALEXSCAVES_GATE.get();
        citadelCache = CITADEL_CACHE.get();
        antlersDayCounter = ANTLERS_DAY_COUNTER.get();
        towwSyncGate = TOWW_SYNC_GATE.get();
        rayLoadedOnly = RAY_LOADED_ONLY.get();
        rayDedupe = RAY_DEDUPE.get();
        obsessedChunkWait = OBSESSED_CHUNK_WAIT.get();
        obsessedChunkRadius = OBSESSED_CHUNK_RADIUS.get();
        obsessedReadsLoadedOnly = OBSESSED_READS_LOADED_ONLY.get();
        villagerSleepLoadedOnly = VILLAGER_SLEEP_LOADED_ONLY.get();
        offThreadChunkLog = OFF_THREAD_CHUNK_LOG.get();
        chunkSafeTicking = CS_ENABLED.get();
        chunkSafeAllowDiskLoads = CS_DISK_LOADS.get();
        chunkSafeDiskCheckMs = CS_DISK_MS.get();
        chunkSafeLog = CS_LOG.get();
        chunkSafeExempt = CS_EXEMPT.get().stream().map(String::valueOf).toList();
        wendigoProvokedOnly = WENDIGO_ENABLED.get();
        wendigoProvokeRange = WENDIGO_PROVOKE_RANGE.get();
        wendigoProvokeRangeSneaking = WENDIGO_PROVOKE_RANGE_SNEAKING.get();
        wendigoStareRange = WENDIGO_STARE_RANGE.get();
        wendigoStareSeconds = WENDIGO_STARE_SECONDS.get();
        wendigoCalmRange = WENDIGO_CALM_RANGE.get();
        wendigoVanishAfterCalm = WENDIGO_VANISH_AFTER_CALM.get();
        wendigoKeepAwayRange = WENDIGO_KEEP_AWAY_RANGE.get();
        wendigoSoundRange = WENDIGO_SOUND_RANGE.get();
        wendigoDespawnNormally = WENDIGO_DESPAWN.get();
        activationEnabled = AR_ENABLED.get();
        activationRangeBlocks = AR_RANGE.get();
        activationInterval = AR_INTERVAL.get();
        activationAdaptive = AR_ADAPTIVE.get();
        activationEnableAboveMspt = AR_ENABLE_ABOVE.get();
        activationDisableBelowMspt = AR_DISABLE_BELOW.get();
        activationAgeCatchUp = AR_AGE_CATCH_UP.get();
        activationDespawnChecks = AR_DESPAWN_CHECKS.get();
        ActivationRange.reloadLists(AR_ALWAYS_ENTITIES.get(), AR_ALWAYS_NAMESPACES.get());
    }

    static String summary() {
        return "mimicSleep=" + mimicSleep + "(" + mimicWakeRange + ") watcherFollowRange=" + watcherFollowRange + "(" + watcherFollowRangeCap
                + ") obsessedSync=" + obsessedSync + " spiritLookGate=" + spiritLookGate + " fragileTorches=" + fragileTorches
                + " farmCharmSync=" + farmCharmSync + "(" + farmCharmRefreshTicks + "t) animationGates=" + animationGates
                + " adAstraExtraGate=" + adAstraExtraGate + " fullImmersionGate=" + fullImmersionGate + " alexsCavesGate=" + alexsCavesGate
                + " citadelCache=" + citadelCache + " antlersDayCounter=" + antlersDayCounter + " towwSyncGate=" + towwSyncGate
                + " rayLoadedOnly=" + rayLoadedOnly + " rayDedupe=" + rayDedupe
                + " obsessedChunkWait=" + obsessedChunkWait + "(" + obsessedChunkRadius + ") obsessedReadsLoadedOnly=" + obsessedReadsLoadedOnly
                + " villagerSleepLoadedOnly=" + villagerSleepLoadedOnly + " offThreadChunkLog=" + offThreadChunkLog
                + " chunkSafeTicking=" + chunkSafeTicking + "(diskLoads=" + chunkSafeAllowDiskLoads + ", exempt=" + chunkSafeExempt.size() + ")"
                + " | antlersWendigo enabled=" + wendigoProvokedOnly + " provoke=" + wendigoProvokeRange + "/" + wendigoProvokeRangeSneaking
                + " keepAway=" + wendigoKeepAwayRange + " stare=" + wendigoStareRange + "@" + wendigoStareSeconds + "s calm=" + wendigoCalmRange
                + " vanishAfterCalm=" + wendigoVanishAfterCalm + " sound=" + wendigoSoundRange
                + " despawnNormally=" + wendigoDespawnNormally
                + " | activationRange enabled=" + activationEnabled + " range=" + activationRangeBlocks
                + " interval=" + activationInterval + " adaptive=" + activationAdaptive + "(" + activationEnableAboveMspt + "/"
                + activationDisableBelowMspt + " ms)";
    }

    private TickFixesConfig() {}
}
