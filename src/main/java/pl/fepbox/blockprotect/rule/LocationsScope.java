package pl.fepbox.blockprotect.rule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LocationsScope implements RuleScope {

    private final String worldName;
    private final Set<Long> encodedLocations;

    public LocationsScope(String worldName, Set<Long> encodedLocations) {
        this.worldName = worldName.toLowerCase(Locale.ROOT);
        this.encodedLocations = Collections.unmodifiableSet(new HashSet<>(encodedLocations));
    }

    @Override
    public ScopeType getType() {
        return ScopeType.LOCATIONS;
    }

    @Override
    public boolean contains(BlockContext context) {
        String ctxWorld = context.getWorld().getName().toLowerCase(Locale.ROOT);
        if (!ctxWorld.equals(worldName)) {
            return false;
        }
        long key = BlockPositionEncoding.encode(context.getX(), context.getY(), context.getZ());
        return encodedLocations.contains(key);
    }

    @Override
    public Set<String> getWorldNamesHint() {
        return Collections.singleton(worldName);
    }

    @Override
    public boolean isGlobal() {
        return false;
    }
}

