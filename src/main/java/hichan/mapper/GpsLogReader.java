package hichan.mapper;

import hichan.common.GpsEntry;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GpsLogReader implements Closeable {

    private final BufferedReader reader;
    private final String fileName;

    private int lineNumber = 0;
    private boolean headerSkipped = false;

    public GpsLogReader(Path logFile)
            throws IOException {

        this.reader = Files.newBufferedReader(logFile);
        this.fileName = logFile.getFileName().toString();
    }

    public GpsEntry nextEntry()
            throws IOException {

        String line;

        while ((line = reader.readLine()) != null) {

            lineNumber++;

            if (!headerSkipped) {
                headerSkipped = true;
                continue;
            }

            if (line.isBlank()) {
                continue;
            }

            String[] fields = line.split(",");

            if (fields.length != 6) {

                System.err.printf(
                        "ERROR: %s, line %d: expected 6 fields, found %d%n",
                        fileName,
                        lineNumber,
                        fields.length);

                return invalidEntry(line);
            }

            try {

                double latitude =
                        Double.parseDouble(fields[0].trim());

                double longitude =
                        Double.parseDouble(fields[1].trim());

                double elevation =
                        Double.parseDouble(fields[2].trim());

                String date =
                        fields[3].trim();

                String time =
                        fields[4].trim();

                double speed =
                        Double.parseDouble(fields[5].trim());

                if (latitude < -90 || latitude > 90) {

                    System.err.printf(
                            "ERROR: %s, line %d: latitude out of range%n",
                            fileName,
                            lineNumber);

                    return invalidEntry(line);
                }

                if (longitude < -180 || longitude > 180) {

                    System.err.printf(
                            "ERROR: %s, line %d: longitude out of range%n",
                            fileName,
                            lineNumber);

                    return invalidEntry(line);
                }

                return new GpsEntry(
                        lineNumber,
                        line,
                        latitude,
                        longitude,
                        elevation,
                        date,
                        time,
                        speed,
                        0,
                        true);

            } catch (NumberFormatException ex) {

                System.err.printf(
                        "ERROR: %s, line %d: invalid numeric value%n",
                        fileName,
                        lineNumber);

                return invalidEntry(line);
            }
        }

        return null;
    }

    private GpsEntry invalidEntry(String rawLine) {

        return new GpsEntry(
                lineNumber,
                rawLine,
                0,
                0,
                0,
                "",
                "",
                0,
                0,
                false);
    }

    @Override
    public void close()
            throws IOException {

        reader.close();
    }
}