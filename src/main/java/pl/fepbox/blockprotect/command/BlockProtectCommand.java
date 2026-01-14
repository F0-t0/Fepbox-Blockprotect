package pl.fepbox.blockprotect.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import pl.fepbox.blockprotect.FepboxBlockProtectPlugin;
import pl.fepbox.blockprotect.listener.WandListener;
import pl.fepbox.blockprotect.rule.BlockProtectRule;
import pl.fepbox.blockprotect.rule.RuleManager;
import pl.fepbox.blockprotect.rule.ScopeType;
import pl.fepbox.blockprotect.selection.Selection;
import pl.fepbox.blockprotect.selection.SelectionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BlockProtectCommand implements CommandExecutor, TabCompleter {

    private final FepboxBlockProtectPlugin plugin;
    private final RuleManager ruleManager;
    private final SelectionManager selectionManager;

    public BlockProtectCommand(FepboxBlockProtectPlugin plugin,
                               RuleManager ruleManager,
                               SelectionManager selectionManager) {
        this.plugin = plugin;
        this.ruleManager = ruleManager;
        this.selectionManager = selectionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/bp wand|createcuboid|addruleblock|removeruleblock|setflag|delete|list|reload");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "wand" -> handleWand(sender);
            case "createcuboid" -> handleCreateCuboid(sender, args);
            case "addruleblock" -> handleAddRuleBlock(sender, args);
            case "removeruleblock" -> handleRemoveRuleBlock(sender, args);
            case "setflag" -> handleSetFlag(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage("Nieznana komenda. Użyj: /bp list");
        }

        return true;
    }

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ta komenda jest tylko dla graczy.");
            return;
        }
        if (!sender.hasPermission("fepbox.blockprotect.admin")) {
            sender.sendMessage("Brak uprawnień.");
            return;
        }
        player.getInventory().addItem(WandListener.createWandItem());
        sender.sendMessage("Otrzymano różdżkę zaznaczania cuboida.");
    }

    private void handleCreateCuboid(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ta komenda jest tylko dla graczy.");
            return;
        }
        if (!sender.hasPermission("fepbox.blockprotect.admin")) {
            sender.sendMessage("Brak uprawnień.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Użycie: /bp createcuboid <id>");
            return;
        }
        String id = args[1];
        Selection selection = selectionManager.getSelection(player);
        if (selection == null || !selection.isComplete()) {
            sender.sendMessage("Najpierw zaznacz pos1 i pos2 różdżką.");
            return;
        }
        ruleManager.createCuboidRule(
                id,
                selection.getWorld().getName(),
                selection.getMinX(),
                selection.getMinY(),
                selection.getMinZ(),
                selection.getMaxX(),
                selection.getMaxY(),
                selection.getMaxZ()
        );
        sender.sendMessage("Utworzono/zmodyfikowano regułę cuboid: " + id);
    }

    private void handleAddRuleBlock(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fepbox.blockprotect.admin")) {
            sender.sendMessage("Brak uprawnień.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Użycie: /bp addruleblock <id> <MATERIAL|#group>");
            return;
        }
        String id = args[1];
        String value = args[2];
        boolean ok = ruleManager.addRuleBlock(id, value);
        if (!ok) {
            sender.sendMessage("Nie znaleziono reguły: " + id);
        } else {
            sender.sendMessage("Dodano blok/grupę do reguły: " + id);
        }
    }

    private void handleRemoveRuleBlock(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fepbox.blockprotect.admin")) {
            sender.sendMessage("Brak uprawnień.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Użycie: /bp removeruleblock <id> <MATERIAL|#group>");
            return;
        }
        String id = args[1];
        String value = args[2];
        boolean ok = ruleManager.removeRuleBlock(id, value);
        if (!ok) {
            sender.sendMessage("Nie znaleziono reguły lub bloku/grupy.");
        } else {
            sender.sendMessage("Usunięto blok/grupę z reguły: " + id);
        }
    }

    private void handleSetFlag(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fepbox.blockprotect.admin")) {
            sender.sendMessage("Brak uprawnień.");
            return;
        }
        if (args.length < 4) {
            sender.sendMessage("Użycie: /bp setflag <id> <break|explosion|piston|fire|fluids> <true|false>");
            return;
        }
        String id = args[1];
        String flag = args[2];
        boolean value = Boolean.parseBoolean(args[3]);
        boolean ok = ruleManager.setFlag(id, flag, value);
        if (!ok) {
            sender.sendMessage("Nie znaleziono reguły lub flagi.");
        } else {
            sender.sendMessage("Ustawiono flagę " + flag + " na " + value + " dla reguły " + id + ".");
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fepbox.blockprotect.admin")) {
            sender.sendMessage("Brak uprawnień.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Użycie: /bp delete <id>");
            return;
        }
        String id = args[1];
        boolean ok = ruleManager.deleteRule(id);
        if (!ok) {
            sender.sendMessage("Nie znaleziono reguły: " + id);
        } else {
            sender.sendMessage("Usunięto regułę: " + id);
        }
    }

    private void handleList(CommandSender sender) {
        Collection<BlockProtectRule> rules = ruleManager.getAllRules();
        if (rules.isEmpty()) {
            sender.sendMessage("Brak zdefiniowanych reguł.");
            return;
        }
        sender.sendMessage("Reguły BlockProtect:");
        for (BlockProtectRule rule : rules) {
            ScopeType type = rule.getScope().getType();
            int blockCount = rule.getMaterials().size();
            sender.sendMessage("- " + rule.getId() + " [" + type + "], bloki: " + blockCount);
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("fepbox.blockprotect.reload")) {
            sender.sendMessage("Brak uprawnień.");
            return;
        }
        plugin.reloadConfig();
        ruleManager.reloadFromConfig(plugin.getConfig());
        sender.sendMessage("Przeładowano konfigurację Fepbox-BlockProtect.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> sub = Arrays.asList("wand", "createcuboid", "addruleblock", "removeruleblock", "setflag", "delete", "list", "reload");
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], sub, completions);
            return completions;
        }
        if (args.length == 2 && Arrays.asList("createcuboid", "addruleblock", "removeruleblock", "setflag", "delete").contains(args[0].toLowerCase(Locale.ROOT))) {
            List<String> ids = new ArrayList<>();
            for (BlockProtectRule rule : ruleManager.getAllRules()) {
                ids.add(rule.getId());
            }
            Collections.sort(ids);
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[1], ids, completions);
            return completions;
        }
        if (args.length == 3 && "setflag".equalsIgnoreCase(args[0])) {
            List<String> flags = Arrays.asList("break", "explosion", "piston", "fire", "fluids");
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[2], flags, completions);
            return completions;
        }
        if (args.length == 4 && "setflag".equalsIgnoreCase(args[0])) {
            List<String> bools = Arrays.asList("true", "false");
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[3], bools, completions);
            return completions;
        }
        return Collections.emptyList();
    }
}

