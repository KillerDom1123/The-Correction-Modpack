package spookydookie.ultimineshapes;

import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A one-layer rectangle laid flat against the face of the block the player hit, like FTB Ultimine's own Small Square
 * (3x3), but any size. Nothing is mined behind the hit block.
 *
 * <p>Axes, as the player sees the face:
 * <ul>
 *   <li>{@code width} runs across the face, left to right.</li>
 *   <li>{@code height} runs up the face on a wall. On a floor or ceiling it runs forward, away from the player.</li>
 * </ul>
 * An odd size is centred on the hit block. An even size can't be centred, so the extra block goes to the player's
 * right, forward on a floor or ceiling, and towards eye level on a wall. So a 2x2 aimed at either the head-height or
 * the foot-height block in front of a standing player clears the same 2-tall walkway.
 *
 * <p>Immutable and stateless, so it is safe on any thread (DimThreads ticks each dimension on its own worker).
 */
final class FaceShape implements Shape {
    private final String name;
    /** (across, along) offsets, nearest first, excluding (0, 0). One list per direction the even 'along' size grows. */
    private final int[][] alongPositive;
    private final int[][] alongNegative;

    /** {@code id} becomes the shape name {@code spooky_ultimine_shapes.<id>}, labelled by the lang key ftbultimine.shape.<name>. */
    FaceShape(String id, int width, int height) {
        if (width < 1 || height < 1) throw new IllegalArgumentException(id + ": size must be at least 1x1");
        this.name = SpookyUltimineShapes.MOD_ID + "." + id;
        this.alongPositive = cells(width, height, true);
        this.alongNegative = cells(width, height, false);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<BlockPos> getBlocks(ShapeContext ctx) {
        ServerPlayer player = ctx.player();
        // getDirection(): the player's horizontal facing. getEyeY(): eye height.
        List<BlockPos> all = positions(ctx.pos(), ctx.face(), player.m_6350_(), player.m_20188_());
        int max = Math.max(1, ctx.maxBlocks());
        List<BlockPos> out = new ArrayList<>(Math.min(max, all.size()));
        out.add(all.get(0));                         // the hit block, as FTB's own shapes do
        for (int i = 1; i < all.size() && out.size() < max; i++) {
            BlockPos pos = all.get(i);
            BlockState state = ctx.block(pos);
            if (!state.m_60795_() && ctx.check(state)) out.add(pos);    // isAir()
        }
        return out;
    }

    /** Every position of the shape for this hit, the hit block first, then nearest first. Unfiltered. */
    List<BlockPos> positions(BlockPos origin, Direction face, Direction playerFacing, double eyeY) {
        if (face == null) face = Direction.UP;
        Direction across;
        Direction along;
        boolean positive;
        if (face.m_122434_().m_122478_()) {         // getAxis().isVertical(): a floor or ceiling
            across = playerFacing.m_122427_();      // getClockWise(): the player's right
            along = playerFacing;
            positive = true;
        } else {                                    // a wall: the player looks into the face
            across = face.m_122424_().m_122427_();  // getOpposite().getClockWise()
            along = Direction.UP;
            // getY() below the block holding the eyes: grow upwards, otherwise downwards.
            positive = origin.m_123342_() < (int) Math.floor(eyeY);
        }

        int[][] cells = positive ? alongPositive : alongNegative;
        List<BlockPos> out = new ArrayList<>(cells.length + 1);
        out.add(origin);
        for (int[] c : cells) {
            int a = c[0];
            int b = c[1];
            out.add(origin.m_7918_(                                     // offset(x, y, z)
                    across.m_122429_() * a + along.m_122429_() * b,     // getStepX()
                    across.m_122430_() * a + along.m_122430_() * b,     // getStepY()
                    across.m_122431_() * a + along.m_122431_() * b));   // getStepZ()
        }
        return out;
    }

    private static int[][] cells(int width, int height, boolean alongPositive) {
        int[] a = span(width, true);
        int[] b = span(height, alongPositive);
        List<int[]> list = new ArrayList<>();
        for (int i = a[0]; i <= a[1]; i++) {
            for (int j = b[0]; j <= b[1]; j++) {
                if (i != 0 || j != 0) list.add(new int[] {i, j});
            }
        }
        // Nearest first, so a short budget (max_blocks, hunger) takes the blocks around the hit block.
        list.sort(Comparator.<int[]>comparingInt(c -> Math.abs(c[0]) + Math.abs(c[1]))
                .thenComparingInt(c -> Math.abs(c[1]))
                .thenComparingInt(c -> c[1])
                .thenComparingInt(c -> c[0]));
        return list.toArray(new int[0][]);
    }

    /** Lowest and highest offset for a size: centred when odd; the extra block on the positive or negative side when even. */
    private static int[] span(int size, boolean positive) {
        if (size % 2 == 1) return new int[] {-(size - 1) / 2, (size - 1) / 2};
        return positive ? new int[] {-(size / 2 - 1), size / 2} : new int[] {-size / 2, size / 2 - 1};
    }
}
