package hichan.common;

/** Direction of travel along I-90, as determined by {@code DirectionTracker}. */
public enum Direction {
  /** Eastbound: mile markers increasing. */
  EB("EB"),
  /** Westbound: mile markers decreasing. */
  WB("WB"),
  /** Not yet established (FN6.11). */
  UNKNOWN("??");

  private final String label;

  Direction(String label) {
    this.label = label;
  }

  /**
   * Returns the text used inside a Channel ID.
   *
   * @return "EB", "WB", or "??"
   */
  public String label() {
    return label;
  }
}
