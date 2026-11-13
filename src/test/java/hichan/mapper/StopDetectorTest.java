package hichan.mapper;

import hichan.common.ChannelId;
import hichan.common.Direction;
import hichan.common.GpsEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StopDetectorTest {

    @Test
    void repeatedLocationIsDetected() {

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

        ChannelId channelId = new ChannelId(
                "I90",
                "WA",
                Direction.EB,
                10);

        detector.record(entry1, channelId);

        assertTrue(
                detector.isRepeat(
                        entry2,
                        Direction.EB));
    }

    @Test
    void differentLocationIsNotRepeat() {

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

        assertFalse(
                detector.isRepeat(
                        entry3,
                        Direction.EB));
    }

    @Test
    void returnsLastRecordedChannelId() {

        StopDetector detector = new StopDetector();

        GpsEntry entry = new GpsEntry(
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

        ChannelId channelId = new ChannelId(
                "I90",
                "WA",
                Direction.EB,
                10);

        detector.record(entry, channelId);

        assertEquals(
                channelId,
                detector.getLastChannelId());
    }
}