package hichan.preprocessor;

import hichan.common.Segment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Command-line entry point for the Preprocessor (FN6.4, Detailed Design 3.1).
 *
 * <p>Usage: java -jar preprocessor.jar data/nhpn_i90.json data/lookup.csv
 *
 * <p>Exit codes: 0 success, 1 usage error, 2 file error, 3 internal error.
 */
public class PreprocessorApp {

    static final int EXIT_OK = 0;
    static final int EXIT_USAGE = 1;
    static final int EXIT_FILE = 2;
    static final int EXIT_INTERNAL = 3;

    /** Runs the Preprocessor and exits with its result code. */
    public static void main(String[] args) {
        System.exit(run(args));
    }

    /** Runs the read, map state, index, write pipeline and returns the exit code. */
    static int run(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar preprocessor.jar <nhpn json file> <lookup output file>");
            return EXIT_USAGE;
        }
        Path inFile = Paths.get(args[0]);
        Path outFile = Paths.get(args[1]);

        if (!Files.isReadable(inFile)) {
            System.err.println("ERROR: " + inFile + ": cannot read file");
            return EXIT_FILE;
        }
        if (inFile.toAbsolutePath().normalize().equals(outFile.toAbsolutePath().normalize())) {
            System.err.println("ERROR: output path must differ from input path (NF7.9)");
            return EXIT_FILE;
        }

        try {
            List<Segment> segments = new SegmentReader().readSegments(inFile);

            StateMapper stateMapper = new StateMapper();
            for (Segment segment : segments) {
                segment.setState(stateMapper.toStateCode(segment.getStateFips()));
            }

            Map<String, List<Segment>> index = new GeohashIndexer().buildIndex(segments);
            if (index.isEmpty()) {
                System.err.println("WARNING: no I-90 segments found; lookup file will be empty");
            }

            new LookupFileWriter().write(index, outFile);
            System.out.println("Wrote " + segments.size() + " segments in " + index.size()
                    + " cells to " + outFile);
            return EXIT_OK;
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            return EXIT_FILE;
        } catch (RuntimeException e) {
            System.err.println("ERROR: internal error: " + e);
            return EXIT_INTERNAL;
        }
    }
}