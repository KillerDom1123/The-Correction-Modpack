package spookydookie.tickfixes;

/** Duck interface added to Mob by MobFlagsMixin. */
public interface MobTickFlags {
    /** True while Full Immersion's begin() was skipped for this mob's current AI step, so finishAi() is skipped too. */
    boolean spookyTickFixes$fiSkipped();

    void spookyTickFixes$setFiSkipped(boolean skipped);
}
