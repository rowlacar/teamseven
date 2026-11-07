package hichan.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hichan.common.Location;
import hichan.common.Segment;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MileMarkerEstimator} (FN6.65). */
class MileMarkerEstimatorTest {

  /** Meters in 0.001 degree of latitude on a 6,371 km sphere. */
  private static final double METERS_PER_MILLIDEGREE_LAT = 111.19;

  private final MileMarkerEstimator estimator = new MileMarkerEstimator();

  /** A west-to-east line along latitude 47.7, mile 10 to 20, with evenly spaced points. */
  private static final Segment STRAIGHT = segment(1, "WA", 10, 20,
      new double[] {47.7, -117.0}, new double[] {47.7, -116.95}, new double[] {47.7, -116.9});

  private static Segment segment(int id, String state, double begMp, double endMp,
      double[]... points) {
    Segment segment = new Segment(id, state.equals("WA") ? 53 : 16, begMp, endMp, List.of(points));
    segment.setState(state);
    return segment;
  }

  private Location estimate(double lat, double lon, Segment... candidates) {
    return estimator.estimate(lat, lon, List.of(candidates));
  }

  @Test
  void testFN6_65_PointOnStartVertexGivesBegMp() {
    Location loc = estimate(47.7, -117.0, STRAIGHT);
    assertEquals(10.0, loc.getMileMarker(), 1e-6);
    assertEquals(0.0, loc.getDistanceMeters(), 0.01);
  }

  @Test
  void testFN6_65_PointOnEndVertexGivesEndMp() {
    assertEquals(20.0, estimate(47.7, -116.9, STRAIGHT).getMileMarker(), 1e-6);
  }

  @Test
  void testFN6_65_MidpointInterpolates() {
    assertEquals(15.0, estimate(47.7, -116.95, STRAIGHT).getMileMarker(), 1e-6);
    assertEquals(17.5, estimate(47.7, -116.925, STRAIGHT).getMileMarker(), 1e-6);
  }

  @Test
  void testFN6_65_InterpolatesByLengthNotByPointCount() {
    // Uneven spacing: the middle point is only 10% of the way along the line.
    Segment uneven = segment(2, "WA", 10, 20,
        new double[] {47.7, -117.0}, new double[] {47.7, -116.99}, new double[] {47.7, -116.9});
    assertEquals(11.0, estimate(47.7, -116.99, uneven).getMileMarker(), 1e-6);
  }

  @Test
  void testFN6_65_FollowsBendsInTheLine() {
    // East 0.01 degree of longitude (~750 m), then north 0.01 degree of latitude (~1112 m).
    Segment bent = segment(3, "ID", 0, 10,
        new double[] {47.7, -117.0}, new double[] {47.7, -116.99}, new double[] {47.71, -116.99});
    double east = 0.01 * Math.cos(Math.toRadians(47.7)) * METERS_PER_MILLIDEGREE_LAT * 1000;
    double north = 0.01 * METERS_PER_MILLIDEGREE_LAT * 1000;
    double expected = 10 * (east + north / 2) / (east + north);
    assertEquals(expected, estimate(47.705, -116.99, bent).getMileMarker(), 0.01);
  }

  @Test
  void testFN6_65_DistanceIsPerpendicularToTheLine() {
    Location loc = estimate(47.701, -116.95, STRAIGHT);
    assertEquals(15.0, loc.getMileMarker(), 0.01);
    assertEquals(METERS_PER_MILLIDEGREE_LAT, loc.getDistanceMeters(), 0.5);
  }

  @Test
  void testFN6_65_PointBeforeStartClampsToBegMp() {
    Location loc = estimate(47.7, -117.01, STRAIGHT);
    assertEquals(10.0, loc.getMileMarker(), 1e-6);
    assertEquals(750, loc.getDistanceMeters(), 5);
  }

  @Test
  void testFN6_65_PointPastEndClampsToEndMp() {
    assertEquals(20.0, estimate(47.7, -116.89, STRAIGHT).getMileMarker(), 1e-6);
  }

  @Test
  void testFN6_65_PicksNearestOfTwoSegments() {
    // A parallel segment about 1.1 km north of STRAIGHT.
    Segment north = segment(2, "WA", 30, 40,
        new double[] {47.71, -117.0}, new double[] {47.71, -116.9});
    assertEquals(1, estimate(47.701, -116.95, STRAIGHT, north).getSegmentId());
    assertEquals(2, estimate(47.709, -116.95, STRAIGHT, north).getSegmentId());
    assertEquals(35.0, estimate(47.709, -116.95, STRAIGHT, north).getMileMarker(), 0.01);
  }

  @Test
  void testFN6_65_TieGoesToLowerSegmentId() {
    Segment five = segment(5, "WA", 0, 10, new double[] {47.7, -117.0}, new double[] {47.7, -116.9});
    Segment three = segment(3, "WA", 0, 10, new double[] {47.7, -117.0}, new double[] {47.7, -116.9});
    assertEquals(3, estimate(47.7, -116.95, five, three).getSegmentId());
  }

  @Test
  void testFN6_65_LocationCarriesSegmentStateAndId() {
    Segment idaho = segment(9, "ID", 0, 6.825,
        new double[] {47.6966, -117.0417}, new double[] {47.7121, -116.9029});
    Location loc = estimate(47.6966, -117.0417, idaho);
    assertEquals("ID", loc.getState());
    assertEquals(9, loc.getSegmentId());
  }

  @Test
  void testFN6_65_EmptyCandidatesThrows() {
    assertThrows(IllegalArgumentException.class, () -> estimator.estimate(47.7, -117.0, List.of()));
    assertThrows(IllegalArgumentException.class, () -> estimator.estimate(47.7, -117.0, null));
  }
}
