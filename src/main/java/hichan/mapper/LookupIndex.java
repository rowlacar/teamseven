package hichan.mapper;

import hichan.common.Config;
import hichan.common.Segment;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The lookup file loaded into memory: geohash cell to the segments passing through it (FN6.4,
 * NF7.1, Detailed Design 3.9).
 *
 * <p>The file is read once at startup. A segment appears on one row per cell, but every row with
 * the same segment id shares one {@link Segment} object.
 */
public final class LookupIndex {

  private final Map<String, List<Segment>> cells;
  private final int segmentCount;

  /**
   * Creates an index from an already-built map. Used by tests to skip the file.
   *
   * @param cells geohash cell to segments
   */
  LookupIndex(Map<String, List<Segment>> cells) {
    Map<String, List<Segment>> copy = new HashMap<>();
    Set<Integer> ids = new HashSet<>();
    for (Map.Entry<String, List<Segment>> entry : cells.entrySet()) {
      copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
      for (Segment segment : entry.getValue()) {
        ids.add(segment.getId());
      }
    }
    this.cells = copy;
    this.segmentCount = ids.size();
  }

  /**
   * Loads a lookup file written by the Preprocessor (format in Detailed Design 4.2).
   *
   * @param lookupFile path to lookup.csv
   * @return the loaded index
   * @throws IOException if the file cannot be read, is empty, or has a malformed row; the
   *     message names the file and line
   */
  public static LookupIndex load(Path lookupFile) throws IOException {
    Map<String, List<Segment>> cells = new HashMap<>();
    Map<Integer, Segment> byId = new HashMap<>();
    String fileName = String.valueOf(lookupFile.getFileName());

    try (BufferedReader reader = Files.newBufferedReader(lookupFile, StandardCharsets.UTF_8)) {
      String line;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        if (line.isBlank()) {
          continue;
        }
        String where = fileName + ", line " + lineNumber + ": ";
        String[] fields = line.split(",", 6);
        if (fields.length != 6) {
          throw new IOException(where + "expected 6 fields, found " + fields.length);
        }
        String geohash = fields[0].trim();
        if (geohash.isEmpty()) {
          throw new IOException(where + "geohash is empty");
        }

        int id = parseInt(fields[1], "segmentId", where);
        Segment segment = byId.get(id);
        if (segment == null) {
          segment = parseSegment(id, fields, where);
          byId.put(id, segment);
        }
        cells.computeIfAbsent(geohash, k -> new ArrayList<>()).add(segment);
      }
    }
    if (cells.isEmpty()) {
      throw new IOException(fileName + ": lookup file is empty; rerun the Preprocessor");
    }
    return new LookupIndex(cells);
  }

  /**
   * Returns the segments that pass through a geohash cell.
   *
   * @param geohash cell key
   * @return unmodifiable list; empty if no segment passes through the cell
   */
  public List<Segment> segmentsIn(String geohash) {
    return cells.getOrDefault(geohash, List.of());
  }

  /** Returns the number of geohash cells in the index. */
  public int cellCount() {
    return cells.size();
  }

  /** Returns the number of distinct segments in the index. */
  public int segmentCount() {
    return segmentCount;
  }

  /** Builds a segment from the state, mile, and coordinate fields of a row. */
  private static Segment parseSegment(int id, String[] fields, String where) throws IOException {
    String state = fields[2].trim();
    double begMp = parseDouble(fields[3], "begMp", where);
    double endMp = parseDouble(fields[4], "endMp", where);

    List<double[]> points = new ArrayList<>();
    for (String pair : fields[5].split(";")) {
      String[] latLon = pair.trim().split("\\s+");
      if (latLon.length != 2) {
        throw new IOException(where + "bad coordinate \"" + pair + "\"");
      }
      points.add(new double[] {
          parseDouble(latLon[0], "latitude", where), parseDouble(latLon[1], "longitude", where)});
    }
    if (points.size() < 2) {
      throw new IOException(where + "segment " + id + " has fewer than 2 points");
    }

    Segment segment = new Segment(id, fipsFor(state, where), begMp, endMp, points);
    segment.setState(state);
    return segment;
  }

  /** The lookup file stores the state code, but Segment also needs the FIPS number. */
  private static int fipsFor(String state, String where) throws IOException {
    for (Map.Entry<Integer, String> entry : Config.STATE_CODES.entrySet()) {
      if (entry.getValue().equals(state)) {
        return entry.getKey();
      }
    }
    throw new IOException(where + "unknown state \"" + state + "\"");
  }

  private static int parseInt(String text, String field, String where) throws IOException {
    try {
      return Integer.parseInt(text.trim());
    } catch (NumberFormatException e) {
      throw new IOException(where + field + " is not a number");
    }
  }

  private static double parseDouble(String text, String field, String where) throws IOException {
    try {
      return Double.parseDouble(text.trim());
    } catch (NumberFormatException e) {
      throw new IOException(where + field + " is not a number");
    }
  }
}
