package hichan.preprocessor;

import hichan.common.Segment;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the NHPN JSON file and builds one Segment per mile range (FN6.1, Detailed Design 3.2).
 */
public class SegmentReader {

    public List<Segment> readSegments(Path jsonFile) throws IOException {
        return new ArrayList<>(); // TODO: parse JSON, filter, stitch pieces
    }
}