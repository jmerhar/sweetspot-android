package today.sweetspot.util

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [EvCharging] — the pure EV charging maths shared by the ViewModel and SoC dialog. */
class EvChargingTest {

    @Test
    fun `effective power is the lesser of vehicle and charger`() {
        assertEquals(7.4, EvCharging.effectivePowerKw(11.0, 7.4), 0.0)
        assertEquals(11.0, EvCharging.effectivePowerKw(11.0, 22.0), 0.0)
    }

    @Test
    fun `charge minutes for a typical AC session`() {
        // 20 -> 80% of a 60 kWh battery = 36 kWh; at 11 kW that is 3.2727h = 196.36 min -> 196.
        assertEquals(196, EvCharging.chargeMinutes(20, 80, 60.0, 11.0))
    }

    @Test
    fun `charge minutes rounds to the nearest minute`() {
        // 0 -> 10% of a 66 kWh battery = 6.6 kWh; at 6 kW that is 1.1h = 66.0 min.
        assertEquals(66, EvCharging.chargeMinutes(0, 10, 66.0, 6.0))
        // A half-minute rounds up: 10.5 min -> 11.
        assertEquals(11, EvCharging.chargeMinutes(0, 21, 5.0, 6.0))
    }

    @Test
    fun `charge minutes is at least one for a tiny top-up`() {
        assertEquals(1, EvCharging.chargeMinutes(50, 51, 100.0, 100.0))
    }
}
