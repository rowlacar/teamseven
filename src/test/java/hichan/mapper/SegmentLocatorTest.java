package hichan.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hichan.common.Config;
import hichan.common.GeoUtils;
import hichan.common.GpsEntry;
import hichan.common.Location;
import hichan.common.Segment;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SegmentLocator} (FN6.6, FN6.66, NF7.1, NF7.2). */
class SegmentLocatorTest {

  /** The Spokane cell from Detailed Design 4.2, and its bounds {minLat, minLon, maxLat, maxLon}. */
  private static final String CELL = "c2krpu";
  private static final double[] BOUNDS =
      {47.65869140625, -117.432861328125, 47.6641845703125, -117.421875};
  private static final double CENTER_LAT = (BOUNDS[0] + BOUNDS[2]) / 2;
  private static final double CENTER_LON = (BOUNDS[1] + BOUNDS[3]) / 2;

  /** A west-to-east line across the middle of CELL, mile 100 to 101, indexed only in CELL. */
  private static final Segment IN_CELL = segment(7, 100, 101,
      new double[] {CENTER_LAT, BOUNDS[1] + 1e-5}, new double[] {CENTER_LAT, BOUNDS[3] - 1e-5});

  private static Segment segment(int id, double begMp, double endMp, double[]... points) {
    Segment segment = new Segment(id, 53, begMp, endMp, List.of(points));
    segment.setState("WA");
    return segment;
  }

  /** A valid GPS entry at a point; only latitude and longitude matter to SegmentLocator. */
  private static GpsEntry at(double lat, double lon) {
    return new GpsEntry(1, lat + "," + lon, lat, lon, 0, "", "", 0, 0, true);
  }

  private static SegmentLocator locatorWith(Map<String, List<Segment>> cells) {
    return new SegmentLocator(new LookupIndex(cells));
  }

  @Test
  void testNF7_2_TestCellBoundsMatchGeoUtils() {
    assertEquals(CELL, GeoUtils.encodeGeohash(CENTER_LAT, CENTER_LON, Config.GEOHASH_PRECISION));
    assertEquals(GeoUtils.neighbors(CELL).get(0),
        GeoUtils.encodeGeohash(BOUNDS[2] + 1e-6, CENTER_LON, Config.GEOHASH_PRECISION));
    assertEquals(GeoUtils.neighbors(CELL).get(2),
        GeoUtils.encodeGeohash(CENTER_LAT, BOUNDS[3] + 1e-6, Config.GEOHASH_PRECISION));
  }

  @Test
  void testFN6_6_LocatesPointOnSegment() throws SegmentNotFoundException {
    SegmentLocator locator = locatorWith(Map.of(CELL, List.of(IN_CELL)));
    Location loc = locator.locate(at(CENTER_LAT, CENTER_LON));
    assertEquals(7, loc.getSegmentId());
    assertEquals("WA", loc.getState());
    assertEquals(100.5, loc.getMileMarker(), 0.01);
    assertTrue(loc.getDistanceMeters() < 1, loc.toString());
  }

  @Test
  void testNF7_2_FindsSegmentFromNeighborCell() throws SegmentNotFoundException {
    // ~55 m north of CELL's top edge, so the point's own cell is the N neighbor.
    double lat = BOUNDS[2] + 0.0005;
    assertNotEquals(CELL, GeoUtils.encodeGeohash(lat, CENTER_LON, Config.GEOHASH_PRECISION));

    SegmentLocator locator = locatorWith(Map.of(CELL, List.of(IN_CELL)));
    assertEquals(1, locator.findCandidates(lat, CENTER_LON).size());
    assertEquals(7, locator.locate(at(lat, CENTER_LON)).getSegmentId());
  }

  @Test
  void testNF7_2_SearchesAllEightNeighbors() {
    List<String> neighbors = GeoUtils.neighbors(CELL);
    Segment[] segments = new Segment[neighbors.size()];
    Map<String, List<Segment>> cells = new java.util.HashMap<>();
    for (int i = 0; i < neighbors.size(); i++) {
      segments[i] = segment(i + 1, 0, 1, new double[] {0, 0}, new double[] {0, 1});
      cells.put(neighbors.get(i), List.of(segments[i]));
    }
    cells.put("zzzzzz", List.of(segment(99, 0, 1, new double[] {0, 0}, new double[] {0, 1})));

    List<Segment> found = locatorWith(cells).findCandidates(CENTER_LAT, CENTER_LON);
    assertEquals(8, found.size());
    assertTrue(found.stream().noneMatch(s -> s.getId() == 99));
  }

  @Test
  void testNF7_1_RemovesDuplicateCandidates() {
    String north = GeoUtils.neighbors(CELL).get(0);
    SegmentLocator locator = locatorWith(Map.of(CELL, List.of(IN_CELL), north, List.of(IN_CELL)));
    assertEquals(1, locator.findCandidates(CENTER_LAT, CENTER_LON).size());
  }

  @Test
  void testFN6_66_NoCandidatesThrows() {
    SegmentLocator locator = locatorWith(Map.of(CELL, List.of(IN_CELL)));
    SegmentNotFoundException e = assertThrows(SegmentNotFoundException.class,
        () -> locator.locate(at(0.0, 0.0)));
    assertTrue(e.getMessage().contains("no I-90 segment nearby"), e.getMessage());
  }

  @Test
  void testFN6_66_FartherThanMaxDistanceThrows() {
    // In the N neighbor cell but ~630 m from the segment.
    double lat = BOUNDS[2] + 0.003;
    SegmentLocator locator = locatorWith(Map.of(CELL, List.of(IN_CELL)));
    assertEquals(1, locator.findCandidates(lat, CENTER_LON).size());
    SegmentNotFoundException e = assertThrows(SegmentNotFoundException.class,
        () -> locator.locate(at(lat, CENTER_LON)));
    assertTrue(e.getMessage().contains("m away"), e.getMessage());
  }

  @Test
  void testFN6_66_JustInsideMaxDistanceMatches() throws SegmentNotFoundException {
    // 0.0044 degree of latitude is about 489 m.
    double lat = CENTER_LAT + 0.0044;
    Location loc = locatorWith(Map.of(CELL, List.of(IN_CELL))).locate(at(lat, CENTER_LON));
    assertTrue(loc.getDistanceMeters() < Config.MAX_MATCH_DISTANCE_METERS, loc.toString());
  }

  @Test
  void testFN6_6_PicksNearestOfOverlappingSegments() throws SegmentNotFoundException {
    Segment nearby = segment(8, 200, 201,
        new double[] {CENTER_LAT + 0.002, BOUNDS[1]}, new double[] {CENTER_LAT + 0.002, BOUNDS[3]});
    SegmentLocator locator = locatorWith(Map.of(CELL, List.of(IN_CELL, nearby)));
    assertEquals(8, locator.locate(at(CENTER_LAT + 0.0018, CENTER_LON)).getSegmentId());
    assertEquals(7, locator.locate(at(CENTER_LAT + 0.0002, CENTER_LON)).getSegmentId());
  }

  @Test
  void testFN6_6_NullIndexRejected() {
    assertThrows(NullPointerException.class, () -> new SegmentLocator(null));
  }
}
