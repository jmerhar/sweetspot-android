package today.sweetspot.data.stats

/**
 * Collects API request outcome statistics for later reporting.
 *
 * Implementations store [StatsRecord] entries and provide read/clear access
 * for the reporting pipeline. The interface is Android-free so it can be
 * faked in pure JUnit tests.
 */
interface StatsCollector {

    /**
     * Records a single API request outcome.
     *
     * @param record The stats record to store.
     */
    fun record(record: StatsRecord)

    /**
     * Reads all accumulated stats records.
     *
     * @return All stored records, or an empty list if none exist.
     */
    fun readAll(): List<StatsRecord>

    /**
     * Deletes all accumulated stats records.
     *
     * Typically called after a successful report submission.
     */
    fun clear()
}
