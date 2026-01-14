package pl.fepbox.blockprotect.selection;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    private final Map<UUID, Selection> selections = new HashMap<>();

    public Selection getOrCreateSelection(Player player) {
        Selection selection = selections.get(player.getUniqueId());
        World world = player.getWorld();
        if (selection == null || selection.getWorld() != world) {
            selection = new Selection(world);
            selections.put(player.getUniqueId(), selection);
        }
        return selection;
    }

    public Selection getSelection(Player player) {
        return selections.get(player.getUniqueId());
    }
}

