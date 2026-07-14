package today.sweetspot.ui.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Renders [content] as a square QR code [ImageBitmap] of [sizePx] × [sizePx] pixels.
 *
 * Pure image generation via ZXing — no camera and no Android view involved, so it works offline
 * and needs no permissions. Uses low error correction to keep a long share payload within a single
 * scannable code. Modules are drawn black on an opaque white background (QR codes must be
 * dark-on-light) so the code stays scannable regardless of the app's light/dark theme; the caller
 * is responsible for the surrounding quiet zone.
 *
 * @param content The text to encode (the share deep link).
 * @param sizePx Output edge length in pixels.
 */
fun qrBitmap(content: String, sizePx: Int): ImageBitmap {
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
        EncodeHintType.MARGIN to 1,
    )
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val bitmap = createBitmap(matrix.width, matrix.height)
    val black = 0xFF000000.toInt()
    val white = 0xFFFFFFFF.toInt()
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            bitmap[x, y] = if (matrix[x, y]) black else white
        }
    }
    return bitmap.asImageBitmap()
}
