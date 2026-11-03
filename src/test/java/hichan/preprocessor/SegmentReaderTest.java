package hichan.preprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hichan.common.Segment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for SegmentReader (FN6.1). */
class SegmentReaderTest {

  private final SegmentReader reader = new SegmentReader();

  /** Writes a small JSON file with the given features and returns its path. */
  private Path writeJson(String features) throws IOException {
    Path file = Files.createTempFile("nhpn", ".json");
    Files.writeString(file, "{\"type\":\"FeatureCollection\",\"features\":[" + features + "]}");
    return file;
  }

  /** Builds one feature in the real NHPN layout (fields at the top level). */
  private String feature(String sign, String fips, double beg, double end, String coords) {
    return "{\"geometry\":{\"type\":\"LineString\",\"coordinates\":" + coords + "},"
        + "\"SIGN1\":\"" + sign + "\",\"STFIPS\":\"" + fips + "\","
        + "\"BEGMP\":" + beg + ",\"ENDMP\":" + end + "}";
  }

  @Test
  void testFN6_1_StitchesUnorderedAndReversedPieces() throws IOException {
    // One mile range split into two pieces: the east piece comes first and is reversed
    String east = feature("I90", "53", 0, 1, "[[-117.0,47.0],[-117.5,47.0]]");
    String west = feature("I90", "53", 0, 1, "[[-118.0,47.0],[-117.5,47.0]]");
    String next = feature("I90", "53", 1, 2, "[[-117.0,47.0],[-116.5,47.0]]");
    List<Segment> segments = reader.readSegments(writeJson(east + "," + west + "," + next));

    assertEquals(2, segments.size());
    List<double[]> points = segments.get(0).getPoints();
    // Shared point is kept once, and the line runs west to east toward the next range
    assertEquals(3, points.size());
    assertEquals(-118.0, points.get(0)[1], 1e-9);
    assertEquals(-117.5, points.get(1)[1], 1e-9);
    assertEquals(-117.0, points.get(2)[1], 1e-9);
  }

  @Test
  void testFN6_1_ConvertsLonLatToLatLon() throws IOException {
    List<Segment> segments = reader.readSegments(
        writeJson(feature("I90", "16", 0, 1, "[[-117.04,47.69],[-117.00,47.68]]")));
    double[] first = segments.get(0).getPoints().get(0);
    assertEquals(47.69, first[0], 1e-9);
    assertEquals(-117.04, first[1], 1e-9);
  }

  @Test
  void testFN6_1_FiltersOtherHighwaysAndStates() throws IOException {
    String i90Wa = feature("I90", "53", 0, 1, "[[-118.0,47.0],[-117.9,47.0]]");
    String i82Wa = feature("I82", "53", 0, 1, "[[-120.0,46.0],[-119.9,46.0]]");
    String i90Mt = feature("I90", "30", 0, 1, "[[-115.0,47.0],[-114.9,47.0]]");
    List<Segment> segments = reader.readSegments(writeJson(i90Wa + "," + i82Wa + "," + i90Mt));
    assertEquals(1, segments.size());
    assertEquals(53, segments.get(0).getStateFips());
  }

  @Test
  void testFN6_1_SkipsFeatureMissingField() throws IOException {
    String good = feature("I90", "53", 0, 1, "[[-118.0,47.0],[-117.9,47.0]]");
    String noEndMp = "{\"geometry\":{\"type\":\"LineString\",\"coordinates\":"
        + "[[-117.0,47.0],[-116.9,47.0]]},\"SIGN1\":\"I90\",\"STFIPS\":\"53\",\"BEGMP\":1}";
    assertEquals(1, reader.readSegments(writeJson(good + "," + noEndMp)).size());
  }

  @Test
  void testFN6_1_SkipsLineWithOnePoint() throws IOException {
    String onePoint = feature("I90", "53", 0, 1, "[[-118.0,47.0]]");
    assertEquals(0, reader.readSegments(writeJson(onePoint)).size());
  }

  @Test
  void testFN6_1_SwapsReversedMileMarkers() throws IOException {
    List<Segment> segments = reader.readSegments(
        writeJson(feature("I90", "53", 5, 2, "[[-118.0,47.0],[-117.9,47.0]]")));
    assertEquals(2.0, segments.get(0).getBegMp(), 1e-9);
    assertEquals(5.0, segments.get(0).getEndMp(), 1e-9);
  }

  @Test
  void testFN6_1_InvalidJsonThrows() throws IOException {
    Path file = Files.createTempFile("bad", ".json");
    Files.writeString(file, "this is not json");
    assertThrows(IOException.class, () -> reader.readSegments(file));
  }

  @Test
  void testFN6_1_RealNhpnFile() throws IOException {
    List<Segment> segments = reader.readSegments(Path.of("data/nhpn_i90.json"));
    // 194 distinct (state, BEGMP, ENDMP) ranges in the provided file
    assertEquals(194, segments.size());
    // Washington mile 0 is in Seattle, west of -122
    Segment firstWa = segments.stream().filter(s -> s.getStateFips() == 53).findFirst().get();
    assertTrue(firstWa.getPoints().get(0)[1] < -122.0);
  }
}
