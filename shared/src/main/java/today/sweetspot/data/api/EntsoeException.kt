package today.sweetspot.data.api

/**
 * Exception thrown when the ENTSO-E API returns an Acknowledgement_MarketDocument error.
 *
 * The ENTSO-E API sometimes responds with HTTP 200 but an XML error body instead of
 * price data (e.g. "No matching data found"). This typed exception allows the stats
 * error categoriser to distinguish these API-level errors from generic runtime failures.
 *
 * @param reason The error reason text extracted from the Acknowledgement document.
 */
class EntsoeException(val reason: String) : RuntimeException("ENTSO-E API error: $reason")
