package today.sweetspot

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import today.sweetspot.data.stats.StatsRecord

/**
 * Receives API-reliability stats pushed from the Wear OS app via the Wearable Data Layer.
 *
 * Isolated behind an interface so the phone's handling logic
 * ([SweetSpotViewModel.onWatchStatsReceived]) stays unit-testable with a fake, free of Google Play
 * Services. The production implementation is [WearableStatsBridge].
 */
interface WatchStatsBridge {
    /**
     * Starts listening for watch stats, delivering each batch of decoded records to [onStats].
     *
     * @param onStats Callback invoked with the records decoded from each `/stats` update.
     */
    fun observe(onStats: (List<StatsRecord>) -> Unit)

    /** Stops listening for watch stats. */
    fun stop()
}

/**
 * Production [WatchStatsBridge]: the real Wearable Data Layer plumbing (listener registration and
 * `/stats` byte decoding). Deliberately thin and free of decision logic — the handling lives in
 * [SweetSpotViewModel.onWatchStatsReceived] — so it is excluded from coverage (it can only run with
 * Google Play Services on a real device/emulator).
 *
 * @param context Android context for [Wearable.getDataClient].
 */
class WearableStatsBridge(private val context: Context) : WatchStatsBridge, DataClient.OnDataChangedListener {

    private var onStats: ((List<StatsRecord>) -> Unit)? = null

    override fun observe(onStats: (List<StatsRecord>) -> Unit) {
        this.onStats = onStats
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
                if (event.dataItem.uri.path != "/stats") continue
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val bytes = dataMap.getByteArray("data") ?: continue
                onStats?.invoke(StatsRecord.decodeFromBytes(bytes))
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
