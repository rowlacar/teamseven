package hichan.common;

public class ChannelIdTest {

    public static void main(String[] args) {

        ChannelId channel1 =
                new ChannelId(
                        "I90",
                        "WA",
                        Direction.EB,
                        10);

        ChannelId channel2 =
                new ChannelId(
                        "I90",
                        "ID",
                        Direction.WB,
                        30);

        System.out.println("=== Channel ID Test ===");

        printChannel(channel1);
        printChannel(channel2);
    }

    private static void printChannel(ChannelId channel) {

        System.out.println("Highway: " + channel.getHighway());
        System.out.println("State: " + channel.getState());
        System.out.println("Direction: " + channel.getDirection());
        System.out.println("Channel: " + channel.getChannel());
        System.out.println("Channel ID: " + channel);
        System.out.println();
    }
}