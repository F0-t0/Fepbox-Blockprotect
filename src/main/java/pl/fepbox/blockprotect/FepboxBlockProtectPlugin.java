package pl.fepbox.blockprotect;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pl.fepbox.blockprotect.command.BlockProtectCommand;
import pl.fepbox.blockprotect.listener.BlockProtectionListener;
import pl.fepbox.blockprotect.listener.WandListener;
import pl.fepbox.blockprotect.rule.BlockGroupRegistry;
import pl.fepbox.blockprotect.rule.RuleManager;
import pl.fepbox.blockprotect.selection.SelectionManager;

public class FepboxBlockProtectPlugin extends JavaPlugin {

    private RuleManager ruleManager;
    private SelectionManager selectionManager;
    private BlockGroupRegistry blockGroupRegistry;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.blockGroupRegistry = new BlockGroupRegistry();
        this.ruleManager = new RuleManager(this, blockGroupRegistry);
        this.selectionManager = new SelectionManager();

        this.ruleManager.reloadFromConfig(getConfig());

        getServer().getPluginManager().registerEvents(
                new BlockProtectionListener(ruleManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new WandListener(selectionManager),
                this
        );

        PluginCommand bpCommand = getCommand("bp");
        if (bpCommand != null) {
            BlockProtectCommand executor = new BlockProtectCommand(this, ruleManager, selectionManager);
            bpCommand.setExecutor(executor);
            bpCommand.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        // configuration is saved on each modification, nothing else to persist
    }

    public RuleManager getRuleManager() {
        return ruleManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public BlockGroupRegistry getBlockGroupRegistry() {
        return blockGroupRegistry;
    }
}

