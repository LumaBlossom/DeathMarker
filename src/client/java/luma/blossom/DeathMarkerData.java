package luma.blossom;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class DeathMarkerData {
    private final BlockPos position;
    private final ResourceKey<Level> dimension;
    private long accumulatedTicks;
    private final long creationTime;

    public static final int DESPAWN_TICKS = 6000;
    public static final int SIMULATION_DISTANCE_BLOCKS = 128;

    public DeathMarkerData(BlockPos position, ResourceKey<Level> dimension) {
        this.position = position;
        this.dimension = dimension;
        this.accumulatedTicks = 0;
        this.creationTime = System.currentTimeMillis();
    }

    public DeathMarkerData(BlockPos position, ResourceKey<Level> dimension, long accumulatedTicks) {
        this.position = position;
        this.dimension = dimension;
        this.accumulatedTicks = accumulatedTicks;
        this.creationTime = System.currentTimeMillis();
    }

    public BlockPos getPosition() {
        return position;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public long getAccumulatedTicks() {
        return accumulatedTicks;
    }

    public void addTick() {
        this.accumulatedTicks++;
    }

    public boolean isExpired() {
        return accumulatedTicks >= DESPAWN_TICKS;
    }

    public int getRemainingSeconds() {
        long remainingTicks = DESPAWN_TICKS - accumulatedTicks;
        return (int) Math.max(0, remainingTicks / 20);
    }

    public String getFormattedTimeRemaining() {
        int totalSeconds = getRemainingSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public double getDistanceTo(double x, double y, double z) {
        return Math.sqrt(
            Math.pow(position.getX() + 0.5 - x, 2) +
            Math.pow(position.getY() + 0.5 - y, 2) +
            Math.pow(position.getZ() + 0.5 - z, 2)
        );
    }

    public boolean isPlayerInRange(double playerX, double playerY, double playerZ) {
        return getDistanceTo(playerX, playerY, playerZ) <= SIMULATION_DISTANCE_BLOCKS;
    }
}
