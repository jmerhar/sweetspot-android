package today.sweetspot.util

/**
 * Pure builders + constants for the Help section's external links, so URL/deep-link shaping is
 * testable and lives in one place. Android-free.
 */
object HelpLinks {

    const val WEBSITE_BASE = "https://sweetspot.today"
    const val CONTACT_EMAIL = "hello@sweetspot.today"
    const val PLAY_STORE_ID = "today.sweetspot"
    const val GITHUB_REPO = "jmerhar/sweetspot-android"

    /**
     * The GitHub login the feedback Worker posts as. Reports (and, later, replies) are authored by
     * this bot on the reporter's behalf, so the in-app thread labels its entries as the user's own.
     */
    const val BOT_LOGIN = "sweetspot-support"

    /**
     * Prefix the feedback Worker prepends to a reporter's in-app reply when it posts the comment as the
     * bot (so a reader on GitHub can tell it's the reporter speaking, not the maintainer). In the app
     * the entry is already labelled "You", so the prefix is stripped for display — keeping it here in
     * sync with the Worker's `commentBody`. Must match `server/feedback-worker/src/index.js`.
     */
    const val REPLY_PREFIX = "💬 **Reporter (via app):**"

    /**
     * Localized website URL for [path] (e.g. `"faq"`, `"privacy"`, `"changelog"`). English is served
     * at the root; every other language lives under `/<lang>/`. [languageTag] is a BCP-47 tag — its
     * region is dropped and, if it is a comma-joined list, only the first entry is used; a blank tag
     * falls back to English.
     *
     * A `?lang=<code>` query is always appended so the site honours the app's language even when the
     * browser has a different language saved from a previous visit (the site JS would otherwise
     * redirect to that saved preference), and a `&theme=<light|dark>` query so the site matches the
     * app's current light/dark mode (opened in a Custom Tab, it would otherwise look mismatched). See
     * `site/static/js/main.js` and `site/layouts/partials/head.html`.
     */
    fun localizedUrl(path: String, languageTag: String, dark: Boolean = false, base: String = WEBSITE_BASE): String {
        val lang = languageTag.substringBefore(',').substringBefore('-').lowercase()
        val code = lang.ifBlank { "en" }
        val prefix = if (code == "en") "" else "/$code"
        val theme = if (dark) "dark" else "light"
        return "$base$prefix/$path/?lang=$code&theme=$theme"
    }

    /** Play Store deep link (opens the Play app); [playStoreUrl] is the browser fallback. */
    fun playStoreUri(): String = "market://details?id=$PLAY_STORE_ID"

    fun playStoreUrl(): String = "https://play.google.com/store/apps/details?id=$PLAY_STORE_ID"

    /** Public web URL of a GitHub issue (used by "My reports"). */
    fun issueUrl(number: Int): String = "https://github.com/$GITHUB_REPO/issues/$number"
}
