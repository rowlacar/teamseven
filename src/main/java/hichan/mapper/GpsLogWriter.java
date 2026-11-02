package hichan.mapper;

import hichan.common.GpsEntry;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GpsLogWriter implements Closeable {

    private final BufferedWriter writer;

    public GpsLogWriter(Path outputFile)
            throws IOException {

        writer = Files.newBufferedWriter(outputFile);
    }

    public void write(
            GpsEntry entry,
            String label)
            throws IOException {

        writer.write(
                entry.getRawLine()
                        + ","
                        + label);

        writer.newLine();
    }

    @Override
    public void close()
            throws IOException {

        writer.close();
    }
}
