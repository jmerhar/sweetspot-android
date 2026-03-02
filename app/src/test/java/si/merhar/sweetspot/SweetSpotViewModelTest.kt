package si.merhar.sweetspot

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import si.merhar.sweetspot.model.Appliance

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SweetSpotViewModelTest {

    private lateinit var viewModel: SweetSpotViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = SweetSpotViewModel(app)
    }

    // --- Initial state ---

    @Test
    fun `initial state has default duration of 1h 0m`() {
        val state = viewModel.uiState.value
        assertEquals(1, state.durationHours)
        assertEquals(0, state.durationMinutes)
    }

    @Test
    fun `initial state is not loading`() {
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `initial state has no error`() {
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `initial state has no result`() {
        assertNull(viewModel.uiState.value.result)
    }

    @Test
    fun `initial state has settings hidden`() {
        assertEquals(false, viewModel.uiState.value.showSettings)
    }

    // --- Duration changes ---

    @Test
    fun `onDurationChanged updates hours and minutes`() {
        viewModel.onDurationChanged(3, 30)
        val state = viewModel.uiState.value
        assertEquals(3, state.durationHours)
        assertEquals(30, state.durationMinutes)
    }

    @Test
    fun `onDurationChanged to zero`() {
        viewModel.onDurationChanged(0, 0)
        val state = viewModel.uiState.value
        assertEquals(0, state.durationHours)
        assertEquals(0, state.durationMinutes)
    }

    // --- Quick duration ---

    @Test
    fun `onQuickDuration sets duration and result label`() {
        viewModel.onQuickDuration(2, 0)
        val state = viewModel.uiState.value
        assertEquals(2, state.durationHours)
        assertEquals(0, state.durationMinutes)
        assertEquals("2h", state.resultLabel)
    }

    @Test
    fun `onQuickDuration with minutes sets correct label`() {
        viewModel.onQuickDuration(1, 30)
        assertEquals("1h 30m", viewModel.uiState.value.resultLabel)
    }

    // --- Appliance duration ---

    @Test
    fun `onApplianceDuration sets duration and label`() {
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 2, durationMinutes = 30, icon = "laundry")
        viewModel.onApplianceDuration(appliance)
        val state = viewModel.uiState.value
        assertEquals(2, state.durationHours)
        assertEquals(30, state.durationMinutes)
        assertEquals("Washer \u00b7 2h 30m", state.resultLabel)
    }

    // --- Validation ---

    @Test
    fun `onFindClicked with zero duration sets error`() {
        viewModel.onDurationChanged(0, 0)
        viewModel.onFindClicked()
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("duration greater than zero"))
        assertNull(state.result)
    }

    // --- Settings toggle ---

    @Test
    fun `onShowSettings sets showSettings to true`() {
        viewModel.onShowSettings()
        assertEquals(true, viewModel.uiState.value.showSettings)
    }

    @Test
    fun `onHideSettings sets showSettings to false`() {
        viewModel.onShowSettings()
        viewModel.onHideSettings()
        assertEquals(false, viewModel.uiState.value.showSettings)
    }

    // --- Clear result ---

    @Test
    fun `onClearResult clears result and related fields`() {
        // Set some state first
        viewModel.onQuickDuration(1, 0)
        // Now clear
        viewModel.onClearResult()
        val state = viewModel.uiState.value
        assertNull(state.result)
        assertNull(state.resultLabel)
        assertNull(state.error)
        assertTrue(state.allPrices.isEmpty())
    }

    // --- Appliance CRUD ---

    @Test
    fun `onAddAppliance adds to list`() {
        viewModel.onAddAppliance("Dryer", 1, 30, "dryer")
        val appliances = viewModel.uiState.value.appliances
        assertEquals(1, appliances.size)
        assertEquals("Dryer", appliances[0].name)
        assertEquals(1, appliances[0].durationHours)
        assertEquals(30, appliances[0].durationMinutes)
        assertEquals("dryer", appliances[0].icon)
    }

    @Test
    fun `onAddAppliance generates unique IDs`() {
        viewModel.onAddAppliance("A", 1, 0, "bolt")
        viewModel.onAddAppliance("B", 2, 0, "bolt")
        val appliances = viewModel.uiState.value.appliances
        assertEquals(2, appliances.size)
        assertTrue(appliances[0].id != appliances[1].id)
    }

    @Test
    fun `onUpdateAppliance replaces matching appliance`() {
        viewModel.onAddAppliance("Old", 1, 0, "bolt")
        val added = viewModel.uiState.value.appliances[0]
        val updated = added.copy(name = "New", durationHours = 3)
        viewModel.onUpdateAppliance(updated)
        val appliances = viewModel.uiState.value.appliances
        assertEquals(1, appliances.size)
        assertEquals("New", appliances[0].name)
        assertEquals(3, appliances[0].durationHours)
    }

    @Test
    fun `onDeleteAppliance removes by ID`() {
        viewModel.onAddAppliance("A", 1, 0, "bolt")
        viewModel.onAddAppliance("B", 2, 0, "bolt")
        val idToDelete = viewModel.uiState.value.appliances[0].id
        viewModel.onDeleteAppliance(idToDelete)
        val appliances = viewModel.uiState.value.appliances
        assertEquals(1, appliances.size)
        assertEquals("B", appliances[0].name)
    }

    @Test
    fun `onDeleteAppliance with unknown ID does nothing`() {
        viewModel.onAddAppliance("A", 1, 0, "bolt")
        viewModel.onDeleteAppliance("nonexistent")
        assertEquals(1, viewModel.uiState.value.appliances.size)
    }

    // --- Timezone ---

    @Test
    fun `initial state uses default timezone`() {
        assertTrue(viewModel.uiState.value.isUsingDefaultZone)
    }

    @Test
    fun `onZoneSelected with null reverts to default`() {
        viewModel.onZoneSelected(java.time.ZoneId.of("Asia/Tokyo"))
        assertEquals(false, viewModel.uiState.value.isUsingDefaultZone)
        viewModel.onZoneSelected(null)
        assertTrue(viewModel.uiState.value.isUsingDefaultZone)
    }

    @Test
    fun `onZoneSelected sets custom timezone`() {
        val tokyo = java.time.ZoneId.of("Asia/Tokyo")
        viewModel.onZoneSelected(tokyo)
        assertEquals(tokyo, viewModel.uiState.value.zoneId)
        assertEquals(false, viewModel.uiState.value.isUsingDefaultZone)
    }

    // --- Async fetch (coroutine) ---

    @Test
    fun `onFindClicked sets isLoading immediately`() {
        viewModel.onDurationChanged(1, 0)
        viewModel.onFindClicked()

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.result)
    }

    @Test
    fun `onFindClicked preserves resultLabel during loading`() {
        viewModel.onQuickDuration(2, 0)
        viewModel.onFindClicked()

        assertEquals("2h", viewModel.uiState.value.resultLabel)
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onFindClicked with appliance label preserves appliance label`() {
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 1, durationMinutes = 30, icon = "laundry")
        viewModel.onApplianceDuration(appliance)
        viewModel.onFindClicked()

        assertEquals("Washer \u00b7 1h 30m", viewModel.uiState.value.resultLabel)
        assertTrue(viewModel.uiState.value.isLoading)
    }
}
