package hichan.common;

import java.util.Map;

/**
 * Shared constants for the Preprocessor and Channel Mapper (Detailed Design 3.13).
 */
public final class Config {

    public static final String HIGHWAY = "I90";
    public static final Map<Integer, String> STATE_CODES = Map.of(53, "WA", 16, "ID");
    public static final int CHANNEL_WIDTH = 10;
    public static final int DIRECTION_WINDOW_SIZE = 3;
    public static final int GEOHASH_PRECISION = 6;
    public static final double SAMPLE_INTERVAL_METERS = 200.0;
    public static final double MAX_MATCH_DISTANCE_METERS = 500.0;
    public static final double EARTH_RADIUS_METERS = 6_371_000.0;
    public static final String INVALID_LABEL = "INVALID";

    private Config() {
        // Constants only; not instantiable.
    }
}