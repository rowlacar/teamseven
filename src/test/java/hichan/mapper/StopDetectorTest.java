package hichan.mapper;

import hichan.common.ChannelId;
import hichan.common.Direction;
import hichan.common.GpsEntry;

public class StopDetectorTest {

    public static void main(String[] args) {

        StopDetector detector = new StopDetector();

        GpsEntry entry1 = new GpsEntry(
                1,
                "raw1",
                47.6587,
                -117.4260,
                200,
                "20261108",
                "120000",
                55,
                8,
                true);

        GpsEntry entry2 = new GpsEntry(
                2,
                "raw2",
                47.6587,
                -117.4260,
                200,
                "20261108",
                "120100",
                55,
                8,
                true);

        GpsEntry entry3 = new GpsEntry(
                3,
                "raw3",
                47.7000,
                -117.5000,
                205,
                "20261108",
                "120200",
                60,
                8,
                true);

        ChannelId channelId = new ChannelId(
                "I90",
                "WA",
                Direction.EB,
                10);

        detector.record(entry1, channelId);

        System.out.println(
                "Entry 2 repeat: "
                        + detector.isRepeat(
                        entry2,
                        Direction.EB));

        System.out.println(
                "Entry 3 repeat: "
                        + detector.isRepeat(
                        entry3,
                        Direction.EB));

        System.out.println(
                "Last Channel ID: "
                        + detector.getLastChannelId());
    }
}