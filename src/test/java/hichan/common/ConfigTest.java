package hichan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Config}: values must match Detailed Design Section 3.13. */
class ConfigTest {

  @Test
  void testFN6_1_HighwayIsI90() {
    assertEquals("I90", Config.HIGHWAY);
  }

  @Test
  void testFN6_3_StateCodesMapFipsToAbbreviation() {
    assertEquals(2, Config.STATE_CODES.size());
    assertEquals("WA", Config.STATE_CODES.get(53));
    assertEquals("ID", Config.STATE_CODES.get(16));
  }

  @Test
  void testFN6_3_StateCodesCannotBeModified() {
    assertThrows(UnsupportedOperationException.class, () -> Config.STATE_CODES.put(30, "MT"));
  }

  @Test
  void testFN6_7_ChannelWidthIsTenMiles() {
    assertEquals(10, Config.CHANNEL_WIDTH);
  }

  @Test
  void testFN6_8_DirectionWindowIsThreeMarkers() {
    assertEquals(3, Config.DIRECTION_WINDOW_SIZE);
  }

  @Test
  void testFN6_2_GeohashSettings() {
    assertEquals(6, Config.GEOHASH_PRECISION);
    assertEquals(200.0, Config.SAMPLE_INTERVAL_METERS);
  }

  @Test
  void testFN6_66_MatchDistanceAndInvalidLabel() {
    assertEquals(500.0, Config.MAX_MATCH_DISTANCE_METERS);
    assertEquals("INVALID", Config.INVALID_LABEL);
  }

  @Test
  void testFN6_6_EarthRadius() {
    assertEquals(6_371_000.0, Config.EARTH_RADIUS_METERS);
  }
}
