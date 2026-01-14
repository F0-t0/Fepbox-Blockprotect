package pl.fepbox.blockprotect.rule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class WorldScope implements RuleScope {

    private final Set<String> worlds;

    public WorldScope(Set<String> worlds) {
        Set<String> lower = new HashSet<>();
        for (String w : worlds) {
            lower.add(w.toLowerCase(Locale.ROOT));
        }
        this.worlds = Collections.unmodifiableSet(lower);
    }

    @Override
    public ScopeType getType() {
        return ScopeType.WORLD;
    }

    @Override
    public boolean contains(BlockContext context) {
        String worldName = context.getWorld().getName().toLowerCase(Locale.ROOT);
        return worlds.contains(worldName);
    }

    @Override
    public Set<String> getWorldNamesHint() {
        return worlds;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }
}

