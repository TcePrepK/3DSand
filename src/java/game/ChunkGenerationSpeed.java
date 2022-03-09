package game;

public enum ChunkGenerationSpeed {
    SLOW_1,
    MEDIUM_4,
    FAST_8,
    SUPER_FAST_16,
    ENOUGH_TO_NOT_CRASH_32;

    public static int enumToSpeed(final String value) {
        final char[] chars = value.toCharArray();
        final char lastChar = chars[chars.length - 1];

        try {
            return Integer.parseInt(String.valueOf(lastChar));
        } catch (final NumberFormatException ex) {
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
