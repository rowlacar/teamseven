package hichan.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SegmentNotFoundExceptionTest {

    @Test
    void exceptionStoresMessage() {

        SegmentNotFoundException exception =
                new SegmentNotFoundException(
                        "No matching segment found.");

        assertEquals(
                "No matching segment found.",
                exception.getMessage());
    }

    @Test
    void exceptionCanBeThrown() {

        SegmentNotFoundException exception =
                assertThrows(
                        SegmentNotFoundException.class,
                        () -> {
                            throw new SegmentNotFoundException(
                                    "No matching segment found.");
                        });

        assertEquals(
                "No matching segment found.",
                exception.getMessage());
    }
}