package today.sweetspot.data.stats

import org.junit.Assert.assertEquals
import org.junit.Test
import today.sweetspot.data.api.EntsoeException
import today.sweetspot.data.api.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorCategoryTest {

    @Test
    fun `HttpException maps to HTTP_code`() {
        assertEquals("HTTP_503", categorise(HttpException(503, "Service Unavailable")))
    }

    @Test
    fun `HttpException 401 maps to HTTP_401`() {
        assertEquals("HTTP_401", categorise(HttpException(401, "Unauthorized")))
    }

    @Test
    fun `HttpException 429 maps to HTTP_429`() {
        assertEquals("HTTP_429", categorise(HttpException(429, "Too Many Requests")))
    }

    @Test
    fun `SocketTimeoutException maps to TIMEOUT`() {
        assertEquals("TIMEOUT", categorise(SocketTimeoutException("Read timed out")))
    }

    @Test
    fun `UnknownHostException maps to DNS`() {
        assertEquals("DNS", categorise(UnknownHostException("api.example.com")))
    }

    @Test
    fun `ConnectException maps to CONNECTION`() {
        assertEquals("CONNECTION", categorise(ConnectException("Connection refused")))
    }

    @Test
    fun `IOException maps to IO`() {
        assertEquals("IO", categorise(IOException("Unexpected EOF")))
    }

    @Test
    fun `SerializationException maps to PARSE`() {
        assertEquals("PARSE", categorise(kotlinx.serialization.SerializationException("Bad JSON")))
    }

    @Test
    fun `NumberFormatException maps to PARSE`() {
        assertEquals("PARSE", categorise(NumberFormatException("For input string: abc")))
    }

    @Test
    fun `unknown exception maps to UNKNOWN`() {
        assertEquals("UNKNOWN", categorise(IllegalStateException("Something else")))
    }

    @Test
    fun `XmlPullParserException maps to PARSE`() {
        assertEquals("PARSE", categorise(org.xmlpull.v1.XmlPullParserException("Bad XML")))
    }

    @Test
    fun `RuntimeException maps to UNKNOWN`() {
        assertEquals("UNKNOWN", categorise(RuntimeException("Generic error")))
    }

    @Test
    fun `EntsoeException maps to ENTSOE_ERROR`() {
        assertEquals("ENTSOE_ERROR", categorise(EntsoeException("No matching data found")))
    }
}
