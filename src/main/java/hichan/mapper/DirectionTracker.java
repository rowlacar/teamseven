package hichan.mapper;

import hichan.common.Config;
import hichan.common.Direction;
import hichan.common.Location;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Determines direction of travel from consecutive mile markers (Detailed Design Section 3.10).
 *
 * <p>Direction is UNKNOWN until the window holds {@link Config#DIRECTION_WINDOW_SIZE} markers that
 * all increase (EB) or all decrease (WB) (FN6.8, FN6.11). After that, a window with no consistent
 * trend keeps the last direction. Crossing the WA/ID line clears the window but keeps the direction,
 * because mile markers restart at each state line (FN6.9).
 */
public class DirectionTracker {

  private final Deque<Double> window = new ArrayDeque<>();
  private Direction direction = Direction.UNKNOWN;
  private String currentState;

  /**
   * Adds a location's mile marker and returns the current direction.
   *
   * @param location the located GPS point
   * @return EB, WB, or UNKNOWN
   * @throws IllegalArgumentException if location is null
   */
  public Direction update(Location location) {
    if (location == null) {
      throw new IllegalArgumentException("location must not be null");
    }
    if (currentState != null && !currentState.equals(location.getState())) {
      handleStateChange(location.getState());
    }
    currentState = location.getState();

    window.addLast(location.getMileMarker());
    if (window.size() > Config.DIRECTION_WINDOW_SIZE) {
      window.removeFirst();
    }
    if (window.size() == Config.DIRECTION_WINDOW_SIZE) {
      Direction trend = evaluateTrend();
      if (trend != Direction.UNKNOWN) {
        direction = trend;
      }
    }
    return direction;
  }

  /**
   * Returns the current direction without updating.
   *
   * @return EB, WB, or UNKNOWN
   */
  public Direction getDirection() {
    return direction;
  }

  /** StateLineHandler: markers from different states can't be compared, so start a new window. */
  private void handleStateChange(String newState) {
    window.clear();
  }

  /** TrendEvaluator: EB if strictly increasing, WB if strictly decreasing, else UNKNOWN. */
  private Direction evaluateTrend() {
    boolean increasing = true;
    boolean decreasing = true;
    Iterator<Double> it = window.iterator();
    double prev = it.next();
    while (it.hasNext()) {
      double next = it.next();
      increasing &= next > prev;
      decreasing &= next < prev;
      prev = next;
    }
    if (increasing) {
      return Direction.EB;
    }
    if (decreasing) {
      return Direction.WB;
    }
    return Direction.UNKNOWN;
  }
}
