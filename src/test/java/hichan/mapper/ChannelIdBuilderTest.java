package hichan.mapper;

import hichan.common.ChannelId;
import hichan.common.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChannelIdBuilderTest {

    @Test
    void testWaEb0() {
        ChannelId id =
                ChannelIdBuilder.build("WA", Direction.EB, 0);

        assertEquals("I90WAEB_10", id.toString());
    }

    @Test
    void testWaEb5() {
        ChannelId id =
                ChannelIdBuilder.build("WA", Direction.EB, 5);

        assertEquals("I90WAEB_10", id.toString());
    }

    @Test
    void testWaEb10() {
        ChannelId id =
                ChannelIdBuilder.build("WA", Direction.EB, 10);

        assertEquals("I90WAEB_20", id.toString());
    }

    @Test
    void testWaWb25() {
        ChannelId id =
                ChannelIdBuilder.build("WA", Direction.WB, 25);

        assertEquals("I90WAWB_30", id.toString());
    }

    @Test
    void testIdEb64() {
        ChannelId id =
                ChannelIdBuilder.build("ID", Direction.EB, 64);

        assertEquals("I90IDEB_70", id.toString());
    }

    @Test
    void testIdWb103() {
        ChannelId id =
                ChannelIdBuilder.build("ID", Direction.WB, 103);

        assertEquals("I90IDWB_110", id.toString());
    }

    @Test
    void testNegativeMileMarker() {
        ChannelId id =
                ChannelIdBuilder.build("WA", Direction.EB, -5);

        assertEquals("I90WAEB_10", id.toString());
    }
}