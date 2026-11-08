package hichan.mapper;

import hichan.common.GpsEntry;

import java.nio.file.Path;

public class GpsLogWriterTest {

    public static void main(String[] args) {

        try {

            Path outputFile =
                    Path.of("gps_writer_test.csv");

            GpsLogWriter writer =
                    new GpsLogWriter(outputFile);

            GpsEntry entry1 =
                    new GpsEntry(
                            1,
                            "47.6587,-117.4260",
                            47.6587,
                            -117.4260,
                            200.0,
                            "20261108",
                            "120000",
                            55.0,
                            8,
                            true);

            GpsEntry entry2 =
                    new GpsEntry(
                            2,
                            "47.7000,-117.5000",
                            47.7000,
                            -117.5000,
                            205.0,
                            "20261108",
                            "120100",
                            60.0,
                            7,
                            true);

            writer.write(entry1, "I90WAEB_10");
            writer.write(entry2, "I90WAEB_20");

            writer.close();

            System.out.println(
                    "Test completed successfully.");
            System.out.println(
                    "Created file: gps_writer_test.csv");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}