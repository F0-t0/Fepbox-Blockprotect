package pl.fepbox.blockprotect.rule;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class CuboidScope implements RuleScope {

    private final String worldName;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public CuboidScope(String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldName = worldName.toLowerCase(Locale.ROOT);
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    @Override
    public ScopeType getType() {
        return ScopeType.CUBOID;
    }

    @Override
    public boolean contains(BlockContext context) {
        String ctxWorld = context.getWorld().getName().toLowerCase(Locale.ROOT);
        if (!ctxWorld.equals(worldName)) {
            return false;
        }
        int x = context.getX();
        int y = context.getY();
        int z = context.getZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
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

