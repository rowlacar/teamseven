package hichan.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared geospatial helpers for the Preprocessor and Channel Mapper (Detailed Design 3.13).
 * Both programs must use this one class so their geohashes always match (risk R2).
 */
public final class GeoUtils {

  private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

  private GeoUtils() {
    // Static helpers only; not instantiable.
  }

  /**
   * Returns the geohash for a point (Detailed Design 6.1).
   *
   * @param lat latitude in decimal degrees
   * @param lon longitude in decimal degrees
   * @param precision number of characters, for example 6
   */
  public static String encodeGeohash(double lat, double lon, int precision) {
    double minLat = -90;
    double maxLat = 90;
    double minLon = -180;
    double maxLon = 180;
    StringBuilder hash = new StringBuilder(precision);
    boolean evenBit = true; // geohash bits alternate, starting with longitude
    int bit = 0;
    int ch = 0;

    while (hash.length() < precision) {
      if (evenBit) {
        double mid = (minLon + maxLon) / 2;
        if (lon >= mid) {
          ch = (ch << 1) | 1;
          minLon = mid;
        } else {
          ch = ch << 1;
          maxLon = mid;
        }
      } else {
        double mid = (minLat + maxLat) / 2;
        if (lat >= mid) {
          ch = (ch << 1) | 1;
          minLat = mid;
        } else {
          ch = ch << 1;
          maxLat = mid;
        }
      }
      evenBit = !evenBit;

      if (++bit == 5) { // every 5 bits becomes one base-32 character
        hash.append(BASE32.charAt(ch));
        bit = 0;
        ch = 0;
      }
    }
    return hash.toString();
  }

  /**
   * Returns the bounding box of a geohash cell as {minLat, minLon, maxLat, maxLon}.
   */
  private static double[] decodeBounds(String geohash) {
    double minLat = -90;
    double maxLat = 90;
    double minLon = -180;
    double maxLon = 180;
    boolean evenBit = true;

    for (char c : geohash.toCharArray()) {
      int value = BASE32.indexOf(c);
      if (value < 0) {
        throw new IllegalArgumentException("Invalid geohash: " + geohash);
      }
      for (int i = 4; i >= 0; i--) {
        int b = (value >> i) & 1;
        if (evenBit) {
          double mid = (minLon + maxLon) / 2;
          if (b == 1) {
            minLon = mid;
          } else {
            maxLon = mid;
          }
        } else {
          double mid = (minLat + maxLat) / 2;
          if (b == 1) {
            minLat = mid;
          } else {
            maxLat = mid;
          }
        }
        evenBit = !evenBit;
      }
    }
    return new double[] {minLat, minLon, maxLat, maxLon};
  }

  /**
   * Returns the 8 cells surrounding a geohash, in the order
   * N, NE, E, SE, S, SW, W, NW (Detailed Design 6.2).
   */
  public static List<String> neighbors(String geohash) {
    double[] b = decodeBounds(geohash);
    double latSize = b[2] - b[0];
    double lonSize = b[3] - b[1];
    double centerLat = (b[0] + b[2]) / 2;
    double centerLon = (b[1] + b[3]) / 2;
    int precision = geohash.length();

    // {latitude step, longitude step} for each direction
    int[][] steps = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
    List<String> result = new ArrayList<>(8);
    for (int[] s : steps) {
      result.add(encodeGeohash(
          centerLat + s[0] * latSize, centerLon + s[1] * lonSize, precision));
    }
    return result;
  }

  /**
   * Returns the great-circle distance in meters between two points (haversine formula).
   */
  public static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    return 2 * Config.EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
  }

  /**
   * Projects a point to flat x/y meters around a reference latitude (equirectangular).
   * Accurate over short distances; used for point-to-line math (Detailed Design 6.4).
   *
   * @return {x, y} in meters, x east and y north
   */
  public static double[] toLocalXY(double lat, double lon, double refLat) {
    double x = Math.toRadians(lon) * Math.cos(Math.toRadians(refLat)) * Config.EARTH_RADIUS_METERS;
    double y = Math.toRadians(lat) * Config.EARTH_RADIUS_METERS;
    return new double[] {x, y};
  }
}
