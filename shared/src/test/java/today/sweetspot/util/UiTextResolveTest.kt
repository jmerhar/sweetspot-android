package today.sweetspot.util

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import today.sweetspot.shared.R

/**
 * Robolectric coverage of [UiText.resolve] against real `:shared` string resources — every branch:
 * [UiText.Raw], [UiText.Res] with and without args, [UiText.Plural] with and without args,
 * [UiText.Composite], and the recursive resolution of a nested [UiText] argument.
 *
 * (The `:app` `UiTextResolveTest` additionally exercises `%s`/`%d` app strings; this keeps the
 * function covered in its own module using the duration/relative resources available here.)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UiTextResolveTest {

    private lateinit var res: Resources

    @Before
    fun setUp() {
        res = ApplicationProvider.getApplicationContext<Context>().resources
    }

    @Test
    fun `Raw resolves to its literal value`() {
        assertEquals("Washer · 20→80%", UiText.Raw("Washer · 20→80%").resolve(res))
    }

    @Test
    fun `Res without args returns the plain string`() {
        assertEquals("now", UiText.Res(R.string.relative_now).resolve(res))
    }

    @Test
    fun `Res with a numeric arg substitutes it`() {
        assertEquals("30m", UiText.Res(R.string.duration_minutes_only, listOf(30)).resolve(res))
    }

    @Test
    fun `Plural without args returns the raw quantity string`() {
        // Degenerate but real branch: no format args, so no substitution occurs.
        assertEquals("%dh", UiText.Plural(R.plurals.duration_hours_only, 2, emptyList()).resolve(res))
    }

    @Test
    fun `Plural with a numeric arg substitutes it`() {
        assertEquals("2h", UiText.Plural(R.plurals.duration_hours_only, 2, listOf(2)).resolve(res))
    }

    @Test
    fun `Composite joins resolved parts in order`() {
        assertEquals("Washer · 2h 30m", UiText.applianceLabel("Washer", 2, 30).resolve(res))
    }

    @Test
    fun `a nested UiText argument is resolved recursively`() {
        // relative_now has no placeholder, so the resolved arg is dropped — but resolving it still
        // exercises the `arg is UiText` branch of resolveArgs.
        assertEquals("now", UiText.Res(R.string.relative_now, listOf(UiText.duration(1, 0))).resolve(res))
    }
}
