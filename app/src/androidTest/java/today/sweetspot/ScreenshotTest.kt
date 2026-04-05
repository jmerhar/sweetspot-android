package today.sweetspot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

/**
 * Instrumented test that captures 5 screenshots for each locale via Fastlane Screengrab.
 *
 * Navigates through the real UI using Compose test semantics:
 * 1. Result screen (cheapest window for washing machine)
 * 2. Home screen (form with translated appliance chips)
 * 3. Price chart (scrolled to show "Upcoming Prices" title + chart)
 * 4. Settings screen
 * 5. Language picker
 *
 * Test data is pre-populated via [ScreenshotTestData] with a fixed time override
 * and 15-minute resolution prices. Appliance names are translated per locale.
 */
class ScreenshotTest {

    companion object {
        @get:ClassRule
        @JvmStatic
        val localeTestRule = LocaleTestRule()
    }

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    /** Translated washing machine name for the current locale. */
    private lateinit var washerName: String

    /** Localized content description for the Settings icon. */
    private lateinit var cdSettings: String

    /** Localized content description for the Back button. */
    private lateinit var cdBack: String

    /** Localized title for the "Upcoming Prices" chart section. */
    private lateinit var labelUpcomingPrices: String

    @Before
    fun setUp() {
        // 1. Populate SharedPreferences and cache BEFORE launching.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ScreenshotTestData.populate(context)
        washerName = ScreenshotTestData.washingMachineName()

        // 2. Launch Activity (ViewModel reads pre-populated data).
        scenario = ActivityScenario.launch(MainActivity::class.java)

        // 3. Set the per-app locale from the running Activity. On API 33+,
        //    AppCompatDelegate.setApplicationLocales() triggers an Activity
        //    recreation automatically.
        scenario.onActivity { ScreenshotTestData.applyTestLocale() }

        // 4. Wait for the recreation to settle, then resolve localized strings.
        Thread.sleep(2_000)
        scenario.onActivity { activity ->
            cdSettings = activity.getString(R.string.cd_settings)
            cdBack = activity.getString(R.string.cd_back)
            labelUpcomingPrices = activity.getString(R.string.result_upcoming_prices)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureScreenshots() {
        // 1. Home screen — form with translated appliance chips
        composeTestRule.waitUntilAtLeastOneExists(hasText(washerName), timeoutMillis = 10_000)
        Screengrab.screenshot("2_home")

        // 2. Result screen — tap washing machine to trigger search
        composeTestRule.onNodeWithText(washerName).performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasText(washerName, substring = true), timeoutMillis = 15_000)
        composeTestRule.waitForIdle()
        Thread.sleep(2_000)
        Screengrab.screenshot("1_result")

        // 3. Price chart — scroll "Upcoming Prices" into view, then swipe up
        //    so the title sits near the top with the chart visible below it.
        composeTestRule.onNodeWithText(labelUpcomingPrices).performScrollTo()
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().performTouchInput {
            swipeUp(startY = bottom * 0.5f, endY = bottom * 0.3f, durationMillis = 400)
        }
        composeTestRule.waitForIdle()
        Thread.sleep(1_000)
        Screengrab.screenshot("3_prices")

        // 4. Settings screen
        composeTestRule.onNodeWithContentDescription(cdBack).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(cdSettings).performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1_000)
        Screengrab.screenshot("4_settings")

        // 5. Language picker — click the language row (identified by testTag,
        //    since the "Language" header text is a non-clickable label above it)
        composeTestRule.onNodeWithTag("language_row")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1_000)
        Screengrab.screenshot("5_languages")
    }
}
