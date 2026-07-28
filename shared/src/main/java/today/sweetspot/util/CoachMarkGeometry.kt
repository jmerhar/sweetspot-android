package today.sweetspot.util

import kotlin.math.roundToInt

/** A target control's bounds in window pixels (the control a coach mark points at). */
data class RectPx(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    /** Horizontal centre of the target, in window pixels. */
    val centerX: Float get() = (left + right) / 2f
}

/**
 * Where to draw a coach-mark bubble and its tail.
 *
 * @property x Bubble left, in window pixels.
 * @property y Bubble top, in window pixels.
 * @property above `true` when the bubble sits above the target (tail points down), `false` below.
 * @property tailCenterX Tail centre, in pixels measured from the bubble's left edge.
 */
data class CalloutPlacement(val x: Int, val y: Int, val above: Boolean, val tailCenterX: Int)

/**
 * Pure placement geometry for the anchored coach-mark callout — the two-axis, tailed sibling of
 * [ChartGeometry.tooltipTopY].
 *
 * The bubble is preferred **above** the target (a [gapPx] gap), flipping **below** when it wouldn't fit
 * above, and is clamped to stay fully within the window on both axes. The tail points at the target's
 * horizontal centre, clamped to remain within the bubble (minus [tailInset]) so it can't slide off a
 * corner when the bubble is pushed sideways by the edge clamp.
 */
object CoachMarkGeometry {

    /**
     * @param target The anchored control's bounds, in window pixels.
     * @param bubbleW Measured bubble width in pixels.
     * @param bubbleH Measured bubble height in pixels.
     * @param windowW Window width in pixels.
     * @param windowH Window height in pixels.
     * @param gapPx Gap between the target and the bubble's near edge.
     * @param tailInset Minimum distance the tail centre keeps from the bubble's left/right edges.
     */
    fun calloutFor(
        target: RectPx,
        bubbleW: Int,
        bubbleH: Int,
        windowW: Int,
        windowH: Int,
        gapPx: Float,
        tailInset: Float = 0f
    ): CalloutPlacement {
        val aboveTop = target.top - gapPx - bubbleH
        val above = aboveTop >= 0f
        val yRaw = if (above) aboveTop else target.bottom + gapPx
        val maxTop = (windowH - bubbleH).coerceAtLeast(0).toFloat()
        val y = yRaw.coerceIn(0f, maxTop)

        val maxLeft = (windowW - bubbleW).coerceAtLeast(0).toFloat()
        val x = (target.centerX - bubbleW / 2f).coerceIn(0f, maxLeft)

        val tailMax = (bubbleW - tailInset).coerceAtLeast(tailInset)
        val tail = (target.centerX - x).coerceIn(tailInset, tailMax)

        return CalloutPlacement(x.roundToInt(), y.roundToInt(), above, tail.roundToInt())
    }
}
