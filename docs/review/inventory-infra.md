# SweetSpot Android — Ground-Truth Infra Inventory

Derived strictly from the actual files (not README/CLAUDE.md claims). Evidence cited as `path:line` or target names.
Repo root: `/Users/jmerhar/local-repos/jure/sweetspot-android`

---

## 1. Make targets (`Makefile`)

| Target | What it actually runs | Notes / caveats |
|---|---|---|
| `help` (L3) | `grep`/`awk` over MAKEFILE to print `##`-annotated targets | — |
| `build` (L9) | `./gradlew assembleDebug` | Builds all modules' debug (phone+watch). |
| `build-release` (L12) | `./gradlew assembleRelease` | Needs release signing in `local.properties`. |
| `bundle` (L15) | `./gradlew bundleRelease`, copies AABs → `build/sweetspot-phone.aab`, `build/sweetspot-wear.aab` | Source AABs: `app/build/outputs/bundle/release/sweetspot-release.aab`, `wear/.../sweetspot-wear-release.aab`. |
| `clean` (L24) | `./gradlew clean` | — |
| `test` (L29) | `./gradlew test` | Runs all modules (not just Debug variant). |
| `test-suppliers` (L32) | `python3 bin/test_build_suppliers.py` | 25 test methods (pure logic, no network). |
| `test-feedback` (L35) | `cd server/feedback-worker && npm test` (vitest) | 18 `it(...)` tests. |
| `inspect` (L38) | `./bin/inspect.sh` | Only *summarises* pre-exported XML; **script always `exit 1`** (see §2). |
| `debug` (L43) | `debug-phone` + `debug-watch` | — |
| `debug-phone` (L45) | `./gradlew app:assembleDebug` + `./bin/install.sh phone --debug` | — |
| `debug-watch` (L49) | `./gradlew wear:assembleDebug` + `./bin/install.sh watch --debug` | — |
| `install` / `install-phone` / `install-watch` (L53-59) | `./bin/install.sh <target>` (release APK) | — |
| `release` (L63) | `./bin/release.sh $(VERSION) -n docs/notes/release.md $(if $(DRAFT),--draft)` | Requires `VERSION=`. |
| `deploy` (L66) | `TRACK=$(or…,alpha) APP=$(or…,both) ./bin/deploy.sh` | Wear skipped on alpha (see §2). |
| `deploy-stats` (L69) | `./bin/deploy-stats.sh` | scp to `aurora`. |
| `deploy-feedback` (L72) | `./bin/deploy-feedback.sh` | wrangler deploy + health check. |
| `site` (L77) | `site-screenshots` then `open localhost:1313` + `hugo server` | — |
| `site-screenshots` (L80) | `./bin/site-screenshots.sh` | — |
| `site-validate` (L83) | `site-screenshots` then `./bin/site-validate.sh` | — |
| `ev-db` (L86) | `./bin/build-ev-db.py` | — |
| `suppliers` (L89) | `./bin/build-suppliers.py` | Needs `ENEVER_TOKEN`. |
| `screenshots` (L94) | `bundle exec fastlane screenshots$(if LOCALE, locale:…)` | Fastlane/Screengrab. |
| `frames` (L97) | `LOCALE=$(LOCALE) ./bin/frame-screenshots.sh` | ImageMagick 7. |
| `feature-graphic` (L100) | `LOCALE=$(LOCALE) ./bin/feature-graphic.sh` | ImageMagick 7 + Python 3. |
| `publish` (L103) | `bundle exec fastlane publish` | Uploads listing metadata/images. |

Behavior matches intent throughout. `.PHONY` list (L1) is comprehensive.

---

## 2. bin/ scripts

### `build-suppliers.py` (21.8 KB)
- **Purpose:** builds per-country all-in tariff feed `site/static/data/suppliers/<cc>.json` + committed enever registry `site/static/data/enever-suppliers.json`.
- **Env/args:** `ENEVER_TOKEN` (env or `local.properties` line, resolved L119-121); no CLI args.
- **Sources:** Frank Energie GraphQL (`FRANK_URL`, L63, NL essentials — VAT + energy tax + own surcharge); enever.nl (`ENEVER_URL`, L64) per-supplier all-in differenced to surcharge; enever legend HTML (`ENEVER_LEGENDA_URL`, L65).
- **Country registry:** `COUNTRIES` (L408) — **NL only**. `SCHEMA_VERSION = 1` (L58).
- **Behavior facts:** No baked fallbacks — a country whose essentials can't be sourced raises `TariffError`, no file written, last-good kept, and `main()` exits non-zero (L488-504). Plausibility bounds are sanity gates only, never substituted: `VAT 0–0.30`, `ENERGY_TAX 0–0.20`, `SURCHARGE -0.01–0.06` (L75-77). Skips rewrite when only `generated` timestamp differs (`_unchanged_except_generated`, L451). Registry write skipped when unchanged (L270-277).
- Pure functions tested by `test_build_suppliers.py`.

### `build-ev-db.py` (5.9 KB)
- **Purpose:** builds `app/src/main/assets/ev-vehicles.json` (JSON array, one compact object/line).
- **Sources:** Kilowatt `open-ev-data` (L31) + `open-ev-data-dataset` latest GitHub release (L32). `SOURCES` order = merge precedence, later wins (L123, L137).
- **Filters:** cars only; requires battery kWh + AC power or entry dropped (`normalise`, L45-64; `adapt_kilowatt` skips non-`car`, L70). Dedup key = brand+model+year case-insensitive (L126-128).
- No env/args.

### `coverage-report.py` (6.1 KB)
- **Purpose:** reads each module's `<module>/build/reports/kover/reportDebug.xml`.
- **Args:** `--format md|reports` (default md); `--gate`.
- **GATES (L32):** `{"shared": 98.0, "app": 97.0, "wear": 93.0}` line-coverage %. `MODULES = ("shared","app","wear")` (L27).
- Gate tolerance `+0.05` (L128). `--gate` exits 1 on any module below bound or missing report. `reports` format emits leading `total` (combined) entry then per-module.
- Reads Kover XML rather than `koverVerifyDebug` (unreliable wildcard excludes on Kover 0.9.8).

### `collect-coverage.sh` (852 B)
- Copies `<module>/build/reports/kover/htmlDebug` → `<out>/<module>` for shared/app/wear + writes `reports.json` (via `coverage-report.py --format reports`). Default out `coverage-upload`. For `jmerhar/coverage` publishing.

### `deploy.sh` (5.5 KB) — Play Store deploy
- **Env:** `TRACK` (default `alpha`), `APP` (default `both`; validated `phone|wear|both`).
- **Wear-on-alpha rule (L30-36):** `APP=wear`+alpha → exit 0 no-op; `APP=both`+alpha → forces `APP=phone`.
- Reads version via `app:printVersionName` / `printVersionCode` / `wear:printVersionCode` (L41-51). Requires AABs at `build/sweetspot-phone.aab` / `build/sweetspot-wear.aab`.
- Extracts changelog per language from `site/content/*/changelog.md`; **hard-fails if any language's changelog version != app versionName** (L83-87) and **if any entry > 500 chars** (L109-113). Writes `fastlane/metadata/android/<locale>/changelogs/<code>.txt`. Locale mapping via `website_to_metadata` (lib/common.sh).
- Calls `bundle exec fastlane deploy` with `track/phone_code/wear_code/skip_phone/skip_wear`.

### `release.sh` (3.7 KB)
- **Args:** `<version> -n <notes-file> [--draft]`.
- Preflight: must be on `main`, clean tree (L13-22). Bumps `versionCode`/`versionName` in `buildSrc/src/main/kotlin/sweetspot-app.gradle.kts` (`CONVENTION_FILE`, L56). Portable `sedi` (L25). Builds `assembleRelease bundleRelease`, renames APKs to include version, copies AABs to `build/`, commits `chore: release vX`, tags `vX`, pushes, `gh release create` with both APKs + Full Changelog link (prev tag via `git tag --sort=-v:refname | sed -n '2p'`, L113).
- **Note:** commit message `chore: release v${VERSION}` — no Co-Authored-By (matches global instructions).

### `install.sh` (2.8 KB)
- **Args:** `<phone|watch> [--debug]` (default variant `release`).
- Finds adb at `~/Library/Android/sdk/platform-tools/adb` then PATH. Phone = device NOT matching `watch|wrist`; watch = matching. Picks newest APK by mtime, `sweetspot-*.apk` pattern with `app-debug.apk`/etc. fallback.

### `deploy-feedback.sh` (1.7 KB)
- Runs `npx --yes wrangler deploy "$@"` in `server/feedback-worker/`. Health-checks `https://feedback.sweetspot.today/` expecting body `ok` (exit 1 on mismatch). `--dry-run` skips health check. Requires `wrangler.jsonc` present. One-time prereqs: `wrangler login`, 3 secrets, KV id.

### `deploy-stats.sh` (489 B)
- `scp server/stats/stats.php server/stats/clear-rate-limit.sh aurora:/var/www/stats.sweetspot.today/`. Hardcoded `REMOTE=aurora`.

### `inspect.sh` (1.4 KB)
- Summarises `inspect/xml/*.xml` (`grep -c '<problem>'`). **GOTCHA: `exit 1` on the last line (L50) even on success** — so `make inspect` always returns non-zero exit code by design (it's a summary, not a gate).

### `install-hugo.sh` (555 B)
- Downloads latest Hugo **extended** linux-amd64 to `/usr/local/bin`. Used by CI.

### `site-validate.sh` (5.3 KB)
- Builds Hugo `--minify`, then 5 checks: expected pages exist (25 langs × `index/faq/changelog/privacy/import` + 404), shared assets + per-lang badges, internal links resolve, page size ≥ 500 bytes (skips refresh-alias pages), i18n key parity vs `en.toml` (missing AND extra keys). `LANGUAGES` list (L44) = 25 langs. Exit 1 on any failure.

### `site-screenshots.sh` (2.8 KB)
- Converts framed PNGs in `fastlane/metadata/android/<play-locale>/images/phoneScreenshots/` → WebP `site/static/images/screenshots/<lang>/{1..6}.webp` (563×1000, q82). Prefers `cwebp`, falls back to `magick`. Shots `1_result 2_home 3_prices 4_settings 5_ev_charging 6_languages`. Outputs gitignored.

### `frame-screenshots.sh` (16.5 KB) / `feature-graphic.sh` (11.5 KB)
- Frame raw Screengrab shots with marketing text (ImageMagick 7); feature graphics 1024×500 (needs macOS Avenir Next TTC + Python 3). Both `source lib/common.sh`, produce `build/*.html` galleries.

### `lib/common.sh`
- Locale mapping helpers: `locale_name`, `metadata_locale` (device BCP47→Play locale), `website_to_metadata` (lang→Play locale), `require_command`, gallery HTML helpers.

---

## 3. CI workflows (`.github/workflows/`)

### `test.yml` — "Test"
- **Triggers:** push + PR to `main`.
- **Perms:** `contents: read`, `statuses: write`. `env FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: true`.
- **Steps:** checkout@v6; setup-java@v5 temurin **17**; gradle/actions/setup-gradle@v6; `./gradlew testDebugUnitTest koverHtmlReportDebug koverXmlReportDebug --continue`; coverage MD summary → `$GITHUB_STEP_SUMMARY`; **3× codecov-action@v5** (flags shared/app/wear, `fail_ci_if_error: false`); codecov test_results upload (`if: !cancelled()`); **`python3 bin/coverage-report.py --gate`** (the real gate); then coverage-HTML publish to `jmerhar/coverage` (guarded on `success() && push && main && COVERAGE_PAGES_TOKEN != ''`) + a `coverage/report` commit status.
- **Secrets:** `CODECOV_TOKEN`, `COVERAGE_PAGES_TOKEN`, `GITHUB_TOKEN`.

### `build-suppliers.yml` — "Build Suppliers"
- **Triggers:** cron `17 4 * * *` (daily) + `workflow_dispatch`. `permissions: contents: write`. `concurrency: build-suppliers` (no cancel).
- **Steps:** checkout@v6 with `token: SITE_COMMIT_TOKEN` (PAT so the commit triggers Deploy Site); run `test_build_suppliers.py`; run `build-suppliers.py` with `ENEVER_TOKEN`; commit+push under `site/static/data/` only if changed (`chore(data): refresh all-in tariff feeds`, as `github-actions[bot]`).
- **Secrets:** `SITE_COMMIT_TOKEN`, `ENEVER_TOKEN`.

### `deploy-site.yml` — "Deploy Site"
- **Triggers:** push to `main` on `site/**`, `fastlane/metadata/android/**`, `bin/site-screenshots.sh`, `.github/workflows/deploy-site.yml`.
- **Perms:** `contents: read`, `pages: write`, `id-token: write`. `concurrency: pages` (cancel-in-progress).
- **Steps:** checkout@v6; `apt-get install webp`; `bin/site-screenshots.sh`; `bin/install-hugo.sh`; `hugo --source site --minify`; **custom tar of `site/public`** (keeps `.well-known/` for App Links — the standard `upload-pages-artifact` would drop dotdirs); upload-artifact@v4 `github-pages`; deploy job → deploy-pages@v5. No repo secrets (uses Pages OIDC).

### `publish-listing.yml` — "Publish Listing"
- **Triggers:** push to `main` on `fastlane/metadata/android/**`. `concurrency: play-store`. `env FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: true`.
- **Steps:** checkout@v6; ruby/setup-ruby@v1 **3.3** bundler-cache; write `PLAY_STORE_SERVICE_ACCOUNT_JSON` secret → `fastlane/credentials.json`; `bundle exec fastlane publish`.
- **Secret:** `PLAY_STORE_SERVICE_ACCOUNT_JSON`.

### `site-validate.yml` — "Validate Site"
- **Triggers:** push + PR to `main` on `site/**`, `bin/site-validate.sh`, `bin/install-hugo.sh`.
- **Steps:** checkout@v6; `bin/install-hugo.sh`; `bin/site-validate.sh`. No secrets.

---

## 4. Backend services

### Feedback Worker (`server/feedback-worker/`)
Cloudflare Worker, `src/index.js` (single file), ES module. `wrangler.jsonc`: name `sweetspot-feedback`, `compatibility_date 2026-07-28`, observability on, custom domain `feedback.sweetspot.today`, KV binding `FEEDBACK_KV` (id `0399c4773fe54c3dabcc6b9cc9007c46`). Non-secret `vars`: `GITHUB_OWNER jmerhar`, `GITHUB_REPO sweetspot-android`, `BOT_LOGIN sweetspot-support`, `BREVO_SENDER_EMAIL notifications@sweetspot.today`, `REPLY_TO_EMAIL hello@sweetspot.today`, `RATE_LIMIT_PER_DAY 5`, `REPLY_RATE_LIMIT_PER_DAY 20`, `MAX_SUBJECT 120`, `MAX_BODY 4000`. Secrets (NOT in repo): `GITHUB_TOKEN`, `WEBHOOK_SECRET`, `BREVO_API_KEY`.

**Endpoints (fetch handler L26-54):**
- `GET /` → `"ok"` 200 (health).
- `POST /report` (L57) → creates GitHub issue labelled `from-app` + `bug`/`enhancement` (`CATEGORY_LABEL = {bug:"bug", feedback:"enhancement"}`, L23). Requires JSON (415), validates category/subject/body/diagnostics/email; per-IP daily KV rate limit (429); on success stores `issue:<n>` → `{email, token}` (34560000s ≈ 400-day TTL) and returns `{number, url, replyToken}` 201. Email NEVER written into the public issue. GitHub non-201 → 502.
- `POST /webhook` (L199) → HMAC-SHA256 verify `X-Hub-Signature-256` (401 on fail); on `issue_comment.created` (skips bot's own via `BOT_LOGIN`) or `issues.closed`, emails opted-in reporter via Brevo w/ tokenized RFC-8058 one-click unsubscribe. Background send via `ctx.waitUntil`.
- `POST /reply` (L143) → token-authorised (constant-time compare) comment as bot; separate per-IP rate limit (`rlr:` key, default 20). 403 on token mismatch, 502 on GitHub failure.
- `GET /unsubscribe` (L344) → confirmation HTML page (non-mutating). `POST /unsubscribe` (L365) → clears stored email (keeps token) when token matches; RFC-8058 one-click supported.

**Tested (vitest, `test/index.test.js`, 18 tests):** exported pure fns `readSubscription`, `notificationFor`, `buildIssueBody`, `intVar`, `truncate`, `escapeHtml`, `handleUnsubscribeGet`. **NOT tested (need Workers runtime):** `timingSafeEqualStr`, `verifySignature`, and the `fetch` handler's GitHub/Brevo calls (matches CLAUDE.md).

### Stats endpoint (`server/stats/`)
- **`stats.php` (11 KB):** POST-only JSON ingestion → InfluxDB 3 Core line protocol. `INFLUXDB_URL = http://localhost:8181/api/v3/write_lp?db=sweetspot&precision=second` (L33). Config via env `INFLUX_TOKEN` (required) + `KUMA_PUSH_URL` (optional) (L34-38). Rate limit 300s/IP via files in `/tmp/sweetspot_rate` (L39-40, checked AFTER validation so bad requests don't consume quota, L311). Limits: `MAX_BODY_SIZE 64KB`, `MAX_RECORDS 500`.
  - **Validation:** POST only (405); User-Agent must start `SweetSpot/` (403); version ∈ {1,2}; app `/^[\d.]+$/`; **status whitelist `['trial','subscribed','unlocked','expired','unknown']` (L259)** — `subscribed` current, `unlocked` legacy (matches the CLAUDE.md status-sync gotcha); zone `/^[A-Z][A-Z0-9_]{0,15}$/`; source `/^[a-z][a-z0-9_]{0,31}$/`; device ∈ {phone,watch}; timestamp int 1.7e9–4.1e9; `ok` bool; failures need error `/^[A-Z][A-Z0-9_]{0,31}$/`; `ms` 0–300000.
  - **Response:** 200 `{"ok":true,"records":N}` returned **only after** InfluxDB acks 204 (write failure → 502). Then best-effort `ping_kuma()` heartbeat (swallows all errors).
  - Measurement `api_fetch`; tags zone/source/device/app/outcome(ok|fail)/error/lang/status; fields count=1i, duration_ms.
- **`stats.sweetspot.today.conf`:** Apache vhost; rewrites `POST /report` → `stats.php`; denies all other paths via `<LocationMatch "^/(?!report$)">`; trusts `CF-Connecting-IP`.
- **`test.sh`:** smoke tests against prod (or local arg). Valid + 12 rejection cases + rate-limit case. Test traffic uses synthetic marker `s=test, z=ZZ, app=0.0.0` (auto-excluded from dashboards).
- **`clear-rate-limit.sh`:** `sudo find /tmp -name sweetspot_rate -type d -exec rm -rf` (handles Apache PrivateTmp).
- **`grafana-dashboard.json`:** committed dashboard "SweetSpot API Reliability"; **11 queries carry `source != 'test'`** filter.
- **Deploy:** `bin/deploy-stats.sh` scp to `aurora`. `.htaccess` (holds secrets) is NOT committed.

---

## 5. Build / coverage config facts

- **Modules (`settings.gradle.kts`):** `:app`, `:shared`, `:wear`. Root name `SweetSpot`. Repos: google, mavenCentral, **JitPack** (for compose-markdown). Foojay toolchain resolver 1.0.0.
- **Version (`buildSrc/src/main/kotlin/sweetspot-app.gradle.kts`):** `applicationId today.sweetspot`, `compileSdk 36`, `targetSdk 36`, **`versionCode = 37`, `versionName = "6.6"`** (L25-26). `:app` minSdk 26, `:wear` minSdk 30. **Wear versionCode offset +1_000_000** (`wear/build.gradle.kts:36`) → 1000037.
  - **DRIFT: CLAUDE.md/README use v3.x examples; actual shipping version is 6.6 / code 37.**
- **Convention plugin** (`sweetspot-app`) shared by `:app` + `:wear`: applies `com.android.application`, kotlin compose + serialization; signing from `local.properties` (`RELEASE_STORE_FILE` etc.); debug `applicationIdSuffix .debug`; release `isMinifyEnabled + isShrinkResources + ndk.debugSymbolLevel FULL + proguard`. Java/Kotlin 17. `buildConfig` field `ENTSOE_API_TOKEN` from `local.properties` `ENTSOE_API_TOKEN` (empty if absent). Tasks `printVersionCode`/`printVersionName`.
- **localeFilters (L31-35):** 25 codes `bg cs da de el en es et fi fr hr hu it lt lv mk nb nl pl pt ro sk sl sr sv`. Montenegrin `cnr` deliberately excluded.
- **Kover excludes:**
  - `:shared` (`shared/build.gradle.kts:34`): `@Serializable`-annotated + `*.BuildConfig`.
  - `:app` (`app/build.gradle.kts:8`): `@Composable`; `*ComposableSingletons*`; `*.BuildConfig`; all `today.sweetspot.ui.*` sub-packages listed explicitly (components/onboarding/settings/share/theme — deliberately not relying on one `ui.*`); `MainActivity`(+`$*`,`Kt`); `WearableStatsBridge`/`WearableUsageBridge`(+`$*`); `data.billing.PlayBillingRepository`; `data.stats.HttpStatsPoster`; `data.support.HttpReportSubmitter`(+`$*`).
  - `:wear` (`wear/build.gradle.kts:8`): `@Composable`; `*ComposableSingletons*`; `*.BuildConfig`; `WearActivity`; `WearableSync`(+`$*`).
- **Coverage GATES (`bin/coverage-report.py:32`):** shared **98.0**, app **97.0**, wear **93.0** (line %). CI gate = the script, not `koverVerifyDebug`.
- **codecov.yml:** project + patch status `informational: true` (no gate); `comment: false`; per-module flags shared/app/wear with `carryforward: true`.
- **Test count (verified):** **726** `@Test` methods total — shared 495, app 203, wear 28 (726 = exactly matches README/CLAUDE.md "726 tests"). Worker: 18 vitest; supplier script: 25 python tests.
- **Subscription product id:** `PRODUCT_ID = "yearly_subscription"` (`app/.../data/billing/PlayBillingRepository.kt:46`) — matches CLAUDE.md.
- **Key deps (`gradle/libs.versions.toml`):** AGP **9.0.1**, Kotlin **2.3.20**, Kover **0.9.8**, compose-bom **2026.03.01**, okhttp **5.3.2**, serialization-json **1.11.0**, coroutines **1.10.2**, billing **8.3.0**, play-services-wearable **19.0.0**, wear-compose **1.6.1**, robolectric **4.16.1**, junit 4.13.2, zxing 3.5.3, compose-markdown **0.5.7** (JitPack), reorderable 3.1.0, browser 1.8.0, dokka 2.2.0.
- **`.ruby-version`:** 3.3.11.
- **buildSrc plugin deps** (`buildSrc/build.gradle.kts`): AGP 9.0.1, kotlin-gradle-plugin/compose-compiler/serialization all 2.3.20 — **hardcoded, duplicated from libs.versions.toml with a "keep in sync" comment (drift risk).**

### Fastlane (`fastlane/Fastfile`)
- `screenshots` — `assembleDebug` + `assembleDebugAndroidTest` + Screengrab (all or `locale:xx`).
- `publish` — resolves max alpha-track version code, `upload_to_play_store` package `today.sweetspot` json_key `fastlane/credentials.json`, `sync_image_upload:true`, skips apk/aab/changelogs (metadata+images only).
- `deploy` — phone AAB → `track`, wear AAB → `wear:<track>`, `release_status: completed`; `skip_phone`/`skip_wear` params. Metadata/images/screenshots skipped.
- **Note:** `fastlane/credentials.json` (service account) appears committed in the dir listing — verify it's a placeholder / gitignored.

---

## 6. Outdated / inconsistent / surprising

1. **Version drift (docs):** shipping `versionName 6.6` / `versionCode 37` (`sweetspot-app.gradle.kts:25-26`); CLAUDE.md "Releasing" and README examples still say `3.0`. Only an example, but stale.
2. **`bin/inspect.sh` always exits 1** (L50) even on a clean summary — `make inspect` never returns 0. Intentional but surprising; not a gate.
3. **buildSrc plugin versions hardcoded** (AGP/Kotlin 9.0.1 / 2.3.20) and manually kept in sync with `libs.versions.toml` — a real drift hazard on version bumps.
4. **`dokka` is declared in `libs.versions.toml` (2.2.0) but never applied** in any build file — dead/unused dependency entry.
5. **`fastlane/credentials.json` present in the working tree** — a Play service account key; confirm it's gitignored (it's written from a secret in CI, L25 of publish-listing.yml, so the committed copy is likely a leftover/placeholder — worth flagging).
6. **`test` vs CI:** `make test` runs `./gradlew test` (all variants); CI runs `testDebugUnitTest` only. Local `make test` is heavier than CI.
7. **Two overlapping locale-mapping tables** exist: `bin/lib/common.sh` (`website_to_metadata`, `metadata_locale`) and an independent `metadata_dir` inside `bin/site-screenshots.sh` — same lang→Play-locale mapping duplicated; could drift.
8. **Stats status whitelist** still the known sync-point: `stats.php:259` lists `subscribed` (current) + `unlocked` (legacy). Correct now, but this is the exact field that silently 400'd for months previously — any app-side status string change must land here.
9. **Feedback Worker KV namespace id is committed** in `wrangler.jsonc` (public config, harmless, but the comment says "Replace the id after `wrangler kv namespace create`" implying a placeholder — it's actually a live id).
10. **Worker `compatibility_date 2026-07-28`** — very recent; fine but pinned.
