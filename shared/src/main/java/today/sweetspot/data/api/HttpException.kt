package today.sweetspot.data.api

/**
 * Exception thrown when an API returns a non-successful HTTP status code.
 *
 * Provides a typed exception for HTTP errors so that error categorisation
 * can reliably distinguish HTTP failures from other exceptions without
 * parsing exception messages.
 *
 * @param code The HTTP status code (e.g. 401, 503).
 * @param message Human-readable error message.
 */
class HttpException(val code: Int, message: String) : RuntimeException(message)
