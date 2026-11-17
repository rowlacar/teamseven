package hichan.mapper;

import hichan.common.GpsEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GpsLogReaderTest {

    @Test
    void readsPhoneLogFormat() throws Exception {

        Path file = Files.createTempFile("gps", ".csv");

        Files.writeString(
                file,
                """
                DateTime,Latitude,Longitude,Altitude,Speed,Sats
                2026-09-22 23:07:25.000,47.6123,-117.4012,2300,65,12
                """
        );

        try (GpsLogReader reader =
                     new GpsLogReader(file)) {

            GpsEntry entry = reader.nextEntry();

            assertNotNull(entry);
            assertTrue(entry.isValid());

            assertEquals(
                    47.6123,
                    entry.getLatitude(),
                    0.0001);

            assertEquals(
                    -117.4012,
                    entry.getLongitude(),
                    0.0001);

            assertEquals(
                    2300.0,
                    entry.getElevation(),
                    0.0001);

            assertEquals(
                    "2026-09-22",
                    entry.getDate());

            assertEquals(
                    "23:07:25.000",
                    entry.getTime());

            assertEquals(
                    65.0,
                    entry.getSpeed(),
                    0.0001);

            assertEquals(
                    12,
                    entry.getSatelliteCount());
        }
    }

    @Test
    void returnsInvalidEntryForBadLatitude()
            throws Exception {

        Path file = Files.createTempFile("gps", ".csv");

        Files.writeString(
                file,
                """
                DateTime,Latitude,Longitude,Altitude,Speed,Sats
                2026-09-22 23:07:25.000,ABC,-117.4012,2300,65,12
                """
        );

        try (GpsLogReader reader =
                     new GpsLogReader(file)) {

            GpsEntry entry = reader.nextEntry();

            assertFalse(entry.isValid());
        }
    }
}