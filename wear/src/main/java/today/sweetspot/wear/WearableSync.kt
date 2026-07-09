package today.sweetspot.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItemBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Production [WearSync]: the real Wearable Data Layer plumbing (listener registration, initial
 * read, DataMap parsing, and pushing stats). This is deliberately thin and free of decision logic
 * — everything meaningful lives in [WearViewModel] — so it is excluded from coverage (it can only
 * be exercised with Google Play Services on a real device/emulator).
 *
 * @param context Android context for [Wearable.getDataClient].
 * @param scope Coroutine scope for the initial async read (the ViewModel's `viewModelScope`).
 * @param ioDispatcher Dispatcher for the initial read.
 */
class WearableSync(
    private val context: Context,
    private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
) : WearSync, DataClient.OnDataChangedListener {

    private var onAppliances: ((String) -> Unit)? = null
    private var onSettings: ((WearSettings) -> Unit)? = null

    override fun observe(onAppliances: (String) -> Unit, onSettings: (WearSettings) -> Unit) {
        this.onAppliances = onAppliances
        this.onSettings = onSettings
        Wearable.getDataClient(context).addListener(this)
        scope.launch(ioDispatcher) {
            var buffer: DataItemBuffer? = null
            try {
                buffer = Wearable.getDataClient(context).dataItems.await()
                for (item in buffer) {
                    deliver(item.uri.path, DataMapItem.fromDataItem(item).dataMap)
                }
            } catch (e: Exception) {
                Log.w("WearableSync", "Could not read from Data Layer", e)
            } finally {
                buffer?.release()
            }
        }
    }

    override fun onDataChanged(events: DataEventBuffer) {
        try {
            for (event in events) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                deliver(event.dataItem.uri.path, DataMapItem.fromDataItem(event.dataItem).dataMap)
            }
        } finally {
            events.release()
        }
    }

    /** Routes a DataMap for a given path to the appropriate callback. */
    private fun deliver(path: String?, map: DataMap) {
        when (path) {
            "/appliances" -> map.getString("json")?.let { onAppliances?.invoke(it) }
            "/settings" -> onSettings?.invoke(
                WearSettings(
                    countryCode = map.getString("country_code"),
                    priceZoneId = map.getString("price_zone_id"),
                    sourceOrder = map.getString("source_order"),
                    disabledSources = map.getString("disabled_sources"),
                    language = map.getString("language"),
                    statsEnabled = map.getBoolean("stats_enabled", false),
                    isTrialExpired = map.getBoolean("is_trial_expired", false),
                    isUnlocked = map.getBoolean("is_unlocked", false),
                )
            )
        }
    }

    override fun stop() {
        Wearable.getDataClient(context).removeListener(this)
    }

    override suspend fun pushStats(bytes: ByteArray) {
        val request = PutDataMapRequest.create("/stats").apply {
            dataMap.putByteArray("data", bytes)
            dataMap.putLong("ts", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(request).await()
    }
}
