package hichan.mapper;

import hichan.common.ChannelId;
import hichan.common.Direction;
import hichan.common.Config;

public final class ChannelIdBuilder {

    private ChannelIdBuilder() {
        // Utility class
    }

    public static ChannelId build(
            String state,
            Direction direction,
            double mileMarker) {

        int channel = toChannel(mileMarker);

        return new ChannelId(
                Config.HIGHWAY,
                state,
                direction,
                channel);
    }

    public static int toChannel(double mileMarker) {

        if (mileMarker < 0) {
            mileMarker = 0;
        }

        return ((int) (mileMarker / Config.CHANNEL_WIDTH) + 1)
                * Config.CHANNEL_WIDTH;
    }
}