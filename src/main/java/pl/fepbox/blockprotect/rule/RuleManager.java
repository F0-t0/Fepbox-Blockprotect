package pl.fepbox.blockprotect.rule;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.fepbox.blockprotect.FepboxBlockProtectPlugin;

import java.util.*;

public class RuleManager {

    private final FepboxBlockProtectPlugin plugin;
    private final BlockGroupRegistry groupRegistry;

    private final Map<String, BlockProtectRule> rulesById = new HashMap<>();
    private final List<BlockProtectRule> allRules = new ArrayList<>();
    private final List<BlockProtectRule> globalRules = new ArrayList<>();
    private final Map<String, List<BlockProtectRule>> rulesByWorld = new HashMap<>();

    public RuleManager(FepboxBlockProtectPlugin plugin, BlockGroupRegistry groupRegistry) {
        this.plugin = plugin;
        this.groupRegistry = groupRegistry;
    }

    public void reloadFromConfig(FileConfiguration config) {
        rulesById.clear();
        allRules.clear();
        globalRules.clear();
        rulesByWorld.clear();
        groupRegistry.clear();

        ConfigurationSection groupsSection = config.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String groupId : groupsSection.getKeys(false)) {
                List<String> materials = groupsSection.getStringList(groupId);
                groupRegistry.registerGroup(groupId, materials);
            }
        }

        ConfigurationSection rulesSection = config.getConfigurationSection("rules");
        if (rulesSection == null) {
            return;
        }

        for (String id : rulesSection.getKeys(false)) {
            ConfigurationSection ruleSection = rulesSection.getConfigurationSection(id);
            if (ruleSection == null) {
                continue;
            }

            RuleScope scope = parseScope(ruleSection.getConfigurationSection("scope"));
            if (scope == null) {
                continue;
            }

            Set<Material> materials = parseMaterials(ruleSection.getStringList("blocks"));
            ProtectionFlags flags = parseFlags(ruleSection.getConfigurationSection("flags"));

            ConfigurationSection bypassSection = ruleSection.getConfigurationSection("bypass");
            RuleBypass bypass;
            if (bypassSection != null) {
                List<String> permissions = bypassSection.getStringList("permissions");
                List<String> gamemodes = bypassSection.getStringList("gamemodes");
                List<String> players = bypassSection.getStringList("players");
                List<String> tools = bypassSection.getStringList("tools");
                bypass = RuleBypass.fromConfig(permissions, gamemodes, players, tools);
            } else {
                bypass = RuleBypass.empty();
            }

            BlockProtectRule rule = new BlockProtectRule(id, scope, materials, flags, bypass);
            registerRule(rule);
        }
    }

    private RuleScope parseScope(ConfigurationSection scopeSection) {
        if (scopeSection == null) {
            return null;
        }

        String typeStr = scopeSection.getString("type", "GLOBAL").toUpperCase(Locale.ROOT);
        ScopeType type;
        try {
            type = ScopeType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return null;
        }

        switch (type) {
            case GLOBAL -> {
                return new GlobalScope();
            }
            case WORLD -> {
                Set<String> worlds = new HashSet<>();
                String singleWorld = scopeSection.getString("world");
                if (singleWorld != null) {
                    worlds.add(singleWorld);
                }
                List<String> worldList = scopeSection.getStringList("worlds");
                if (worldList != null) {
                    worlds.addAll(worldList);
                }
                if (worlds.isEmpty()) {
                    return null;
                }
                return new WorldScope(worlds);
            }
            case CUBOID -> {
                String worldName = scopeSection.getString("world");
                List<Integer> pos1 = scopeSection.getIntegerList("pos1");
                List<Integer> pos2 = scopeSection.getIntegerList("pos2");
                if (worldName == null || pos1.size() != 3 || pos2.size() != 3) {
                    return null;
                }
                return new CuboidScope(
                        worldName,
                        pos1.get(0), pos1.get(1), pos1.get(2),
                        pos2.get(0), pos2.get(1), pos2.get(2)
                );
            }
            case RADIUS -> {
                String worldName = scopeSection.getString("world");
                List<Integer> center = scopeSection.getIntegerList("center");
                int radius = scopeSection.getInt("radius", -1);
                if (worldName == null || center.size() != 3 || radius <= 0) {
                    return null;
                }
                return new RadiusScope(worldName, center.get(0), center.get(1), center.get(2), radius);
            }
            case LOCATIONS -> {
                String worldName = scopeSection.getString("world");
                List<?> rawList = scopeSection.getList("locations");
                if (worldName == null || rawList == null) {
                    return null;
                }
                Set<Long> encoded = new HashSet<>();
                for (Object obj : rawList) {
                    if (obj instanceof List<?> list && list.size() == 3) {
                        try {
                            int x = Integer.parseInt(String.valueOf(list.get(0)));
                            int y = Integer.parseInt(String.valueOf(list.get(1)));
                            int z = Integer.parseInt(String.valueOf(list.get(2)));
                            encoded.add(BlockPositionEncoding.encode(x, y, z));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                if (encoded.isEmpty()) {
                    return null;
                }
                return new LocationsScope(worldName, encoded);
            }
            default -> {
                return null;
            }
        }
    }

    private ProtectionFlags parseFlags(ConfigurationSection flagsSection) {
        if (flagsSection == null) {
            return new ProtectionFlags(true, true, true, true, true);
        }
        boolean breakFlag = flagsSection.getBoolean("break", true);
        boolean explosionFlag = flagsSection.getBoolean("explosion", true);
        boolean pistonFlag = flagsSection.getBoolean("piston", true);
        boolean fireFlag = flagsSection.getBoolean("fire", true);
        boolean fluidsFlag = flagsSection.getBoolean("fluids", true);
        return new ProtectionFlags(breakFlag, explosionFlag, pistonFlag, fireFlag, fluidsFlag);
    }

    private Set<Material> parseMaterials(List<String> blockDefs) {
        Set<Material> materials = new HashSet<>();
        if (blockDefs == null) {
            return materials;
        }
        for (String def : blockDefs) {
            if (def == null || def.isEmpty()) {
                continue;
            }
            if (def.startsWith("#")) {
                String groupId = def.substring(1);
                materials.addAll(groupRegistry.getGroupMaterials(groupId));
            } else {
                try {
                    materials.add(Material.valueOf(def.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return materials;
    }

    private void registerRule(BlockProtectRule rule) {
        rulesById.put(rule.getId(), rule);
        allRules.add(rule);
        if (rule.getScope().isGlobal()) {
            globalRules.add(rule);
        }
        Set<String> worlds = rule.getScope().getWorldNamesHint();
        if (worlds != null) {
            for (String world : worlds) {
                rulesByWorld.computeIfAbsent(world.toLowerCase(Locale.ROOT), k -> new ArrayList<>())
                        .add(rule);
            }
        }
    }

    public Collection<BlockProtectRule> getAllRules() {
        return Collections.unmodifiableList(allRules);
    }

    public BlockProtectRule getRule(String id) {
        return rulesById.get(id);
    }

    private Collection<BlockProtectRule> getCandidateRules(BlockContext context) {
        List<BlockProtectRule> candidates = new ArrayList<>(globalRules);
        String worldName = context.getWorld().getName().toLowerCase(Locale.ROOT);
        List<BlockProtectRule> perWorld = rulesByWorld.get(worldName);
        if (perWorld != null) {
            candidates.addAll(perWorld);
        }
        return candidates;
    }

    public boolean isProtected(BlockContext context,
                               ProtectionType type,
                               Player player,
                               ItemStack tool,
                               boolean applyBypass) {
        Collection<BlockProtectRule> candidates = getCandidateRules(context);
        if (candidates.isEmpty()) {
            return false;
        }
        boolean protectedByAny = false;
        for (BlockProtectRule rule : candidates) {
            if (!rule.matches(context)) {
                continue;
            }
            if (!rule.isProtectionEnabled(type)) {
                continue;
            }
            if (applyBypass && player != null && rule.isBypassed(player, tool)) {
                continue;
            }
            protectedByAny = true;
            break;
        }
        return protectedByAny;
    }

    public void createCuboidRule(String id,
                                 String worldName,
                                 int x1,
                                 int y1,
                                 int z1,
                                 int x2,
                                 int y2,
                                 int z2) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection rulesSection = config.getConfigurationSection("rules");
        if (rulesSection == null) {
            rulesSection = config.createSection("rules");
        }

        ConfigurationSection ruleSection = rulesSection.getConfigurationSection(id);
        if (ruleSection == null) {
            ruleSection = rulesSection.createSection(id);
        }

        ConfigurationSection scopeSection = ruleSection.createSection("scope");
        scopeSection.set("type", "CUBOID");
        scopeSection.set("world", worldName);
        scopeSection.set("pos1", Arrays.asList(x1, y1, z1));
        scopeSection.set("pos2", Arrays.asList(x2, y2, z2));

        ConfigurationSection flagsSection = ruleSection.createSection("flags");
        flagsSection.set("break", true);
        flagsSection.set("explosion", true);
        flagsSection.set("piston", true);
        flagsSection.set("fire", true);
        flagsSection.set("fluids", true);

        ruleSection.set("blocks", new ArrayList<String>());

        ConfigurationSection bypassSection = ruleSection.createSection("bypass");
        bypassSection.set("permissions", new ArrayList<String>());
        bypassSection.set("gamemodes", new ArrayList<String>());
        bypassSection.set("players", new ArrayList<String>());
        bypassSection.set("tools", new ArrayList<String>());

        plugin.saveConfig();
        reloadFromConfig(plugin.getConfig());
    }

    public boolean addRuleBlock(String id, String value) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection rulesSection = config.getConfigurationSection("rules");
        if (rulesSection == null) {
            return false;
        }
        ConfigurationSection ruleSection = rulesSection.getConfigurationSection(id);
        if (ruleSection == null) {
            return false;
        }
        List<String> blocks = ruleSection.getStringList("blocks");
        if (!blocks.contains(value)) {
            blocks.add(value);
        }
        ruleSection.set("blocks", blocks);
        plugin.saveConfig();
        reloadFromConfig(plugin.getConfig());
        return true;
    }

    public boolean removeRuleBlock(String id, String value) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection rulesSection = config.getConfigurationSection("rules");
        if (rulesSection == null) {
            return false;
        }
        ConfigurationSection ruleSection = rulesSection.getConfigurationSection(id);
        if (ruleSection == null) {
            return false;
        }
        List<String> blocks = ruleSection.getStringList("blocks");
        if (!blocks.remove(value)) {
            return false;
        }
        ruleSection.set("blocks", blocks);
        plugin.saveConfig();
        reloadFromConfig(plugin.getConfig());
        return true;
    }

    public boolean setFlag(String id, String flagName, boolean value) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection rulesSection = config.getConfigurationSection("rules");
        if (rulesSection == null) {
            return false;
        }
        ConfigurationSection ruleSection = rulesSection.getConfigurationSection(id);
        if (ruleSection == null) {
            return false;
        }
        ConfigurationSection flagsSection = ruleSection.getConfigurationSection("flags");
        if (flagsSection == null) {
            flagsSection = ruleSection.createSection("flags");
        }
        switch (flagName.toLowerCase(Locale.ROOT)) {
            case "break" -> flagsSection.set("break", value);
            case "explosion" -> flagsSection.set("explosion", value);
            case "piston" -> flagsSection.set("piston", value);
            case "fire" -> flagsSection.set("fire", value);
            case "fluids" -> flagsSection.set("fluids", value);
            default -> {
                return false;
            }
        }
        plugin.saveConfig();
        reloadFromConfig(plugin.getConfig());
        return true;
    }

    public boolean deleteRule(String id) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection rulesSection = config.getConfigurationSection("rules");
        if (rulesSection == null) {
            return false;
        }
        if (!rulesSection.isConfigurationSection(id)) {
            return false;
        }
        rulesSection.set(id, null);
        plugin.saveConfig();
        reloadFromConfig(plugin.getConfig());
        return true;
    }
}

