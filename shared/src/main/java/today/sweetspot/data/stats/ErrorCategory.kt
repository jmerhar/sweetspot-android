package today.sweetspot.data.stats

import today.sweetspot.data.api.EntsoeException
import today.sweetspot.data.api.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Categorises an exception into a short, stable error category string for stats collection.
 *
 * Categories:
 * - `HTTP_<code>` — HTTP error with specific status code (e.g. "HTTP_503")
 * - `ENTSOE_ERROR` — ENTSO-E Acknowledgement error (HTTP 200 but error response body)
 * - `TIMEOUT` — Socket timeout
 * - `DNS` — DNS resolution failure
 * - `CONNECTION` — Connection refused or reset
 * - `IO` — Other I/O errors
 * - `PARSE` — JSON/XML/number parsing errors
 * - `UNKNOWN` — Anything else
 *
 * @param exception The exception to categorise.
 * @return A short category string suitable for stats reporting.
 */
fun categorise(exception: Exception): String = when (exception) {
    is HttpException -> "HTTP_${exception.code}"
    is EntsoeException -> "ENTSOE_ERROR"
    is SocketTimeoutException -> "TIMEOUT"
    is UnknownHostException -> "DNS"
    is ConnectException -> "CONNECTION"
    is IOException -> "IO"
    is kotlinx.serialization.SerializationException,
    is org.xmlpull.v1.XmlPullParserException,
    is NumberFormatException -> "PARSE"
    else -> "UNKNOWN"
}
