package today.sweetspot

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import today.sweetspot.data.usage.UsageSnapshot
import today.sweetspot.model.ApplianceUsage

/**
 * Receives per-appliance tap usage pushed from the Wear OS app via the Wearable Data Layer.
 *
 * The watch sends its whole cumulative usage snapshot plus the reset token it currently holds.
 * Isolated behind an interface so the phone's handling logic
 * ([SweetSpotViewModel.onWatchUsageReceived]) stays unit-testable with a fake. The production
 * implementation is [WearableUsageBridge].
 */
interface WatchUsageBridge {
    /**
     * Starts listening for watch usage, delivering each decoded snapshot and its reset token.
     *
     * @param onUsage Callback invoked with (usage map, reset token) from each `/usage` update.
     */
    fun observe(onUsage: (Map<String, ApplianceUsage>, Long) -> Unit)

    /** Stops listening for watch usage. */
    fun stop()
}

/**
 * Production [WatchUsageBridge]: the real Wearable Data Layer plumbing (listener registration and
 * `/usage` decoding). Deliberately thin and free of decision logic — the handling lives in
 * [SweetSpotViewModel.onWatchUsageReceived] — so it is excluded from coverage (it can only run with
 * Google Play Services on a real device/emulator).
 *
 * @param context Android context for [Wearable.getDataClient].
 */
class WearableUsageBridge(private val context: Context) : WatchUsageBridge, DataClient.OnDataChangedListener {

    private var onUsage: ((Map<String, ApplianceUsage>, Long) -> Unit)? = null

    override fun observe(onUsage: (Map<String, ApplianceUsage>, Long) -> Unit) {
        this.onUsage = onUsage
        try {
            Wearable.getDataClient(context).addListener(this)
        } catch (_: Exception) {
            // Play Services unavailable — watch sync not supported
        }
    }

    override fun onDataChanged(events: DataEventBuffer) {
        try {
            for (event in events) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                if (event.dataItem.uri.path != "/usage") continue
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val bytes = dataMap.getByteArray("data") ?: continue
                onUsage?.invoke(UsageSnapshot.decodeFromBytes(bytes), dataMap.getLong("token"))
            }
        } finally {
            events.release()
        }
    }

    override fun stop() {
        try {
            Wearable.getDataClient(context).removeListener(this)
        } catch (_: Exception) {
            // Play Services unavailable
        }
    }
}
