package spookydookie.farsightimmptl;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Marker mod class; the behaviour lives in two client mixins and {@link HeldUnloads}. */
@Mod(FarsightImmPtlCompat.MOD_ID)
public final class FarsightImmPtlCompat {
    public static final String MOD_ID = "farsight_immptl_compat";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public FarsightImmPtlCompat() {
        LOGGER.info("[{}] loaded: Farsight's delayed chunk unload now runs on Immersive Portals' chunk map", MOD_ID);
    }
}
