package spookydookie.tickfixes.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads Forge's Entity.persistentData field without creating it (getPersistentData() allocates on first use). */
@Mixin(value = Entity.class, remap = false)
public interface EntityPersistentDataAccessor {
    @Accessor(value = "persistentData", remap = false)
    CompoundTag spookyTickFixes$getRawPersistentData();
}
