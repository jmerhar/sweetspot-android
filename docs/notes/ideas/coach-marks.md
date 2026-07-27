# Contextual Coach Marks

## Idea

Follow-up to the first-launch onboarding intro (shipped — see `ideas/done/onboarding.md`). The intro
deliberately stays value-first and short; it does **not** tour every feature. The complement, per the
onboarding best-practice research, is **just-in-time contextual hints**: introduce an advanced feature
with a one-time coach mark *the first time the user reaches it*, rather than front-loading everything
on day one.

## Where hints add value (first-encounter, one-time each)

- **Results screen — Earlier / Cheaper**: what stepping to a sooner (costlier) window does, and that
  you can step back toward the cheapest.
- **Results screen — press-and-hold the chart**: the per-slot price tooltip isn't discoverable; a hint
  ("press and hold a bar for its price") the first time a results chart is shown.
- **All-in total ⇄ spot toggle**: the first time the toggle appears on the results screen (only once
  all-in is configured), point out that it flips between the total and the bare market price.
- **EV chip → state-of-charge prompt**: brief note that tapping a vehicle plans charging by battery %.

The home-screen empty state already nudges toward Settings when there are no appliances — keep that as
the natural customisation prompt rather than a coach mark.

## Principles (from the onboarding research)

- **One at a time, at the moment of relevance** — never a stack of tooltips on one screen.
- **Once only** — each hint has its own "seen" flag; dismissing it (or acting on the feature) retires it.
- **Never block** — a hint is dismissible and never gates the underlying control.
- **Reachable/again**: the "How it works" intro already lives in Settings; individual hints don't need
  re-entry, but a "reset tips" dev/settings action could help testing.

## Technical notes (net-new infrastructure)

- **No coach-mark/anchored-tooltip infrastructure exists yet.** This needs a small reusable helper: an
  anchored popup/callout pointing at a target composable (measure the target's bounds, draw a bubble
  with a tail, scrim the rest). Compose has no built-in; either build a light one (a `Popup` positioned
  from the target's `onGloballyPositioned` bounds — similar to the chart tooltip's `Popup` approach) or
  evaluate a small dependency. Prefer building a minimal one to avoid a new dependency.
- **Per-hint "seen" flags** in `SettingsRepository` (one boolean each, mirroring `onboarding_shown` /
  `stats_prompt_shown`); the ViewModel exposes which hint (if any) is due, and the screen renders it.
  Keep the "which hint is due" decision in the ViewModel/a pure helper (tested), not the composable.
- **TalkBack / a11y**: coach marks are net-new for accessibility — the callout needs a content
  description and must not trap focus; verify with TalkBack. There's no reduce-motion convention yet.
- **Don't stack with onboarding or the paywall** — suppress hints while `showOnboarding`/`showPaywall`
  are active (same guard as the overlay dialogs).
- **Localisation**: each hint is a short string ×25 locales — keep them to one line.

## Open questions

- Build a minimal anchored-callout helper, or a simpler non-anchored bottom-sheet/snackbar-style hint
  (much cheaper, less precise)?
- Show at most one hint per session to avoid overwhelming, even if several are "due"?
- A dev/settings "reset tips" action for testing and for users who want to see them again?

## Out of scope

- Re-touring on every update; a hint fires once per feature, not per release.
- Wear app hints (phone-driven).
