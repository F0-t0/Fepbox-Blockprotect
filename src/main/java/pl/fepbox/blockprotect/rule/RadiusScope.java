package pl.fepbox.blockprotect.rule;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class RadiusScope implements RuleScope {

    private final String worldName;
    private final int centerX;
    private final int centerY;
    private final int centerZ;
    private final int radiusSquared;

    public RadiusScope(String worldName, int centerX, int centerY, int centerZ, int radius) {
        this.worldName = worldName.toLowerCase(Locale.ROOT);
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radiusSquared = radius * radius;
    }

    @Override
    public ScopeType getType() {
        return ScopeType.RADIUS;
    }

    @Override
    public boolean contains(BlockContext context) {
        String ctxWorld = context.getWorld().getName().toLowerCase(Locale.ROOT);
        if (!ctxWorld.equals(worldName)) {
            return false;
        }
        int dx = context.getX() - centerX;
        int dy = context.getY() - centerY;
        int dz = context.getZ() - centerZ;
        int distSq = dx * dx + dy * dy + dz * dz;
        return distSq <= radiusSquared;
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

