package hichan.mapper;

import hichan.common.GeoUtils;
import hichan.common.Location;
import hichan.common.Segment;
import java.util.List;

/**
 * Picks the candidate segment closest to a GPS point and estimates the mile marker there
 * (FN6.65, Detailed Design 3.9 and 6.4).
 *
 * <p>The point is projected onto each segment's line. The mile marker is the segment's begMp
 * plus the fraction of the line's length before the projected point, scaled to the segment's
 * mile range. Scaling by length keeps the result between begMp and endMp even though NHPN's
 * mile range and the drawn line's length never match exactly.
 */
public class MileMarkerEstimator {

  /**
   * Returns the location on the nearest candidate segment.
   *
   * @param lat GPS latitude in decimal degrees
   * @param lon GPS longitude in decimal degrees
   * @param candidates segments to consider; points must run from begMp to endMp
   * @return location on the closest segment; ties go to the lower segment id
   * @throws IllegalArgumentException if candidates is null or empty
   */
  public Location estimate(double lat, double lon, List<Segment> candidates) {
    if (candidates == null || candidates.isEmpty()) {
      throw new IllegalArgumentException("candidates must not be empty");
    }
    Location best = null;
    for (Segment segment : candidates) {
      Location location = estimateOn(lat, lon, segment);
      if (best == null
          || location.getDistanceMeters() < best.getDistanceMeters()
          || (location.getDistanceMeters() == best.getDistanceMeters()
              && location.getSegmentId() < best.getSegmentId())) {
        best = location;
      }
    }
    return best;
  }

  /** Projects the point onto one segment's line and interpolates the mile marker. */
  private Location estimateOn(double lat, double lon, Segment segment) {
    List<double[]> points = segment.getPoints();
    double[] p = GeoUtils.toLocalXY(lat, lon, lat);

    double bestDistance = Double.MAX_VALUE;
    double bestAlong = 0;
    double lengthSoFar = 0;
    double[] a = toLocalXY(points.get(0), lat);
    if (points.size() == 1) {
      bestDistance = Math.hypot(p[0] - a[0], p[1] - a[1]);
    }
    for (int i = 1; i < points.size(); i++) {
      double[] b = toLocalXY(points.get(i), lat);
      double dx = b[0] - a[0];
      double dy = b[1] - a[1];
      double pieceLength = Math.hypot(dx, dy);

      // Fraction of the way from a to b of the closest point, kept on the piece.
      double t = 0;
      if (pieceLength > 0) {
        t = ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / (pieceLength * pieceLength);
        t = Math.max(0, Math.min(1, t));
      }
      double distance = Math.hypot(p[0] - (a[0] + t * dx), p[1] - (a[1] + t * dy));
      if (distance < bestDistance) {
        bestDistance = distance;
        bestAlong = lengthSoFar + t * pieceLength;
      }
      lengthSoFar += pieceLength;
      a = b;
    }

    double mileMarker = segment.getBegMp();
    if (lengthSoFar > 0) {
      mileMarker += (segment.getEndMp() - segment.getBegMp()) * bestAlong / lengthSoFar;
    }
    return new Location(segment.getState(), segment.getId(), mileMarker, bestDistance);
  }

  private static double[] toLocalXY(double[] point, double refLat) {
    return GeoUtils.toLocalXY(point[0], point[1], refLat);
  }
}
