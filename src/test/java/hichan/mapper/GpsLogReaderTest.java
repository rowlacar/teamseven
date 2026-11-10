package hichan.mapper;

import hichan.common.GpsEntry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GpsLogReaderTest {

    public static void main(String[] args) {

        try {

            Path testFile =
                    Path.of("gps_reader_test.csv");

            Files.write(
                    testFile,
                    List.of(
                            "latitude,longitude,elevation,date,time,speed",
                            "47.6587,-117.4260,200.0,20261108,120000,55.0",
                            "47.7000,-117.5000,205.0,20261108,120100,60.0",
                            "BAD,DATA,LINE"
                    )
            );

            GpsLogReader reader =
                    new GpsLogReader(testFile);

            GpsEntry entry;

            while ((entry = reader.nextEntry()) != null) {

                System.out.println(
                        "Line Number: "
                                + entry.getLineNumber());

                System.out.println(
                        "Valid: "
                                + entry.isValid());

                System.out.println(
                        "Latitude: "
                                + entry.getLatitude());

                System.out.println(
                        "Longitude: "
                                + entry.getLongitude());

                System.out.println(
                        "Raw Line: "
                                + entry.getRawLine());

                System.out.println(
                        "------------------------");
            }

            reader.close();

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }
}