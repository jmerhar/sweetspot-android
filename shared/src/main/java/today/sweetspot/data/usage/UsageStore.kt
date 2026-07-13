package today.sweetspot.data.usage

import today.sweetspot.model.ApplianceUsage

/**
 * Local, cumulative per-appliance tap store.
 *
 * Used by the watch to accumulate its own usage and report the full snapshot to the phone. The
 * store carries a reset token adopted from the phone: when the phone purges usage it bumps the
 * token, and the store zeroes itself the next time it sees a newer one. Android-free so it can
 * be faked in tests.
 */
interface UsageStore {

    /** Records one tap for [id] at [nowMs] (increments count, sets last-used). */
    fun record(id: String, nowMs: Long)

    /** The current cumulative usage per appliance id. */
    fun snapshot(): Map<String, ApplianceUsage>

    /** Zeroes all usage and adopts [token] as the current reset token. */
    fun reset(token: Long)

    /** The reset token this store currently holds. */
    fun token(): Long
}
