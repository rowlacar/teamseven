package hichan.mapper;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ChannelMapperAppTest {

    @Test
    void acceptsDifferentPaths() {

        assertDoesNotThrow(
                () -> ChannelMapperApp.validatePaths(
                        Path.of("gps.csv"),
                        Path.of("output.csv"))
        );
    }

    @Test
    void rejectsSamePaths() {

        assertThrows(
                IllegalArgumentException.class,
                () -> ChannelMapperApp.validatePaths(
                        Path.of("gps.csv"),
                        Path.of("gps.csv"))
        );
    }
}
