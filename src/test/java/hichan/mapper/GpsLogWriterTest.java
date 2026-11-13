package hichan.mapper;

import hichan.common.GpsEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GpsLogWriterTest {

    @Test
    void writesSingleEntry() throws Exception {

        Path outputFile = Path.of("gps_writer_test.csv");

        GpsEntry entry = new GpsEntry(
                1,
                "47.6587,-117.4260,200.0,20261108,120000,55.0",
                47.6587,
                -117.4260,
                200.0,
                "20261108",
                "120000",
                55.0,
                0,
                true);

        try (GpsLogWriter writer =
                     new GpsLogWriter(outputFile)) {

            writer.write(
                    entry,
                    "I90WAEB_10");
        }

        List<String> lines =
                Files.readAllLines(outputFile);

        assertEquals(1, lines.size());

        assertEquals(
                "47.6587,-117.4260,200.0,20261108,120000,55.0,I90WAEB_10",
                lines.get(0));

        Files.deleteIfExists(outputFile);
    }

    @Test
    void writesInvalidLabel() throws Exception {

        Path outputFile = Path.of("gps_writer_invalid_test.csv");

        GpsEntry entry = new GpsEntry(
                2,
                "BAD,DATA,LINE",
                0,
                0,
                0,
                "",
                "",
                0,
                0,
                false);

        try (GpsLogWriter writer =
                     new GpsLogWriter(outputFile)) {

            writer.write(
                    entry,
                    "INVALID");
        }

        List<String> lines =
                Files.readAllLines(outputFile);

        assertEquals(1, lines.size());

        assertEquals(
                "BAD,DATA,LINE,INVALID",
                lines.get(0));

        Files.deleteIfExists(outputFile);
    }

    @Test
    void writesMultipleEntries() throws Exception {

        Path outputFile = Path.of("gps_writer_multiple_test.csv");

        GpsEntry entry1 = new GpsEntry(
                1,
                "LINE1",
                0, 0, 0,
                "", "",
                0,
                0,
                true);

        GpsEntry entry2 = new GpsEntry(
                2,
                "LINE2",
                0, 0, 0,
                "", "",
                0,
                0,
                true);

        try (GpsLogWriter writer =
                     new GpsLogWriter(outputFile)) {

            writer.write(entry1, "LABEL1");
            writer.write(entry2, "LABEL2");
        }

        List<String> lines =
                Files.readAllLines(outputFile);

        assertEquals(2, lines.size());

        assertEquals(
                "LINE1,LABEL1",
                lines.get(0));

        assertEquals(
                "LINE2,LABEL2",
                lines.get(1));

        Files.deleteIfExists(outputFile);
    }
}