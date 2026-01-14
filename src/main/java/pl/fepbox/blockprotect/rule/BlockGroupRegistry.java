package pl.fepbox.blockprotect.rule;

import org.bukkit.Material;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockGroupRegistry {

    private final Map<String, Set<Material>> groups = new HashMap<>();

    public void clear() {
        groups.clear();
    }

    public void registerGroup(String id, List<String> materialNames) {
        Set<Material> materials = new HashSet<>();
        if (materialNames != null) {
            for (String name : materialNames) {
                try {
                    materials.add(Material.valueOf(name.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        groups.put(id.toLowerCase(), materials);
    }

    public Set<Material> getGroupMaterials(String id) {
        return groups.getOrDefault(id.toLowerCase(), Collections.emptySet());
    }
}

