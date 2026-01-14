package pl.fepbox.blockprotect.rule;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockContext {

    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final Material material;
    private final Player player;
    private final ItemStack tool;

    public BlockContext(World world,
                        int x,
                        int y,
                        int z,
                        Material material,
                        Player player,
                        ItemStack tool) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
        this.player = player;
        this.tool = tool;
    }

    public static BlockContext from(Block block) {
        return new BlockContext(
                block.getWorld(),
                block.getX(),
                block.getY(),
                block.getZ(),
                block.getType(),
                null,
                null
        );
    }

    public static BlockContext from(Block block, Player player, ItemStack tool) {
        return new BlockContext(
                block.getWorld(),
                block.getX(),
                block.getY(),
                block.getZ(),
                block.getType(),
                player,
                tool
        );
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Material getMaterial() {
        return material;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getTool() {
        return tool;
    }
}

