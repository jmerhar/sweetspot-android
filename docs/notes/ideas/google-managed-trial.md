# Google-Managed Trial (Replace App-Managed Trial)

## Status: Pending

## Summary

Replace the app-managed 14-day trial with a Google Play-managed free trial on the
`yearly` subscription base plan. Google handles trial tracking, payment collection,
and automatic conversion — reducing code complexity and preventing trial resets.

## Current state

The app manages its own trial via SharedPreferences (`trial_start` timestamp).
`isTrialExpired()` checks if 14 days have elapsed. When expired, a paywall screen
appears with a "Subscribe" button. Trial/unlock state syncs to the watch via the
Data Layer. Users can reset the trial by clearing app data or reinstalling.

## What changes

### Play Console

- Add a 14-day free trial offer to the `yearly` base plan (Monetize > Subscriptions >
  `yearly_subscription` > Base plan > Offers > Add offer > Free trial)
- No code change needed for the billing flow itself — `PlayBillingRepository` already
  handles subscriptions

### Code to remove

- `SettingsRepository`: remove `trial_start`, `isTrialExpired()`, `trialDaysRemaining()`,
  `TRIAL_DAYS` constant
- `SweetSpotViewModel`: remove trial state tracking, paywall logic based on trial expiry,
  trial days remaining in `UiState`, stats prompt delay (tied to first launch time)
- `MainActivity` / UI: remove paywall screen (`PaywallScreen`), trial countdown chip on
  main screen, "Subscribe" option in settings during trial
- Watch Data Layer: remove `is_trial_expired` field from `/settings` sync
- `WearViewModel`: remove `isLocked` computation from trial state
- Wear UI: remove `WearLockedScreen` (Google handles access — if subscription lapses,
  the phone paywall handles re-subscription, watch just won't get fresh prices)
- Tests: remove trial-related tests from `SweetSpotViewModelTest` and `WearViewModelTest`

### Simplified flow

1. User installs → Google prompts "Start 14-day free trial" (payment info required)
2. User enters payment method → trial starts → app works normally
3. After 14 days → auto-converts to €2.49/year subscription
4. If user cancels during trial → no charge, app becomes locked on next `onResume()`
   purchase re-query (same mechanism as subscription expiry today)

## Trade-offs

### Benefits

- **No trial reset exploit** — Google tracks trials per account, survives reinstalls
  and device switches
- **Higher conversion rate** — ~40-60% vs ~2-5% for app-managed trials, because users
  must actively cancel (payment info already entered)
- **Less code** — removes trial tracking, paywall screen, Data Layer trial sync, watch
  locked screen, and associated tests
- **Play Console analytics** — trial-to-paid conversion, churn at trial end, retention
  curves come for free
- **Consistent UX** — users see the standard Google Play subscription dialog they're
  familiar with

### Downsides

- **Fewer trial starts** — requiring payment info upfront typically reduces trial uptake
  by 50-70%. Many users won't enter a credit card for an app they've never used.
- **Less organic discovery** — fewer people experience the app, fewer reviews, less
  word-of-mouth. Matters more for a new app building traction.
- **Value needs to be communicated upfront** — users must be convinced by the store
  listing alone, rather than experiencing the app first. SweetSpot's value ("find the
  cheapest time window") is abstract until you've used it.

### Net revenue estimate

Rough comparison at 1000 installs/month:

| Metric              | App-managed | Google-managed |
|---------------------|-------------|----------------|
| Trial starts        | ~1000       | ~300-500       |
| Conversion rate     | 2-5%        | 40-60%         |
| Paying users        | 20-50       | 120-300        |

Google-managed likely yields more revenue despite fewer trial starts.

## When to implement

Consider switching when:

- The app has enough ratings and reviews that users trust it before trying
- Store listing screenshots and description clearly communicate the value
- Trial reset abuse becomes a measurable problem
- You want to simplify the codebase

Not recommended while the app is still new and building its initial user base.
