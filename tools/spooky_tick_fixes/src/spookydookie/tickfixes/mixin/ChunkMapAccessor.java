package spookydookie.tickfixes.mixin;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** ChunkMap.getVisibleChunkIfPresent is protected. */
@Mixin(value = ChunkMap.class, remap = false)
public interface ChunkMapAccessor {
    @Invoker(value = "m_140327_", remap = false)
    ChunkHolder spookyTickFixes$getVisibleChunkIfPresent(long key);
}
