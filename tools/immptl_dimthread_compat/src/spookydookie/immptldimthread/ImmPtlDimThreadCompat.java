package spookydookie.immptldimthread;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Marker mod class; all behaviour lives in the single mixin. */
@Mod(ImmPtlDimThreadCompat.MOD_ID)
public final class ImmPtlDimThreadCompat {
    public static final String MOD_ID = "immptl_dimthread_compat";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ImmPtlDimThreadCompat() {
        LOGGER.info("[{}] loaded: Immersive Portals' chunk-ticket flush now accepts the DimThreads worker that owns the level", MOD_ID);
    }
}
