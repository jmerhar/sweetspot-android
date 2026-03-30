package today.sweetspot.data.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction over Play Billing for one-time in-app purchase.
 *
 * Provides a [StateFlow] of unlock state and methods to manage the purchase lifecycle.
 * The interface enables injecting a fake in ViewModel tests.
 */
interface BillingRepository {

    /** Observable unlock state. `true` when the user has purchased the full unlock. */
    val isUnlocked: StateFlow<Boolean>

    /** Observable localized price string (e.g. "€2.99"), or `null` if not yet loaded. */
    val productPrice: StateFlow<String?>

    /** Connects to the billing service. Call from ViewModel init. */
    fun connect()

    /** Disconnects from the billing service. Call from ViewModel onCleared. */
    fun disconnect()

    /**
     * Launches the purchase flow for the full unlock product.
     *
     * @param activity The activity to host the purchase UI.
     */
    fun launchPurchaseFlow(activity: Activity)

    /** Re-queries existing purchases to restore unlock state (e.g. reinstall or new device). */
    fun queryPurchases()
}
