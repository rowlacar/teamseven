package hichan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for GeoUtils. Expected values were checked against an independent geohash library. */
class GeoUtilsTest {

    @Test
    void testFN6_2_GeohashSpokaneExample() {
        // The example point from Detailed Design 4.2
        assertEquals("c2krpu", GeoUtils.encodeGeohash(47.65880, -117.42600, 6));
    }

    @Test
    void testFN6_2_GeohashStandardExample() {
        // The well-known reference point used in geohash documentation
        assertEquals("u4pruydqqvj", GeoUtils.encodeGeohash(57.64911, 10.40744, 11));
    }

    @Test
    void testFN6_2_GeohashAtStateLine() {
        // First point of Idaho I-90 in our NHPN data
        assertEquals("c2kz2d", GeoUtils.encodeGeohash(47.6966260000899, -117.041693999878, 6));
    }

    @Test
    void testNF7_2_NeighborsOfSpokaneCell() {
        // N, NE, E, SE, S, SW, W, NW
        List<String> expected = List.of(
                "c2krpv", "c2kx0j", "c2kx0h", "c2kx05", "c2krpg", "c2krpe", "c2krps", "c2krpt");
        assertEquals(expected, GeoUtils.neighbors("c2krpu"));
    }

    @Test
    void testDecodeBoundsContainsPoint() {
        double[] b = GeoUtils.decodeBounds("c2krpu");
        double lat = 47.65880;
        double lon = -117.42600;
        assertEquals(true, lat >= b[0] && lat <= b[2] && lon >= b[1] && lon <= b[3]);
    }

    @Test
    void testDecodeBoundsRejectsBadCharacter() {
        // 'a' is not in the geohash alphabet
        assertThrows(IllegalArgumentException.class, () -> GeoUtils.decodeBounds("c2kra"));
    }

    @Test
    void testDistanceBetweenNearbyPoints() {
        // Two points from the lookup-file example in Detailed Design 4.2
        double d = GeoUtils.distanceMeters(47.65880, -117.42600, 47.66020, -117.39100);
        assertEquals(2625.9, d, 0.1);
    }

    @Test
    void testDistanceOfOneDegreeLatitude() {
        // One degree of latitude is about 111,195 m on a 6,371 km sphere
        assertEquals(111_195, GeoUtils.distanceMeters(47, -117, 48, -117), 1);
    }
}