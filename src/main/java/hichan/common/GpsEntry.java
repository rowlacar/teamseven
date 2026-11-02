package hichan.common;

public class GpsEntry {

    private final int lineNumber;
    private final String rawLine;

    private final double latitude;
    private final double longitude;
    private final double elevation;

    private final String date;
    private final String time;

    private final double speed;
    private final int satelliteCount;

    private final boolean valid;

    public GpsEntry(
            int lineNumber,
            String rawLine,
            double latitude,
            double longitude,
            double elevation,
            String date,
            String time,
            double speed,
            int satelliteCount,
            boolean valid) {

        this.lineNumber = lineNumber;
        this.rawLine = rawLine;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.date = date;
        this.time = time;
        this.speed = speed;
        this.satelliteCount = satelliteCount;
        this.valid = valid;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getRawLine() {
        return rawLine;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getElevation() {
        return elevation;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public double getSpeed() {
        return speed;
    }

    public int getSatelliteCount() {
        return satelliteCount;
    }

    public boolean isValid() {
        return valid;
    }
}
