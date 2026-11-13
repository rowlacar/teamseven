package hichan.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChannelIdTest {

    @Test
    void channelIdStoresWashingtonEastboundValues() {

        ChannelId channel = new ChannelId(
                "I90",
                "WA",
                Direction.EB,
                10);

        assertEquals("I90", channel.getHighway());
        assertEquals("WA", channel.getState());
        assertEquals(Direction.EB, channel.getDirection());
        assertEquals(10, channel.getChannel());
        assertEquals("I90WAEB_10", channel.toString());
    }

    @Test
    void channelIdStoresIdahoWestboundValues() {

        ChannelId channel = new ChannelId(
                "I90",
                "ID",
                Direction.WB,
                30);

        assertEquals("I90", channel.getHighway());
        assertEquals("ID", channel.getState());
        assertEquals(Direction.WB, channel.getDirection());
        assertEquals(30, channel.getChannel());
        assertEquals("I90IDWB_30", channel.toString());
    }

    @Test
    void toStringProducesCorrectFormat() {

        ChannelId channel = new ChannelId(
                "I90",
                "WA",
                Direction.EB,
                20);

        assertEquals(
                "I90WAEB_20",
                channel.toString());
    }
}
