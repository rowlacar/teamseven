# Hi_Chan: Highway Channel Mapper

**CSCD 350 Software Development Principles, Fall 2026: Team Seven**
Malachi McCormick, Justus Danielli, Carl Rowland

Hi_Chan labels each entry in a truck's GPS log with a **Channel ID** that identifies the highway, state, direction of travel, and 10-mile stretch of I-90 the truck was on, for example `I90WAEB_290`.

The system has two programs:

| Program | Jar | Runs | Purpose |
| --- | --- | --- | --- |
| **Preprocessor** | `preprocessor.jar` | Once | Converts NHPN highway data into an indexed lookup file (`data/lookup.csv`) |
| **Channel Mapper** | `hichan.jar` | Once per GPS log | Uses the lookup file to append a Channel ID to every line of a GPS log |

Scope: I-90 in Washington and Idaho only. Hi_Chan runs entirely offline and sends no data over a network.

---

## Requirements

- Java 17 or newer
- Apache Maven 3.8 or newer (downloads Gson and JUnit 5 on the first build)

## Project layout

```
pom.xml
data/
  nhpn_i90.json          NHPN highway data, filtered to I-90 in WA and ID (Preprocessor input)
  lookup.csv             geohash lookup file (Preprocessor output, Channel Mapper input)
logs/
  westbound.csv          sample GPS log, westbound
  eastbound.csv          sample GPS log, eastbound
output/                  Channel Mapper output goes here
src/main/java/hichan/
  common/                shared classes: Config, GeoUtils, Segment, Location, Direction, GpsEntry, ChannelId
  preprocessor/          PreprocessorApp, SegmentReader, StateMapper, GeohashIndexer, LookupFileWriter
  mapper/                ChannelMapperApp, LookupIndex, GpsLogReader, StopDetector, SegmentLocator,
                         MileMarkerEstimator, DirectionTracker, ChannelIdBuilder, GpsLogWriter,
                         SegmentNotFoundException
src/test/java/hichan/    unit tests, mirroring the packages above
```

## Building and testing

From the project root:

```
mvn test        # compile and run all unit tests
mvn package     # build target/preprocessor.jar and target/hichan.jar
```

Both jars include their dependencies, so they run with a plain `java -jar`.

---

## Step 1: Run the Preprocessor

```
java -jar target/preprocessor.jar data/nhpn_i90.json data/lookup.csv
```

| Argument | Description |
| --- | --- |
| 1 | NHPN JSON file (GeoJSON FeatureCollection) |
| 2 | Path for the lookup file to write |

On success it prints a summary:

```
Wrote 194 segments in 976 cells to data/lookup.csv
```

The Preprocessor only needs to run again when the NHPN data changes. One lookup file serves every Channel Mapper run.

## Step 2: Run the Channel Mapper

```
java -jar target/hichan.jar <gps log> <lookup file> <output file>
```

Example:

```
java -jar target/hichan.jar logs/westbound.csv data/lookup.csv output/westbound_channels.csv
```

| Argument | Description |
| --- | --- |
| 1 | GPS log to process |
| 2 | Lookup file written by the Preprocessor |
| 3 | Output file to write (use a path in `output/` that differs from the input) |

Put quotes around any path that contains spaces, for example `"logs/20260922-131715 - I90WB.csv"`.

On success it prints a summary:

```
Processed 124 entries (0 invalid)
```

---

## File formats

### GPS log (Channel Mapper input)

Comma-separated, one entry per line, 7 fields. Blank lines are skipped.

| # | Field | Type | Notes |
| --- | --- | --- | --- |
| 1 | Latitude | decimal degrees | −90 to 90 |
| 2 | Longitude | decimal degrees | −180 to 180 |
| 3 | Elevation | number | As recorded |
| 4 | Date | text | Copied through, not used in calculations |
| 5 | Time | text | Copied through, not used in calculations |
| 6 | Speed | number | Copied through, not used in calculations |
| 7 | Satellite count | integer | Number of satellites in the fix |

Example: `47.65880,-117.42600,610.2,2026-09-22,13:17:15,62.1,9`

### Lookup file (`data/lookup.csv`)

Comma-separated, no header, sorted by geohash. Each row pairs one geohash cell with one segment that passes through it, so a segment appears once for each cell it crosses.

| # | Field | Description |
| --- | --- | --- |
| 1 | geohash | 6-character cell key (about 1.2 km × 0.6 km) |
| 2 | segmentId | Segment number assigned by the Preprocessor |
| 3 | state | `WA` or `ID` |
| 4 | begMp | Begin mile marker |
| 5 | endMp | End mile marker |
| 6 | coordinates | Segment points from west to east, as `lat lon` pairs separated by `;` |

### Output log

Each input line is copied unchanged, with the Channel ID appended as an 8th field. The output always has exactly one line for each input entry, in the same order.

Example: `47.65880,-117.42600,610.2,2026-09-22,13:17:15,62.1,9,I90WAWB_290`

---

## Channel IDs

Format: `I90` + state + direction + `_` + channel

| Part | Values |
| --- | --- |
| State | `WA` or `ID` |
| Direction | `EB` (eastbound), `WB` (westbound), or `??` (not yet known) |
| Channel | The upper end of the 10-mile range containing the mile marker: 10, 20, 30, … |

Mile markers map to channels like this: 0.0 → 10, 7.3 → 10, 10.0 → 10, 10.1 → 20, 289.6 → 290.

How the Channel Mapper decides each part:

- **Location.** Each GPS point is matched to the nearest I-90 segment in its geohash cell and the 8 surrounding cells. If no segment is within 500 m, the point is treated as off I-90.
- **Direction.** Direction comes from the trend in the last 3 mile markers: rising means EB, falling means WB. Until a trend is established, direction is `??`, so the first entries of a log usually show `??`.
- **State line.** Mile markers restart at the WA/ID border. When the state changes, the mile-marker history is cleared but the current direction is kept.
- **Stops.** If the truck reports the same position as its previous entry and the direction is known, the previous Channel ID is reused.
- **Invalid lines.** A line that fails validation, or that is not on I-90, gets the label `INVALID` and still appears in the output.

---

## Messages and exit codes

Errors go to standard error and summaries go to standard output. Messages are plain text.

| Exit code | Meaning |
| --- | --- |
| 0 | Success |
| 1 | Usage error (wrong number of arguments) |
| 2 | File error (missing, unreadable, or unwritable file) |
| 3 | Internal error (Preprocessor only) |

Typical messages:

```
ERROR: data/nhpn_i90.json: cannot read file
ERROR: westbound.csv, line 12: expected 7 fields, found 6
ERROR: westbound.csv, line 40: latitude out of range
WARNING: feature 17: BEGMP > ENDMP, swapping
```
