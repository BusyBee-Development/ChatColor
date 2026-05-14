package net.busybee.chatcolor.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ColorUtilTest {

    @Test
    void testStripLegacy() {
        assertEquals("Test", ColorUtil.stripLegacy("§aTest"));
        assertEquals("Test", ColorUtil.stripLegacy("§lTest"));
        // Bungee hex color: §x§F§F§A§A§0§0
        assertEquals("Test", ColorUtil.stripLegacy("§x§F§F§A§A§0§0Test"));
        assertEquals("Hello World", ColorUtil.stripLegacy("§6Hello §x§1§2§3§4§5§6World"));
    }
}
