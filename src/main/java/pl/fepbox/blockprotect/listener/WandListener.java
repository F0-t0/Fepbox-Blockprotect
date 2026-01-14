package pl.fepbox.blockprotect.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.fepbox.blockprotect.selection.Selection;
import pl.fepbox.blockprotect.selection.SelectionManager;

public class WandListener implements Listener {

    private static final String WAND_NAME = "BlockProtect Wand";

    private final SelectionManager selectionManager;

    public WandListener(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!isWand(item)) {
            return;
        }

        Action action = event.getAction();
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        Selection selection = selectionManager.getOrCreateSelection(player);

        if (action == Action.LEFT_CLICK_BLOCK) {
            selection.setPos1(clicked);
            player.sendMessage("Ustawiono pos1: "
                    + clicked.getX() + ", "
                    + clicked.getY() + ", "
                    + clicked.getZ());
            event.setCancelled(true);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            selection.setPos2(clicked);
            player.sendMessage("Ustawiono pos2: "
                    + clicked.getX() + ", "
                    + clicked.getY() + ", "
                    + clicked.getZ());
            event.setCancelled(true);
        }
    }

    private boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        return WAND_NAME.equals(meta.getDisplayName());
    }

    public static ItemStack createWandItem() {
        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(WAND_NAME);
            wand.setItemMeta(meta);
        }
        return wand;
    }
}

