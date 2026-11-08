package spookydookie.ultimineshapes;

import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Adds face-oriented mining shapes to FTB Ultimine: Square (2x2), Square (5x5) and Rectangle (5x3). FTB's own
 * Small Square (3x3) is the 3x3. See tools/spooky_ultimine_shapes/README.md.
 *
 * <p>FTB Ultimine looks shapes up by index on both sides: the server sends the selected index and the client prints
 * that entry's name. So both sides must register the same shapes in the same order, and this mod must be on both.
 */
@Mod(SpookyUltimineShapes.MOD_ID)
public final class SpookyUltimineShapes {
    public static final String MOD_ID = "spooky_ultimine_shapes";
    static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public SpookyUltimineShapes(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(this::commonSetup);
    }

    /**
     * Runs in the parallel part of common setup, on purpose. Registration order is then the same on every start:
     * <ol>
     *   <li>FTB Ultimine's built-ins, from its constructor. Every constructor has finished before common setup.</li>
     *   <li>These shapes.</li>
     *   <li>Ultimine Shapes+'s JSON shapes, from common setup's enqueueWork, which runs only after every
     *       parallel handler has finished.</li>
     * </ol>
     * Two mods using enqueueWork would be ordered by thread timing. ShapeRegistry's list is a CopyOnWriteArrayList,
     * so registering from a worker thread is safe. The client freezes the registry at client setup, which comes later.
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        int before = ShapeRegistry.shapeCount();
        ShapeRegistry.register(new FaceShape("square_2x2", 2, 2));
        ShapeRegistry.register(new FaceShape("square_5x5", 5, 5));
        ShapeRegistry.register(new FaceShape("rectangle_5x3", 5, 3));
        LOGGER.info("[{}] registered {} shapes at indices {}..{}", MOD_ID,
                ShapeRegistry.shapeCount() - before, before, ShapeRegistry.shapeCount() - 1);
    }
}
