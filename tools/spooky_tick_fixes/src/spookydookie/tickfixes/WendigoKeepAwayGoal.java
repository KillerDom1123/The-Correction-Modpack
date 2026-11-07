package spookydookie.tickfixes;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Added to Antlers' Wendigo (night form) at goal priority 1, ahead of its stroll, look-at-player and melee goals.
 * While it is not provoked it moves away from players (WendigoTemper.keepAwayFrom picks who): a player it can see within
 * keepAwayRange, or, after it calmed down, any player within 48 blocks. Like vanilla AvoidEntityGoal: a random spot up to
 * 16 blocks away that is further from the player, faster when the player is within 7 blocks. It also holds the LOOK
 * flag, so it doesn't stand watching a player it is leaving. Inert while [antlersWendigo] is off or it is provoked.
 */
public final class WendigoKeepAwayGoal extends Goal {
    private static final double WALK = 1.5D;   // x its 0.2 movement speed: ~6 m/s, a bit faster than a sprinting player
    private static final double RUN = 1.9D;
    private final PathfinderMob mob;
    private Player from;
    private int repath;

    public WendigoKeepAwayGoal(PathfinderMob mob) {
        this.mob = mob;
        m_7021_(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean m_8036_() {
        from = WendigoTemper.keepAwayFrom(mob);
        return from != null && pathAway();
    }

    @Override
    public boolean m_8045_() {
        return from != null && !mob.m_21573_().m_26571_() && WendigoTemper.stillKeepingAway(mob, from);
    }

    @Override
    public void m_8056_() {
        repath = 20;
        WendigoTemper.setFleeing(mob, true);
    }

    @Override
    public void m_8041_() {
        from = null;
        WendigoTemper.setFleeing(mob, false);
    }

    @Override
    public void m_8037_() {
        if (from == null) return;
        mob.m_21573_().m_26517_(mob.m_20280_(from) < 49.0D ? RUN : WALK);
        // A player who keeps following gets a fresh path once a second (the nearest one, if that changed).
        if (--repath <= 0) {
            repath = 20;
            Player nearest = WendigoTemper.keepAwayFrom(mob);
            if (nearest != null) {
                from = nearest;
                pathAway();
            }
        }
    }

    private boolean pathAway() {
        Vec3 away = DefaultRandomPos.m_148407_(mob, 16, 7, from.m_20182_());
        if (away == null || from.m_20275_(away.f_82479_, away.f_82480_, away.f_82481_) < from.m_20280_(mob)) return false;
        return mob.m_21573_().m_26519_(away.f_82479_, away.f_82480_, away.f_82481_, mob.m_20280_(from) < 49.0D ? RUN : WALK);
    }
}
