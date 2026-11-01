package hichan.preprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests for StateMapper (FN6.3). */
class StateMapperTest {

    // The object under test
    private final StateMapper mapper = new StateMapper();

    @Test
    void testFN6_3_WashingtonCode() {
        // 53 is Washington's FIPS code
        assertEquals("WA", mapper.toStateCode(53));
    }

    @Test
    void testFN6_3_IdahoCode() {
        // 16 is Idaho's FIPS code
        assertEquals("ID", mapper.toStateCode(16));
    }

    @Test
    void testFN6_3_UnknownCodeThrows() {
        // 41 is Oregon, which is out of scope, so it should be rejected
        assertThrows(IllegalArgumentException.class, () -> mapper.toStateCode(41));
    }
}