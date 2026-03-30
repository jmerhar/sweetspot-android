package today.sweetspot.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import today.sweetspot.data.repository.SettingsRepository

/**
 * Play Billing implementation of [BillingRepository].
 *
 * Wraps [BillingClient] to manage the full-unlock non-consumable in-app purchase.
 * On connect, queries existing purchases to restore state and fetches product details
 * for the price display. Caches unlock state in [SettingsRepository] for offline access.
 *
 * @param context Application context for [BillingClient].
 * @param settingsRepository Used to cache unlock state locally.
 * @param coroutineScope Scope for async billing operations (typically viewModelScope).
 */
class PlayBillingRepository(
    context: Context,
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope
) : BillingRepository {

    private companion object {
        const val TAG = "PlayBilling"
        const val PRODUCT_ID = "full_unlock"
    }

    private val _isUnlocked = MutableStateFlow(settingsRepository.isUnlocked())
    override val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _productPrice = MutableStateFlow<String?>(null)
    override val productPrice: StateFlow<String?> = _productPrice.asStateFlow()

    private var productDetails: ProductDetails? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    override fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                    queryProductDetails()
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    override fun disconnect() {
        billingClient.endConnection()
    }

    override fun launchPurchaseFlow(activity: Activity) {
        val details = productDetails
        if (details == null) {
            Log.w(TAG, "Product details not loaded, cannot launch purchase flow")
            return
        }

        details.oneTimePurchaseOfferDetails ?: return
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun queryPurchases() {
        coroutineScope.launch {
            try {
                val params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                val result = billingClient.queryPurchasesAsync(params)
                val hasUnlock = result.purchasesList.any { purchase ->
                    purchase.products.contains(PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                updateUnlockState(hasUnlock)

                // Acknowledge any unacknowledged purchases
                for (purchase in result.purchasesList) {
                    if (!purchase.isAcknowledged &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        acknowledgePurchase(purchase)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to query purchases", e)
            }
        }
    }

    /**
     * Fetches product details for the full unlock product.
     *
     * Runs asynchronously within [coroutineScope]. Updates [_productPrice] when loaded.
     */
    private fun queryProductDetails() {
        coroutineScope.launch {
            try {
                val product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(listOf(product))
                    .build()
                val result = billingClient.queryProductDetails(params)
                productDetails = result.productDetailsList?.firstOrNull()
                _productPrice.value = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
            } catch (e: Exception) {
                Log.w(TAG, "Failed to query product details", e)
            }
        }
    }

    /**
     * Processes a completed purchase: acknowledges if needed and updates unlock state.
     *
     * @param purchase The purchase to handle.
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.products.contains(PRODUCT_ID)) {
                updateUnlockState(true)
                if (!purchase.isAcknowledged) {
                    coroutineScope.launch { acknowledgePurchase(purchase) }
                }
            }
        }
    }

    /**
     * Acknowledges a purchase so Google Play doesn't auto-refund it.
     *
     * @param purchase The purchase to acknowledge.
     */
    private suspend fun acknowledgePurchase(purchase: Purchase) {
        try {
            val params = com.android.billingclient.api.AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acknowledge purchase", e)
        }
    }

    /**
     * Updates unlock state in both the [StateFlow] and [SettingsRepository].
     *
     * @param unlocked Whether the user is unlocked.
     */
    private fun updateUnlockState(unlocked: Boolean) {
        settingsRepository.setUnlocked(unlocked)
        _isUnlocked.value = unlocked
    }
}
