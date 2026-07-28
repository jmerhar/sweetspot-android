package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies the anchored coach-mark bubble placement: above/below flip, clamping, and tail tracking. */
class CoachMarkGeometryTest {

    // A 1000×2000 window; a 200×100 bubble; 10px gap.
    private val windowW = 1000
    private val windowH = 2000
    private val bubbleW = 200
    private val bubbleH = 100
    private val gap = 10f

    @Test
    fun `sits above the target when there is room`() {
        // Target mid-screen: bubble should be above it (tail points down).
        val target = RectPx(left = 400f, top = 1000f, right = 600f, bottom = 1040f)
        val p = CoachMarkGeometry.calloutFor(target, bubbleW, bubbleH, windowW, windowH, gap)
        assertTrue(p.above)
        assertEquals((1000f - gap - bubbleH).toInt(), p.y) // bubble bottom sits `gap` above target top
    }

    @Test
    fun `flips below when the target is near the top`() {
        val target = RectPx(left = 400f, top = 20f, right = 600f, bottom = 60f)
        val p = CoachMarkGeometry.calloutFor(target, bubbleW, bubbleH, windowW, windowH, gap)
        assertTrue(!p.above)
        assertEquals((60f + gap).toInt(), p.y) // bubble top sits `gap` below target bottom
    }

    @Test
    fun `centres on the target horizontally and clamps to the window`() {
        // Centred target → bubble centred (x = 500 - 100 = 400).
        val centred = RectPx(400f, 1000f, 600f, 1040f)
        assertEquals(400, CoachMarkGeometry.calloutFor(centred, bubbleW, bubbleH, windowW, windowH, gap).x)

        // Target hard against the right edge → bubble clamped so it stays fully on screen.
        val rightEdge = RectPx(960f, 1000f, 1000f, 1040f)
        val p = CoachMarkGeometry.calloutFor(rightEdge, bubbleW, bubbleH, windowW, windowH, gap)
        assertEquals(windowW - bubbleW, p.x)
    }

    @Test
    fun `tail tracks the target centre and stays within the bubble`() {
        // Centred: tail at the bubble's middle.
        val centred = RectPx(400f, 1000f, 600f, 1040f)
        assertEquals(bubbleW / 2, CoachMarkGeometry.calloutFor(centred, bubbleW, bubbleH, windowW, windowH, gap).tailCenterX)

        // Target pushed to the right edge (bubble clamped): tail moves toward the bubble's right side,
        // but never past the inset.
        val inset = 12f
        val rightEdge = RectPx(960f, 1000f, 1000f, 1040f)
        val p = CoachMarkGeometry.calloutFor(rightEdge, bubbleW, bubbleH, windowW, windowH, gap, tailInset = inset)
        assertTrue(p.tailCenterX in inset.toInt()..(bubbleW - inset).toInt())
        assertTrue(p.tailCenterX > bubbleW / 2) // shifted toward the target side
    }
}
