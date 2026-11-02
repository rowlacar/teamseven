package hichan.common;

/**
 * Where a GPS point falls on I-90: the matched segment and the estimated mile marker.
 *
 * <p>Produced by {@code SegmentLocator}; consumed by {@code DirectionTracker} and {@code
 * ChannelIdBuilder}. Immutable.
 */
public final class Location {

  private final String state;
  private final int segmentId;
  private final double mileMarker;
  private final double distanceMeters;

  /**
   * Creates a location.
   *
   * @param state two-letter state code, WA or ID
   * @param segmentId id of the matched segment
   * @param mileMarker estimated mile marker within the segment
   * @param distanceMeters distance from the GPS point to the segment
   */
  public Location(String state, int segmentId, double mileMarker, double distanceMeters) {
    this.state = state;
    this.segmentId = segmentId;
    this.mileMarker = mileMarker;
    this.distanceMeters = distanceMeters;
  }

  /** Returns the two-letter state code. */
  public String getState() {
    return state;
  }

  /** Returns the id of the matched segment. */
  public int getSegmentId() {
    return segmentId;
  }

  /** Returns the estimated mile marker. */
  public double getMileMarker() {
    return mileMarker;
  }

  /** Returns the distance in meters from the GPS point to the matched segment. */
  public double getDistanceMeters() {
    return distanceMeters;
  }

  @Override
  public String toString() {
    return String.format(
        "Location[%s seg %d, mm %.3f, %.1f m]", state, segmentId, mileMarker, distanceMeters);
  }
}
