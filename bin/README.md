# `bin/`

Helper scripts for building, testing, releasing, and maintaining SweetSpot, grouped by concern to
mirror the `make` targets (run `make help`). Python holds the data/logic-heavy tools (unit-tested);
bash holds thin orchestration of external tools (Gradle, ADB, Fastlane, Hugo, ImageMagick, Wrangler,
ssh). Most scripts assume the repo root as the working directory — invoke them via `make`.

## `lib/` — shared bash libraries
- `log.sh` — `log_info` / `log_success` / `log_warn` / `log_error` / `die` (TTY-aware colours).
- `locale.sh` — language ↔ Play Console locale mapping helpers.
- `gallery.sh` — HTML gallery builders used by the image scripts.
- `require.sh` — `require_command` dependency check.
- `common.sh` — aggregator that sources all of the above.

## `deploy/` — release & deploy
- `release.sh` — bump version, build, tag, push, create a GitHub Release.
- `deploy.sh` — deploy AABs with localised release notes to the Play Store.
- `deploy-stats.sh` — deploy `stats.php` to the stats server.
- `deploy-feedback.sh` — deploy the feedback Worker to Cloudflare.

## `device/` — on-device install
- `install.sh` — install a release/debug APK on a connected phone or watch via ADB.

## `site/` — website
- `site-screenshots.sh` — per-language WebP screenshots from the framed Play images.
- `site-validate.sh` — build and validate the Hugo site (pages, links, i18n parity).
- `install-hugo.sh` — install the Hugo extended binary (used by CI).

## `data/` — bundled data builders (Python, unit-tested)
- `build-ev-db.py` — build the bundled EV vehicle database.
- `build-suppliers.py` — build the all-in tariff feeds.
- `test_build_ev_db.py` / `test_build_suppliers.py` — their unit tests.

## `playstore/` — marketing images
- `frame-screenshots.sh` — frame raw screenshots with marketing text.
- `feature-graphic.sh` — generate localised Play Store feature graphics.
- `check-listing-lengths.sh` — CI guardrail: fail if any locale's title/short/full description exceeds the Play Store limit.

## `quality/` — tests & coverage
- `inspect.sh` — summarise Android Studio inspection XML exports.

The coverage summary, gate and publishing are shared tooling from `jmerhar/coverage`, driven by
`coverage.toml` in the repo root — see the coverage section of `CLAUDE.md`.
