package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChartGeometryTest {

    // 8 cells (e.g. 2 hours × 4 quarter-hours), all present, 800px tall → 100px per cell.
    private val full = List(8) { true }

    @Test
    fun `maps y to the cell it falls in`() {
        assertEquals(0, ChartGeometry.selectedCell(50f, 800, full))    // 0–100 → cell 0
        assertEquals(3, ChartGeometry.selectedCell(350f, 800, full))   // 300–400 → cell 3
        assertEquals(7, ChartGeometry.selectedCell(750f, 800, full))   // 700–800 → cell 7
    }

    @Test
    fun `clamps out-of-bounds y to the first and last cell`() {
        assertEquals(0, ChartGeometry.selectedCell(-20f, 800, full))
        assertEquals(7, ChartGeometry.selectedCell(9999f, 800, full))
    }

    @Test
    fun `snaps an empty leading cell to the nearest present one`() {
        // Partial first hour: first two quarter-hours missing (present from cell 2).
        val present = listOf(false, false, true, true, true, true, true, true)
        assertEquals(2, ChartGeometry.selectedCell(10f, 800, present))   // top lands on empty → cell 2
        assertEquals(2, ChartGeometry.selectedCell(150f, 800, present))  // cell 1 empty → nearest present 2
        assertEquals(4, ChartGeometry.selectedCell(450f, 800, present))  // present cell returned as-is
    }

    @Test
    fun `empty tie prefers the earlier present cell`() {
        // Cells 0 and 2 present, 1 empty; a hit on cell 1 is equidistant → prefer the earlier (0).
        val present = listOf(true, false, true)
        assertEquals(0, ChartGeometry.selectedCell(150f, 300, present))  // 100–200 → cell 1 → tie → 0
    }

    @Test
    fun `hourly grid selects the hour`() {
        val present = List(4) { true }  // slotsPerHour == 1 → one cell per hour
        assertEquals(2, ChartGeometry.selectedCell(550f, 800, present))  // 400–600 → cell 2
    }

    @Test
    fun `tooltip sits a gap above the finger when there is room`() {
        // finger at 1000, gap 200, tooltip 300 → top = 1000 - 200 - 300 = 500.
        assertEquals(500, ChartGeometry.tooltipTopY(fingerY = 1000f, gapPx = 200f, tooltipHeight = 300, windowHeight = 2400))
    }

    @Test
    fun `tooltip flips below the finger when too close to the top`() {
        // finger at 100, gap 200, tooltip 300 → above would be -400 (off top) → below: 100 + 200 = 300.
        assertEquals(300, ChartGeometry.tooltipTopY(fingerY = 100f, gapPx = 200f, tooltipHeight = 300, windowHeight = 2400))
    }

    @Test
    fun `tooltip is clamped to stay within the window bottom`() {
        // Short window: below placement (260) would leave the tooltip's bottom past 350 → clamp to 50.
        assertEquals(50, ChartGeometry.tooltipTopY(fingerY = 60f, gapPx = 200f, tooltipHeight = 300, windowHeight = 350))
    }

    @Test
    fun `returns null for no height or no present cell`() {
        assertNull(ChartGeometry.selectedCell(100f, 0, full))
        assertNull(ChartGeometry.selectedCell(100f, 800, emptyList()))
        assertNull(ChartGeometry.selectedCell(100f, 800, List(4) { false }))
    }
}
