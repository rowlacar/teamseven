package hichan.preprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hichan.common.Config;
import hichan.common.GeoUtils;
import hichan.common.Segment;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests for GeohashIndexer (FN6.2). */
class GeohashIndexerTest {

  private final GeohashIndexer indexer = new GeohashIndexer();

  /** A straight line about 7.5 km long with only two points, like a long NHPN piece. */
  private Segment longStraightSegment() {
    Segment segment = new Segment(1, 53, 0, 4.7,
        List.of(new double[] {47.60, -117.60}, new double[] {47.65, -117.52}));
    segment.setState("WA");
    return segment;
  }

  @Test
  void testFN6_2_NoCellSkippedOnLongLine() {
    Map<String, List<Segment>> index = indexer.buildIndex(List.of(longStraightSegment()));
    // Walk the line in 20 m steps; every cell the line touches must be indexed
    for (int i = 0; i <= 375; i++) {
      double t = i / 375.0;
      String cell = GeoUtils.encodeGeohash(
          47.60 + 0.05 * t, -117.60 + 0.08 * t, Config.GEOHASH_PRECISION);
      assertTrue(index.containsKey(cell), "missing cell " + cell);
    }
  }

  @Test
  void testFN6_2_SegmentListedOncePerCell() {
    Map<String, List<Segment>> index = indexer.buildIndex(List.of(longStraightSegment()));
    for (List<Segment> segments : index.values()) {
      assertEquals(1, segments.size());
    }
  }

  @Test
  void testFN6_2_EmptyListGivesEmptyIndex() {
    assertTrue(indexer.buildIndex(List.of()).isEmpty());
  }
}
