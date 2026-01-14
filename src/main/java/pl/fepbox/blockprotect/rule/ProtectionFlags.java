package pl.fepbox.blockprotect.rule;

public class ProtectionFlags {

    private final boolean breakFlag;
    private final boolean explosionFlag;
    private final boolean pistonFlag;
    private final boolean fireFlag;
    private final boolean fluidsFlag;

    public ProtectionFlags(boolean breakFlag,
                           boolean explosionFlag,
                           boolean pistonFlag,
                           boolean fireFlag,
                           boolean fluidsFlag) {
        this.breakFlag = breakFlag;
        this.explosionFlag = explosionFlag;
        this.pistonFlag = pistonFlag;
        this.fireFlag = fireFlag;
        this.fluidsFlag = fluidsFlag;
    }

    public boolean isEnabled(ProtectionType type) {
        return switch (type) {
            case BREAK -> breakFlag;
            case EXPLOSION -> explosionFlag;
            case PISTON -> pistonFlag;
            case FIRE -> fireFlag;
            case FLUIDS -> fluidsFlag;
        };
    }

    public boolean isBreakFlag() {
        return breakFlag;
    }

    public boolean isExplosionFlag() {
        return explosionFlag;
    }

    public boolean isPistonFlag() {
        return pistonFlag;
    }

    public boolean isFireFlag() {
        return fireFlag;
    }

    public boolean isFluidsFlag() {
        return fluidsFlag;
    }
}

