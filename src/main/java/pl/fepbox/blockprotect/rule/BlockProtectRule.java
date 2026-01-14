package pl.fepbox.blockprotect.rule;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BlockProtectRule {

    private final String id;
    private final RuleScope scope;
    private final Set<Material> materials;
    private final ProtectionFlags flags;
    private final RuleBypass bypass;

    public BlockProtectRule(String id,
                            RuleScope scope,
                            Set<Material> materials,
                            ProtectionFlags flags,
                            RuleBypass bypass) {
        this.id = id;
        this.scope = scope;
        this.materials = Collections.unmodifiableSet(new HashSet<>(materials));
        this.flags = flags;
        this.bypass = bypass;
    }

    public String getId() {
        return id;
    }

    public RuleScope getScope() {
        return scope;
    }

    public Set<Material> getMaterials() {
        return materials;
    }

    public ProtectionFlags getFlags() {
        return flags;
    }

    public RuleBypass getBypass() {
        return bypass;
    }

    public boolean matches(BlockContext context) {
        return scope.contains(context) && materials.contains(context.getMaterial());
    }

    public boolean isProtectionEnabled(ProtectionType type) {
        return flags.isEnabled(type);
    }

    public boolean isBypassed(Player player, ItemStack tool) {
        return bypass != null && bypass.isBypassed(player, tool);
    }
}

