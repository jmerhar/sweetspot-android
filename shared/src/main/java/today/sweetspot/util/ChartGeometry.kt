package today.sweetspot.util

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pure hit-test geometry for the price bar chart's press-and-hold tooltip.
 *
 * The chart lays every hour out as an equal-height row, and pads each row to the same number of
 * sub-slots ([slotsPerHour]) so the whole chart is a uniform vertical grid of equal-height cells,
 * top-to-bottom in time order. A partial first/last hour leaves some cells empty (no slot).
 */
object ChartGeometry {

    /**
     * Maps a vertical pointer position to the flat cell (sub-slot) it falls on.
     *
     * The cell is chosen by uniform division of [totalHeightPx] into `present.size` bands. If that
     * cell is empty (a padding gap from a partial hour), the nearest present cell is returned instead,
     * so dragging never lands on "nothing". Ties prefer the earlier (smaller-index) cell.
     *
     * @param y Pointer y within the rows container, in pixels.
     * @param totalHeightPx Height of the rows container, in pixels.
     * @param present One flag per grid cell in top-to-bottom order; `true` when a slot occupies it.
     * @return The index of the selected present cell, or `null` when the container has no height or
     *   no cell is present.
     */
    fun selectedCell(y: Float, totalHeightPx: Int, present: List<Boolean>): Int? {
        if (totalHeightPx <= 0 || present.isEmpty()) return null
        val count = present.size
        val raw = (y / totalHeightPx * count).toInt().coerceIn(0, count - 1)
        if (present[raw]) return raw

        // Nearest present cell to `raw`, preferring the earlier one on a tie.
        var best: Int? = null
        for (i in present.indices) {
            if (!present[i]) continue
            if (best == null || abs(i - raw) < abs(best - raw)) best = i
        }
        return best
    }

    /**
     * Top y (in window pixels) for the press-and-hold tooltip, placed a [gapPx] gap **above** the
     * finger when it fits, otherwise the same gap **below** it (finger too close to the top of the
     * window). Clamped so the tooltip never runs past the bottom of the window.
     *
     * @param fingerY Finger position in window pixels.
     * @param gapPx Gap between the finger and the tooltip's near edge.
     * @param tooltipHeight Measured tooltip height in pixels.
     * @param windowHeight Window height in pixels.
     * @return The tooltip's top y, in window pixels.
     */
    fun tooltipTopY(fingerY: Float, gapPx: Float, tooltipHeight: Int, windowHeight: Int): Int {
        val above = fingerY - gapPx - tooltipHeight
        val y = if (above >= 0f) above else fingerY + gapPx
        val maxTop = (windowHeight - tooltipHeight).coerceAtLeast(0).toFloat()
        return y.coerceIn(0f, maxTop).roundToInt()
    }
}
