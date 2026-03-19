package dev.anoopkrishnarao.bolted.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.anoopkrishnarao.bolted.Bolted;
import dev.anoopkrishnarao.bolted.lock.LockState;
import dev.anoopkrishnarao.bolted.lock.LockStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class LockIndicatorRenderer {

    private static final int COLOR_UNLOCKED      = 0xFF39FF14;
    private static final int COLOR_ENTITY_LOCKED = 0xFFFFE600;
    private static final int COLOR_FULLY_LOCKED  = 0xFFFF073A;
    private static final int COLOR_BORDER        = 0xFF1a1a1a;

    private static final double DOOR_THICKNESS = 0.1875;
    private static final float PIXEL_SIZE = 0.013f;

    private static final int[][] CIRCLE_PIXELS = {
            {-1,3},{0,3},{1,3},
            {-2,2},{-1,2},{0,2},{1,2},{2,2},
            {-3,1},{-2,1},{-1,1},{0,1},{1,1},{2,1},{3,1},
            {-3,0},{-2,0},{-1,0},{0,0},{1,0},{2,0},{3,0},
            {-3,-1},{-2,-1},{-1,-1},{0,-1},{1,-1},{2,-1},{3,-1},
            {-2,-2},{-1,-2},{0,-2},{1,-2},{2,-2},
            {-1,-3},{0,-3},{1,-3},
    };

    private static final int[][] BORDER_PIXELS = {
            {-1,4},{0,4},{1,4},
            {-1,-4},{0,-4},{1,-4},
            {-4,-1},{-4,0},{-4,1},
            {4,-1},{4,0},{4,1},
            {-2,3},{-3,2},{-3,1},
            {2,3},{3,2},{3,1},
            {-2,-3},{-3,-2},{-3,-1},
            {2,-3},{3,-2},{3,-1},
    };

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || Bolted.LOCK_STORAGE == null) return;
        if (!Bolted.CONFIG.showIndicator) return;

        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        LockStorage storage = Bolted.LOCK_STORAGE;
        String dimension = level.dimension().identifier().toString();

        BlockPos playerPos = mc.player.blockPosition();
        Iterable<BlockPos> nearby = BlockPos.betweenClosed(
                playerPos.offset(-32, -32, -32),
                playerPos.offset(32, 32, 32)
        );

        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.debugQuads());
        Vec3 camPos = new Vec3(camX, camY, camZ);

        for (BlockPos pos : nearby) {
            BlockState blockState = level.getBlockState(pos);
            if (!(blockState.getBlock() instanceof DoorBlock)) continue;
            if (blockState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) continue;

            LockState state = storage.getState(dimension, pos);
            int color = getColor(state);

            for (boolean front : new boolean[]{true, false}) {
                float[] normal = getDoorNormal(blockState, front);
                Vec3 dotPos = getDoorDotPosition(blockState, pos, front);

                Vec3 toCamera = camPos.subtract(dotPos).normalize();
                double dot = toCamera.x * normal[0] + toCamera.y * normal[1] + toCamera.z * normal[2];
                if (dot <= 0) continue;

                poseStack.pushPose();
                poseStack.translate(dotPos.x - camX, dotPos.y - camY, dotPos.z - camZ);
                Matrix4f mat = poseStack.last().pose();
                drawPixelCircle(consumer, mat, normal, color);
                poseStack.popPose();
            }
        }
    }

    private static Vec3 getDoorDotPosition(BlockState state, BlockPos pos, boolean front) {
        var facing = state.getValue(DoorBlock.FACING);
        boolean open = state.getValue(DoorBlock.OPEN);
        var hinge = state.getValue(DoorBlock.HINGE);

        var ef = open
                ? (hinge == DoorHingeSide.LEFT ? facing.getCounterClockWise() : facing.getClockWise())
                : facing;

        Level level = Minecraft.getInstance().level;
        var shape = state.getShape(level, pos);
        var aabb = shape.bounds();

        double cy = pos.getY() + aabb.minY + (aabb.maxY - aabb.minY) * 0.6;
        double epsilon = 0.001;

        double x, z;
        switch (ef) {
            case NORTH -> {
                z = front
                        ? pos.getZ() + aabb.minZ - epsilon
                        : pos.getZ() + aabb.maxZ + epsilon;
                x = pos.getX() + aabb.minX + (aabb.maxX - aabb.minX) * 0.5;
            }
            case SOUTH -> {
                z = front
                        ? pos.getZ() + aabb.maxZ + epsilon
                        : pos.getZ() + aabb.minZ - epsilon;
                x = pos.getX() + aabb.minX + (aabb.maxX - aabb.minX) * 0.5;
            }
            case WEST -> {
                x = front
                        ? pos.getX() + aabb.minX - epsilon
                        : pos.getX() + aabb.maxX + epsilon;
                z = pos.getZ() + aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5;
            }
            case EAST -> {
                x = front
                        ? pos.getX() + aabb.maxX + epsilon
                        : pos.getX() + aabb.minX - epsilon;
                z = pos.getZ() + aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5;
            }
            default -> {
                x = pos.getX() + 0.5;
                z = pos.getZ() + 0.5;
            }
        }

        return new Vec3(x, cy, z);
    }

    private static float[] getDoorNormal(BlockState state, boolean front) {
        var facing = state.getValue(DoorBlock.FACING);
        boolean open = state.getValue(DoorBlock.OPEN);
        var hinge = state.getValue(DoorBlock.HINGE);

        var ef = open
                ? (hinge == DoorHingeSide.LEFT ? facing.getCounterClockWise() : facing.getClockWise())
                : facing;

        float s = front ? -1f : 1f;
        return switch (ef) {
            case NORTH -> new float[]{0, 0,  s};
            case SOUTH -> new float[]{0, 0, -s};
            case WEST  -> new float[]{ s, 0, 0};
            case EAST  -> new float[]{-s, 0, 0};
            default    -> new float[]{0,  s, 0};
        };
    }

    private static void drawPixelCircle(VertexConsumer consumer, Matrix4f mat,
                                        float[] normal, int colorArgb) {
        for (int[] pixel : BORDER_PIXELS) {
            drawPixel(consumer, mat, normal, pixel[0], pixel[1], COLOR_BORDER);
        }
        for (int[] pixel : CIRCLE_PIXELS) {
            drawPixel(consumer, mat, normal, pixel[0], pixel[1], colorArgb);
        }
    }

    private static void drawPixel(VertexConsumer consumer, Matrix4f mat,
                                  float[] normal, int col, int row, int argb) {
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8)  & 0xFF) / 255f;
        float b = ((argb)       & 0xFF) / 255f;

        float nx = normal[0], ny = normal[1], nz = normal[2];
        float[] uDir, vDir;
        if (Math.abs(nx) > 0.5f) {
            uDir = new float[]{0, 0, 1};
            vDir = new float[]{0, 1, 0};
        } else if (Math.abs(nz) > 0.5f) {
            uDir = new float[]{1, 0, 0};
            vDir = new float[]{0, 1, 0};
        } else {
            uDir = new float[]{1, 0, 0};
            vDir = new float[]{0, 0, 1};
        }

        float cx = (uDir[0] * col + vDir[0] * row) * PIXEL_SIZE;
        float cy = (uDir[1] * col + vDir[1] * row) * PIXEL_SIZE;
        float cz = (uDir[2] * col + vDir[2] * row) * PIXEL_SIZE;

        float hx = uDir[0] * PIXEL_SIZE * 0.5f;
        float hy = uDir[1] * PIXEL_SIZE * 0.5f;
        float hz = uDir[2] * PIXEL_SIZE * 0.5f;

        float vx = vDir[0] * PIXEL_SIZE * 0.5f;
        float vy = vDir[1] * PIXEL_SIZE * 0.5f;
        float vz = vDir[2] * PIXEL_SIZE * 0.5f;

        consumer.addVertex(mat, cx - hx - vx, cy - hy - vy, cz - hz - vz).setColor(r, g, b, a);
        consumer.addVertex(mat, cx + hx - vx, cy + hy - vy, cz + hz - vz).setColor(r, g, b, a);
        consumer.addVertex(mat, cx + hx + vx, cy + hy + vy, cz + hz + vz).setColor(r, g, b, a);
        consumer.addVertex(mat, cx - hx + vx, cy - hy + vy, cz - hz + vz).setColor(r, g, b, a);
    }

    private static int getColor(LockState state) {
        return switch (state) {
            case UNLOCKED      -> COLOR_UNLOCKED;
            case ENTITY_LOCKED -> COLOR_ENTITY_LOCKED;
            case FULLY_LOCKED  -> COLOR_FULLY_LOCKED;
        };
    }
}