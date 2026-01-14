package pl.fepbox.blockprotect.rule;

public final class BlockPositionEncoding {

    private BlockPositionEncoding() {
    }

    public static long encode(int x, int y, int z) {
        long lx = ((long) x & 0x3FFFFFFL) << 38;
        long ly = ((long) y & 0xFFFL) << 26;
        long lz = (long) z & 0x3FFFFFFL;
        return lx | ly | lz;
    }
}

