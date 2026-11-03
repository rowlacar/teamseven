package hichan.preprocessor;

import hichan.common.Segment;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Finds the geohash cells each segment passes through (FN6.2, Detailed Design 3.4, 6.3).
 */
public class GeohashIndexer {

    public Map<String, List<Segment>> buildIndex(List<Segment> segments) {
        return new TreeMap<>(); // TODO: sample each segment and collect its cells
    }
}