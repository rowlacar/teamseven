package hichan.mapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hichan.common.Segment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link LookupIndex} (FN6.4, NF7.1). */
class LookupIndexTest {

  /** Rows in the Detailed Design 4.2 format; segment 17 passes through two cells. */
  private static final String GOOD_FILE = String.join("\n",
      "c2krpu,17,WA,283.41,287.92,47.65880 -117.42600;47.66020 -117.39100;47.66310 -117.35620",
      "c2kx0h,17,WA,283.41,287.92,47.65880 -117.42600;47.66020 -117.39100;47.66310 -117.35620",
      "c2kz2d,1,ID,0.0,6.825,47.696626 -117.041694;47.711150 -116.980258",
      "");

  @TempDir
  Path dir;

  private Path write(String content) throws IOException {
    Path file = dir.resolve("lookup.csv");
    Files.writeString(file, content, StandardCharsets.UTF_8);
    return file;
  }

  @Test
  void testFN6_4_LoadsEveryRow() throws IOException {
    LookupIndex index = LookupIndex.load(write(GOOD_FILE));
    assertEquals(3, index.cellCount());
    assertEquals(2, index.segmentCount());
    assertEquals(1, index.segmentsIn("c2krpu").size());
    assertEquals(17, index.segmentsIn("c2krpu").get(0).getId());
  }

  @Test
  void testFN6_4_RowsWithSameIdShareOneSegment() throws IOException {
    LookupIndex index = LookupIndex.load(write(GOOD_FILE));
    assertSame(index.segmentsIn("c2krpu").get(0), index.segmentsIn("c2kx0h").get(0));
  }

  @Test
  void testFN6_4_ParsesSegmentFields() throws IOException {
    Segment seg = LookupIndex.load(write(GOOD_FILE)).segmentsIn("c2krpu").get(0);
    assertEquals("WA", seg.getState());
    assertEquals(53, seg.getStateFips());
    assertEquals(283.41, seg.getBegMp());
    assertEquals(287.92, seg.getEndMp());
    assertEquals(3, seg.getPoints().size());
    assertArrayEquals(new double[] {47.66020, -117.39100}, seg.getPoints().get(1));
  }

  @Test
  void testFN6_4_MapsIdahoBackToFips16() throws IOException {
    Segment seg = LookupIndex.load(write(GOOD_FILE)).segmentsIn("c2kz2d").get(0);
    assertEquals("ID", seg.getState());
    assertEquals(16, seg.getStateFips());
  }

  @Test
  void testNF7_1_UnknownCellReturnsEmptyList() throws IOException {
    assertTrue(LookupIndex.load(write(GOOD_FILE)).segmentsIn("zzzzzz").isEmpty());
  }

  @Test
  void testNF7_1_ReturnedListCannotBeModified() throws IOException {
    List<Segment> segments = LookupIndex.load(write(GOOD_FILE)).segmentsIn("c2krpu");
    assertThrows(UnsupportedOperationException.class, () -> segments.clear());
  }

  @Test
  void testFN6_4_SkipsBlankLines() throws IOException {
    LookupIndex index = LookupIndex.load(write("\n" + GOOD_FILE + "\n\n"));
    assertEquals(3, index.cellCount());
  }

  @Test
  void testNF7_15_WrongFieldCountReportsLine() throws IOException {
    Path file = write(GOOD_FILE + "c2krpu,17,WA,283.41\n");
    IOException e = assertThrows(IOException.class, () -> LookupIndex.load(file));
    assertTrue(e.getMessage().contains("lookup.csv, line 4"), e.getMessage());
    assertTrue(e.getMessage().contains("expected 6 fields, found 4"), e.getMessage());
  }

  @Test
  void testNF7_15_BadNumberReportsLineAndField() throws IOException {
    Path file = write("c2krpu,17,WA,abc,287.92,47.6 -117.4;47.7 -117.3\n");
    IOException e = assertThrows(IOException.class, () -> LookupIndex.load(file));
    assertTrue(e.getMessage().contains("line 1: begMp is not a number"), e.getMessage());
  }

  @Test
  void testNF7_15_BadCoordinateReportsLine() throws IOException {
    Path file = write("c2krpu,17,WA,283.41,287.92,47.6 -117.4;47.7\n");
    IOException e = assertThrows(IOException.class, () -> LookupIndex.load(file));
    assertTrue(e.getMessage().contains("line 1: bad coordinate"), e.getMessage());
  }

  @Test
  void testNF7_15_SinglePointSegmentRejected() throws IOException {
    Path file = write("c2krpu,17,WA,283.41,287.92,47.6 -117.4\n");
    assertThrows(IOException.class, () -> LookupIndex.load(file));
  }

  @Test
  void testNF7_15_UnknownStateRejected() throws IOException {
    Path file = write("c2krpu,17,MT,0,1,47.6 -117.4;47.7 -117.3\n");
    IOException e = assertThrows(IOException.class, () -> LookupIndex.load(file));
    assertTrue(e.getMessage().contains("unknown state \"MT\""), e.getMessage());
  }

  @Test
  void testFN6_4_EmptyFileRejected() throws IOException {
    Path file = write("\n\n");
    IOException e = assertThrows(IOException.class, () -> LookupIndex.load(file));
    assertTrue(e.getMessage().contains("empty"), e.getMessage());
  }

  @Test
  void testFN6_4_MissingFileThrows() {
    assertThrows(IOException.class, () -> LookupIndex.load(dir.resolve("missing.csv")));
  }
}
