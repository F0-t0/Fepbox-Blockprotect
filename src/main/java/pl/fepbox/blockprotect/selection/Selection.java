package pl.fepbox.blockprotect.selection;

import org.bukkit.World;
import org.bukkit.block.Block;

public class Selection {

    private final World world;
    private BlockPosition pos1;
    private BlockPosition pos2;

    public Selection(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public BlockPosition getPos1() {
        return pos1;
    }

    public BlockPosition getPos2() {
        return pos2;
    }

    public void setPos1(Block block) {
        this.pos1 = new BlockPosition(block.getX(), block.getY(), block.getZ());
    }

    public void setPos2(Block block) {
        this.pos2 = new BlockPosition(block.getX(), block.getY(), block.getZ());
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    public int getMinX() {
        return Math.min(pos1.x(), pos2.x());
    }

    public int getMinY() {
        return Math.min(pos1.y(), pos2.y());
    }

    public int getMinZ() {
        return Math.min(pos1.z(), pos2.z());
    }

    public int getMaxX() {
        return Math.max(pos1.x(), pos2.x());
    }

    public int getMaxY() {
        return Math.max(pos1.y(), pos2.y());
    }

    public int getMaxZ() {
        return Math.max(pos1.z(), pos2.z());
    }

    public record BlockPosition(int x, int y, int z) {
    }
}

