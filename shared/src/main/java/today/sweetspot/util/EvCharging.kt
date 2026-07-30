package today.sweetspot.util

import kotlin.math.roundToInt

/**
 * Pure electric-vehicle charging maths.
 *
 * Shared by the phone ViewModel (which searches for the cheapest charging window) and the
 * state-of-charge dialog (which previews the estimate before the search), so the estimate the
 * user sees and the duration actually searched are always computed the same way.
 */
object EvCharging {

    /**
     * Effective AC charging power in kW: the lesser of the vehicle's maximum AC intake and the
     * home charger's output. A car charges no faster than the weaker side of the connection allows.
     */
    fun effectivePowerKw(vehicleAcMaxKw: Double, homeChargerKw: Double): Double =
        minOf(vehicleAcMaxKw, homeChargerKw)

    /**
     * Minutes to charge from [currentSoc] to [targetSoc] (percent) using a pure-linear AC model:
     * the energy needed (ΔSoC × [batteryKwh]) divided by [powerKw], converted to minutes, rounded,
     * and clamped to at least one minute.
     *
     * The caller is responsible for validating that [targetSoc] > [currentSoc] and [powerKw] > 0.
     */
    fun chargeMinutes(currentSoc: Int, targetSoc: Int, batteryKwh: Double, powerKw: Double): Int {
        val energyKwh = (targetSoc - currentSoc) / 100.0 * batteryKwh
        return (energyKwh / powerKw * 60).roundToInt().coerceAtLeast(1)
    }
}
