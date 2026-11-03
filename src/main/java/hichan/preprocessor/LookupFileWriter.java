package hichan.preprocessor;

import hichan.common.Segment;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Writes the geohash index as the lookup file (FN6.2, Detailed Design 3.5)
 */
public class LookupFileWriter {

    public void write(Map<String, List<Segment>> index, Path outFile) throws IOException {
        try (BufferedWriter out = Files.newBufferedWriter(outFile)) {
            // we'll write rows here next
        }
    }

}
