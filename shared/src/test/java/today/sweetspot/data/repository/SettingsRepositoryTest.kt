package today.sweetspot.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import today.sweetspot.model.Appliance
import today.sweetspot.model.ApplianceSort
import today.sweetspot.model.ApplianceUsage
import today.sweetspot.model.Countries
import today.sweetspot.model.EvPosition
import today.sweetspot.model.EvSpec
import today.sweetspot.model.SortCriterion
import today.sweetspot.model.SortKey
import java.time.ZoneId

/**
 * Tests for [SettingsRepository]: trial/unlock logic, source-order and disabled-source
 * persistence (incl. the country-change reset), appliance/EV serialization, price-zone
 * resolution, timezone precedence, and the developer time override. Robolectric supplies a
 * real [Context] (SharedPreferences).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SettingsRepositoryTest {

    private lateinit var repo: SettingsRepository
    private lateinit var context: Context

    private companion object {
        const val DAY_MS = 24 * 60 * 60 * 1000L
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE).edit().clear().commit()
        repo = SettingsRepository(context)
    }

    // --- Trial & unlock ---

    @Test
    fun `fresh install has the full trial and is not expired`() {
        assertEquals(14, repo.trialDaysRemaining())
        assertFalse(repo.isTrialExpired())
    }

    @Test
    fun `trial expires after the trial window elapses`() {
        val firstLaunch = repo.getFirstLaunchMs()
        repo.setTimeOverrideMs(firstLaunch + 15 * DAY_MS)
        assertEquals(0, repo.trialDaysRemaining())
        assertTrue(repo.isTrialExpired())
    }

    @Test
    fun `unlock keeps an elapsed trial from being expired`() {
        val firstLaunch = repo.getFirstLaunchMs()
        repo.setTimeOverrideMs(firstLaunch + 15 * DAY_MS)
        repo.setUnlocked(true)
        assertTrue(repo.isUnlocked())
        assertFalse(repo.isTrialExpired())
    }

    @Test
    fun `developer unlock bypasses an elapsed trial`() {
        val firstLaunch = repo.getFirstLaunchMs()
        repo.setTimeOverrideMs(firstLaunch + 15 * DAY_MS)
        repo.setDevUnlocked(true)
        assertFalse(repo.isTrialExpired())
    }

    @Test
    fun `trial days remaining is clamped to the trial length`() {
        // Override "now" to before first launch → elapsed is negative, must clamp to the max.
        val firstLaunch = repo.getFirstLaunchMs()
        repo.setTimeOverrideMs(firstLaunch - 5 * DAY_MS)
        assertEquals(14, repo.trialDaysRemaining())
    }

    // --- Source order & disabled sources ---

    @Test
    fun `source order defaults to null and round-trips`() {
        assertNull(repo.getSourceOrder())
        repo.setSourceOrder(listOf("energyzero", "entsoe"))
        assertEquals(listOf("energyzero", "entsoe"), repo.getSourceOrder())
    }

    @Test
    fun `changing country clears the custom source order and disabled sources`() {
        repo.setSourceOrder(listOf("energyzero", "entsoe"))
        repo.setDisabledSources(setOf("entsoe"))
        repo.setCountryCode("DE")
        assertNull(repo.getSourceOrder())
        assertTrue(repo.getDisabledSources().isEmpty())
    }

    @Test
    fun `disabled sources round-trip and an empty set is cleared`() {
        assertTrue(repo.getDisabledSources().isEmpty())
        repo.setDisabledSources(setOf("entsoe", "awattar"))
        assertEquals(setOf("entsoe", "awattar"), repo.getDisabledSources())
        repo.setDisabledSources(emptySet())
        assertTrue(repo.getDisabledSources().isEmpty())
    }

    // --- Appliances ---

    @Test
    fun `appliances default to empty and round-trip including EV specs`() {
        assertTrue(repo.getAppliances().isEmpty())
        val appliances = listOf(
            Appliance(id = "1", name = "Washer", durationHours = 2, durationMinutes = 30, icon = "laundry"),
            Appliance(id = "2", name = "Car", durationHours = 0, durationMinutes = 0, ev = EvSpec(60.0, 11.0))
        )
        repo.setAppliances(appliances)
        val read = repo.getAppliances()
        assertEquals(appliances, read)
        assertTrue(read[1].isEv)
        assertNull(read[1].icon)
    }

    // --- Appliance sorting, EV placement & usage ---

    @Test
    fun `appliance sort defaults to custom and round-trips`() {
        assertTrue(repo.getApplianceSort().isCustom)
        val sort = ApplianceSort(listOf(SortCriterion(SortKey.TYPE), SortCriterion(SortKey.FREQUENCY, descending = true)))
        repo.setApplianceSort(sort)
        assertEquals(sort, repo.getApplianceSort())
    }

    @Test
    fun `ev position and separate section default and round-trip`() {
        assertEquals(EvPosition.INTERLEAVED, repo.getEvPosition())
        assertFalse(repo.isEvSeparateSection())
        repo.setEvPosition(EvPosition.LAST)
        repo.setEvSeparateSection(true)
        assertEquals(EvPosition.LAST, repo.getEvPosition())
        assertTrue(repo.isEvSeparateSection())
    }

    @Test
    fun `appliance usage records, accumulates and clears`() {
        assertTrue(repo.getApplianceUsage().isEmpty())
        repo.recordApplianceUsage("a", 100)
        repo.recordApplianceUsage("a", 250)
        repo.recordApplianceUsage("b", 300)
        assertEquals(ApplianceUsage(2, 250), repo.getApplianceUsage()["a"])
        assertEquals(ApplianceUsage(1, 300), repo.getApplianceUsage()["b"])
        repo.clearApplianceUsage()
        assertTrue(repo.getApplianceUsage().isEmpty())
    }

    @Test
    fun `watch usage snapshot round-trips and clears`() {
        assertTrue(repo.getWatchUsage().isEmpty())
        repo.setWatchUsage(mapOf("x" to ApplianceUsage(4, 999)))
        assertEquals(ApplianceUsage(4, 999), repo.getWatchUsage()["x"])
        repo.setWatchUsage(emptyMap())
        assertTrue(repo.getWatchUsage().isEmpty())
    }

    @Test
    fun `usage reset token starts at zero and bumps monotonically`() {
        assertEquals(0L, repo.getUsageResetToken())
        repo.bumpUsageResetToken()
        repo.bumpUsageResetToken()
        assertEquals(2L, repo.getUsageResetToken())
    }

    // --- Price zone resolution ---

    @Test
    fun `single-zone country resolves to its only zone automatically`() {
        repo.setCountryCode("NL")
        assertEquals(Countries.findPriceZoneById("NL"), repo.getResolvedPriceZone())
    }

    @Test
    fun `multi-zone country resolves to null until a zone is chosen`() {
        val multi = Countries.all.first { it.zones.size > 1 }
        repo.setCountryCode(multi.code)
        assertNull(repo.getResolvedPriceZone())
        repo.setPriceZoneId(multi.zones[1].id)
        assertEquals(multi.zones[1], repo.getResolvedPriceZone())
    }

    // --- Timezone ---

    @Test
    fun `timezone defaults to the resolved zone's timezone`() {
        repo.setCountryCode("NL")
        assertTrue(repo.isUsingDefaultTimezone())
        assertEquals(ZoneId.of("Europe/Amsterdam"), repo.getTimeZoneId())
    }

    @Test
    fun `a custom timezone overrides the default and can be cleared`() {
        repo.setCountryCode("NL")
        repo.setTimeZoneId(ZoneId.of("Asia/Tokyo"))
        assertFalse(repo.isUsingDefaultTimezone())
        assertEquals(ZoneId.of("Asia/Tokyo"), repo.getTimeZoneId())
        repo.clearTimeZoneId()
        assertTrue(repo.isUsingDefaultTimezone())
        assertEquals(ZoneId.of("Europe/Amsterdam"), repo.getTimeZoneId())
    }

    // --- EV settings ---

    @Test
    fun `EV settings expose defaults and persist changes`() {
        assertEquals(11.0, repo.getEvHomeChargerKw(), 0.001)
        assertEquals(80, repo.getEvDefaultTargetSoc())
        assertEquals(20, repo.getEvLastCurrentSoc())
        repo.setEvHomeChargerKw(7.4)
        repo.setEvDefaultTargetSoc(90)
        repo.setEvLastCurrentSoc(35)
        assertEquals(7.4, repo.getEvHomeChargerKw(), 0.001)
        assertEquals(90, repo.getEvDefaultTargetSoc())
        assertEquals(35, repo.getEvLastCurrentSoc())
    }

    // --- Stats prefs ---

    @Test
    fun `stats prefs default off and persist`() {
        assertFalse(repo.isStatsEnabled())
        assertFalse(repo.isStatsPromptShown())
        repo.setStatsEnabled(true)
        repo.setStatsPromptShown()
        assertTrue(repo.isStatsEnabled())
        assertTrue(repo.isStatsPromptShown())
    }

    // --- Time override & clock ---

    @Test
    fun `time override is stored and cleared`() {
        assertNull(repo.getTimeOverrideMs())
        repo.setTimeOverrideMs(1_700_000_000_000L)
        assertEquals(1_700_000_000_000L, repo.getTimeOverrideMs())
        repo.setTimeOverrideMs(null)
        assertNull(repo.getTimeOverrideMs())
    }

    @Test
    fun `devClock is fixed at the override instant when set`() {
        repo.setTimeOverrideMs(1_700_000_000_000L)
        val clock = repo.devClock(ZoneId.of("UTC"))
        assertEquals(1_700_000_000_000L, clock.instant().toEpochMilli())
    }

    // --- Theme ---

    @Test
    fun `theme mode defaults to system and persists`() {
        assertEquals("system", repo.getThemeMode())
        repo.setThemeMode("dark")
        assertEquals("dark", repo.getThemeMode())
    }

    // --- Developer options ---

    @Test
    fun `dev options default off and persist`() {
        assertFalse(repo.isDevOptionsEnabled())
        repo.setDevOptionsEnabled()
        assertTrue(repo.isDevOptionsEnabled())
    }

    @Test
    fun `cooldown-disabled defaults off and persists`() {
        assertFalse(repo.isCooldownDisabled())
        repo.setCooldownDisabled(true)
        assertTrue(repo.isCooldownDisabled())
    }

    @Test
    fun `production-logo override defaults off and persists`() {
        assertFalse(repo.isUseProductionLogo())
        repo.setUseProductionLogo(true)
        assertTrue(repo.isUseProductionLogo())
    }

    @Test
    fun `price zone id can be set and cleared`() {
        repo.setPriceZoneId("SE1")
        assertEquals("SE1", repo.getPriceZoneId())
        repo.setPriceZoneId(null)
        assertNull(repo.getPriceZoneId())
    }

    // --- All-in price ---

    @Test
    fun `all-in enabled defaults off and persists`() {
        assertFalse(repo.isAllInEnabled())
        repo.setAllInEnabled(true)
        assertTrue(repo.isAllInEnabled())
    }

    @Test
    fun `supplier id can be set and cleared`() {
        assertNull(repo.getSupplierId())
        repo.setSupplierId("frankenergie")
        assertEquals("frankenergie", repo.getSupplierId())
        repo.setSupplierId(null)
        assertNull(repo.getSupplierId())
    }

    @Test
    fun `manual surcharge can be set and cleared`() {
        assertNull(repo.getManualSurcharge())
        repo.setManualSurcharge(0.0185)
        assertEquals(0.0185, repo.getManualSurcharge()!!, 1e-9)
        repo.setManualSurcharge(null)
        assertNull(repo.getManualSurcharge())
    }

    @Test
    fun `changing country clears the chosen supplier and manual surcharge`() {
        repo.setSupplierId("frankenergie")
        repo.setManualSurcharge(0.02)
        repo.setCountryCode("DE")
        assertNull(repo.getSupplierId())
        assertNull(repo.getManualSurcharge())
    }

    @Test
    fun `devClock uses the system clock when no override is set`() {
        val clock = repo.devClock(ZoneId.of("UTC"))
        assertTrue(kotlin.math.abs(clock.millis() - System.currentTimeMillis()) < 5_000)
    }

    @Test
    fun `country code is auto-detected and persisted on first access`() {
        // No country set → detection runs, returns a supported code, and persists it.
        val detected = repo.getCountryCode()
        assertNotNull(Countries.findByCode(detected))
        assertEquals(detected, repo.getCountryCode()) // second read is the persisted value
    }

    @Test
    fun `an invalid stored timezone falls back to the zone default`() {
        repo.setCountryCode("NL")
        context.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
            .edit().putString("zone_id", "Not/AZone").commit()
        assertEquals(ZoneId.of("Europe/Amsterdam"), repo.getTimeZoneId())
    }

    @Test
    fun `timezone falls back to system default when no zone resolves`() {
        val multi = Countries.all.first { it.zones.size > 1 }
        repo.setCountryCode(multi.code) // multi-zone, no selection → no resolved zone
        assertEquals(ZoneId.systemDefault(), repo.getTimeZoneId())
    }

    @Test
    fun `resolved price zone ignores an unknown stored zone id`() {
        repo.setCountryCode("NL")
        repo.setPriceZoneId("BOGUS")
        // Single-zone NL still resolves to its only zone despite the bad stored id.
        assertEquals(Countries.findPriceZoneById("NL"), repo.getResolvedPriceZone())
    }

    @Test
    fun `resolved price zone falls back to the default country when the code is unknown`() {
        repo.setCountryCode("ZZ") // not a supported country
        assertEquals(Countries.defaultCountry().zones.first(), repo.getResolvedPriceZone())
    }

    @Test
    fun `an invalid stored timezone falls back to the resolved zone`() {
        repo.setCountryCode("NL")
        context.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
            .edit().putString("zone_id", "Invalid/Zone").commit()
        assertEquals(ZoneId.of(Countries.findPriceZoneById("NL")!!.timeZoneId), repo.getTimeZoneId())
    }

    @Test
    fun `an invalid stored timezone with no resolvable zone falls back to system default`() {
        val multi = Countries.all.first { it.zones.size > 1 }
        repo.setCountryCode(multi.code) // multi-zone, no selection → no resolved zone
        context.getSharedPreferences("sweetspot_settings", Context.MODE_PRIVATE)
            .edit().putString("zone_id", "Invalid/Zone").commit()
        assertEquals(ZoneId.systemDefault(), repo.getTimeZoneId())
    }
}
