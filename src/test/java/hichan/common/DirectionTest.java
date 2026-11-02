package hichan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Direction}. */
class DirectionTest {

  @Test
  void testFN6_11_LabelsMatchChannelIdFormat() {
    assertEquals("EB", Direction.EB.label());
    assertEquals("WB", Direction.WB.label());
    assertEquals("??", Direction.UNKNOWN.label());
  }
}
