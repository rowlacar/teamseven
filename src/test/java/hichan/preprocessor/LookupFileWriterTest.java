package hichan.preprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hichan.common.Segment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Tests for LookupFileWriter (FN6.2, FN6.4). */
class LookupFileWriterTest {

  private Segment segment(int id, String state) {
    Segment segment = new Segment(id, 53, 283.41, 287.92,
        List.of(new double[] {47.6588, -117.426}, new double[] {47.6602, -117.391}));
    segment.setState(state);
    return segment;
  }

  @Test
  void testFN6_2_RowFormat() throws IOException {
    Path out = Files.createTempFile("lookup", ".csv");
    new LookupFileWriter().write(Map.of("c2krpu", List.of(segment(17, "WA"))), out);
    // Matches the example row format in Detailed Design 4.2
    assertEquals(List.of("c2krpu,17,WA,283.41,287.92,47.658800 -117.426000;47.660200 -117.391000"),
        Files.readAllLines(out));
  }

  @Test
  void testFN6_2_RowsSortedByGeohashThenId() throws IOException {
    Path out = Files.createTempFile("lookup", ".csv");
    Map<String, List<Segment>> index = Map.of(
        "c2kz2d", List.of(segment(2, "WA")),
        "c2krpu", List.of(segment(9, "WA"), segment(3, "WA")));
    new LookupFileWriter().write(index, out);
    List<String> lines = Files.readAllLines(out);
    assertEquals(3, lines.size());
    assertEquals(true, lines.get(0).startsWith("c2krpu,3,"));
    assertEquals(true, lines.get(1).startsWith("c2krpu,9,"));
    assertEquals(true, lines.get(2).startsWith("c2kz2d,2,"));
  }
}
