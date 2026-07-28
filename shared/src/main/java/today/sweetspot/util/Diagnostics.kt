package today.sweetspot.util

/**
 * Builds the diagnostics block appended to bug reports so the maintainer has the environment context
 * that shapes most issues — with **no personal data**: just app/OS versions, device model, the app
 * language, and the active price zone/source. Pure and unit-tested; the Android values (version,
 * `Build.*`, locale, zone/source) are gathered by the caller and passed in.
 */
object Diagnostics {

    fun build(
        appVersion: String,
        versionCode: Int,
        androidRelease: String,
        sdkInt: Int,
        device: String,
        languageTag: String,
        zoneId: String?,
        source: String?
    ): String = buildString {
        appendLine("App: $appVersion ($versionCode)")
        appendLine("Android: $androidRelease (API $sdkInt)")
        appendLine("Device: $device")
        appendLine("Language: ${languageTag.ifBlank { "system" }}")
        appendLine("Zone: ${zoneId ?: "-"}")
        append("Source: ${source ?: "-"}")
    }
}
