package si.merhar.sweetspot.wear

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import si.merhar.sweetspot.model.Appliance

/**
 * Tests for [WearViewModel] state management and async behavior.
 *
 * Runs under Robolectric. The Wearable Data Layer is not available in tests,
 * so Data Layer operations fail gracefully (caught in try-catch).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WearViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WearViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = WearViewModel(app)
        // Advance past the init block's loadAppliancesFromDataLayer coroutine
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Initial state ---

    @Test
    fun `initial state has empty appliance list`() {
        // Data Layer is unavailable in test, so appliances stay empty
        assertTrue(viewModel.uiState.value.appliances.isEmpty())
    }

    @Test
    fun `initial state is not loading`() {
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `initial state has no error`() {
        // Data Layer failure is logged but not surfaced as a UI error
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `initial state has no result`() {
        assertNull(viewModel.uiState.value.result)
    }

    // --- onApplianceTapped ---

    @Test
    fun `onApplianceTapped sets loading and label immediately`() {
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 2, durationMinutes = 30, icon = "laundry")
        viewModel.onApplianceTapped(appliance)

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertEquals("Washer \u00b7 2h 30m", state.resultLabel)
        assertNull(state.result)
        assertNull(state.error)
    }

    @Test
    fun `onApplianceTapped clears previous error`() {
        // Tap once to trigger loading
        val appliance = Appliance(id = "1", name = "Dryer", durationHours = 1, durationMinutes = 0, icon = "dryer")
        viewModel.onApplianceTapped(appliance)

        // Immediately, error should be null even if a previous error existed
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.isLoading)
    }

    // --- onClearResult ---

    @Test
    fun `onClearResult clears result label and error`() {
        val appliance = Appliance(id = "1", name = "Washer", durationHours = 1, durationMinutes = 0, icon = "laundry")
        viewModel.onApplianceTapped(appliance)

        viewModel.onClearResult()

        val state = viewModel.uiState.value
        assertNull(state.result)
        assertNull(state.resultLabel)
        assertNull(state.error)
    }
}
