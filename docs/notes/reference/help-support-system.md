# Help & Support System — Reference

> **Status: implemented and live.** The in-app **Help & support** section, the Cloudflare Worker
> (`feedback.sweetspot.today`), and the GitHub-webhook → Brevo email notifications are all in place.
> This note is the permanent under-the-hood reference: what each piece does, how they fit together, the
> file map, the retry/outbox behaviour, the auth model, and the alternatives considered (kept so the
> design can be revisited without re-deriving it).

## Goal

A **Help & support** section in Settings that:
- absorbs the two guidance actions (replay the onboarding intro; reset the contextual coach-mark tips),
- lets users **report a problem** / **send feedback** from an in-app form (no browser, no GitHub
  account) that lands as a **GitHub issue**,
- lets users **track their own reports** in-app and optionally get **email notifications** on activity,
- surfaces the usual support links (a short in-app quick guide, FAQ, privacy, changelog, rate, contact).

Guiding constraint: **the app never holds a write-capable secret** (an APK is public and the project is
open source). Anything that creates issues or sends email runs behind the Worker. The app only POSTs to
the Worker and reads *public* GitHub data.

---

## In-app UX

`SettingsRoute.Help` opens `HelpSettingsScreen` (a self-contained coordinator on the shared
`SettingsSubScreen` scaffold, its own `rememberSaveable` sub-state: menu / report form / My reports /
quick guide), from a single **Help & support** row on the Settings root menu. The old root "How it
works" row moved *into* Help; "Reset tips" was **promoted out of developer options** into Help; the
version footer + 7-tap developer-options unlock **moved off the root menu** into Help → About.

```
LEARN
  Quick guide          How to use SweetSpot         → in-app screen (offline)
  How it works         See the intro again          → onReplayOnboarding (replays onboarding overlay)
  Reset tips           Show the one-time hints again → onDevResetCoachMarks (+ confirmation snackbar)
  FAQ                  Common questions             → website /faq  (Custom Tab)

SUPPORT
  Report a problem     Something isn't working      → in-app form (category: bug)
  Send feedback        Ideas and suggestions        → in-app form (category: feedback)
  My reports           Track what you've submitted  → in-app list; tap → in-app conversation thread (read + reply)
  Contact us           Email us directly            → mailto:hello@sweetspot.today
  Rate SweetSpot       Rate us on Google Play       → market://details?id=today.sweetspot (browser fallback)

ABOUT
  What's new           Latest changes               → website /changelog  (Custom Tab)
  Privacy policy       How your data is handled     → website /privacy    (Custom Tab)
  SweetSpot vX.Y · GPL v3 · Open source on GitHub   (whole footer is the 7-tap dev-options unlock target)
```

The stats opt-in stays on the Settings **root** menu (a toggle row), not under Help.

**Quick guide** — a short, offline, scrollable screen (icon + heading + one line, 6 items): find the
cheapest time; save appliances; plan EV charging; read the result (Earlier/Cheaper + press-and-hold
chart); all-in price; Wear OS. The **all-in item is shown only where `allInSupported`** (same visibility
as the Total price sub-screen), so it isn't advertised where there's no tariff feed.

On entry, a `LaunchedEffect` calls `loadMyReports()` + `flushOutbox()`.

---

## End-to-end architecture

```
                        ┌──────────────────────────── Android app ────────────────────────────┐
                        │  Help sub-screen · Report/Feedback form · My reports · Quick guide    │
                        └───────┬──────────────────────────────────────────────┬───────────────┘
      submit (POST, no secret)  │                                               │  read status (public, no auth)
                                ▼                                               ▼
              ┌───────────────────────────────┐                   GET api.github.com/repos/jmerhar/sweetspot-android/issues/{n}
              │   Cloudflare Worker            │                   (60 req/hr per IP, public repo)
              │   feedback.sweetspot.today     │
              │   • validate + length caps     │
              │   • rate-limit (Workers KV)    │
              │   • create issue (GitHub API)  │──── auth: bot classic PAT (sweetspot-support) ──▶ GitHub Issues
              │   • KV: issue# → email (opt-in)│                                                   (labels: from-app + bug|enhancement)
              └───────────────▲───────────────┘
                              │ webhook (issues, issue_comment)
                              │ HMAC-verified (WEBHOOK_SECRET)
                    GitHub ───┘
                              │ Worker looks up email by issue#, sends via Brevo
                              ▼
                    Brevo transactional API ──▶ notification email to the reporter (SPF/DKIM/DMARC aligned)
```

Three independent flows:
1. **Submit** — app → Worker → GitHub issue. The only path that needs a secret (server-side).
2. **Track** — app reads *public* issue state directly from GitHub. No secret, no Worker.
3. **Notify** — GitHub webhook → Worker → Brevo → reporter email. Opt-in; the email is never made public.

---

## Implementation map

### `:shared` (pure, unit-tested)
- **`model/FeedbackReport.kt`** — `ReportCategory` (`BUG`/`FEEDBACK`; `wireValue` = `"bug"`/`"feedback"`);
  `@Serializable FeedbackReport(category, subject, body, diagnostics?, email?)` (the `/report` body);
  `@Serializable MyReport(number, subject, category, submittedAtMs)` (a tracked report);
  `@Serializable PendingReport(report, createdAtMs, attempts)` (an outbox entry — carries the full
  request incl. the diagnostics captured at submit time, so a retry sends the identical payload).
- **`data/support/FeedbackCodec.kt`** — `encodeRequest(report)` (kotlinx-serialization);
  `parseSubmitResponse(body)` → `SubmitResult.Success(number,url)` / `Malformed` (lenient `Json`,
  sealed-result idiom); `submitOutcomeFor(code)` → `SubmitOutcome.SENT` (2xx) / `RETRYABLE` (429, 5xx) /
  `PERMANENT` (other 4xx). Mirrors `SetupShare` + `StatsReporter.reportOutcomeFor`.
- **`util/Diagnostics.kt`** — `build(appVersion, versionCode, androidRelease, sdkInt, device,
  languageTag, zoneId, source)` → a **no-PII** diagnostics block appended to bug reports (app
  version/build, Android release + SDK int, manufacturer + model, app language tag, price-zone id, data
  source name). No name, no email, no IP, no location. A test asserts `"name"` never appears.
- **`util/HelpLinks.kt`** — constants (`WEBSITE_BASE`, `CONTACT_EMAIL`, `PLAY_STORE_ID`, `GITHUB_REPO`);
  `localizedUrl(path, languageTag, dark)` → English at root else `/<lang>/`, **always** carrying
  `?lang=<code>&theme=<light|dark>` (region and comma-joined tags reduced to the first 2-letter code);
  `playStoreUri()`/`playStoreUrl()`; `issueUrl(number)`.
- **`data/api/GithubIssueApi.kt`** — reads **public** issue data (unauthenticated, 60 req/hr per IP).
  `fetch(n)` → `IssueStatus(number, state, title, comments, htmlUrl)` for "My reports"; `fetchThread(n)`
  → `IssueThread` (title/state/htmlUrl + `ThreadItem`s = the issue body then comments) for the in-app
  conversation. Follows the three-layer `fetch → fetchRaw + parse` pattern (shared `getRaw`,
  `HttpException` on non-2xx); `parseThread` marks each `ThreadItem.mine` when authored by the bot.
  `open` so tests fake it.
- **`SettingsRepository`** — `getMyReports()`/`addMyReport(...)` and `getOutbox()`/`setOutbox(...)` over
  the existing `getJson`/`putJson` helpers (keys `my_reports`, `report_outbox`).

### `:app`
- **`data/support/ReportSubmitter.kt`** — `fun interface ReportSubmitter { fun submit(json):
  SubmitHttpResult }` (`code` + `body`); production `HttpReportSubmitter` POSTs via `HttpURLConnection`
  (reads `inputStream` on 2xx else `errorStream`), mirroring `HttpStatsPoster`. **Coverage-excluded**
  (thin IO); a fake drives the ViewModel tests.
- **`SweetSpotViewModel`** — owns the flow. `ReportSubmission` (`Idle` / `Submitting` /
  `Success(number?,url?)` / `Error(retrying)`); `MyReportView(report, status?)` + the tested
  `MyReportView.issueUrl` extension (issue `html_url` when fetched, else derived from the number).
  Handlers: `onSubmitReport(category, subject, body, notifyEmail?)`, `onDismissReportResult()`,
  `flushOutbox()`, `loadMyReports()`; private `buildReport(...)`, `trySubmit(...)`, `SubmitAttempt`.
  Injectable `reportSubmitterOverride` / `githubIssueApiOverride`. `HttpReportSubmitter(BuildConfig.
  VERSION_NAME)` / `GithubIssueApi()` are the real defaults.
- **`ui/settings/HelpSettingsScreen.kt`** (coverage-excluded) — the coordinator + report form + My
  reports + quick guide, and the Custom Tab / mail / Play launchers. Determines `isSystemInDarkTheme()`
  (the app applies its theme via `AppCompatDelegate.setDefaultNightMode`, so this reflects the effective
  mode) and passes it to `localizedUrl(..., dark)` and the Custom Tab colour scheme.
- **Relocations** — `SettingsRoute.Help` added; the root "How it works" row and version footer removed
  from `SettingsMenu`; "Reset tips" removed from `DeveloperSection`/`AdvancedSettingsScreen`. New
  callbacks threaded `MainActivity` → `SettingsScreen` → `HelpSettingsScreen`.

### Coverage rule
Logic lives in the tested classes (`:shared` codec/diagnostics/links/API + repo store, and the
ViewModel handlers driven by a fake `ReportSubmitter` + fake `GithubIssueApi`). The only new
coverage-excluded code is `HttpReportSubmitter` (+ its companion) and the Composables.

---

## Behaviour details

### Retry / outbox (never lose a report, never duplicate one)
`onSubmitReport` builds the request (bug reports get the diagnostics block), then classifies the attempt
via `submitOutcomeFor`:
- **SENT** — persist a `MyReport`, show `Success` ("Report #N filed"), refresh My reports.
- **RETRYABLE** (429 / 5xx / network exception) — append a `PendingReport` to the **outbox** and show
  `Error(retrying = true)`. The form keeps its content but offers **no manual Retry** — the outbox will
  resend automatically, and a manual resend would create a *duplicate* issue (there's no client
  idempotency key). The user taps Done; the copy is safe in the outbox.
- **PERMANENT** (other 4xx — validation) — show `Error(retrying = false)`, **not** queued. Nothing was
  created, so the form lets the user edit and resend (a fresh submit, no duplicate risk).

`flushOutbox()` runs on ViewModel init and on Help open. It is guarded by a `flushJob` (no concurrent
flushes) and resends each queued report: **SENT** → move to My reports; **RETRYABLE** → keep, bumping
`attempts` (dropped once it reaches `maxOutboxAttempts = 5`); **PERMANENT** → drop. All outbox
read-modify-writes (the submit-time append and the flush rewrite) are serialised by an `outboxMutex`,
and the flush **reconciles** against the current outbox — it re-writes `newcomers + remaining`, so a
report queued *while a flush is in flight* isn't clobbered.

### My reports
Shows `#N · subject`, the live **Open/Closed** state, and a **comment count** (chat icon + number) when
there's activity — the in-app signal that a maintainer replied (email-opted reporters also get notified
out of band). Tapping a row opens `issueUrl` in a Custom Tab. `loadMyReports()` fetches statuses **in
parallel**, capped to the most recent `maxTrackedStatusFetch = 20`, to bound the unauthenticated GitHub
quota; older entries render without a live status.

### Unread indicator
"My reports" shows an unread dot when an issue has more comments than the user has seen. The seen count
per report is persisted (`SettingsRepository.getSeenComments`/`markThreadSeen`); `loadMyReports` sets
`MyReportView.hasUnread = status.comments > seen`, and opening the thread marks it seen (comment count =
thread items − the issue body) and clears the dot.

### In-app replies
`ThreadScreen` shows a reply composer when the open report has a stored `replyToken` (i.e. this device
submitted it — `MyReportView`/`MyReport.replyToken`). Sending calls the ViewModel's `onSendReply`, which
POSTs `{issue, token, body}` to the Worker `/reply` (via `ReportSubmitter.submitReply`). It mirrors the
report-submit retry policy via `FeedbackCodec.submitOutcomeFor`: **SENT** appends the reply to the open
thread **optimistically** (as the user's own — GitHub's public REST API is edge-cached for unauthenticated
reads, so an immediate refetch usually wouldn't see the just-posted comment; the next thread open reloads
canonically); a **transient** failure queues the reply in a **reply outbox**
(`PendingReply`, `SettingsRepository.get/setReplyOutbox`) and shows `ReplyState.QUEUED`; a **permanent**
4xx shows `ReplyState.ERROR`. `flushReplyOutbox` (on VM init + Help open, guarded by `replyFlushJob`,
reconciling under `reportStoreMutex` with the same attempts cap as the report outbox) resends queued
replies and reloads the open thread if any lands. The draft clears on SENT or QUEUED (kept on error).
The composer states plainly that the reply is posted publicly. The webhook skips bot-authored comments,
so a reporter's own reply doesn't email them; a maintainer's reply still does.

Comment bodies (issue body + replies) render as **markdown** via `compose-markdown`
(`com.github.jeziellago:compose-markdown`, JitPack) so GitHub formatting (bold, lists, links, code)
shows properly.

### Website links (Custom Tabs + theming)
FAQ / privacy / changelog / What's new open in a **Chrome Custom Tab** (`androidx.browser`), toolbar
themed to `MaterialTheme.colorScheme.surface`, light/dark chrome matching the app; falls back to the
default browser if no Custom Tabs provider exists. The URL carries `?lang=<code>&theme=<light|dark>` so
the site matches the app. On the **site**, an inline script in `head.html` (before the stylesheet, so no
flash) reads `?theme=`, persists it to `sessionStorage`, and sets `data-theme="dark"`; `style.css` has a
`:root[data-theme="dark"]` palette override (surface/text flip; brand colours, the blue hero, and the
dark footer stay). `main.js` treats an explicit `?lang=` as an assertion (saves it, moves to the
matching path, suppresses the saved-preference redirect) — otherwise the site's remembered language
would override the app's deep link. **These site behaviours require a site deploy to take effect.**

---

## Cloudflare Worker (`server/feedback-worker/`)

Hostname **`feedback.sweetspot.today`** (Workers route). Endpoints:

**`POST /report`**
```jsonc
// request
{ "category": "bug" | "feedback",
  "subject":  "string (<=120)",
  "body":     "string (<=4000)",
  "diagnostics": "string, optional (bug only)",
  "email":    "string, optional — only if the user opted into notifications" }
// response 201
{ "number": 123, "url": "https://github.com/jmerhar/sweetspot-android/issues/123" }
// 400 invalid · 415 wrong content-type · 429 rate-limited · 5xx upstream
```
Steps: validate + length caps → per-IP rate-limit via **Workers KV** → create the issue via the GitHub
API with labels **`from-app`** + **`bug`**/**`enhancement`** → always store `issue:{n} → {email?, token}`
in KV (email only if opted in; the token is the report's capability) → return `{number, url, replyToken}`.

**`POST /reply`** — the app posts a comment on its own report: `{issue, token, body}`. The Worker
verifies the token (constant-time) against the stored one, then creates a GitHub comment **as the bot**
(prefixed to mark it's from the reporter), rate-limited per IP (`REPLY_RATE_LIMIT_PER_DAY`). Only a
device that submitted the report (and thus holds `replyToken`) can reply — the app has no GitHub
identity, so everything it posts is bot-authored. **Gotcha:** `readSubscription` must return the stored
`token` **regardless of whether an email is present** — the KV entry is `{email:null, token}` for the
common no-email report, so a guard that only accepts a string `email` would drop the token and 403 every
reply/unsubscribe for those reports. The token, not the email, is the capability.

**`POST /webhook`** — GitHub webhook target. Verifies HMAC with `WEBHOOK_SECRET`; on issue activity not
authored by the bot, looks up the email in KV and sends a notification via the **Brevo transactional
API**, including a tokenized unsubscribe link. **`GET /unsubscribe?issue=N&token=…`** — a non-mutating
confirmation page (so an email link prefetcher can't unsubscribe anyone); **`POST /unsubscribe`** (the
form submit, or an RFC 8058 one-click `List-Unsubscribe=One-Click` body) clears the stored **email**
when the token matches (constant-time), keeping the token so in-app replies still work. **`GET /`** —
health check.

**Secrets** (Worker, never in app/repo): `GITHUB_TOKEN` (bot classic `public_repo` PAT), `WEBHOOK_SECRET`,
`BREVO_API_KEY`. **KV** holds the `issue# → {email?, token}` map and rate-limit counters (it
also tolerates legacy bare-email entries, which simply get no unsubscribe link). Repo owner/name and
label names are configured in `wrangler.jsonc`.

---

## GitHub & auth

- Repo `jmerhar/sweetspot-android` (public). Labels **`from-app`** (marker) + **`bug`**/**`enhancement`**
  (category). Filter app submissions with `label:from-app`.
- Bot identity **`sweetspot-support`** (`hello@sweetspot.today`), a **write collaborator**, authenticating
  with a **classic `public_repo` PAT** (issues authored by `sweetspot-support`).

### Auth options (chosen vs. considered)
- **(chosen) Bot classic PAT (`public_repo`).** Works immediately on a collaborator repo. **Trade-off:**
  broader than ideal (`public_repo` can touch any public repo the bot can reach) — acceptable because the
  bot has access to nothing else and the token is Worker-only, but not least-privilege.
- **(recommended future) GitHub App**, Issues: Read & write, installed on this one repo. Tightest scope,
  bot authorship (`sweetspot-support[bot]`), no org, no repo move. Migration is Worker-code-only (mint a
  short RS256 JWT with WebCrypto → 1-hour installation token) plus creating/installing the app.
- **Org + fine-grained PAT** — needs the repo in an **organization** (fine-grained PATs can't target
  another user's personal repo). Rejected: an org transfer is invasive (see blast radius) and not worth
  it just for this.
- **Own fine-grained PAT (no bot)** — tightest/simplest since `jmerhar` owns the repo, but issues are
  authored by *you*, not a bot.

#### Blast radius of moving the repo to an org (why we avoided it)
The website lives in this repo, so an org transfer is invasive: **GitHub Pages** custom-domain binding
(`sweetspot.today`) likely clears and needs re-adding/re-verifying (possible brief downtime); Pages
source + `github-pages` environment may reset; **Actions repo secrets don't carry over** (re-add
`CODECOV_TOKEN`, `COVERAGE_PAGES_TOKEN`, `PLAY_STORE_SERVICE_ACCOUNT_JSON`, `ENEVER_TOKEN`,
`SITE_COMMIT_TOKEN`); org Actions **policies** must be enabled; **`SITE_COMMIT_TOKEN`** must be reissued;
**Codecov** slug changes (re-authorise, reissue token, update badge); personal **collaborators are
dropped**; update README badges, git remotes, and `jmerhar/sweetspot-android` references. **Unaffected:**
Play Store, Play signing, Fastlane, `assetlinks.json`, DNS at Cloudflare, commit history/tags/releases/
issues/PRs/stars. The **coverage site** (`jmerhar/coverage`) is keyed by repo name, so it keeps working
once the secret is re-added. Only pursue an org for *other* reasons (multiple repos, teams).

---

## Notifications

Opt-in only. The email is provided in the form and stored **only** in Worker KV keyed by issue number —
**never** written into the public issue (public issues get scraped). The form states this explicitly
(`report_email_note`). Pipeline: GitHub webhook → Worker `/webhook` (HMAC-verified) → look up email by
issue# → Brevo → email the reporter with the activity + a link to the issue. Verified end-to-end
(DKIM/SPF/DMARC-aligned).

**Unsubscribe.** Each report carries a random per-report `token` (a `crypto.randomUUID()`, the
capability — also the app's `replyToken`). Every notification includes an `…/unsubscribe?issue=N&token=…`
link and RFC 8058 one-click `List-Unsubscribe` headers. `GET /unsubscribe` only renders a confirmation
page (never mutates — link prefetchers/scanners can't unsubscribe you); the `POST` (form submit or
one-click) clears the stored **email** after a constant-time token check (keeping the token, so in-app
replies still work), and responds identically whether or not an entry existed (no enumeration). The
privacy policy discloses that a provided email is stored to notify the reporter and can be removed via
the unsubscribe link in any notification (or on request).

---

## Anti-abuse
`/report` is unauthenticated from the app's side (any client can POST). Defenses:
- **Baseline (in place):** per-IP rate limit in Workers KV + subject/body length caps + manual moderation.
  Sufficient for a niche app.
- **Optional hardening — Play Integrity API** (the native equivalent of a CAPTCHA): app requests an
  integrity token, Worker verifies it with Google before creating the issue. Add only if bots appear.
- **Not usable:** Turnstile is a web/JS widget — no clean native-Android integration.

---

## Email infrastructure (`hello@sweetspot.today`)
- **Inbound:** Google Workspace MX (backs **Contact us** `mailto:` and receives DMARC reports).
- **Outbound:** **Brevo transactional** (Worker notifications) + Google (send-as); both SPF/DKIM/DMARC
  aligned. The bot's GitHub email is the same address; notifications are sent from
  `notifications@sweetspot.today` (a forwarding alias).
- **DNS (Cloudflare, verified):** MX→Google; SPF `v=spf1 include:_spf.google.com include:spf.brevo.com
  ~all`; DKIM `google._domainkey` + `brevo1/brevo2._domainkey`; DMARC `p=quarantine` with `rua`/`ruf`.
  Optional later hardening: SPF `-all`, DMARC `p=reject`.

---

## Secrets & configuration

| Value | Lives in | Secret? |
|---|---|---|
| Bot classic PAT (`public_repo`) — *or later* GitHub App id + private key | Worker secret (`GITHUB_TOKEN`) | 🔒 never in app/repo |
| `WEBHOOK_SECRET` (GitHub webhook HMAC) | Worker secret | 🔒 |
| `BREVO_API_KEY` (transactional email) | Worker secret | 🔒 |
| `issue# → {email?, token}` map, rate-limit counters | Workers KV | 🔒 (server-side, not public) |
| Worker URL, repo owner/name, Play Store id, contact email, website base | baked in app (`HelpLinks`) | no |

---

## Alternatives considered
- **Backend host:** Cloudflare Worker (**chosen** — serverless, already on Cloudflare, holds the token,
  cheap) vs. the **aurora** PHP host (a VM to maintain) vs. **GitHub Pages** (❌ static, can't hold a
  secret) vs. **Google Forms** (❌ browser hand-off, siloed from GitHub triage).
- **Issue auth:** classic PAT (chosen) vs. GitHub App (recommended future) vs. org + fine-grained vs. own
  PAT — see auth section.
- **Baking a PAT into the app:** ❌ never — an APK is public; GitHub secret scanning would auto-revoke it
  and it could be abused as the bot.
- **Outbound email:** Brevo (chosen — already configured & domain-aligned) vs. Resend (extra service).
- **Report via prefilled GitHub issue URL:** ❌ opens a browser and requires a GitHub account; the in-app
  form is friendlier and account-free.
- **Website theming:** explicit `?theme=` parameter from the app (chosen — simple, matches the app) vs. a
  `prefers-color-scheme` / on-site toggle (deferred; no site UI yet).
- **In-app browser:** Chrome Custom Tabs (chosen — inherits the browser engine so our JS/redirects/
  responsive site work, themed toolbar, minimal code) vs. a full `WebView` (❌ own the cookies/JS/
  security/navigation, worse for external content).

## Security posture
- The app holds **no** write secret; it only POSTs to the Worker and reads *public* GitHub data.
- The one over-scoped element is the bot's **classic `public_repo`** token. Mitigated by: the bot has no
  other access, the token is Worker-only, and it can be rotated. **Recommended migration: GitHub App**
  (least privilege, same bot authorship) when convenient — a Worker-code-only change plus app creation.
- Reporter emails are opt-in, stored server-side only, never in public issues; disclosed in the privacy
  policy with a removal path.

## Possible future work
- Migrate issue auth to a **GitHub App** (least privilege).
- **Play Integrity** anti-abuse if spam appears.
- Website **dark-mode toggle** / `prefers-color-scheme` for direct visitors.
