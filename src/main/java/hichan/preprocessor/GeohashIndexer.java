package hichan.preprocessor;

import hichan.common.Config;
import hichan.common.GeoUtils;
import hichan.common.Segment;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Finds the geohash cells each segment passes through (FN6.2, Detailed Design 3.4, 6.3).
 */
public class GeohashIndexer {

    /** Stop splitting a gap once it is shorter than this. */
    private static final double MIN_SPLIT_METERS = 0.5;

    /**
     * Builds the index from geohash cell to the segments that pass through it.
     *
     * @param segments segments with their state set
     * @return map sorted by geohash; empty if the list is empty
     */
    public Map<String, List<Segment>> buildIndex(List<Segment> segments) {
        Map<String, List<Segment>> index = new TreeMap<>();
        for (Segment segment : segments) {
            for (String cell : cellsFor(segment)) {
                index.computeIfAbsent(cell, k -> new ArrayList<>()).add(segment);
            }
        }
        return index;
    }

    /** Returns the cells one segment passes through, without duplicates. */
    private Set<String> cellsFor(Segment segment) {
        Set<String> cells = new LinkedHashSet<>();
        List<double[]> points = segment.getPoints();
        for (int i = 0; i + 1 < points.size(); i++) {
            double[] a = points.get(i);
            double[] b = points.get(i + 1);
            double length = GeoUtils.distanceMeters(a[0], a[1], b[0], b[1]);
            int steps = Math.max(1, (int) Math.ceil(length / Config.SAMPLE_INTERVAL_METERS));

            double[] previous = a;
            cells.add(cellOf(a));
            for (int s = 1; s <= steps; s++) {
                double t = (double) s / steps; // fraction of the way from a to b
                double[] sample = {a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t};
                addCellsBetween(previous, sample, cells);
                previous = sample;
            }
        }
        return cells;
    }

    /**
     * Adds the cells of p and q, plus any cell the straight line between them crosses.
     * Splits the line in half until each half stays in one cell or two edge-sharing cells.
     */
    private void addCellsBetween(double[] p, double[] q, Set<String> cells) {
        String cellP = cellOf(p);
        String cellQ = cellOf(q);
        cells.add(cellQ);
        if (cellP.equals(cellQ) || sharesEdge(cellP, cellQ)
                || GeoUtils.distanceMeters(p[0], p[1], q[0], q[1]) < MIN_SPLIT_METERS) {
            return;
        }
        double[] middle = {(p[0] + q[0]) / 2, (p[1] + q[1]) / 2};
        addCellsBetween(p, middle, cells);
        addCellsBetween(middle, q, cells);
    }

    /** True if the two cells touch along a side (N, E, S, or W), not just at a corner. */
    private boolean sharesEdge(String cell, String other) {
        List<String> around = GeoUtils.neighbors(cell); // N, NE, E, SE, S, SW, W, NW
        return other.equals(around.get(0)) || other.equals(around.get(2))
                || other.equals(around.get(4)) || other.equals(around.get(6));
    }

    private String cellOf(double[] point) {
        return GeoUtils.encodeGeohash(point[0], point[1], Config.GEOHASH_PRECISION);
    }
}