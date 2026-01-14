package pl.fepbox.blockprotect.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import pl.fepbox.blockprotect.rule.BlockContext;
import pl.fepbox.blockprotect.rule.ProtectionType;
import pl.fepbox.blockprotect.rule.RuleManager;

public class BlockProtectionListener implements Listener {

    private final RuleManager ruleManager;

    public BlockProtectionListener(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        BlockContext context = BlockContext.from(block, player, tool);

        boolean blocked = ruleManager.isProtected(
                context,
                ProtectionType.BREAK,
                player,
                tool,
                true
        );

        if (blocked) {
            event.setCancelled(true);
            player.sendMessage("Nie możesz zniszczyć tego bloku");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            BlockContext context = BlockContext.from(block);
            return ruleManager.isProtected(
                    context,
                    ProtectionType.EXPLOSION,
                    null,
                    null,
                    false
            );
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            BlockContext context = BlockContext.from(block);
            return ruleManager.isProtected(
                    context,
                    ProtectionType.EXPLOSION,
                    null,
                    null,
                    false
            );
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            BlockContext context = BlockContext.from(block);
            if (ruleManager.isProtected(
                    context,
                    ProtectionType.PISTON,
                    null,
                    null,
                    false
            )) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            BlockContext context = BlockContext.from(block);
            if (ruleManager.isProtected(
                    context,
                    ProtectionType.PISTON,
                    null,
                    null,
                    false
            )) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        BlockContext context = BlockContext.from(block);
        if (ruleManager.isProtected(
                context,
                ProtectionType.FIRE,
                null,
                null,
                false
        )) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        BlockContext context = BlockContext.from(block);
        if (ruleManager.isProtected(
                context,
                ProtectionType.FIRE,
                null,
                null,
                false
        )) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block to = event.getToBlock();
        BlockContext context = BlockContext.from(to);
        if (ruleManager.isProtected(
                context,
                ProtectionType.FLUIDS,
                null,
                null,
                false
        )) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }

        Block block = event.getBlock();
        Material to = event.getTo();

        if (to == Material.AIR) {
            BlockContext contextCurrent = BlockContext.from(block);
            if (ruleManager.isProtected(
                    contextCurrent,
                    ProtectionType.BREAK,
                    null,
                    null,
                    false
            )) {
                event.setCancelled(true);
                return;
            }
        }

        Block below = block.getRelative(BlockFace.DOWN);
        BlockContext contextBelow = BlockContext.from(below);
        if (ruleManager.isProtected(
                contextBelow,
                ProtectionType.BREAK,
                null,
                null,
                false
        )) {
            event.setCancelled(true);
        }
    }
}

