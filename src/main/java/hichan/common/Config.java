package hichan.common;

import java.util.Map;

/**
 * Shared constants for the Preprocessor and Channel Mapper (Detailed Design Section 3.13).
 *
 * <p>All tunable values live here so neither program hard-codes them (NF7.17).
 */
public final class Config {

  /** Highway prefix for Channel IDs and the SIGN1 value kept from the NHPN data. */
  public static final String HIGHWAY = "I90";

  /** State FIPS code to two-letter state code, for the states Hi_Chan covers. */
  public static final Map<Integer, String> STATE_CODES = Map.of(53, "WA", 16, "ID");

  /** Width of one channel in miles (FN6.7). */
  public static final int CHANNEL_WIDTH = 10;

  /** Number of consecutive mile markers used to decide direction (FN6.8). */
  public static final int DIRECTION_WINDOW_SIZE = 3;

  /** Geohash length used for lookup file keys (about 820 m x 610 m cells at I-90's latitude). */
  public static final int GEOHASH_PRECISION = 6;

  /** Spacing in meters for sampling segments during indexing (Section 6.3). */
  public static final double SAMPLE_INTERVAL_METERS = 200.0;

  /** A GPS point farther than this from every segment is not on I-90. */
  public static final double MAX_MATCH_DISTANCE_METERS = 500.0;

  /** Mean Earth radius used by distance calculations. */
  public static final double EARTH_RADIUS_METERS = 6_371_000.0;

  /** Label written in place of a Channel ID for a rejected log line. */
  public static final String INVALID_LABEL = "INVALID";

  private Config() {}
}
