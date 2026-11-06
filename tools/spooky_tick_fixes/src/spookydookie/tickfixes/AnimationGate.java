package spookydookie.tickfixes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * MCreator's EntityAnimationFactory.onEntityTick runs for every living entity and walks an instanceof /
 * String.equals chain over the mod's own entity classes (verified per jar: every instanceof target is in the
 * mod's entity package). A mob whose class hierarchy contains no class from that package can never match, so
 * the handler is skipped for it. The answer is cached per class.
 */
public final class AnimationGate {
    public static final AnimationGate BORN_IN_CHAOS = new AnimationGate("net.mcreator.borninchaosv.entity.");
    public static final AnimationGate OBSESSED = new AnimationGate("net.theobsessed.entity.");
    public static final AnimationGate CREEPING_WOODS = new AnimationGate("net.mcreator.creepingwoods.entity.");
    public static final AnimationGate FOES = new AnimationGate("mod.foes.entity.");
    public static final AnimationGate TOWW = new AnimationGate("net.mcreator.theonewhowatches.entity.");
    public static final AnimationGate RAKE = new AnimationGate("net.mcreator.rakethearrival.entity.");
    public static final AnimationGate ANTLERS = new AnimationGate("net.mcreator.antlers.entity.");

    private final String entityPackage;
    private final ClassValue<Boolean> own = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                if (c.getName().startsWith(entityPackage)) return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    };

    private AnimationGate(String entityPackage) {
        this.entityPackage = entityPackage;
    }

    /** @return true if the handler can be skipped for this event. */
    public boolean skip(LivingEvent.LivingTickEvent event) {
        if (!TickFixesConfig.animationGates) return false;
        LivingEntity entity = event.getEntity();
        return entity != null && !own.get(entity.getClass());
    }
}
