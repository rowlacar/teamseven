package hichan.common;

public class ChannelId {

    private final String highway;
    private final String state;
    private final Direction direction;
    private final int channel;

    public ChannelId(
            String highway,
            String state,
            Direction direction,
            int channel) {

        this.highway = highway;
        this.state = state;
        this.direction = direction;
        this.channel = channel;
    }

    public String getHighway() {
        return highway;
    }

    public String getState() {
        return state;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getChannel() {
        return channel;
    }

    @Override
    public String toString() {

        return highway
                + state
                + direction.label()
                + "_"
                + channel;
    }
}