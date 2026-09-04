# Upstream data-source monitoring

**Status:** planned, not implemented.

## The gap

Between 30 Aug and 3 Sep 2026, ENTSO-E returned HTTP 503 (and timeouts) for the
PT zone. PT had ENTSO-E as its only source, so the app was completely unable to
show prices there for several days. Nothing alerted; the outage surfaced only
when a user reported it, and the evidence had been sitting in `api_fetch` the
whole time.

The three existing signals (see the *Stats Backend & Monitoring* section of
`CLAUDE.md`) all watch **our own ingestion pipeline** — whether reports reach
InfluxDB, and whether the endpoint is turning them away. All three were green
throughout, correctly: the pipeline was fine. It faithfully recorded that price
fetches were failing, and nothing was watching that.

So this is a fourth question, distinct from the other three:

> Are the upstream price APIs actually serving prices?

## Two signals, because "fetches are failing" has two shapes

The distinction that governs the whole design is the same one that separates
`/health` from the traffic-driven push monitor:

- **`api_fetch` is traffic-driven.** Rows exist only when someone opens the app.
  With the current user base a quiet day produces no rows at all, so a naive
  "ENTSO-E failure rate > 50%" rule is silent exactly when there is no traffic,
  and trigger-happy on a day with three requests of which two failed. It cannot
  be the primary availability signal.
- **A probe is monitor-driven.** It runs on a schedule whether or not anyone is
  using the app, which is what turns "ENTSO-E is down" into a detection in
  minutes rather than a support ticket days later.

Both are worth having, for different questions, and neither substitutes for the
other.

## Component 1 — `bin/monitor/probe-sources.py` (cron on aurora)

Probes each data source on a schedule and records the outcome.

- **One representative zone per source**, not all 43. A full sweep is ~100
  requests and the fallback APIs rate-limit well below that (measured: Energy-Charts
  starts answering 429 after roughly eight rapid requests, so requests need ≥8s
  spacing). ENTSO-E's documented limit is 400 requests/minute per IP and token,
  with a 10-minute ban on breach — a 15-minute probe is negligible against that.
- **Asserts semantic health, not HTTP 200.** The verdict is: 200 *and* parseable
  *and* contains enough price points to cover today. This is the crux — ENTSO-E
  answers an outage-adjacent condition with a 200 carrying an
  `Acknowledgement_MarketDocument`, and returns 200 with zero points for a period
  it has no data for. A plain HTTP monitor calls both healthy. This is the same
  blindness that `/health` has to rejections, and the reason a bespoke probe earns
  its keep over a stock Kuma HTTP check.
- **Writes to a new `upstream_probe` measurement** — tags `source`, `zone`,
  `outcome`, `error`; fields `duration_ms`, `points`. Kept out of `api_fetch` for
  the same reason `ingest_canary` is: synthetic traffic must never skew the
  reliability figures, and InfluxDB 3 Core cannot delete rows, so a separate
  measurement is the only way to keep the option of dropping it wholesale.
- **Pure logic (response → verdict) in testable functions**, with
  `bin/monitor/test_probe_sources.py` wired into `make test-scripts`, following
  `build-suppliers.py` / `test_build_suppliers.py`.
- ENTSO-E token from the environment, as `build-suppliers.py` reads `ENEVER_TOKEN`.

Recording `duration_ms` also gives the latency distribution that the read-timeout
value depends on, instead of the hand-measured sample it currently rests on.

## Component 2 — `GET /sources` on `stats.php`

Summarises recent `upstream_probe` rows into a single verdict for a monitor to
watch, exactly as `/rejects` does for `endpoint_reject`.

- Returns `{"healthy":bool,"degraded":[...],...}` with `healthy` **first**, because
  Kuma truncates the body to 47 characters in its alert text.
- The keyword must be one the failure body cannot contain: match `"healthy":true`,
  since a failing body reads `"healthy":false`. (Matching `"healthy"` alone would
  repeat the `/health` trap, where `"ok"` appears in `"ok":false`.)
- **The vhost carries the route map**, so `stats.sweetspot.today.conf` must add
  `/sources` and deploy together with `stats.php` — otherwise the live vhost
  denies the new route while the script happily implements it.

## Component 3 — Kuma monitors

- Keyword monitor on `/sources`, 15-minute interval, in the
  `Infrastructure / SweetSpot` group (`parent: 135`), shared notification
  (`notificationIDList: {"1": true}`).
- Grafana deep link in the monitor `description`, not the body — the existing
  notification template surfaces it, and the body has no room.
- Created via the socket.io CLI (`docker exec kuma-api python /app/kuma_client.py`),
  copying the shape of an existing monitor with `get <id>`.

## Component 4 — the alert that would have caught this one

The highest-signal rule is not "a source is failing" but **"a zone has no working
source"**. A primary failing while a fallback succeeds is a non-event for the
user; every source in a zone's chain failing is a total outage for whoever lives
there. `api_fetch` can already tell these apart: group by zone over a window and
check whether *any* row for that zone has `outcome='ok'`.

This one is safe to build on traffic-driven data, because it only ever fires on
the *presence* of all-failed attempts. No traffic means no alert, which is the
correct answer — nobody was affected.

Worth pairing with the reverse view for tuning rather than alerting: zones where
the primary failed but a fallback carried the request. That is the number that
says whether the fallback chains are doing their job.

## Component 5 — dashboard panels

Add to the committed `server/stats/grafana-dashboard.json`:

- Upstream probe availability per source over time (from `upstream_probe`).
- Zones with no working source (the Component 4 query).
- Probe latency percentiles per source, to keep the read-timeout choice evidenced.

## Component 6 — periodic coverage audit

A weekly job that probes **every** zone against **every** source registered for
it, asserting real prices come back. This validates the zone maps themselves
rather than the APIs' uptime: a renamed or retired `bzn` degrades a fallback to a
permanent HTTP 400, which presents as a source that exists but never works — a
failure invisible to a probe that only ever checks one representative zone per
source. The 15 zone mappings added for the PT fix are 15 new opportunities for
exactly that rot.

Weekly, heavily spaced, and it may need to run per-source across separate windows
to stay inside the fallback APIs' burst limits.

## Sequencing

1. Component 4 first. It is a query over data that already exists, needs no new
   infrastructure, and is the alert that maps directly onto user impact.
2. Components 1–3 next: the probe, the endpoint, the monitor.
3. Components 5 and 6 after, once the probe data has accumulated.

## Open questions

- Which zone represents each source? It should be one the source genuinely serves
  well, and for ENTSO-E ideally not a zone that is itself flaky.
- Alert threshold for Component 4: one all-failed zone-hour is probably noise, and
  a whole day is too slow. Needs calibrating against the historical `api_fetch`
  data rather than guessed.
- Whether the probe should also assert *price plausibility* (a feed stuck at a
  constant value, or wildly out of range, is broken in a way a point count cannot
  see).
