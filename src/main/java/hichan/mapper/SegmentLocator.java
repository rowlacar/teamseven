package hichan.mapper;

import hichan.common.Config;
import hichan.common.GeoUtils;
import hichan.common.GpsEntry;
import hichan.common.Location;
import hichan.common.Segment;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Finds the I-90 segment and mile marker for a GPS point (FN6.6, FN6.66, Detailed Design 3.9).
 *
 * <p>Only the point's geohash cell and its 8 neighbors are searched (NF7.1, NF7.2). A precision-6
 * cell is about 820 m by 610 m at I-90's latitude, larger than {@link
 * Config#MAX_MATCH_DISTANCE_METERS}, so any segment close enough to match is in one of the 9 cells.
 */
public class SegmentLocator {

  private final LookupIndex index;
  private final MileMarkerEstimator estimator = new MileMarkerEstimator();

  /**
   * Creates a locator over a loaded index.
   *
   * @param index the lookup index
   */
  public SegmentLocator(LookupIndex index) {
    this.index = Objects.requireNonNull(index, "index");
  }

  /**
   * Locates a valid GPS entry on I-90.
   *
   * @param entry a GPS entry with {@code isValid()} true
   * @return the matched segment's state and the estimated mile marker
   * @throws SegmentNotFoundException if no segment is within {@link
   *     Config#MAX_MATCH_DISTANCE_METERS}; the caller adds the file and line to the message
   */
  public Location locate(GpsEntry entry) throws SegmentNotFoundException {
    double lat = entry.getLatitude();
    double lon = entry.getLongitude();
    List<Segment> candidates = findCandidates(lat, lon);
    if (candidates.isEmpty()) {
      throw new SegmentNotFoundException("no I-90 segment nearby");
    }
    Location location = estimator.estimate(lat, lon, candidates);
    if (location.getDistanceMeters() > Config.MAX_MATCH_DISTANCE_METERS) {
      throw new SegmentNotFoundException(String.format(
          "no I-90 segment nearby (closest is %.0f m away)", location.getDistanceMeters()));
    }
    return location;
  }

  /**
   * CandidateSearch: returns the segments in the point's cell and its 8 neighbors, each once.
   *
   * @param lat latitude in decimal degrees
   * @param lon longitude in decimal degrees
   * @return candidate segments in a stable order; empty if none
   */
  List<Segment> findCandidates(double lat, double lon) {
    String cell = GeoUtils.encodeGeohash(lat, lon, Config.GEOHASH_PRECISION);
    List<String> searchCells = new ArrayList<>(9);
    searchCells.add(cell);
    searchCells.addAll(GeoUtils.neighbors(cell));

    Map<Integer, Segment> found = new LinkedHashMap<>();
    for (String searchCell : searchCells) {
      for (Segment segment : index.segmentsIn(searchCell)) {
        found.putIfAbsent(segment.getId(), segment);
      }
    }
    return new ArrayList<>(found.values());
  }
}
