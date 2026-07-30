# SweetSpot — Backend & Tooling Adversarial Review

Scope: `server/feedback-worker/`, `server/stats/`, `bin/` (Python + shell). Read-only.
Evidence cited as `path:line`. Confidence marked **confirmed** / **suspected**.

Severity ranking summary:
- **High:** H1 (Worker rate-limit bypass), H2 (CF-Connecting-IP trust)
- **Medium:** M1 (EV DB variant collapse — data loss), M2 (no tests for build-ev-db / coverage-report), M3 (build-suppliers orchestration untested), M4 (deploy.sh char-count / write-before-validate)
- **Low:** L1–L10 (duplication, dead config, portability, doc drift, minor robustness)

Positives verified at the end.

---

## Remediation status (audit follow-up)

Legend: ✅ fixed · ⏳ not fixed (tracked) · 📋 later phase · ➖ won't fix.

| Finding | Status | Where / note |
|---|---|---|
| H1 rate-limit bypass | ✅ | `4a931eb` global cap + Cloudflare edge rule (live) |
| H2 `CF-Connecting-IP` trust | ✅ | `5930d3b`, deployed to aurora |
| M1 EV variant collapse | ✅ | `b25605e` (regenerated asset) |
| M2 no tests (build-ev-db, coverage-report) | ✅ | `b25605e`, `8dfaaa8` |
| M3 build-suppliers orchestration untested | ⏳ | add a `build_country` test with a canned feed |
| M4 deploy.sh writes changelogs before the 500-char gate; byte char-count | ⏳ | local-only; UTF-8 shell |
| L1 duplicated locale table | ✅ | `11e3345` |
| L2 dead `dokka` catalog entry | 📋 | Phase 3 |
| L3 buildSrc hardcoded AGP/Kotlin versions | 📋 | Phase 3 (or accept — Gradle limitation) |
| L4 install.sh macOS-only + stale fallback APK names | ⏳ | local dev convenience |
| L5 diagnostics can break the code fence | ⏳ | cosmetic, low |
| L6 build-suppliers `usable:false` docstring | ✅ | `11098c7` |
| L7 release.sh version not validated | ⏳ | trusted local input |
| L8 per-IP limit vs NAT | ➖ | documented tradeoff |
| L9 coverage gate rounds to 1 dp | ➖ | intended tolerance |
| L10 credentials.json in working tree | ✅ | verified gitignored/untracked |

---

## HIGH

### H1 — Feedback Worker: KV rate limit is bypassable by concurrency + IP rotation → public GitHub issue spam
**`server/feedback-worker/src/index.js:86-91, 116-124`** — **confirmed** (severity of *impact* suspected).

`/report` reads a per-IP-per-day counter, checks `used >= limit`, and only writes `used+1`
**after** a successful GitHub issue create (line 117). Two structural weaknesses:

1. **No atomicity / read-modify-write race.** N concurrent valid requests all `get` the same
   `used` value (KV is eventually consistent and non-transactional), all pass the `>= limit`
   check, all create issues, then all `put` `used+1`. The "5/day" cap is not enforced under
   parallelism — a burst creates far more than 5 public issues.
2. **Per-IP keying (`rl:${ip}:${date}`, line 89)** is trivially defeated by IP rotation; each new
   IP gets a fresh 5-issue budget.

Failure scenario: a script fires 100 parallel valid POSTs from rotating IPs → 100+ public GitHub
issues labelled `from-app`, consuming the repo's issue space and the Worker's GitHub API quota.
The comment at line 86 ("eventually consistent — fine for abuse throttling") acknowledges lag but
not the concurrency bypass. The only real backstop is GitHub's own abuse limits.

Same pattern in `/reply` (`:163-167, 193`) — but replies are additionally token-gated (H1 applies
only weakly there).

Fix direction: use Cloudflare's native Rate Limiting binding or a Durable Object counter
(atomic), and/or gate `/report` behind Turnstile. Increment the slot *before* the GitHub call so
in-flight requests count.

### H2 — Stats endpoint trusts `CF-Connecting-IP` from any source → rate-limit bypass + spoofed analytics IP
**`server/stats/stats.php:78`, `server/stats/stats.sweetspot.today.conf:31`** — **suspected** (depends on origin firewall not in repo).

`stats.php` reads `$_SERVER['HTTP_CF_CONNECTING_IP']` directly for the rate-limit key (line 78), and
the vhost sets `RemoteIPHeader CF-Connecting-IP` (conf:31) **without any `RemoteIPTrustedProxy` /
`RemoteIPInternalProxy` allowlist restricting it to Cloudflare's IP ranges**, and without an origin
firewall rule in the committed config. If the origin (`aurora`) is reachable directly — bypassing
Cloudflare — an attacker can send `CF-Connecting-IP: <arbitrary>`:
- **Evade the rate limiter** by rotating the header value (each fake IP → its own 5-min window).
- **Poison analytics**: not directly (the IP isn't stored in InfluxDB), but rate-limit files are
  created per fake IP, filling `/tmp/sweetspot_rate` (bounded by the 1-hour cleanup at
  `stats.php:92-96`, so DoS risk is small).

This is only safe if the origin is IP-locked to Cloudflare egress ranges at the network layer. That
guarantee is not visible in the repo; the vhost's `LocationMatch` only restricts *paths*, not
*sources*. Recommend `mod_remoteip` `RemoteIPTrustedProxy <cf-ranges>` plus an origin firewall, and
document it in `server/stats/README.md`.

---

## MEDIUM

### M1 — build-ev-db: dedup key excludes `variant` → all variants of a model-year collapse to one (data loss)
**`bin/build-ev-db.py:126-128, 141-144`** — **confirmed** (verified against the shipped asset).

`dedup_key(v)` is `(brand.lower(), model.lower(), year)` — **variant is not part of the key** — yet
`variant` is kept in the normalised schema (`:60`) and is a sort key (`:143`), and the app's picker
surfaces variants. Because `merged[dedup_key(v)] = v` (`:137`) overwrites, two rows that differ only
by variant (e.g. "Model 3 Standard Range" vs "Long Range", same year) cannot coexist — the later one
in iteration order silently wins, both within a source and across sources.

Verified: the shipped `app/src/main/assets/ev-vehicles.json` has 1574 vehicles, **910 carry a
non-null variant, yet 0 (brand, model, year) keys have more than one row** — i.e. the key
structurally cannot hold two variants, so any model-year that had multiple trims lost all but one.
This is a real reduction in the DB's usefulness (users can't pick their exact trim's battery/AC
spec, which drives the charging-time calc). Either fold variant into the dedup key, or document that
variant is intentionally deduplicated away (the current docstring at `:127` says only
"brand + model + year… distinct years kept separate", so the behaviour is undocumented drift).

Secondary: cross-source override (source #2 wins, `:122`) only fires when brand/model/year strings
match exactly across sources; naming differences ("VW" vs "Volkswagen") produce duplicates rather
than the intended override. Inherent to name-based dedup; worth a normalisation note.

### M2 — No unit tests for `build-ev-db.py` or `coverage-report.py` (untested pure logic)
**`bin/build-ev-db.py`, `bin/coverage-report.py`** — **confirmed**.

`build-suppliers.py` has 25 tests, but `build-ev-db.py`'s pure, testable functions (`normalise`
filtering rules — cars-only, battery+AC required; `adapt_kilowatt`/`adapt_openev` field mapping;
`dedup_key`) have **zero** tests. Given M1, the filtering/merge logic is exactly where a silent
data-loss regression hides. Similarly `coverage-report.py`'s gate math (`percentages`,
`line_percent`, `run_gate`, the `+0.05` rounding tolerance, `total_metrics` covered/total summing)
and XML parsing are untested — this script *is* the CI coverage gate, so a bug in it silently
weakens or breaks the gate. Both are trivially unit-testable (pure funcs + canned XML/JSON).

### M3 — build-suppliers: the orchestration/precedence logic is not unit-tested
**`bin/build-suppliers.py:321-368, 417-443`** — **confirmed** gap.

`test_build_suppliers.py` thoroughly covers the *math* (tax derivation, surcharge differencing,
normalisation, legend parse, registry merge, slug, unchanged-guard — good). But the parts that
decide the *shape of the output* are untested:
- **Supplier merge / override precedence** in `build_country` (`:427-431`): `merged[id] = supplier`,
  "later source wins → Frank overrides enever's differenced Frank". This is the invariant most
  likely to silently break on a refactor, and it has no test.
- **enever column discovery** regex `^prijs[A-Z]{2,5}$` and code extraction (`:353-357`) — a change
  to the feed's column naming or the regex would silently drop or mis-map suppliers.
- **`frank_suppliers`** markup→surcharge (`median(markups)/(1+vat)`, `:213-218`) and the "no FR
  registry entry → omit" branch (`:208-210`) — untested.
- **`build_country` NL date/zone** selection (`:421`).

The functions take an injectable `ctx`/`warnings` and could be tested with a fake rows list without
network. Recommend adding a `build_country`/`enever_suppliers` test with a canned enever feed to lock
the precedence + column-discovery behaviour.

### M4 — deploy.sh: changelog written before the 500-char gate; char-count is locale-dependent
**`bin/deploy.sh:98-113`** — **confirmed** (write-before-validate); **suspected** (locale).

1. The per-locale changelog `.txt` is written (`:100-105`) **before** the 500-char check
   (`:108-113`). On an over-limit locale the script `exit 1`s but has already written changelog
   files for that and all prior locales into `fastlane/metadata/android/*/changelogs/`. Those stray
   files persist on disk; if later committed they could be picked up. Validate before writing.
2. `char_count=${#text}` (`:108`) counts **characters only in a UTF-8 `LC_CTYPE`**; under a C/POSIX
   locale it counts **bytes**, over-counting multibyte (accented) languages and producing spurious
   500-char failures. The dev's shell is UTF-8 (verified), and `make deploy` is run locally, so it's
   fine today — but a C-locale CI runner would mis-gate. Prefer an explicit UTF-8 count.

---

## LOW

### L1 — Duplicated locale→Play-locale mapping (drift risk)
**`bin/site-screenshots.sh:32-52` (`metadata_dir`) vs `bin/lib/common.sh:64-74` (`website_to_metadata`)** — **confirmed**.
Byte-for-byte the same mapping (en→en-GB, cs→cs-CZ, …, plus the pass-through comment). `site-screenshots.sh`
doesn't `source lib/common.sh`; the two tables will drift when a language is added/renamed. Collapse to one
(source `common.sh`, or note `en→en-GB` is the only addition `website_to_metadata` already covers).

### L2 — Dead `dokka` version-catalog entry
**`gradle/libs.versions.toml:4, 75`** — **confirmed**. `dokka = "2.2.0"` and the
`org.jetbrains.dokka` plugin alias are declared but **applied nowhere** (grep over all `*.kts`/build
files returns no usage). Dead config; remove or wire up.

### L3 — buildSrc hardcodes AGP/Kotlin versions duplicated from the version catalog
**`buildSrc/build.gradle.kts:22-31`** — **confirmed**. AGP `9.0.1` and Kotlin/compose/serialization
`2.3.20` are hardcoded with a "Keep these versions in sync with gradle/libs.versions.toml" comment +
`//noinspection UseTomlInstead`. A real drift hazard: a catalog bump that misses buildSrc yields a
split-brain toolchain. (Known Gradle limitation — buildSrc can't easily read the catalog for plugin
deps — but worth a CI assertion or a shared constant.)

### L4 — install.sh is macOS-only and its release fallback APK name is stale
**`bin/install.sh:69, 34/46`** — **confirmed** non-portable.
`stat -f '%m %N'` is BSD/macOS syntax; on Linux `stat -f` means something else and the `-exec` fails
(swallowed by `2>/dev/null`), so `APK` is empty and the script falls back to `APK_FALLBACK`. But the
release fallback is `app-release.apk` / `wear-release.apk` (`:34, :46`) while the convention plugin
emits `sweetspot-release.apk` / `sweetspot-wear-release.apk` — so on Linux the fallback file doesn't
exist and install fails with "No APK found". Fine on the author's Mac; non-portable and the fallback
names look stale. Low (local dev convenience).

### L5 — Reporter-controlled diagnostics can break out of the code fence in the public issue
**`server/feedback-worker/src/index.js:130-136`** — **confirmed**, low impact.
`buildIssueBody` wraps `diagnostics.trim()` in a ``` ``` ``` fence with no escaping. Diagnostics
containing a ``` line closes the block early and injects arbitrary Markdown into the (public) issue.
GitHub sanitises HTML/script, so no XSS — worst case is cosmetic Markdown in the reporter's own
issue. Consider a fence guard (longer backtick run) or stripping backticks.

### L6 — build-suppliers docstring/behaviour mismatch: no file is ever written with `usable:false`
**`bin/build-suppliers.py:18-20, 433-443, 486-493`** — **confirmed** doc drift.
The module docstring says an unsourceable country "is marked `usable: false` and NO file is written".
In fact `build_country` always sets `"usable": True` (`:438`); the failure path *raises* and writes
nothing (last-good kept). So a written file is always `usable:true`; `usable:false` never reaches
disk. The behaviour (keep last-good, fail loud) is correct — the "marked usable:false" wording is
misleading. The app's `usable` gate is therefore effectively "file exists & parses".

### L7 — release.sh doesn't validate the version string (sed-delimiter break)
**`bin/release.sh:33, 67-68, 103`** — **confirmed**, low (trusted local input).
`VERSION` is interpolated into `sedi "s/versionName = \".*\"/versionName = \"$VERSION\"/"` and into
`git tag v$VERSION`. A `VERSION` containing `/` breaks the sed delimiter (hard error under `set -e`,
after the versionCode line was already rewritten); other odd characters land in the `.gradle.kts`
verbatim. Add a `^[0-9]+(\.[0-9]+)*$` guard.

### L8 — stats.php per-IP rate limit penalises NAT-shared households
**`server/stats/stats.php:72-97`** — **confirmed**, low.
One request / 5 min / IP. Multiple SweetSpot devices behind a single home NAT share one
`CF-Connecting-IP`; the second device's report inside a 5-min window gets 429 and is deferred to the
next day by the app. Given the 24h cadence and staggering it's rarely observable, but per-IP
(not per-device) limiting slightly under-counts multi-device households.

### L9 — Coverage gate compares a 1-decimal-rounded percentage
**`bin/coverage-report.py:49, 110-115, 128`** — **confirmed**, low (documented).
`percentages()` formats coverage to one decimal (`:.1f%`), and `line_percent` parses that string
back, so the gate operates on the rounded value plus a `+0.05` tolerance. A true 96.96% rounds to
"97.0" and passes a 97.0 gate. Intended tolerance (comment at `:127`), but it throws away precision
the XML already has; gating on the raw `covered/total` would be cleaner.

### L10 — `fastlane/credentials.json` present in the working tree (operational hygiene)
**`fastlane/credentials.json`** — **confirmed** it is **gitignored and NOT tracked** (`git ls-files`
empty; `git check-ignore` matches; CI writes it from a secret in `publish-listing.yml`). So the
inventory's "possibly committed" concern is resolved — no repo leak. Note only: a live 2.3 KB Play
service-account key sits in the dev's working tree; ensure it's never force-added and rotate if the
machine is shared.

---

## Verified positives (no action)

- **Email never leaks into the public issue** — `handleReport` composes the issue from
  `subject` (newlines stripped, `:73`) + `buildIssueBody(body, diagnostics)` only (`:95, 106`); the
  email is stored solely in KV (`:123-124`) and used only for Brevo. Confirmed no path writes email
  into the issue body/title.
- **Constant-time token compare** — `/reply` and `/unsubscribe` use
  `crypto.subtle.timingSafeEqual` with an up-front length guard (`:397-402`); tokens are fixed-length
  UUIDs, so the length short-circuit leaks nothing. Correct.
- **Webhook HMAC** — `verifySignature` verifies over the **raw** request text before `JSON.parse`
  (`:200-212`), uses `crypto.subtle.verify` (constant-time), validates the `sha256=` prefix and hex
  (`:415-434`). Correct.
- **InfluxDB ack-gating** — `stats.php` returns 200 only after a 204 from InfluxDB, else 502
  (`:314-321, 174`). Correct; a test-marked POST verifies ingestion end-to-end.
- **Line-protocol injection not possible** — every tag value is regex-whitelisted to charsets with no
  commas/spaces/equals/newlines (zone/source/app/error/lang, plus device & status whitelists,
  `:246-298`); the `$escape` closure is belt-and-suspenders. `duration_ms`/`timestamp` are int-bounded.
- **Unsubscribe token preservation** — `POST /unsubscribe` clears the email but re-stores the token
  (`:373-375`), and `readSubscription` returns the token even when email is null (`:328-336`), so
  in-app replies keep working after unsubscribe. Guarded by an explicit regression test
  (`test/index.test.js:22-25`).
- **Unsubscribe GET is non-mutating** (`:344-358`) — link prefetchers can't unsubscribe; mutation is
  POST-only. Good.
- **Rate-limit-after-validation** in stats.php (`:310-311`) is intentional and correct (bad requests
  don't consume a legit device's quota).
- **build-suppliers "no baked numbers / last-good kept"** — the failure path writes nothing and exits
  non-zero (`:486-504`); plausibility bounds are reject-only, never substituted (`:180-183, 391`).
  Sound.

## Test-coverage gaps at a glance
- **Feedback Worker:** all exported pure fns are tested (readSubscription, notificationFor,
  buildIssueBody, intVar, truncate, escapeHtml, handleUnsubscribeGet). Untested (need Workers
  runtime, acknowledged in CLAUDE.md): `timingSafeEqualStr`, `verifySignature`, and the `fetch`
  handlers' GitHub/Brevo IO. Reasonable; a `@cloudflare/vitest-pool-workers` harness would close it.
- **build-suppliers:** math well covered; **orchestration/precedence untested** (M3).
- **build-ev-db, coverage-report:** **no tests at all** (M2).
</content>
</invoke>
