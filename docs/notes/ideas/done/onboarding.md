# First-Launch Onboarding

> **Status (implemented):** the 3-screen skippable intro is implemented (`OnboardingScreen`, gated by
> the `onboarding_shown` flag, re-openable from Settings › How it works). The deferred **contextual
> coach marks** in the "Contextual hints" section below were split out into `ideas/coach-marks.md` and
> are not yet built.

## Idea

The app has grown well beyond "pick a duration, see the cheapest window": appliances with power
ratings, EV charging with state-of-charge and a "ready by" deadline, the all-in total price with a
component chart and press-and-hold tooltip, sorting/grouping, household sharing by QR/link, per-zone
data-source preferences, and a Wear companion. A first-time user sees none of that and can't tell
what's possible. We should introduce a **short first-launch onboarding** that explains the core value
and points at what can be customised — **useful, but deliberately not overwhelming**.

Two hard constraints shape this: SweetSpot has **no account/login** (nothing to verify up front), and
the country/zone is **already auto-detected** on first launch. So onboarding is pure education, not
setup — which is exactly the case where market leaders keep it *lightest*.

## What market leaders do (best practices)

From Apple's Human Interface Guidelines, Android/Material onboarding guidance, and how apps like
Duolingo/Headspace/Notion handle it:

- **Value first, not a feature tour.** Lead with the job-to-be-done and its payoff; only mention
  features that support that payoff. A long feature dump is the classic mistake for a multi-feature app.
- **Keep it to ~1–3 full screens, or none.** For a discoverable utility app the guidance leans toward
  *fewer* screens plus contextual hints, over a 4–6 screen carousel.
- **Teach by doing / show the product fast.** People retain an action they take over a slide they read;
  get them into the real UI quickly.
- **Always skippable, and reachable later.** A Skip control up front; store completion; keep the intro
  available from Settings/Help so it's re-openable without being re-forced.
- **Just-in-time contextual hints (coach marks) beat front-loading.** Introduce an advanced feature
  *where and when* it's first encountered, not all at once on day one.
- **Never block usage** unless there's a true prerequisite (we have none).

Sources: Apple HIG — Onboarding (https://developer.apple.com/design/human-interface-guidelines/onboarding);
Android — Authentication & Onboarding (https://developer.android.com/design/ui/mobile/guides/patterns/onboarding);
Material — Onboarding (https://m1.material.io/growth-communications/onboarding.html).

## Proposed approach for SweetSpot

A **2–3 screen value intro** on first launch, then **contextual hints** for the deeper features — not
a tour of everything.

### First-launch intro (skippable `HorizontalPager`, dots + Skip/Next → Get started)

```
┌───────────────────────────┐  ┌───────────────────────────┐  ┌───────────────────────────┐
│        ⚡ + 📉             │  │      🧺  🚗  🔥            │  │        ⚙️  ✅              │
│  Run appliances when       │  │  Save your appliances —    │  │  Prices for <Country> are  │
│  power is cheapest         │  │  tap one for its cheapest  │  │  set automatically.        │
│                            │  │  time. Add your EV to plan │  │  Change country/zone,      │
│  SweetSpot finds the       │  │  charging by battery %.    │  │  add appliances, or show   │
│  cheapest time window from │  │                            │  │  the all-in total price in │
│  today's electricity price │  │                            │  │  Settings anytime.         │
│                    Skip →  │  │                    Skip →  │  │        [ Get started ]     │
└───────────────────────────┘  └───────────────────────────┘  └───────────────────────────┘
   1. the payoff                  2. the core actions            3. "make it yours" + setup
```

- **Screen 1 — the payoff.** One sentence: cheapest time to run an appliance, from live prices.
- **Screen 2 — the core actions.** Quick-duration / appliance chips → a result; EVs plan charging by
  SoC. This is what 90% of users do.
- **Screen 3 — make it yours (+ the one real setup).** Reassure that the country is auto-detected and
  everything (country/zone, appliances, all-in total price) lives in Settings. Optionally let them
  confirm/adjust the detected country here — the *only* setup SweetSpot actually needs.
- End on **Get started**, drop straight into the normal form screen. Keep copy to one short line per
  screen; lean on an illustration/icon per screen.

### Contextual hints (after first launch, at the moment of relevance)

Rather than explain these up front, show a one-time coach mark the first time each is reached:

- **Results screen:** the **Earlier / Cheaper** buttons, and **press-and-hold the chart** for a
  per-slot price tooltip.
- **All-in total price:** the results-screen **total ⇄ spot** toggle (only once all-in is configured).
- **EV chip:** the state-of-charge prompt.
- **Empty state already helps** — when there are no appliances the form shows a CTA into Settings; keep
  that as the natural nudge toward customisation.

## What to avoid

- A 5+ screen carousel that tours every feature (sorting, grouping, sharing, data sources, Wear…).
  Those are discoverable and better introduced contextually or left for the curious.
- Blocking the app behind onboarding, or re-showing it on every launch.
- Stacking onboarding on top of the trial paywall — keep them separate; first launch is onboarding
  with the trial simply running.
- Long paragraphs; anything that isn't the first meaningful action should move later or become a hint.

## Technical notes (fit with the current app)

- **Gating:** a `SharedPreferences` flag in `SettingsRepository` (mirrors `isStatsPromptShown()` /
  first-launch-time), e.g. `isOnboardingShown()` / `setOnboardingShown()`. `SweetSpotViewModel`
  exposes `showOnboarding` on `UiState`; `MainActivity` shows the intro when it's the first launch and
  not yet shown, taking precedence like the import-preview screen does. Any per-hint "seen" state is
  the same pattern (one flag per hint).
- **Where the code lives:** Compose screens under `ui/onboarding/` (a `HorizontalPager` + a thin
  `OnboardingScreen`), excluded from coverage like the rest of `ui.*`. Keep *logic* out of the
  Composables per the repo rule — page model, gating, and "which hints to show" belong in the
  ViewModel or a small pure helper (tested); the screens just render.
- **Re-entry:** add a "How it works" row to the Settings root menu (`SettingsRoute`) that replays the
  intro — cheap, and satisfies "reachable later."
- **Localisation:** every line is a new `strings.xml` key ×25 locales — a real cost, so keep the copy
  minimal (a title + one line per screen). No numbers-in-strings, so no plurals needed.
- **Website/store:** the intro screens double as good Play Store screenshots and a website "how it
  works" section, so design them presentably.

## Open questions

- 2 screens or 3? (Confirming the auto-detected country on screen 3 adds genuine value but also a step.)
- Full-screen intro **and** contextual coach marks, or start with just the intro and add hints later?
- Do we need a coach-mark helper (anchored tooltips) now, or defer hints to a follow-up and ship only
  the intro first?

## Out of scope / later

- A full interactive walkthrough of every feature.
- Onboarding for the **Wear** app (it's driven by the phone; a first-run line on the watch is enough).
- Personalisation questions ("what do you want to save on?") — no payoff here without an account.
