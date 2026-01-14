package pl.fepbox.blockprotect.rule;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RuleBypass {

    private final Set<String> permissions;
    private final EnumSet<GameMode> gameModes;
    private final Set<UUID> players;
    private final Set<Material> tools;

    public RuleBypass(Set<String> permissions,
                      EnumSet<GameMode> gameModes,
                      Set<UUID> players,
                      Set<Material> tools) {
        this.permissions = permissions != null ? permissions : Collections.emptySet();
        this.gameModes = gameModes != null ? gameModes : EnumSet.noneOf(GameMode.class);
        this.players = players != null ? players : Collections.emptySet();
        this.tools = tools != null ? tools : Collections.emptySet();
    }

    public static RuleBypass empty() {
        return new RuleBypass(new HashSet<>(), EnumSet.noneOf(GameMode.class), new HashSet<>(), new HashSet<>());
    }

    public boolean isBypassed(Player player, ItemStack tool) {
        if (player == null) {
            return false;
        }

        for (String permission : permissions) {
            if (player.hasPermission(permission)) {
                return true;
            }
        }

        if (!gameModes.isEmpty() && gameModes.contains(player.getGameMode())) {
            return true;
        }

        if (!players.isEmpty() && players.contains(player.getUniqueId())) {
            return true;
        }

        if (!tools.isEmpty() && tool != null && tools.contains(tool.getType())) {
            return true;
        }

        return false;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public EnumSet<GameMode> getGameModes() {
        return gameModes;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public Set<Material> getTools() {
        return tools;
    }

    public static RuleBypass fromConfig(List<String> permissionList,
                                        List<String> gamemodeList,
                                        List<String> playerList,
                                        List<String> toolList) {
        Set<String> permissionSet = new HashSet<>();
        if (permissionList != null) {
            permissionSet.addAll(permissionList);
        }

        EnumSet<GameMode> gameModes = EnumSet.noneOf(GameMode.class);
        if (gamemodeList != null) {
            for (String gm : gamemodeList) {
                try {
                    gameModes.add(GameMode.valueOf(gm.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        Set<UUID> playerSet = new HashSet<>();
        if (playerList != null) {
            for (String s : playerList) {
                try {
                    playerSet.add(UUID.fromString(s));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        Set<Material> toolSet = new HashSet<>();
        if (toolList != null) {
            for (String t : toolList) {
                try {
                    toolSet.add(Material.valueOf(t.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return new RuleBypass(permissionSet, gameModes, playerSet, toolSet);
    }
}

