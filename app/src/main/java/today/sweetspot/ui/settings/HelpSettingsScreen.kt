package today.sweetspot.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.coroutines.launch
import today.sweetspot.BuildConfig
import today.sweetspot.MyReportView
import today.sweetspot.R
import today.sweetspot.ReplyState
import today.sweetspot.ReportSubmission
import today.sweetspot.ThreadState
import today.sweetspot.model.ReportCategory
import today.sweetspot.shared.R as SharedR
import today.sweetspot.util.HelpLinks

/** Which Help screen is showing (a small self-contained coordinator, no nav library). */
private enum class HelpRoute { Menu, Form, MyReports, QuickGuide }

/**
 * The Help & feedback section: guidance actions (replay intro, reset tips), a report/feedback form
 * that submits to the feedback Worker, an in-app "My reports" tracker (public GitHub status), a short
 * quick guide, and support links (FAQ, privacy, changelog, rate, contact). Also hosts the version
 * footer + 7-tap developer-options unlock, relocated here from the Settings root menu.
 *
 * Self-contained: an internal [HelpRoute] switches between the menu and its sub-screens; [onBack]
 * returns to the Settings root. On open it refreshes "My reports" and flushes any queued submissions.
 */
@Composable
internal fun HelpSettingsScreen(
    reportSubmission: ReportSubmission,
    myReports: List<MyReportView>,
    thread: ThreadState?,
    replySubmission: ReplyState,
    allInSupported: Boolean,
    devOptionsEnabled: Boolean,
    onReplayOnboarding: () -> Unit,
    onResetCoachMarks: () -> Unit,
    onSubmitReport: (ReportCategory, subject: String, body: String, notifyEmail: String?) -> Unit,
    onDismissReportResult: () -> Unit,
    onLoadMyReports: () -> Unit,
    onFlushOutbox: () -> Unit,
    onOpenThread: (Int) -> Unit,
    onCloseThread: () -> Unit,
    onSendReply: (Int, String) -> Unit,
    onDevOptionsUnlocked: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var route by rememberSaveable { mutableStateOf(HelpRoute.Menu) }
    var formCategory by rememberSaveable { mutableStateOf(ReportCategory.BUG) }
    val toMenu = { route = HelpRoute.Menu }

    LaunchedEffect(Unit) {
        onLoadMyReports()
        onFlushOutbox()
    }

    when (route) {
        HelpRoute.Menu -> HelpMenu(
            modifier = modifier,
            devOptionsEnabled = devOptionsEnabled,
            onReplayOnboarding = onReplayOnboarding,
            onResetCoachMarks = onResetCoachMarks,
            onOpenForm = { formCategory = it; route = HelpRoute.Form },
            onOpenMyReports = { route = HelpRoute.MyReports },
            onOpenQuickGuide = { route = HelpRoute.QuickGuide },
            onDevOptionsUnlocked = onDevOptionsUnlocked,
            onBack = onBack
        )

        HelpRoute.Form -> ReportFormScreen(
            category = formCategory,
            submission = reportSubmission,
            onSubmit = onSubmitReport,
            onDismiss = onDismissReportResult,
            onBack = { onDismissReportResult(); toMenu() }
        )

        HelpRoute.MyReports ->
            if (thread != null) {
                ThreadScreen(
                    state = thread,
                    replySubmission = replySubmission,
                    onReply = onSendReply,
                    onRetry = { onOpenThread(thread.number) },
                    onBack = onCloseThread
                )
            } else {
                MyReportsScreen(reports = myReports, onOpenReport = onOpenThread, onBack = toMenu)
            }

        HelpRoute.QuickGuide -> QuickGuideScreen(allInSupported = allInSupported, onBack = toMenu)
    }
}

/** The Help root menu: grouped rows plus the About/version footer. */
@Composable
private fun HelpMenu(
    modifier: Modifier,
    devOptionsEnabled: Boolean,
    onReplayOnboarding: () -> Unit,
    onResetCoachMarks: () -> Unit,
    onOpenForm: (ReportCategory) -> Unit,
    onOpenMyReports: () -> Unit,
    onOpenQuickGuide: () -> Unit,
    onDevOptionsUnlocked: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val languageTag = remember {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) "" else locales.toLanguageTags()
    }
    // Open website pages in a Custom Tab, themed to the app and carrying the app's language + light/dark
    // mode so the page matches (see HelpLinks.localizedUrl + the site's head/main.js).
    val dark = isSystemInDarkTheme()
    val toolbarColor = MaterialTheme.colorScheme.surface.toArgb()
    fun openWebsite(path: String) =
        openInCustomTab(context, HelpLinks.localizedUrl(path, languageTag, dark), dark, toolbarColor)

    val tipsResetMessage = stringResource(R.string.tips_reset_snackbar)

    SettingsSubScreen(
        title = stringResource(R.string.settings_help_title),
        onBack = onBack,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    ) {
        SectionHeader(stringResource(R.string.help_section_learn))
        SettingsMenuRow(SharedR.drawable.ic_menu_book, stringResource(R.string.help_quick_guide_title),
            stringResource(R.string.help_quick_guide_desc), onClick = onOpenQuickGuide)
        SettingsMenuRow(SharedR.drawable.ic_info, stringResource(R.string.settings_how_it_works),
            stringResource(R.string.settings_how_it_works_desc), onClick = onReplayOnboarding)
        SettingsMenuRow(SharedR.drawable.ic_info, stringResource(R.string.help_reset_tips_title),
            stringResource(R.string.help_reset_tips_desc), onClick = {
                onResetCoachMarks()
                scope.launch { snackbarHostState.showSnackbar(tipsResetMessage) }
            })
        SettingsMenuRow(SharedR.drawable.ic_help, stringResource(R.string.help_faq_title),
            stringResource(R.string.help_faq_desc), onClick = { openWebsite("faq") })

        SectionHeader(stringResource(R.string.help_section_support))
        SettingsMenuRow(SharedR.drawable.ic_bug_report, stringResource(R.string.help_report_title),
            stringResource(R.string.help_report_desc), onClick = { onOpenForm(ReportCategory.BUG) })
        SettingsMenuRow(SharedR.drawable.ic_feedback, stringResource(R.string.help_feedback_title),
            stringResource(R.string.help_feedback_desc), onClick = { onOpenForm(ReportCategory.FEEDBACK) })
        SettingsMenuRow(SharedR.drawable.ic_receipt_long, stringResource(R.string.help_my_reports_title),
            stringResource(R.string.help_my_reports_desc), onClick = onOpenMyReports)
        SettingsMenuRow(SharedR.drawable.ic_mail, stringResource(R.string.help_contact_title),
            stringResource(R.string.help_contact_desc), onClick = { sendEmail(context) })
        SettingsMenuRow(SharedR.drawable.ic_star, stringResource(R.string.help_rate_title),
            stringResource(R.string.help_rate_desc), onClick = { openPlayStore(context, uriHandler) })

        SectionHeader(stringResource(R.string.help_section_about))
        SettingsMenuRow(SharedR.drawable.ic_new_releases, stringResource(R.string.help_whats_new_title),
            stringResource(R.string.help_whats_new_desc), onClick = { openWebsite("changelog") })
        SettingsMenuRow(SharedR.drawable.ic_policy, stringResource(R.string.help_privacy_title),
            stringResource(R.string.help_privacy_desc), onClick = { openWebsite("privacy") })

        VersionFooter(
            devOptionsEnabled = devOptionsEnabled,
            onDevOptionsUnlocked = onDevOptionsUnlocked,
            onUnlocked = { scope.launch { snackbarHostState.showSnackbar("Developer options enabled") } }
        )
    }
}

/** Section caption between row groups. */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

/** The version line + 7-tap developer-options unlock, relocated from the Settings root menu. */
@Composable
private fun VersionFooter(
    devOptionsEnabled: Boolean,
    onDevOptionsUnlocked: () -> Unit,
    onUnlocked: () -> Unit
) {
    var taps by remember { mutableIntStateOf(0) }
    Spacer(modifier = Modifier.height(8.dp))
    // The whole footer (both lines + its padding) is the 7-tap target, so any tap on the block counts.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!devOptionsEnabled) {
                    taps++
                    if (taps >= 7) { onDevOptionsUnlocked(); onUnlocked() }
                }
            }
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "SweetSpot v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = stringResource(R.string.help_about_license),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/** Report-a-problem / send-feedback form. */
@Composable
private fun ReportFormScreen(
    category: ReportCategory,
    submission: ReportSubmission,
    onSubmit: (ReportCategory, String, String, String?) -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit
) {
    var subject by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    // Start from a clean submission state each time the form opens.
    LaunchedEffect(Unit) { onDismiss() }

    val isBug = category == ReportCategory.BUG
    val submitting = submission is ReportSubmission.Submitting

    SettingsSubScreen(
        title = stringResource(if (isBug) R.string.help_report_title else R.string.help_feedback_title),
        onBack = onBack
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.report_public_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = subject, onValueChange = { subject = it },
                label = { Text(stringResource(R.string.report_subject_label)) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = body, onValueChange = { body = it },
                label = { Text(stringResource(if (isBug) R.string.report_body_label_bug else R.string.report_body_label_feedback)) },
                minLines = 4, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text(stringResource(R.string.report_email_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.report_email_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            when (val s = submission) {
                is ReportSubmission.Success -> {
                    Text(
                        text = if (s.number != null) stringResource(R.string.report_filed, s.number)
                        else stringResource(R.string.report_sent),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { subject = ""; body = ""; email = ""; onBack() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.report_done)) }
                }
                is ReportSubmission.Error -> if (s.retrying) {
                    // Transient failure: the report is already queued in the outbox and will be resent
                    // automatically. No manual Retry here — a manual resend would duplicate the report
                    // (the queued copy still flushes later). The content is safe in the outbox.
                    Text(
                        text = stringResource(R.string.report_error_retry),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { subject = ""; body = ""; email = ""; onBack() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.report_done)) }
                } else {
                    // Permanent failure (validation): nothing was queued, so letting the user edit and
                    // resend is safe — it can't duplicate anything.
                    Text(
                        text = stringResource(R.string.report_error_permanent),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onSubmit(category, subject, body, email.ifBlank { null }) },
                        enabled = subject.isNotBlank() && body.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.report_retry)) }
                }
                else -> {
                    Button(
                        onClick = { onSubmit(category, subject, body, email.ifBlank { null }) },
                        enabled = !submitting && subject.isNotBlank() && body.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.report_submitting))
                        } else {
                            Text(stringResource(R.string.report_submit))
                        }
                    }
                }
            }
        }
    }
}

/** In-app tracker: the reports this device submitted, with their live GitHub status. */
@Composable
private fun MyReportsScreen(
    reports: List<MyReportView>,
    onOpenReport: (Int) -> Unit,
    onBack: () -> Unit
) {
    SettingsSubScreen(title = stringResource(R.string.help_my_reports_title), onBack = onBack) {
        if (reports.isEmpty()) {
            Text(
                text = stringResource(R.string.my_reports_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Newest first.
            reports.asReversed().forEach { view ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenReport(view.report.number) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("#${view.report.number} · ${view.report.subject}", style = MaterialTheme.typography.bodyLarge)
                        val statusText = when (view.status?.state) {
                            "closed" -> stringResource(R.string.my_reports_closed)
                            "open" -> stringResource(R.string.my_reports_open)
                            else -> ""
                        }
                        val comments = view.status?.comments ?: 0
                        // Status line: Open/Closed plus, when there's been a reply, a comment count —
                        // the in-app signal that a report has activity (reporters with an email also
                        // get notified out-of-band).
                        if (statusText.isNotEmpty() || comments > 0) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                if (statusText.isNotEmpty()) {
                                    Text(statusText, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (comments > 0) {
                                    if (statusText.isNotEmpty()) {
                                        Text(" · ", style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(
                                        painter = painterResource(SharedR.drawable.ic_feedback),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$comments", style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * In-app conversation for one report: the issue body + comments, read from the public GitHub API.
 * Entries authored by the bot login are the reporter's own ("You"); anyone else is "SweetSpot"
 * (the maintainer). An "Open on GitHub" link is always available (and is the fallback on error).
 */
@Composable
private fun ThreadScreen(
    state: ThreadState,
    replySubmission: ReplyState,
    onReply: (Int, String) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    SettingsSubScreen(title = "#${state.number}", onBack = onBack) {
        when (state) {
            is ThreadState.Loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { CircularProgressIndicator() }

            is ThreadState.Error -> Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.thread_load_error), color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.report_retry))
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { uriHandler.openUri(HelpLinks.issueUrl(state.number)) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.thread_open_github)) }
            }

            is ThreadState.Loaded -> Column(modifier = Modifier.padding(16.dp)) {
                Text(state.thread.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (state.thread.state == "closed") stringResource(R.string.my_reports_closed)
                    else stringResource(R.string.my_reports_open),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                state.thread.items.forEach { item ->
                    Text(
                        text = if (item.mine) stringResource(R.string.thread_you) else "SweetSpot",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (item.mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(item.body.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (state.canReply) {
                    ReplyComposer(
                        submission = replySubmission,
                        onSend = { text -> onReply(state.number, text) }
                    )
                }
                TextButton(
                    onClick = { uriHandler.openUri(state.thread.htmlUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.thread_open_github)) }
            }
        }
    }
}

/**
 * Reply composer shown under a thread this device can reply to: a public-visibility note, a text box,
 * and a Send button. The draft clears only after a *successful* send (the submission transitions from
 * SENDING to IDLE); a failure keeps the text and shows an error so the user can retry.
 */
@Composable
private fun ReplyComposer(submission: ReplyState, onSend: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var previous by remember { mutableStateOf(submission) }
    LaunchedEffect(submission) {
        if (previous == ReplyState.SENDING && submission == ReplyState.IDLE) text = ""
        previous = submission
    }
    val sending = submission == ReplyState.SENDING

    Text(
        text = stringResource(R.string.reply_public_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = text, onValueChange = { text = it },
        label = { Text(stringResource(R.string.reply_hint)) },
        minLines = 2, enabled = !sending, modifier = Modifier.fillMaxWidth()
    )
    if (submission == ReplyState.ERROR) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.reply_error),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = { onSend(text) },
        enabled = !sending && text.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (sending) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.report_submitting))
        } else {
            Text(stringResource(R.string.reply_send))
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

/** A single quick-guide item: icon + heading + one-line body. */
private data class GuideItem(@param:DrawableRes val icon: Int, @param:StringRes val title: Int, @param:StringRes val body: Int)

private val quickGuideItems = listOf(
    GuideItem(SharedR.drawable.ic_price, R.string.guide_find_title, R.string.guide_find_body),
    GuideItem(SharedR.drawable.ic_device, R.string.guide_appliances_title, R.string.guide_appliances_body),
    GuideItem(SharedR.drawable.ic_ev_charger, R.string.guide_ev_title, R.string.guide_ev_body),
    GuideItem(SharedR.drawable.ic_stats, R.string.guide_result_title, R.string.guide_result_body),
    GuideItem(SharedR.drawable.ic_price, R.string.guide_allin_title, R.string.guide_allin_body),
    GuideItem(SharedR.drawable.ic_device, R.string.guide_wear_title, R.string.guide_wear_body)
)

/**
 * A short, offline how-to reference — the always-available companion to the onboarding intro. The
 * all-in-price item is shown only where all-in is available (matching the Settings › Total price
 * sub-screen's own visibility), so it isn't advertised in regions without a tariff feed.
 */
@Composable
private fun QuickGuideScreen(allInSupported: Boolean, onBack: () -> Unit) {
    val items = if (allInSupported) quickGuideItems
    else quickGuideItems.filterNot { it.title == R.string.guide_allin_title }
    SettingsSubScreen(title = stringResource(R.string.help_quick_guide_title), onBack = onBack) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    painter = painterResource(item.icon), contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(item.title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(item.body), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * Opens [url] in a Chrome Custom Tab — an in-app browser tab themed to the app ([toolbarColor], and a
 * [dark]/light colour scheme for the tab chrome) so website pages feel integrated. If no Custom Tabs
 * provider is installed the intent falls back to the default browser automatically.
 */
private fun openInCustomTab(context: Context, url: String, dark: Boolean, toolbarColor: Int) {
    val colorParams = CustomTabColorSchemeParams.Builder().setToolbarColor(toolbarColor).build()
    val intent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setColorScheme(if (dark) CustomTabsIntent.COLOR_SCHEME_DARK else CustomTabsIntent.COLOR_SCHEME_LIGHT)
        .setDefaultColorSchemeParams(colorParams)
        .build()
    try {
        intent.launchUrl(context, Uri.parse(url))
    } catch (_: Exception) {
        // No browser at all to handle the link — nothing to do.
    }
}

/** Opens the device email composer to the contact address. */
private fun sendEmail(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${HelpLinks.CONTACT_EMAIL}"))
    try {
        context.startActivity(Intent.createChooser(intent, null))
    } catch (_: Exception) {
        // No mail app — nothing to do.
    }
}

/** Opens the Play Store listing (Play app, or browser fallback). */
private fun openPlayStore(context: android.content.Context, uriHandler: androidx.compose.ui.platform.UriHandler) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(HelpLinks.playStoreUri())))
    } catch (_: Exception) {
        uriHandler.openUri(HelpLinks.playStoreUrl())
    }
}
