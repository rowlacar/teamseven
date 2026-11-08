package hichan.mapper;

import hichan.common.ChannelId;
import hichan.common.Direction;

public class ChannelIdBuilderTest {

    public static void main(String[] args) {

        test("WA", Direction.EB, 0);
        test("WA", Direction.EB, 5);
        test("WA", Direction.EB, 10);
        test("WA", Direction.WB, 25);

        test("ID", Direction.EB, 64);
        test("ID", Direction.WB, 103);

        test("WA", Direction.EB, -5);
    }

    private static void test(
            String state,
            Direction direction,
            double mileMarker) {

        ChannelId channelId =
                ChannelIdBuilder.build(
                        state,
                        direction,
                        mileMarker);

        System.out.println(
                "State: " + state
                        + ", Direction: " + direction
                        + ", Mile Marker: " + mileMarker
                        + " -> " + channelId);
    }
}
