package hichan.preprocessor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import hichan.common.Config;
import hichan.common.GeoUtils;
import hichan.common.Segment;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads the filtered NHPN JSON file and builds one Segment per mile-marker range (FN6.1,
 * Detailed Design 3.2).
 *
 * <p>The NHPN data splits each mile range into many small, unordered pieces, some of them
 * reversed. This class stitches the pieces of each range into one line and orients every
 * line so its points run in the direction of increasing mile markers.
 */
public class SegmentReader {

    /** Pieces whose endpoints are closer than this are treated as touching. */
    private static final double JOIN_TOLERANCE_METERS = 1.0;

    /**
     * Reads, filters, and stitches the segments in a NHPN JSON file.
     *
     * @param jsonFile path to the JSON file
     * @return segments ordered by state and mile marker; state code not yet set
     * @throws IOException if the file cannot be read or is not valid JSON
     */
    public List<Segment> readSegments(Path jsonFile) throws IOException {
        JsonArray features = parseFeatures(jsonFile);

        // Key: "stateFips|begMp|endMp". TreeMap keeps a stable order for reading.
        Map<String, List<List<double[]>>> groups = new TreeMap<>();
        Map<String, double[]> groupInfo = new TreeMap<>(); // {stateFips, begMp, endMp}

        for (int i = 0; i < features.size(); i++) {
            int featureNumber = i + 1;
            JsonObject feature = features.get(i).getAsJsonObject();
            // Real NHPN data has its fields at the top level; standard GeoJSON uses "properties".
            JsonObject props = feature.has("properties")
                    ? feature.getAsJsonObject("properties") : feature;

            String missing = firstMissing(props, feature);
            if (missing != null) {
                System.err.println("ERROR: feature " + featureNumber + ": missing " + missing);
                continue;
            }
            if (!Config.HIGHWAY.equals(props.get("SIGN1").getAsString())) {
                continue;
            }
            int stateFips = props.get("STFIPS").getAsInt(); // works for "53" or 53
            if (!Config.STATE_CODES.containsKey(stateFips)) {
                continue;
            }
            double begMp = props.get("BEGMP").getAsDouble();
            double endMp = props.get("ENDMP").getAsDouble();
            if (begMp > endMp) {
                System.err.println("WARNING: feature " + featureNumber + ": BEGMP > ENDMP, swapping");
                double temp = begMp;
                begMp = endMp;
                endMp = temp;
            }

            List<double[]> points = readPoints(feature.getAsJsonObject("geometry"));
            if (points.size() < 2) {
                System.err.println("WARNING: feature " + featureNumber + ": fewer than 2 points, skipped");
                continue;
            }

            String key = stateFips + "|" + begMp + "|" + endMp;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(points);
            groupInfo.putIfAbsent(key, new double[] {stateFips, begMp, endMp});
        }

        return buildSegments(groups, groupInfo);
    }

    /** Opens the file and returns its "features" array. */
    private JsonArray parseFeatures(Path jsonFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject() || !root.getAsJsonObject().has("features")) {
                throw new IOException(jsonFile + ": not a GeoJSON FeatureCollection");
            }
            return root.getAsJsonObject().getAsJsonArray("features");
        } catch (JsonParseException | IllegalStateException e) {
            throw new IOException(jsonFile + ": invalid JSON (" + e.getMessage() + ")", e);
        }
    }

    /** Returns the name of the first required field that is missing, or null if none are. */
    private String firstMissing(JsonObject props, JsonObject feature) {
        for (String field : new String[] {"SIGN1", "STFIPS", "BEGMP", "ENDMP"}) {
            if (!props.has(field) || props.get(field).isJsonNull()) {
                return field;
            }
        }
        if (!feature.has("geometry") || !feature.get("geometry").isJsonObject()
                || !feature.getAsJsonObject("geometry").has("coordinates")) {
            return "geometry.coordinates";
        }
        return null;
    }

    /** Converts GeoJSON [longitude, latitude] pairs to {lat, lon} arrays. */
    private List<double[]> readPoints(JsonObject geometry) {
        List<double[]> points = new ArrayList<>();
        for (JsonElement coordinate : geometry.getAsJsonArray("coordinates")) {
            JsonArray pair = coordinate.getAsJsonArray();
            points.add(new double[] {pair.get(1).getAsDouble(), pair.get(0).getAsDouble()});
        }
        return points;
    }

    /** Stitches each group, orients the lines state by state, and assigns segment ids. */
    private List<Segment> buildSegments(
            Map<String, List<List<double[]>>> groups, Map<String, double[]> groupInfo) {
        // Sort groups by state, then by begin mile marker.
        List<String> keys = new ArrayList<>(groups.keySet());
        keys.sort((a, b) -> {
            double[] ia = groupInfo.get(a);
            double[] ib = groupInfo.get(b);
            int byState = Double.compare(ia[0], ib[0]);
            return byState != 0 ? byState : Double.compare(ia[1], ib[1]);
        });

        List<List<double[]>> lines = new ArrayList<>();
        for (String key : keys) {
            lines.add(stitch(groups.get(key)));
        }
        orientLines(keys, groupInfo, lines);

        List<Segment> segments = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            double[] info = groupInfo.get(keys.get(i));
            segments.add(new Segment(i + 1, (int) info[0], info[1], info[2], lines.get(i)));
        }
        return segments;
    }

    /**
     * Joins pieces into one line by repeatedly attaching the piece whose endpoint is nearest
     * to either end of the line, flipping the piece when needed.
     */
    private List<double[]> stitch(List<List<double[]>> pieces) {
        List<List<double[]>> remaining = new ArrayList<>(pieces);
        List<double[]> line = new ArrayList<>(remaining.remove(0));

        while (!remaining.isEmpty()) {
            int bestIndex = -1;
            double bestDistance = Double.MAX_VALUE;
            boolean bestAtLineEnd = true;
            boolean bestFlip = false;

            double[] lineStart = line.get(0);
            double[] lineEnd = line.get(line.size() - 1);
            for (int i = 0; i < remaining.size(); i++) {
                List<double[]> piece = remaining.get(i);
                double[] first = piece.get(0);
                double[] last = piece.get(piece.size() - 1);
                // Four ways a piece can attach: to the line's end or start, normal or flipped.
                double[][] options = {
                        {distance(lineEnd, first), 1, 0},
                        {distance(lineEnd, last), 1, 1},
                        {distance(lineStart, last), 0, 0},
                        {distance(lineStart, first), 0, 1}};
                for (double[] option : options) {
                    if (option[0] < bestDistance) {
                        bestDistance = option[0];
                        bestIndex = i;
                        bestAtLineEnd = option[1] == 1;
                        bestFlip = option[2] == 1;
                    }
                }
            }

            List<double[]> piece = new ArrayList<>(remaining.remove(bestIndex));
            if (bestFlip) {
                Collections.reverse(piece);
            }
            if (bestAtLineEnd) {
                int skip = bestDistance < JOIN_TOLERANCE_METERS ? 1 : 0; // drop the shared point
                line.addAll(piece.subList(skip, piece.size()));
            } else {
                int keep = bestDistance < JOIN_TOLERANCE_METERS ? piece.size() - 1 : piece.size();
                line.addAll(0, piece.subList(0, keep));
            }
        }
        return line;
    }

    /**
     * Flips lines so each one starts where the previous mile range in the same state ended.
     * The first line in a state is pointed toward the second.
     */
    private void orientLines(
            List<String> keys, Map<String, double[]> groupInfo, List<List<double[]>> lines) {
        for (int i = 0; i < lines.size(); i++) {
            List<double[]> line = lines.get(i);
            boolean sameStateAsPrevious =
                    i > 0 && groupInfo.get(keys.get(i))[0] == groupInfo.get(keys.get(i - 1))[0];
            boolean sameStateAsNext = i + 1 < lines.size()
                    && groupInfo.get(keys.get(i))[0] == groupInfo.get(keys.get(i + 1))[0];

            if (sameStateAsPrevious) {
                List<double[]> previous = lines.get(i - 1);
                double[] previousEnd = previous.get(previous.size() - 1);
                if (distance(line.get(0), previousEnd)
                        > distance(line.get(line.size() - 1), previousEnd)) {
                    Collections.reverse(line);
                }
            } else if (sameStateAsNext) {
                List<double[]> next = lines.get(i + 1);
                double startToNext = Math.min(
                        distance(line.get(0), next.get(0)), distance(line.get(0), next.get(next.size() - 1)));
                double endToNext = Math.min(
                        distance(line.get(line.size() - 1), next.get(0)),
                        distance(line.get(line.size() - 1), next.get(next.size() - 1)));
                if (startToNext < endToNext) {
                    Collections.reverse(line);
                }
            }
        }
    }

    private double distance(double[] a, double[] b) {
        return GeoUtils.distanceMeters(a[0], a[1], b[0], b[1]);
    }
}