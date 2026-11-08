package hichan.mapper;

import hichan.common.ChannelId;
import hichan.common.Direction;
import hichan.common.GpsEntry;

public class StopDetector {

    private GpsEntry lastEntry;
    private ChannelId lastChannelId;

    public boolean isRepeat(
            GpsEntry entry,
            Direction currentDirection) {

        if (lastEntry == null) {
            return false;
        }

        if (currentDirection == Direction.UNKNOWN) {
            return false;
        }

        return Double.compare(
                lastEntry.getLatitude(),
                entry.getLatitude()) == 0
                &&
                Double.compare(
                        lastEntry.getLongitude(),
                        entry.getLongitude()) == 0;
    }

    public void record(
            GpsEntry entry,
            ChannelId channelId) {

        this.lastEntry = entry;
        this.lastChannelId = channelId;
    }

    public ChannelId getLastChannelId() {
        return lastChannelId;
    }
}