package today.sweetspot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import today.sweetspot.R
import today.sweetspot.ui.onboarding.OnboardingGradientBottom
import today.sweetspot.ui.onboarding.OnboardingGradientTop
import today.sweetspot.util.CalloutPlacement
import today.sweetspot.util.CoachMarkGeometry
import today.sweetspot.util.RectPx

/** Reports this control's window bounds while [active] so a [CoachMarkCallout] can anchor to it. */
fun Modifier.coachMarkAnchor(active: Boolean, onBounds: (Rect) -> Unit): Modifier =
    if (!active) this else onGloballyPositioned { onBounds(it.boundsInWindow()) }

/**
 * A one-time contextual hint bubble with a tail pointing at [target] (the anchored control's bounds
 * in window pixels). Rendered in a [Popup] so it isn't clipped by a scrolling parent, positioned by
 * the pure [CoachMarkGeometry.calloutFor] (prefers above the target, flips below, clamps to the
 * window, tail tracks the target's centre). Renders nothing while the target is scrolled out of the
 * window, so the callout appears only once its control is on screen.
 *
 * @param target Anchored control bounds in window pixels.
 * @param text The hint line.
 * @param onDismiss Called by the "Got it" button.
 */
@Composable
fun CoachMarkCallout(target: Rect, text: String, onDismiss: () -> Unit) {
    // The host view's height is a reliable window height for the "is the target on screen?" guard;
    // the Popup itself is positioned by the system-provided window size in the position provider.
    val windowHeight = LocalView.current.height
    if (windowHeight <= 0 || target.bottom < 0f || target.top > windowHeight) return

    val density = LocalDensity.current
    val gapPx = with(density) { 8.dp.toPx() }
    val tailWidth = with(density) { 16.dp.toPx() }
    val tailHeight = with(density) { 8.dp.toPx() }
    // Same fixed SweetSpot brand palette as the onboarding intro: a blue→indigo gradient with white
    // text. The tail matches whichever gradient edge it sits against (top or bottom).
    val bubbleBrush = Brush.verticalGradient(listOf(OnboardingGradientTop, OnboardingGradientBottom))

    // The position provider knows the measured bubble size; it publishes the resulting placement here
    // so the content can draw the tail on the correct edge and aligned with the target.
    var placement by remember(target) { mutableStateOf<CalloutPlacement?>(null) }

    Popup(
        popupPositionProvider = remember(target, gapPx, tailWidth) {
            CoachMarkPositionProvider(target, gapPx, tailWidth / 2f) { placement = it }
        },
        properties = PopupProperties(focusable = false)
    ) {
        val p = placement
        Column(horizontalAlignment = Alignment.Start) {
            if (p != null && !p.above) Tail(pointsUp = true, centerXPx = p.tailCenterX, widthPx = tailWidth, heightPx = tailHeight, color = OnboardingGradientTop)
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .background(bubbleBrush, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
                    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            Text(stringResource(R.string.coach_got_it))
                        }
                    }
                }
            }
            if (p != null && p.above) Tail(pointsUp = false, centerXPx = p.tailCenterX, widthPx = tailWidth, heightPx = tailHeight, color = OnboardingGradientBottom)
        }
    }
}

/** A small triangular tail, offset so its centre sits [centerXPx] from the bubble's left edge. */
@Composable
private fun Tail(
    pointsUp: Boolean,
    centerXPx: Int,
    widthPx: Float,
    heightPx: Float,
    color: androidx.compose.ui.graphics.Color
) {
    val density = LocalDensity.current
    val widthDp = with(density) { widthPx.toDp() }
    val heightDp = with(density) { heightPx.toDp() }
    val offsetXDp = with(density) { (centerXPx - widthPx / 2f).toDp() }
    Canvas(
        modifier = Modifier
            .offset(x = offsetXDp)
            .size(widthDp, heightDp)
    ) {
        val path = Path().apply {
            if (pointsUp) {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
            }
            close()
        }
        drawPath(path, color)
    }
}

/**
 * Positions the callout with [CoachMarkGeometry.calloutFor] and reports the resulting [CalloutPlacement]
 * (so the content can draw the tail), converting the target's window-pixel bounds to an [IntOffset].
 */
private class CoachMarkPositionProvider(
    private val target: Rect,
    private val gapPx: Float,
    private val tailInsetPx: Float,
    private val onPlacement: (CalloutPlacement) -> Unit
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val placement = CoachMarkGeometry.calloutFor(
            target = RectPx(target.left, target.top, target.right, target.bottom),
            bubbleW = popupContentSize.width,
            bubbleH = popupContentSize.height,
            windowW = windowSize.width,
            windowH = windowSize.height,
            gapPx = gapPx,
            tailInset = tailInsetPx
        )
        onPlacement(placement)
        return IntOffset(placement.x, placement.y)
    }
}
