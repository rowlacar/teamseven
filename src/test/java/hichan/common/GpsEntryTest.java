package hichan.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GpsEntryTest {

    @Test
    void constructorStoresAllValues() {

        GpsEntry entry = new GpsEntry(
                1,
                "47.6587,-117.4260,200,20261108,120000,55,8",
                47.6587,
                -117.4260,
                200.0,
                "20261108",
                "120000",
                55.0,
                8,
                true
        );

        assertEquals(1, entry.getLineNumber());

        assertEquals(
                "47.6587,-117.4260,200,20261108,120000,55,8",
                entry.getRawLine());

        assertEquals(
                47.6587,
                entry.getLatitude(),
                0.0001);

        assertEquals(
                -117.4260,
                entry.getLongitude(),
                0.0001);

        assertEquals(
                200.0,
                entry.getElevation(),
                0.0001);

        assertEquals(
                "20261108",
                entry.getDate());

        assertEquals(
                "120000",
                entry.getTime());

        assertEquals(
                55.0,
                entry.getSpeed(),
                0.0001);

        assertEquals(
                8,
                entry.getSatelliteCount());

        assertTrue(
                entry.isValid());
    }

    @Test
    void invalidEntryStoresValidityFlag() {

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
                false
        );

        assertFalse(
                entry.isValid());

        assertEquals(
                "BAD,DATA,LINE",
                entry.getRawLine());
    }
}