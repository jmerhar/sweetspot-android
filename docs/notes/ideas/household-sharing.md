# Household Sharing

## Idea

Let a user share their list of appliances with other people in the same household, so
everyone doesn't have to re-enter the dishwasher, washing machine, dryer, EV, etc. by
hand. Two people running the same appliances against the same prices should be able to
set up once and copy across.

The natural unit to share is the **appliance list** — the JSON-serialised
`List<Appliance>` that already lives in `SettingsRepository` (SharedPreferences) and is
already pushed to the watch via the Data Layer. Each `Appliance` is self-contained
(name, duration, icon, optional `EvSpec`, optional `powerKw`), so the payload is small,
portable, and needs no server to make sense of.

Open questions that shape the design:
- **One-time copy vs. continuous sync?** "Set up my partner's phone once" is a very
  different feature from "keep both phones in lockstep forever."
- **What else travels with it?** Just appliances, or also the region/zone, source order,
  and all-in tariff settings? Region especially is usually shared within a household.
- **Does it fit the app's ethos?** SweetSpot has no accounts, no backend for app data,
  zero required permissions, and a privacy-first stance (opt-in anonymous stats only).
  Anything that introduces a login or a server holding user data is a real departure and
  should clear a high bar.

Below are a few approaches, roughly ordered from most in-keeping with the current
architecture (offline, no backend) to biggest departure (cloud sync).

## Approach 1 — QR code (offline, one-time copy)

The sender opens Settings → "Share appliances", which renders the appliance list as a QR
code. The receiver scans it with their camera (or an in-app scanner) and gets a
"Import N appliances?" prompt.

- **Payload:** the appliance JSON, gzipped + Base64, encoded into the QR. A dozen
  appliances is well within QR capacity (~2–3 KB at reasonable density); if it ever
  overflows, fall back to a link (Approach 2).
- **Flow:**
  1. Sender: Settings → Share appliances → QR shown on screen.
  2. Receiver: scans → preview list → "Add all" / "Replace my list" / pick individually.
- **Pros:** Fully offline, no server, no accounts, no permissions beyond camera (and even
  that only on the receiving side, only when scanning). Feels magical for two phones in
  the same room — exactly the household case. Strongly matches the app's privacy stance.
- **Cons:** One-time snapshot only — no ongoing sync. Both people must be physically
  together. Camera dependency for the receiver.
- **Effort:** Low–medium. Encoding is trivial (the list is already `@Serializable`). QR
  generation is a small dependency (e.g. ZXing) or hand-rolled; scanning adds CameraX +
  ML Kit or ZXing. A "show a code, type it on the other phone" text fallback avoids the
  camera entirely for accessibility.

## Approach 2 — Share link / file via the Android Sharesheet (offline, one-time copy)

The sender taps "Share appliances" and gets the standard Android share sheet, sending
either a small `.sweetspot` / `.json` file or a deep link that encodes the payload. The
receiver opens it (WhatsApp, email, Nearby Share, Bluetooth, saved file) and the app
handles the intent with the same import preview.

- **Payload options:**
  - **Deep link:** `https://sweetspot.today/import#<base64-gzip-json>` — the fragment
    (`#…`) never leaves the device / is never sent to the server, so no data leaks to the
    static site. The link opens the app via an intent filter; a web fallback page can
    explain "open in the SweetSpot app."
  - **File attachment:** an exported `.json` the receiver opens with the app registered as
    a handler.
- **Pros:** No camera, works across any distance and any channel the user already uses.
  Reuses Android's share infrastructure. Still offline and serverless (with the fragment
  trick). Nice side benefit: doubles as **backup/restore** and **device migration**.
- **Cons:** Still a one-time snapshot. Deep-link length limits if the list is huge (rare).
  Slightly more setup (intent filters, MIME registration, a small landing page).
- **Effort:** Low–medium. This pairs well with Approach 1 — QR for "in the room," link for
  "across town," same encode/decode and same import-preview UI underneath.

## Approach 3 — Short share code via a lightweight relay (near-real-time, opt-in)

For "send to my partner who isn't next to me right now" without a full account system: the
sender uploads the payload to a minimal endpoint, which returns a short human-readable code
(e.g. `SWEET-4F2K`). The receiver types the code to fetch and import. The blob expires
after a short TTL (minutes/hours) and is then deleted.

- **Where it could live:** a tiny PHP endpoint on the existing `aurora` host (same box as
  the stats backend), storing an opaque encrypted blob keyed by code, with a TTL. No
  personal data, no account — just a short-lived envelope.
- **Pros:** No camera, no physical proximity, no accounts. Codes are easy to read aloud
  over the phone. Blob can be **client-side encrypted** (code doubles as the key) so the
  server only ever sees ciphertext.
- **Cons:** Introduces a (small) server component holding user data, even if transient and
  encrypted — a step away from "no backend for app data." Needs abuse/rate-limiting.
  Still fundamentally one-time (fetch-and-forget), not live sync.
- **Effort:** Medium. Small server endpoint + client upload/fetch + the same import UI.

## Approach 4 — Continuous household sync (biggest departure)

A real "household" that multiple devices belong to, where a change on one phone propagates
to the others. This is the "keep everyone in lockstep" interpretation.

- **Sketch:** one person creates a household and shares a join code/QR; joiners are added;
  appliance edits sync through a shared store. Would also naturally cover region/zone and
  source-order settings.
- **Options for the sync substrate:**
  - **Google account + Drive AppData folder** — per-user, private, no server to run;
    but AppData is per-Google-account, so it's cross-device for *one* person, not truly
    multi-person sharing. A **shared Drive file** could work but the sharing UX is clunky.
  - **Firebase / Firestore** — real multi-user sync out of the box, but adds Google Play
    Services dependency, a backend to own, and a privacy story to write. Big departure.
  - **Self-hosted sync doc on `aurora`** — full control, but now the app has to manage
    identity, auth, conflict resolution, and availability.
- **Pros:** The "real" feature — genuine shared state, edits stay in sync, region travels
  too. Best long-term UX for active multi-phone households.
- **Cons:** Needs identity/accounts (or at least stable per-household secrets), conflict
  resolution (two people editing at once), a backend with uptime obligations, and a
  privacy policy update. Contradicts the current no-accounts / no-app-backend design and
  the "your data stays on your device" pitch. Materially larger surface to build, test,
  secure, and support.
- **Effort:** High. This is a product direction, not a weekend feature.

## Recommendation

Start with **Approach 1 (QR) + Approach 2 (share link)** as a single "Share appliances"
feature — they share almost all the code (encode → transport → import preview) and cover
both "in the same room" and "across town" without any backend, accounts, or new required
permissions. That's squarely in keeping with the app's privacy-first, serverless design,
and it also gives **backup/restore and device migration** for free.

Only reach for **Approach 3/4** if there's real demand for cross-distance or continuous
sync — and treat Approach 4 as a deliberate product decision because of the accounts +
backend + privacy implications.

## Import semantics (applies to all approaches)

Regardless of transport, the receive side needs a clear merge policy:

- **Preview first** — always show what's about to be imported before touching the user's
  list. Never silently overwrite.
- **Merge modes:**
  - **Add** — append imported appliances (skip exact duplicates).
  - **Replace** — swap the user's list for the imported one.
  - **Pick** — let the user tick which appliances to import.
- **Duplicate handling** — dedupe by content (name + duration + specs), not by `id`.
  Shared appliances will carry the sender's UUIDs; the receiver should mint fresh `id`s on
  import (or keep, but detect content collisions) so the two lists don't alias.
- **EV specs** — `EvSpec` (`batteryKwh`, `acMaxPowerKw`) travels with the appliance and
  imports cleanly. The home-charger kW and default target SoC are *device* settings, not
  per-appliance — decide whether those ride along or stay local.
- **Icons** — icon IDs reference the shared `applianceIcons` registry, so they resolve on
  any install of the same app version. Unknown IDs already fall back gracefully
  (`ApplianceIconTest`).

## What to include in the payload

- **Definitely:** the appliance list (name, duration, icon, `EvSpec`, `powerKw`).
- **Maybe (household-scoped):** country / price zone — usually shared within a home, and a
  common re-entry pain point. Could be a separate opt-in toggle in the share dialog
  ("also share my region").
- **Probably not by default:** source order (advanced), all-in tariff/supplier/surcharge
  (region-specific, and the supplier is cleared on country change anyway), trial/unlock
  state (per-account entitlement — must **not** travel), stats opt-in (per-device consent).
- **Versioning:** stamp the payload with a schema version so future `Appliance` fields
  import safely and older apps can reject or partially import a newer blob.

## Considerations

- **Privacy** — the whole appeal of the offline approaches is that no data leaves the two
  devices. Keep it that way: use the URL *fragment* for deep links (never sent to the
  server), and if a relay is ever added (Approach 3), encrypt client-side so the server
  holds only ciphertext.
- **Entitlement must not leak** — subscription/unlock state is a per-Google-account
  entitlement via Play Billing. Sharing appliances must never share or grant unlock; the
  receiver's trial/subscription is entirely their own.
- **Watch** — no watch work needed. Appliances already sync phone → watch via the Data
  Layer after any change, so an import just triggers the existing `syncAppliancesToWear()`.
- **Testability** — the encode/decode + merge logic should live in `:shared` (pure,
  `@Serializable`-based) with unit tests (round-trip, dedupe, version-mismatch, malformed
  blob), following the same pattern as the API parse tests. The camera/scanner and
  Sharesheet plumbing stay thin and excluded from coverage per the presentation-vs-logic
  rule.
- **Localisation** — new UI strings (share dialog, import preview, merge modes, errors)
  need the full 25-language treatment, with `<plurals>` for "Import %d appliances".
- **Cross-version safety** — a newer sender and older receiver (or vice versa) will happen.
  The schema version + tolerant deserialisation (unknown keys ignored) handle this; surface
  a friendly "update the app to import this" when a payload is too new to understand.
