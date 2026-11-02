package hichan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Location}. */
class LocationTest {

  @Test
  void testFN6_65_GettersReturnConstructorValues() {
    Location loc = new Location("WA", 17, 285.123, 12.5);
    assertEquals("WA", loc.getState());
    assertEquals(17, loc.getSegmentId());
    assertEquals(285.123, loc.getMileMarker());
    assertEquals(12.5, loc.getDistanceMeters());
  }

  @Test
  void testFN6_65_ToStringShowsKeyFields() {
    String text = new Location("ID", 3, 4.5, 0.25).toString();
    assertTrue(text.contains("ID"), text);
    assertTrue(text.contains("seg 3"), text);
    assertTrue(text.contains("4.500"), text);
  }
}
