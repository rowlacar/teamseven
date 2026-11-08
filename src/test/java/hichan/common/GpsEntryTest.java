package hichan.common;

public class GpsEntryTest {

    public static void main(String[] args) {

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

        System.out.println("=== GPS Entry Test ===");

        System.out.println("Line Number:      " + entry.getLineNumber());
        System.out.println("Raw Line:         " + entry.getRawLine());
        System.out.println("Latitude:         " + entry.getLatitude());
        System.out.println("Longitude:        " + entry.getLongitude());
        System.out.println("Elevation:        " + entry.getElevation());
        System.out.println("Date:             " + entry.getDate());
        System.out.println("Time:             " + entry.getTime());
        System.out.println("Speed:            " + entry.getSpeed());
        System.out.println("Satellite Count:  " + entry.getSatelliteCount());
        System.out.println("Valid:            " + entry.isValid());
    }
}