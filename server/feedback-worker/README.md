# SweetSpot feedback Worker

Cloudflare Worker behind the app's **Report a problem** / **Send feedback** flow. It turns app
submissions into **GitHub issues** and emails the reporter — if they opted in — when their issue gets a
comment or is closed (with a one-click unsubscribe link). Architecture:
`../../docs/notes/reference/help-support-system.md`.

- `POST /report` — app submits `{category, subject, body, diagnostics?, email?}` → creates a labelled
  issue (`from-app` + `bug`/`enhancement`); returns `{number, url, replyToken}`. A per-report token is
  always stored in KV keyed by issue number (with the email, if opted in). The token is the report's
  capability: the app keeps `replyToken` to post replies, and it also backs the emailed unsubscribe link.
- `POST /reply` — app posts a comment on its own report: `{issue, token, body}` → the Worker comments
  **as the bot** (prefixed to mark it's from the reporter) once the token matches (constant-time),
  rate-limited per IP (`REPLY_RATE_LIMIT_PER_DAY`); `201 {ok, url}`.
- `POST /webhook` — GitHub webhook (`issues`, `issue_comment`), HMAC-verified; emails the opted-in
  reporter via Brevo (skips the bot's own comments, so a reporter's app reply doesn't self-notify).
  Each notification carries a tokenized unsubscribe link (+ RFC 8058 one-click `List-Unsubscribe`
  headers).
- `GET /unsubscribe?issue=N&token=…` — confirmation page (non-mutating, so link prefetchers can't
  unsubscribe); `POST /unsubscribe` (the form submit, or a one-click `List-Unsubscribe=One-Click` body)
  clears the stored **email** when the token matches (constant-time), keeping the token so in-app
  replies still work.
- `GET /` — health check.

The app never holds a token: it POSTs here to submit, and reads **public** issue state directly from
GitHub for the in-app "My reports" list.

## Secrets (set by you — never committed)

Run from this directory. `GITHUB_TOKEN` is the `sweetspot-support` **classic `public_repo`** token;
`BREVO_API_KEY` is a Brevo **transactional (v3)** key; `WEBHOOK_SECRET` is a random string you generate.

```bash
cd server/feedback-worker

# generate a webhook secret and keep a copy for the GitHub webhook step
openssl rand -hex 32

wrangler secret put GITHUB_TOKEN     # paste the bot's classic public_repo PAT
wrangler secret put WEBHOOK_SECRET   # paste the string from openssl above
wrangler secret put BREVO_API_KEY    # paste the Brevo transactional API key
```

Non-secret settings (repo, sender address, rate limit) live in `wrangler.jsonc` → `vars`.

## First-time setup & deploy

```bash
cd server/feedback-worker

# 1. Create the KV namespace, then paste the printed id into wrangler.jsonc (kv_namespaces[0].id)
wrangler kv namespace create FEEDBACK_KV

# 2. Deploy (creates the Worker + the feedback.sweetspot.today custom domain)
wrangler deploy          # first time; afterwards just `make deploy-feedback` from the repo root

# 3. Set the three secrets (see above)

# 4. Add the GitHub webhook (enables reporter notifications):
#    repo Settings → Webhooks → Add webhook
#      Payload URL:  https://feedback.sweetspot.today/webhook
#      Content type: application/json
#      Secret:       the WEBHOOK_SECRET from step above
#      Events:       Issues, Issue comments
```

## Verify

```bash
curl https://feedback.sweetspot.today/                       # -> ok
# End-to-end (creates a real test issue — delete it after):
curl -sX POST https://feedback.sweetspot.today/report \
  -H 'content-type: application/json' \
  -d '{"category":"feedback","subject":"Worker test","body":"ignore — setup test"}'
# -> {"number":N,"url":"https://github.com/jmerhar/sweetspot-android/issues/N"}
```

`wrangler tail` streams live logs. Local dev: copy `.dev.vars.example` → `.dev.vars`, then `wrangler dev`.

## Notes

- **Anti-abuse:** per-IP daily rate limit (`RATE_LIMIT_PER_DAY` in `vars`) + length caps. Add Play
  Integrity later if bots appear (Turnstile is web-only — not usable from a native app).
- **Privacy:** the reporter's email is stored only in KV keyed by issue number for notifications —
  never written into the public issue. Every notification includes a one-click unsubscribe link
  (tokenized `GET`/`POST /unsubscribe`) that clears the KV entry; disclosed in the privacy policy.
- **Auth migration:** the classic `public_repo` PAT can later be swapped for a **GitHub App**
  installation token (least privilege, same bot authorship) — a change confined to the GitHub auth
  header in `src/index.js` plus setting the app key as a secret.
