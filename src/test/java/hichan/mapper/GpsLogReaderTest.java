package hichan.mapper;

import hichan.common.GpsEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GpsLogReaderTest {

    @Test
    void validLineIsParsed() throws Exception {

        Path testFile = Path.of("gps_valid_test.csv");

        Files.write(
                testFile,
                List.of(
                        "latitude,longitude,elevation,date,time,speed",
                        "47.6587,-117.4260,200.0,20261108,120000,55.0"
                )
        );

        try (GpsLogReader reader = new GpsLogReader(testFile)) {

            GpsEntry entry = reader.nextEntry();

            assertNotNull(entry);
            assertTrue(entry.isValid());

            assertEquals(
                    47.6587,
                    entry.getLatitude(),
                    0.0001);

            assertEquals(
                    -117.4260,
                    entry.getLongitude(),
                    0.0001);

            assertEquals(
                    "47.6587,-117.4260,200.0,20261108,120000,55.0",
                    entry.getRawLine());
        }

        Files.deleteIfExists(testFile);
    }

    @Test
    void malformedLineIsMarkedInvalid() throws Exception {

        Path testFile = Path.of("gps_invalid_test.csv");

        Files.write(
                testFile,
                List.of(
                        "latitude,longitude,elevation,date,time,speed",
                        "BAD,DATA,LINE"
                )
        );

        try (GpsLogReader reader = new GpsLogReader(testFile)) {

            GpsEntry entry = reader.nextEntry();

            assertNotNull(entry);
            assertFalse(entry.isValid());

            assertEquals(
                    "BAD,DATA,LINE",
                    entry.getRawLine());
        }

        Files.deleteIfExists(testFile);
    }

    @Test
    void latitudeOutOfRangeIsInvalid() throws Exception {

        Path testFile = Path.of("gps_bad_latitude.csv");

        Files.write(
                testFile,
                List.of(
                        "latitude,longitude,elevation,date,time,speed",
                        "200.0,-117.4260,200.0,20261108,120000,55.0"
                )
        );

        try (GpsLogReader reader = new GpsLogReader(testFile)) {

            GpsEntry entry = reader.nextEntry();

            assertNotNull(entry);
            assertFalse(entry.isValid());
        }

        Files.deleteIfExists(testFile);
    }

    @Test
    void longitudeOutOfRangeIsInvalid() throws Exception {

        Path testFile = Path.of("gps_bad_longitude.csv");

        Files.write(
                testFile,
                List.of(
                        "latitude,longitude,elevation,date,time,speed",
                        "47.6587,-250.0,200.0,20261108,120000,55.0"
                )
        );

        try (GpsLogReader reader = new GpsLogReader(testFile)) {

            GpsEntry entry = reader.nextEntry();

            assertNotNull(entry);
            assertFalse(entry.isValid());
        }

        Files.deleteIfExists(testFile);
    }

    @Test
    void readerReturnsNullAtEndOfFile() throws Exception {

        Path testFile = Path.of("gps_eof_test.csv");

        Files.write(
                testFile,
                List.of(
                        "latitude,longitude,elevation,date,time,speed",
                        "47.6587,-117.4260,200.0,20261108,120000,55.0"
                )
        );

        try (GpsLogReader reader = new GpsLogReader(testFile)) {

            assertNotNull(reader.nextEntry());

            assertNull(reader.nextEntry());
        }

        Files.deleteIfExists(testFile);
    }

    @Test
    void multipleEntriesAreReadInOrder() throws Exception {

        Path testFile = Path.of("gps_multiple_test.csv");

        Files.write(
                testFile,
                List.of(
                        "latitude,longitude,elevation,date,time,speed",
                        "47.6587,-117.4260,200.0,20261108,120000,55.0",
                        "47.7000,-117.5000,205.0,20261108,120100,60.0"
                )
        );

        try (GpsLogReader reader = new GpsLogReader(testFile)) {

            GpsEntry first = reader.nextEntry();
            GpsEntry second = reader.nextEntry();

            assertNotNull(first);
            assertNotNull(second);

            assertEquals(47.6587, first.getLatitude(), 0.0001);
            assertEquals(47.7000, second.getLatitude(), 0.0001);
        }

        Files.deleteIfExists(testFile);
    }
}