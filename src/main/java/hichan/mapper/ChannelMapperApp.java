package hichan.mapper;

import hichan.common.ChannelId;
import hichan.common.Config;
import hichan.common.Direction;
import hichan.common.GpsEntry;
import hichan.common.Location;

import java.nio.file.Path;

public class ChannelMapperApp {

    public static void main(String[] args) {

        if (args.length != 3) {

            System.err.println(
                    "Usage: hichan.jar <gpsLog> <lookupFile> <outputFile>");

            System.exit(1);
        }

        int processed = 0;
        int invalid = 0;

        try {

            LookupIndex lookupIndex =
                    LookupIndex.load(Path.of(args[1]));

            SegmentLocator locator =
                    new SegmentLocator(lookupIndex);

            DirectionTracker directionTracker =
                    new DirectionTracker();

            StopDetector stopDetector =
                    new StopDetector();

            try (
                    GpsLogReader reader =
                            new GpsLogReader(Path.of(args[0]));

                    GpsLogWriter writer =
                            new GpsLogWriter(Path.of(args[2]))
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