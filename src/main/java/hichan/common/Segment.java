package hichan.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * One I-90 mile-marker range, built by stitching all NHPN pieces that share
 * the same state and BEGMP/ENDMP into a single ordered line (Detailed Design 5.1).
 * Points run in the direction of increasing mile markers.
 */
public class Segment {

    private final int id;
    private final int stateFips;
    private String state;
    private final double begMp;
    private final double endMp;
    private final List<double[]> points; // each {lat, lon}

    /** Creates a segment. Points must be ordered from begMp to endMp. */
    public Segment(int id, int stateFips, double begMp, double endMp, List<double[]> points) {
        this.id = id;
        this.stateFips = stateFips;
        this.begMp = begMp;
        this.endMp = endMp;
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
    }

    public int getId() {
        return id;
    }

    public int getStateFips() {
        return stateFips;
    }

    public String getState() {
        return state;
    }

    /** Sets the two-letter state code (called after StateMapper runs). */
    public void setState(String state) {
        this.state = state;
    }

    public double getBegMp() {
        return begMp;
    }

    public double getEndMp() {
        return endMp;
    }

    public List<double[]> getPoints() {
        return points;
    }

    /**
     * Returns the points as "lat lon;lat lon;..." for the lookup file
     * Uses 6 decimals
     */
    public String coordinatesToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(String.format(Locale.ROOT, "%.6f %.6f", points.get(i)[0], points.get(i)[1]));
        }
        return sb.toString();
    }
}