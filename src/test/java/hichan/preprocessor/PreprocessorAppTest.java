package hichan.preprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Tests for PreprocessorApp exit codes and the full pipeline (FN6.4). */
class PreprocessorAppTest {

  @Test
  void testWrongArgumentCountIsUsageError() {
    assertEquals(PreprocessorApp.EXIT_USAGE, PreprocessorApp.run(new String[] {"only-one"}));
  }

  @Test
  void testMissingInputIsFileError() {
    assertEquals(PreprocessorApp.EXIT_FILE,
        PreprocessorApp.run(new String[] {"no/such/file.json", "out.csv"}));
  }

  @Test
  void testNF7_9_RefusesToOverwriteInput() {
    String in = "data/nhpn_i90.json";
    assertEquals(PreprocessorApp.EXIT_FILE, PreprocessorApp.run(new String[] {in, in}));
  }

  @Test
  void testFN6_4_EndToEndWritesLookupFile() throws IOException {
    Path out = Files.createTempDirectory("hichan").resolve("lookup.csv");
    int code = PreprocessorApp.run(new String[] {"data/nhpn_i90.json", out.toString()});
    assertEquals(PreprocessorApp.EXIT_OK, code);
    // Every row has the 6 fields from Detailed Design 4.2
    for (String line : Files.readAllLines(out)) {
      assertEquals(6, line.split(",").length);
    }
    assertTrue(Files.size(out) > 0);
  }
}
