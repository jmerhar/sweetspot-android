package today.sweetspot.data.support

import today.sweetspot.model.FeedbackReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies request encoding, response parsing, and the retry policy of [FeedbackCodec]. */
class FeedbackCodecTest {

    @Test
    fun `encodeRequest includes set fields and omits null optionals`() {
        val json = FeedbackCodec.encodeRequest(
            FeedbackReport(category = "bug", subject = "Crash", body = "It broke")
        )
        assertTrue(json.contains("\"category\":\"bug\""))
        assertTrue(json.contains("\"subject\":\"Crash\""))
        assertTrue(json.contains("\"body\":\"It broke\""))
        // Null optionals are omitted, not serialised as null.
        assertFalse(json.contains("diagnostics"))
        assertFalse(json.contains("email"))
    }

    @Test
    fun `encodeRequest includes diagnostics and email when present`() {
        val json = FeedbackCodec.encodeRequest(
            FeedbackReport("bug", "s", "b", diagnostics = "App: 1.0", email = "a@b.co")
        )
        assertTrue(json.contains("\"diagnostics\":\"App: 1.0\""))
        assertTrue(json.contains("\"email\":\"a@b.co\""))
    }

    @Test
    fun `parseSubmitResponse reads number and url`() {
        val r = FeedbackCodec.parseSubmitResponse("""{"number":42,"url":"https://x/issues/42"}""")
        assertEquals(SubmitResult.Success(42, "https://x/issues/42"), r)
    }

    @Test
    fun `parseSubmitResponse returns Malformed on unexpected or invalid bodies`() {
        assertEquals(SubmitResult.Malformed, FeedbackCodec.parseSubmitResponse("""{"url":"x"}"""))
        assertEquals(SubmitResult.Malformed, FeedbackCodec.parseSubmitResponse("""{"error":"bad"}"""))
        assertEquals(SubmitResult.Malformed, FeedbackCodec.parseSubmitResponse("not json"))
        assertEquals(SubmitResult.Malformed, FeedbackCodec.parseSubmitResponse(""))
    }

    @Test
    fun `submitOutcomeFor maps status codes to the retry policy`() {
        assertEquals(SubmitOutcome.SENT, FeedbackCodec.submitOutcomeFor(200))
        assertEquals(SubmitOutcome.SENT, FeedbackCodec.submitOutcomeFor(201))
        assertEquals(SubmitOutcome.RETRYABLE, FeedbackCodec.submitOutcomeFor(429))
        assertEquals(SubmitOutcome.RETRYABLE, FeedbackCodec.submitOutcomeFor(500))
        assertEquals(SubmitOutcome.RETRYABLE, FeedbackCodec.submitOutcomeFor(503))
        assertEquals(SubmitOutcome.PERMANENT, FeedbackCodec.submitOutcomeFor(400))
        assertEquals(SubmitOutcome.PERMANENT, FeedbackCodec.submitOutcomeFor(415))
        assertEquals(SubmitOutcome.PERMANENT, FeedbackCodec.submitOutcomeFor(404))
    }
}
