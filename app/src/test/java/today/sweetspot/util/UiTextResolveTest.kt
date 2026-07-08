package today.sweetspot.util

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import today.sweetspot.R

/**
 * Robolectric tests for [UiText.resolve] against real (English) string resources.
 *
 * The pure-JUnit [UiTextTest] covers construction; this verifies the Android-backed resolution
 * logic end-to-end — the empty-vs-args branches, [UiText.Composite] joining, and the recursive
 * resolution of nested [UiText] format arguments (the `error_not_enough_data` case, which mixes a
 * nested duration `%s` with a numeric `%d`).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UiTextResolveTest {

    private lateinit var resources: Resources

    @Before
    fun setUp() {
        resources = ApplicationProvider.getApplicationContext<Context>().resources
    }

    @Test
    fun `Raw resolves to its literal value`() {
        assertEquals("VW ID.3 · 20→80%", UiText.Raw("VW ID.3 · 20→80%").resolve(resources))
    }

    @Test
    fun `Res without args resolves the plain string`() {
        assertEquals("Cache cleared", UiText.Res(R.string.snackbar_cache_cleared).resolve(resources))
    }

    @Test
    fun `Res with a string arg substitutes the argument`() {
        assertEquals(
            "Could not fetch prices: Boom",
            UiText.Res(R.string.error_network, listOf("Boom")).resolve(resources)
        )
    }

    @Test
    fun `duration resolves without an Android context in the caller`() {
        assertEquals("30m", UiText.duration(0, 30).resolve(resources))
        assertEquals("2h", UiText.duration(2, 0).resolve(resources))
        assertEquals("2h 30m", UiText.duration(2, 30).resolve(resources))
    }

    @Test
    fun `Composite joins its parts in order`() {
        assertEquals("AB", UiText.Composite(listOf(UiText.Raw("A"), UiText.Raw("B"))).resolve(resources))
    }

    @Test
    fun `applianceLabel resolves to name and localised duration`() {
        assertEquals("Washer · 2h 30m", UiText.applianceLabel("Washer", 2, 30).resolve(resources))
    }

    @Test
    fun `Plural resolves the quantity form`() {
        // error_cooldown "other" form for quantity 2.
        assertTrue(UiText.Plural(R.plurals.error_cooldown, 2, listOf(2)).resolve(resources).contains("2"))
    }

    @Test
    fun `Plural with a nested UiText arg resolves the nested text and the numeric arg`() {
        // error_not_enough_data: "...to cover %1$s. Only %2$d hours..." — %1$s is a nested duration.
        val resolved = UiText.Plural(
            R.plurals.error_not_enough_data,
            5,
            listOf(UiText.duration(2, 0), 5L)
        ).resolve(resources)
        assertTrue("expected nested duration '2h' in: $resolved", resolved.contains("2h"))
        assertTrue("expected coverage '5' in: $resolved", resolved.contains("5"))
    }
}
