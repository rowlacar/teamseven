package hichan.mapper;

import hichan.common.ChannelId;
import hichan.common.Config;
import hichan.common.Direction;
import hichan.common.GpsEntry;
import hichan.common.Location;

import java.nio.file.Path;

public class ChannelMapperApp {

    static void validatePaths(
            Path inputPath,
            Path outputPath) {

        if (inputPath.toAbsolutePath().normalize()
                .equals(outputPath.toAbsolutePath().normalize())) {

            throw new IllegalArgumentException(
                    "Output file must be different from input file.");
        }
    }

    public static void main(String[] args) {

        if (args.length != 3) {

            System.err.println(
                    "Usage: hichan.jar <gpsLog> <lookupFile> <outputFile>");

            System.exit(1);
        }

        Path gpsPath = Path.of(args[0]);
        Path lookupPath = Path.of(args[1]);
        Path outputPath = Path.of(args[2]);

        validatePaths(gpsPath, outputPath);

        int processed = 0;
        int invalid = 0;

        try {

            LookupIndex lookupIndex =
                    LookupIndex.load(lookupPath);

            SegmentLocator locator =
                    new SegmentLocator(lookupIndex);

            DirectionTracker directionTracker =
                    new DirectionTracker();

            StopDetector stopDetector =
                    new StopDetector();

            try (
                    GpsLogReader reader =
                            new GpsLogReader(gpsPath);

                    GpsLogWriter writer =
                            new GpsLogWriter(outputPath)
            ) {

                GpsEntry entry;

                while ((entry = reader.nextEntry()) != null) {

                    processed++;

                    if (!entry.isValid()) {

                        invalid++;

                        writer.write(
                                entry,
                                Config.INVALID_LABEL);

                        continue;
                    }

                    try {

                        Location location =
                                locator.locate(entry);

                        Direction direction =
                                directionTracker.update(location);

                        ChannelId channelId;

                        if (stopDetector.isRepeat(
                                entry,
                                direction)) {

                            channelId =
                                    stopDetector.getLastChannelId();

                        } else {

                            channelId =
                                    ChannelIdBuilder.build(
                                            location.getState(),
                                            direction,
                                            location.getMileMarker());

                            stopDetector.record(
                                    entry,
                                    channelId);
                        }

                        writer.write(
                                entry,
                                channelId.toString());

                    } catch (SegmentNotFoundException ex) {

                        invalid++;

                        writer.write(
                                entry,
                                Config.INVALID_LABEL);
                    }
                }
            }

            System.out.printf(
                    "Processed %,d entries (%d invalid)%n",
                    processed,
                    invalid);

        } catch (Exception ex) {

            System.err.println(
                    "ERROR: " + ex.getMessage());

            System.exit(2);
        }
    }
}