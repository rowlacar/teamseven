package hichan.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hichan.common.Direction;
import hichan.common.Location;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DirectionTracker} (FN6.8, FN6.9, FN6.11). */
class DirectionTrackerTest {

  private final DirectionTracker tracker = new DirectionTracker();

  private Direction feed(String state, double... markers) {
    Direction result = null;
    for (double mm : markers) {
      result = tracker.update(new Location(state, 1, mm, 0.0));
    }
    return result;
  }

  @Test
  void testFN6_8_IncreasingMarkersGiveEB() {
    assertEquals(Direction.EB, feed("WA", 281.1, 281.3, 281.5));
  }

  @Test
  void testFN6_8_DecreasingMarkersGiveWB() {
    assertEquals(Direction.WB, feed("WA", 290.0, 289.8, 289.6));
  }

  @Test
  void testFN6_8_WindowSlidesToNewestMarkers() {
    feed("WA", 100.0, 100.2, 100.4);
    assertEquals(Direction.WB, feed("WA", 100.1, 99.9));
  }

  @Test
  void testFN6_11_UnknownUntilWindowFull() {
    assertEquals(Direction.UNKNOWN, feed("WA", 281.1));
    assertEquals(Direction.UNKNOWN, feed("WA", 281.3));
    assertEquals(Direction.EB, feed("WA", 281.5));
  }

  @Test
  void testFN6_11_InconsistentTrendBeforeEstablishedIsUnknown() {
    assertEquals(Direction.UNKNOWN, feed("WA", 281.1, 281.3, 281.2));
  }

  @Test
  void testFN6_11_EqualMarkersAreNotATrend() {
    assertEquals(Direction.UNKNOWN, feed("WA", 281.1, 281.1, 281.1));
  }

  @Test
  void testFN6_8_InconsistentTrendKeepsDirection() {
    feed("WA", 281.1, 281.3, 281.5);
    assertEquals(Direction.EB, feed("WA", 281.4));
    assertEquals(Direction.EB, tracker.getDirection());
  }

  @Test
  void testFN6_9_StateChangeKeepsDirectionAndResetsWindow() {
    feed("ID", 1.2, 0.8, 0.4);
    // Westbound into WA: markers jump from ~0 to ~299. Without the reset this looks increasing.
    assertEquals(Direction.WB, feed("WA", 299.0));
    assertEquals(Direction.WB, feed("WA", 298.8));
    assertEquals(Direction.WB, feed("WA", 298.6));
  }

  @Test
  void testFN6_9_NewStateWindowCanChangeDirection() {
    feed("ID", 1.2, 0.8, 0.4);
    assertEquals(Direction.WB, feed("WA", 298.6, 298.8));
    assertEquals(Direction.EB, feed("WA", 299.0));
  }

  @Test
  void testFN6_8_ClearReversalSwitchesDirection() {
    feed("WA", 281.1, 281.3, 281.5);
    assertEquals(Direction.EB, feed("WA", 281.4));
    assertEquals(Direction.WB, feed("WA", 281.2));
  }

  @Test
  void testFN6_8_SmallStepsStillCountAsTrend() {
    assertEquals(Direction.EB, feed("WA", 50.000, 50.001, 50.002));
  }

  @Test
  void testFN6_8_EqualMarkerAfterEstablishedKeepsDirection() {
    feed("WA", 290.0, 289.8, 289.6);
    assertEquals(Direction.WB, feed("WA", 289.6));
  }

  @Test
  void testFN6_9_StateChangeBeforeDirectionKnownStaysUnknown() {
    feed("ID", 0.5, 0.3);
    assertEquals(Direction.UNKNOWN, feed("WA", 299.0));
    assertEquals(Direction.UNKNOWN, feed("WA", 298.8));
    assertEquals(Direction.WB, feed("WA", 298.6));
  }

  @Test
  void testFN6_9_EastboundCrossingIntoIdahoKeepsEB() {
    feed("WA", 298.6, 298.8, 299.0);
    // Eastbound into ID: markers drop from ~299 to ~0. Without the reset this looks decreasing.
    assertEquals(Direction.EB, feed("ID", 0.1));
    assertEquals(Direction.EB, feed("ID", 0.3));
    assertEquals(Direction.EB, feed("ID", 0.5));
  }

  @Test
  void testFN6_9_RepeatedCrossingsKeepDirection() {
    feed("WA", 298.6, 298.8, 299.0);
    assertEquals(Direction.EB, feed("ID", 0.1));
    assertEquals(Direction.EB, feed("WA", 299.1));
    assertEquals(Direction.EB, feed("ID", 0.2, 0.1));
  }

  @Test
  void testFN6_8_GetDirectionDoesNotChangeState() {
    feed("WA", 281.1, 281.3);
    assertEquals(Direction.UNKNOWN, tracker.getDirection());
    assertEquals(Direction.UNKNOWN, tracker.getDirection());
    assertEquals(Direction.EB, feed("WA", 281.5));
    assertEquals(Direction.EB, tracker.getDirection());
  }

  @Test
  void testFN6_8_NullLocationLeavesTrackerUnchanged() {
    feed("WA", 281.1, 281.3);
    assertThrows(IllegalArgumentException.class, () -> tracker.update(null));
    assertEquals(Direction.EB, feed("WA", 281.5));
  }

  @Test
  void testFN6_11_GetDirectionStartsUnknown() {
    assertEquals(Direction.UNKNOWN, tracker.getDirection());
  }

  @Test
  void testFN6_8_NullLocationThrows() {
    assertThrows(IllegalArgumentException.class, () -> tracker.update(null));
  }
}
