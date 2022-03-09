package game;

public enum ChunkGenerationSpeed {
    SLOW_1,
    MEDIUM_4,
    FAST_8,
    SUPER_FAST_16,
    ENOUGH_TO_NOT_CRASH_32;

    public static int enumToSpeed(final String value) {
        switch (value) {
            case "SLOW_1":
                return 1;
            case "MEDIUM_4":
                return 4;
            case "FAST_8":
                return 8;
            case "SUPER_FAST_16":
                return 16;
            case "ENOUGH_TO_NOT_CRASH_32":
                return 32;
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
