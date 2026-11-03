package hichan.preprocessor;

import hichan.common.Segment;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Writes the geohash index as the comma-separated lookup file (FN6.2, FN6.4,
 * Detailed Design 3.5 and 4.2).
 * Row format, no header: geohash,segmentId,state,begMp,endMp,coordinates
 */
public class LookupFileWriter {

    /**
     * Writes one row per (cell, segment) pair, sorted by geohash and then segment id.
     *
     * @param index map from geohash cell to segments
     * @param outFile path of the lookup file to create or replace
     * @throws IOException if the file cannot be written
     */
    public void write(Map<String, List<Segment>> index, Path outFile) throws IOException {
        Path parent = outFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter out = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, List<Segment>> entry : new TreeMap<>(index).entrySet()) {
                List<Segment> segments = new ArrayList<>(entry.getValue());
                segments.sort(Comparator.comparingInt(Segment::getId));
                for (Segment segment : segments) {
                    out.write(entry.getKey() + ","
                            + segment.getId() + ","
                            + segment.getState() + ","
                            + segment.getBegMp() + ","
                            + segment.getEndMp() + ","
                            + segment.coordinatesToString());
                    out.newLine();
                }
            }
        }
    }
}