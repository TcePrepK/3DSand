package game;

public enum ChunkGenerationSpeed {
    SLOW_SLOW_2,
    MEDIUM_SLOW_4,
    FAST_SLOW_8,
    SLOW_MEDIUM_16,
    MEDIUM_MEDIUM_32,
    FAST_MEDIUM_64,
    SLOW_FAST_128,
    MEDIUM_FAST_256,
    FAST_FAST_512;

    public static int enumToSpeed(final String value) {
        switch (value) {
            case "SLOW_SLOW_2":
                return 2;
            case "MEDIUM_SLOW_4":
                return 4;
            case "FAST_SLOW_8":
                return 8;
            case "SLOW_MEDIUM_16":
                return 16;
            case "MEDIUM_MEDIUM_32":
                return 32;
            case "FAST_MEDIUM_64":
                return 64;
            case "SLOW_FAST_128":
                return 128;
            case "MEDIUM_FAST_256":
                return 256;
            case "FAST_FAST_512":
                return 512;
            default:
                return 0;
        }
    }

    public static String[] valueNames() {
        final ChunkGenerationSpeed[] values = ChunkGenerationSpeed.values();
        final String[] valueNames = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            valueNames[i] = values[i].name();
        }

        return valueNames;
    }
}
