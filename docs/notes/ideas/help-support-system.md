# Help & Support System — Architecture & Setup Reference

> **Status:** design + infra reference (feature not yet implemented). Some infrastructure is already
> provisioned (see [Current status](#current-status)). This note captures the whole architecture — app,
> Cloudflare Worker, GitHub, email, DNS — the chosen approach, and every alternative considered, so the
> design can be revisited without re-deriving it.

## Goal

Add a **Help & feedback** section to Settings that:
- absorbs the two guidance actions we added recently (replay the onboarding intro; reset the contextual
  coach-mark tips),
- lets users **report a problem** and **send feedback** from an in-app form (no browser, no GitHub
  account) that lands as a **GitHub issue**,
- lets users **track their own reports** in-app and optionally get **email notifications** on activity,
- surfaces the usual support links (FAQ, privacy, changelog, rate, contact email, a short quick guide).

The guiding constraint: **the app must never hold a write-capable secret** (an APK is public and the
project is open source). Anything that creates issues or sends email runs behind a server we control.

---

## The Help section (in-app UX)

A new `SettingsRoute.Help` sub-screen (same pattern as Region/Appearance — a `SettingsSubScreen`
scaffold with `SettingsMenuRow`s), opened from a single **Help & feedback** row on the Settings root
menu. The standalone "How it works" row moves *into* Help; "Reset tips" is **promoted out of developer
options** into Help. Rows, grouped:

```
LEARN
  Quick guide          How to use SweetSpot         → in-app screen (offline)
  How it works         See the intro again          → onReplayOnboarding
  Reset tips           Show the hints again         → onResetCoachMarks
  FAQ                  Common questions             → website /faq

SUPPORT
  Report a problem     Something's not working      → in-app form (label: bug)
  Send feedback        Ideas and suggestions        → in-app form (label: enhancement)
  My reports           Track what you've submitted  → in-app list (public GH reads)
  Contact us           Email us directly            → mailto:hello@sweetspot.today
  Rate SweetSpot       Rate us on Google Play       → Play Store

ABOUT
  What's new           Latest changes               → website /changelog
  Privacy policy       → website /privacy
  SweetSpot vX.Y       GPL v3 · source on GitHub    (hosts the 7-tap dev-options unlock)
```

The stats opt-in stays on the Settings **root** menu (not buried under Help).

**Quick guide** — a short, offline, scrollable in-app screen (icon + one-line heading + 1–2 sentence
body per item, ~6 items): find the cheapest time; save appliances; plan EV charging; read the result
(Earlier/Cheaper + press-and-hold chart); all-in price (NL); Wear OS. It's the always-available,
slightly deeper cousin of the onboarding intro; the FAQ link carries the long tail.

---

## End-to-end architecture

```
                        ┌──────────────────────────── Android app ────────────────────────────┐
                        │  Help sub-screen · Report/Feedback form · My reports · Quick guide    │
                        └───────┬──────────────────────────────────────────────┬───────────────┘
      submit (POST, no secret)  │                                               │  read status (public, no auth)
                                ▼                                               ▼
              ┌───────────────────────────────┐                   GET api.github.com/repos/OWNER/REPO/issues/{n}
              │   Cloudflare Worker            │                   (60 req/hr per IP, public repo)
              │   feedback.sweetspot.today     │
              │   • validate + length caps     │
              │   • rate-limit (Workers KV)    │
              │   • [opt] anti-abuse            │
              │   • create issue (GitHub API)  │──── auth: bot classic PAT (now) / GitHub App (future) ──▶ GitHub Issues
              │   • KV: issue# → email (opt-in)│                                                          (labels: from-app + bug|enhancement)
              └───────────────▲───────────────┘
                              │ webhook (issues, issue_comment)  [Phase 2]
                              │ HMAC-verified (WEBHOOK_SECRET)
                    GitHub ───┘
                              │ Worker looks up email by issue#, sends via Brevo
                              ▼
                    Brevo transactional API ──▶ notification email to the user (SPF/DKIM/DMARC aligned)
```

Three independent flows:
1. **Submit** — app → Worker → GitHub issue. The only path that needs a secret (server-side).
2. **Track** — app reads *public* issue state directly from GitHub. No secret, no Worker.
3. **Notify (Phase 2)** — GitHub webhook → Worker → Brevo → user email. Opt-in; email never made public.

---

## Component details

### 1. App layer
- **Help sub-screen** (`ui/settings/HelpSettingsScreen.kt`, coverage-excluded) on the shared
  `SettingsSubScreen` scaffold; rows via `SettingsMenuRow`. New `SettingsRoute.Help`. Threads the
  existing `onReplayOnboarding` / `onResetCoachMarks` VM callbacks; "Reset tips" removed from
  `DeveloperSection`.
- **Report/Feedback form** — a small form (subject + multiline description + a category derived from
  which row opened it; optional "notify me by email" field). On submit, POSTs to the Worker; shows the
  returned issue number ("Report #123 filed") and stores it locally.
- **My reports** — a local list (SharedPreferences/JSON) of `{issueNumber, subject, category,
  submittedAtMs}` for everything this install submitted. The screen reads each issue's **public** state
  (`state`, `title`, comments) from GitHub's unauthenticated REST API and shows Open/Closed + latest
  activity + a "view on GitHub" deep link. No token needed.
- **Links** — FAQ / privacy / changelog open the localized website page via `LocalUriHandler`
  (`sweetspot.today/<lang>/…`, English at root). Rate opens `market://details?id=today.sweetspot`
  (https fallback). Contact opens `mailto:hello@sweetspot.today`.
- **Pure helper (tested, `:shared`)** — `feedbackBody(appVersion, androidVersion, device, locale, zone,
  source)` builds the diagnostics block appended to bug reports (no PII). Keeps the composable thin.
- **Baked-in config (all non-secret):** Worker URL (`https://feedback.sweetspot.today`), repo
  `jmerhar/sweetspot-android` (for public reads), Play Store id `today.sweetspot`, contact address
  `hello@sweetspot.today`, website base URL.

### 2. Submission pipeline — Cloudflare Worker
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
// 400 invalid · 429 rate-limited · 5xx upstream
```
Worker steps: validate + enforce length caps → per-IP rate-limit via **Workers KV** → (optional
anti-abuse, below) → create the issue via the GitHub API with labels **`from-app`** + **`bug`** or
**`enhancement`** → if `email` present, store `issue:{n} → email` in KV (opt-in only) → return number+url.

**`POST /webhook`** (Phase 2) — GitHub webhook target. Verify HMAC with `WEBHOOK_SECRET`; on
`issue_comment.created` (not authored by the bot) or `issues.closed`, look up the email in KV and send a
notification via Brevo. Include an unsubscribe hint (e.g. reply STOP / a link that deletes the KV entry).

**Bindings/secrets:** KV namespace (`FEEDBACK_KV`); secrets `GITHUB_TOKEN` (bot PAT *or* GitHub App
key material), `WEBHOOK_SECRET`, `BREVO_API_KEY`. Repo owner/name and label names hardcoded.

### 3. GitHub layer
- Repo: `jmerhar/sweetspot-android` (public). Labels: **`from-app`** (marker) + **`bug`** /
  **`enhancement`** (category). Filter app submissions with `label:from-app`.
- Bot identity: **`sweetspot-support`** (email `hello@sweetspot.today`), added as a **write
  collaborator** (personal repos have only one collaborator level — write — which is enough to open
  issues *and* apply labels).

### 4. Authentication options (how the Worker writes issues)
The Worker must authenticate to create issues. Options, in order of preference:

- **(chosen now) Bot account + classic PAT (`public_repo`).** Classic tokens grant access to every repo
  the user can reach *including collaborator repos*, so a `public_repo` token on `sweetspot-support`
  works immediately. Issues are authored by **`sweetspot-support`**. **Trade-off:** broader than ideal —
  `public_repo` can create issues/comment on *any* public repo the bot can access. Acceptable because the
  bot has access to nothing else and the token lives only in the Worker, but it is not least-privilege.
- **(recommended future) GitHub App.** A GitHub App (owned by `jmerhar`) with **Issues: Read & write**,
  installed on **only** this repo. The Worker mints a short JWT with the app's private key (WebCrypto
  RS256), exchanges it for a 1-hour installation token, then creates the issue. Authorship shows as
  **`sweetspot-support[bot]`** (or the app's name). **Tightest scope, bot authorship, no org, no repo
  move** — the app can only touch Issues on the one repo it's installed on. Preferred long-term; migration
  is Worker-code-only (swap PAT auth for app-token minting) plus creating/installing the app.
- **Org + fine-grained PAT.** Fine-grained PATs only grant write to repos the token owner *owns*, and a
  personal account can't approve a fine-grained token for another user's personal repo — so a
  fine-grained token on the bot **cannot** target `jmerhar/sweetspot-android` (it shows the repo as
  public/read-only only). Fine-grained works only if the repo lives in an **organization** that approves
  the token. That means transferring the repo to an org — see the blast radius below. Not worth it just
  for this.
- **Own fine-grained PAT (no bot).** Since `jmerhar` owns the repo, a fine-grained PAT on `jmerhar`
  (single repo, Issues:write) is the tightest/simplest — but issues are authored by **you**, not a bot.

#### Blast radius of moving the repo to an org (why we avoided it)
Because the website lives in this repo, an org transfer is invasive: **GitHub Pages** custom-domain
binding (`sweetspot.today`) likely clears and needs re-adding/re-verifying (possible brief downtime);
Pages source + `github-pages` environment may reset; **Actions repo secrets do not carry over**
(re-add `CODECOV_TOKEN`, `COVERAGE_PAGES_TOKEN`, `PLAY_STORE_SERVICE_ACCOUNT_JSON`, `ENEVER_TOKEN`,
`SITE_COMMIT_TOKEN`); org Actions **policies** must be enabled (allowed actions, `GITHUB_TOKEN`
permissions); **`SITE_COMMIT_TOKEN`** (fine-grained, pushes to trigger `deploy-site`) breaks and must be
reissued for the org repo; **Codecov** slug changes (re-authorise the org, reissue token, update README
badge); personal **collaborators are dropped** (re-grant via org team); update README badges, git
remotes, and any `jmerhar/sweetspot-android` references (old URLs redirect, best-effort — never recreate
the old name). **Unaffected:** Play Store, Play signing, Fastlane, `assetlinks.json` (package + cert),
DNS at Cloudflare, commit history/tags/releases/issues/PRs/stars. The **coverage site** (`jmerhar/coverage`)
is keyed by repo name, so it keeps working once the secret is re-added. Only pursue an org if you want it
for *other* reasons (multiple repos, teams).

### 5. Notifications (Phase 2)
- **Opt-in only.** The email is provided in the form and stored **only** in Worker KV keyed by issue
  number — **never** written into the public issue (public issues get scraped by spam bots).
- **Pipeline:** GitHub webhook (Issues + Issue comments) → Worker (`/webhook`, HMAC-verified) → look up
  email by issue# → **Brevo transactional API** → email the user with the new comment / closed status +
  a link to the issue. Provide an unsubscribe path (KV delete).
- **Privacy policy** must mention that an email address, if provided, is stored to notify the user about
  their report and can be removed on request.

### 6. Tracking / "My reports"
- The app remembers the issue numbers it created; **status/comments are read from GitHub's public REST
  API** (unauthenticated, 60 req/hr per IP — ample for a handful of reports). Shows Open/Closed, latest
  reply, reference number, and a "view on GitHub" link. Replying in-app is out of scope (would need the
  Worker to post a comment as the bot — a possible later extension).

### 7. Email infrastructure (`hello@sweetspot.today`)
- **Inbound (receive):** Google Workspace MX. Backs the **Contact us** `mailto:` and receives DMARC
  reports.
- **Outbound (send):** **Brevo transactional** (used by the Worker for notifications) and Google
  (send-as). Both are SPF/DKIM/DMARC-aligned for the domain, so notifications pass authentication.
- The bot's GitHub email is the same `hello@sweetspot.today`.
- **Current DNS (verified, all correct):** MX→Google; SPF `v=spf1 include:_spf.google.com
  include:spf.brevo.com ~all`; DKIM `google._domainkey` + Brevo `brevo1/brevo2._domainkey`; DMARC
  `p=quarantine; rua/ruf` to `dmarc@`/`dmarc+forensic@`. Optional hardening later: SPF `-all`, DMARC
  `p=reject`; confirm the `dmarc@`/`dmarc+forensic@` aliases deliver.

---

## Anti-abuse
The `/report` endpoint is unauthenticated from the app's side (any client can POST). Defenses:
- **Baseline (required):** per-IP/day **rate limit** in Workers KV + subject/body **length caps** +
  manual moderation (close spam issues). Sufficient for a niche app.
- **Optional hardening — Play Integrity API** (the *native* equivalent of a CAPTCHA): the app requests an
  integrity token, the Worker verifies it with Google before creating the issue. Heavier setup (Google
  Cloud project + service-account verification). Add only if bots show up.
- **Not usable:** **Turnstile** is a web/JS widget — no clean native-Android integration (WebView only),
  so it's the wrong tool here.

---

## Secrets & configuration

| Value | Lives in | Secret? |
|---|---|---|
| Bot classic PAT (`public_repo`) — *or later* GitHub App id + private key | Worker secret (`GITHUB_TOKEN` / app key) | 🔒 never in app/repo |
| `WEBHOOK_SECRET` (GitHub webhook HMAC) | Worker secret | 🔒 |
| `BREVO_API_KEY` (transactional email) | Worker secret | 🔒 |
| `issue# → email` map, rate-limit counters | Workers KV | 🔒 (server-side, not public) |
| Worker URL, repo owner/name, Play Store id, contact email, website base | baked in app | no |

## DNS records (Cloudflare)

| Purpose | Record | Status |
|---|---|---|
| Website (Pages) | root `A` → GitHub Pages IPs | ✅ exists |
| Receive mail | `MX` → Google | ✅ exists |
| Authorise senders | `TXT` SPF (Google + Brevo) | ✅ exists |
| DKIM | `google._domainkey`, `brevo1/2._domainkey` | ✅ exists |
| DMARC | `_dmarc` TXT (quarantine + reporting) | ✅ exists |
| Worker endpoint | `feedback.sweetspot.today` route/record | ⏳ to add |

---

## Alternatives considered

- **Backend host:** Cloudflare Worker (**chosen** — serverless, already on Cloudflare, holds the token,
  cheap) vs. the existing **aurora** PHP host (works, but a VM to maintain) vs. **GitHub Pages** (❌
  static only — can't hold a secret or run code) vs. **Google Forms** (❌ browser hand-off, data siloed
  from GitHub triage).
- **Issue auth:** classic PAT (chosen now) vs. **GitHub App** (recommended future) vs. org + fine-grained
  vs. own PAT — see [§4](#4-authentication-options-how-the-worker-writes-issues).
- **Baking a PAT into the app:** ❌ **never** — an APK is public and the repo is open source; the token
  would be extracted and auto-revoked by GitHub secret scanning, and could be abused as the bot.
- **Outbound email:** **Brevo** (chosen — already configured & domain-aligned) vs. Resend (would be an
  extra service).
- **Report a problem via prefilled GitHub issue URL:** rejected — opens a browser and requires the user
  to have a GitHub account; the in-app form is friendlier and account-free.

## Security posture
- App holds **no** write secret; it only POSTs to the Worker and reads *public* GitHub data.
- The one over-scoped element today is the bot's **classic `public_repo`** token. Mitigated by: bot has
  no other access, token is Worker-only, rotate/expire it. **Recommended migration: GitHub App**
  (least privilege, same bot authorship) when convenient — Worker-code-only change plus app creation.
- User emails (Phase 2) are opt-in, stored server-side only, never in public issues; disclosed in the
  privacy policy with a removal path.

---

## Current status

Done:
1. **`hello@sweetspot.today`** send + receive (Google inbound, Brevo outbound) — DNS verified correct &
   secure (SPF/DKIM/DMARC/MX all good).
2. **Bot account `sweetspot-support`** (`hello@sweetspot.today`), write collaborator on the repo, with a
   **classic `public_repo`** token.
3. Repo label **`from-app`** added (alongside `bug`, `enhancement`).

Pending (infra):
- Cloudflare Worker + KV namespace + `feedback.sweetspot.today` route; Worker secrets (`GITHUB_TOKEN`,
  `WEBHOOK_SECRET`, `BREVO_API_KEY`).
- Brevo **transactional** API key (Phase 2 notifications).
- GitHub **webhook** → Worker `/webhook` (Phase 2).
- (Optional/future) migrate issue auth to a **GitHub App**; add **Play Integrity** if spam appears.

Pending (app): Help sub-screen + form + My reports + quick guide + links; `feedbackBody(...)` pure
helper + tests; strings ×25 locales; a few new icons; docs + test-count bump.

## Open decisions
- Anti-abuse at launch: rate-limit + caps only (recommended) vs. Play Integrity from day one.
- Notification unsubscribe UX (reply STOP vs. a tokenized link that clears the KV entry).
- Whether "My reports" later gains in-app replies (Worker posts a comment as the bot).
