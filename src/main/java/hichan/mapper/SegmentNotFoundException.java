package hichan.mapper;

/**
 * Thrown when no matching I-90 segment can be found.
 */
public class SegmentNotFoundException extends Exception {

    public SegmentNotFoundException(String message) {
        super(message);
    }
}