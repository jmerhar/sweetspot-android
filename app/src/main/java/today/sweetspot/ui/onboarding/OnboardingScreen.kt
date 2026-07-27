package today.sweetspot.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import today.sweetspot.R
import today.sweetspot.ui.theme.SweetSpotTheme
import today.sweetspot.shared.R as SharedR

/**
 * Fixed SweetSpot brand gradient for the onboarding intro, deliberately independent of the app's
 * dynamic Material theme so the first-launch screens are unmistakably on-brand (and legible in white)
 * on every device and in both light and dark mode.
 */
private val OnboardingGradientTop = Color(0xFF4A90D9)
private val OnboardingGradientBottom = Color(0xFF274B93)

/** One onboarding page: an icon and a short title + body, all localised. */
private data class OnboardingPage(
    @param:DrawableRes val icon: Int,
    @param:StringRes val title: Int,
    @param:StringRes val body: Int
)

private val onboardingPages = listOf(
    OnboardingPage(SharedR.drawable.ic_price, R.string.onboarding_title_1, R.string.onboarding_body_1),
    OnboardingPage(SharedR.drawable.ic_device, R.string.onboarding_title_2, R.string.onboarding_body_2),
    OnboardingPage(SharedR.drawable.ic_advanced, R.string.onboarding_title_3, R.string.onboarding_body_3)
)

/**
 * First-launch onboarding intro: a small, skippable value-first carousel.
 *
 * Swipe or tap **Next** through the pages; **Skip** (any page) and **Get started** (last page) both
 * call [onFinish]. System back steps to the previous page, or finishes on the first page. It's shown
 * once on first launch and re-openable from Settings — the caller decides when to show it.
 *
 * @param onFinish Called when the intro is dismissed (Skip, Get started, or back on the first page).
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val pages = remember { onboardingPages }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val lastPage = pages.lastIndex
    val onLastPage = pagerState.currentPage >= lastPage

    BackHandler {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        } else {
            onFinish()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OnboardingGradientTop, OnboardingGradientBottom)))
    ) {
        // Inset for the status bar and navigation/gesture bar so Skip and the button clear them.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(24.dp)
        ) {
            // Skip, top-right — hidden on the last page where "Get started" is the primary action.
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                if (!onLastPage) {
                    TextButton(
                        onClick = onFinish,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.onboarding_skip))
                    }
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                OnboardingPageContent(pages[page])
            }

            // Page indicator dots (decorative).
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { i ->
                    val selected = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) Color.White
                                else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (onLastPage) onFinish()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = OnboardingGradientBottom
                )
            ) {
                Text(stringResource(if (onLastPage) R.string.onboarding_get_started else R.string.onboarding_next))
            }
        }
    }
}

/** The centred icon + title + body for a single onboarding page. */
@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(page.icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(page.body),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "Onboarding intro")
@Composable
private fun OnboardingScreenPreview() {
    SweetSpotTheme {
        OnboardingScreen(onFinish = {})
    }
}
