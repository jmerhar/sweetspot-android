# Publishing to Google Play Store

## One-time setup

1. **Google Play Developer account** — $25 one-time fee at [play.google.com/console](https://play.google.com/console). Requires a Google account and identity verification (can take a few days).

2. **App signing** — Google Play manages its own signing key. You upload an AAB (Android App Bundle) instead of an APK. Google re-signs it for distribution. Your current upload key (`release.jks`) becomes the "upload key" that proves it's you.

## Code changes needed before publishing

### Build AAB instead of APK

```bash
./gradlew bundleRelease
```

Produces `app/build/outputs/bundle/release/app-release.aab`.

The release script (`bin/release.sh`) currently builds APKs. Either update it to also
build AABs, or add a separate `make bundle` target.

### Monochrome icon layer

Both `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` and
`wear/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` are missing a `<monochrome>`
layer. On Android 13+ with themed icons enabled, the app shows a blank circle instead
of its icon. Since `targetSdk = 36`, this should be fixed before publishing.

### Play Billing integration (for freemium)

See the [Monetization](#monetization-strategy) section below for full details.

## Store listing requirements

### Text content

- **App name** — "SweetSpot" (max 30 characters)
- **Short description** — one-liner, max 80 characters. E.g. "Find the cheapest time
  to run your appliances with dynamic electricity prices."
- **Full description** — up to 4,000 characters. Cover: what the app does, supported
  countries (30 countries, 43 zones), data sources, Wear OS companion, privacy stance,
  open source. Include keywords naturally for ASO.
- **Privacy policy URL** — link to `https://sweetspot.today/privacy/` (already exists)
- **Category** — Tools or Utilities

### Visual assets

| Asset | Spec | Notes |
|---|---|---|
| App icon | 512×512 PNG | High-res version of the launcher icon |
| Feature graphic | 1024×500 PNG/JPG | Banner shown at the top of the store listing. Show the app UI with a tagline. |
| Phone screenshots | 2–8, 16:9 or 9:16 | At minimum: main screen with appliances, result screen with chart, settings. Ideally 4-6 covering the key flows. |
| 7" tablet screenshots | Optional but recommended | Same flows, tablet layout |
| Wear OS screenshots | 2–8, 1:1 circular | Appliance list, result screen on watch face |

Screenshots should show real-looking data (not placeholder). Consider using a design
tool (Figma, screenshots.pro) to add device frames and captions.

### Required forms

1. **Content rating** — complete the IARC questionnaire in Play Console. The app has
   no violence, gambling, sexual content, or user-generated content. Should get an
   "Everyone" rating.

2. **Data safety form** — declare what data the app accesses:
   - **Network requests** — to ENTSO-E, EnergyZero, Spot-Hinta.fi, Energy-Charts,
     and aWATTar APIs. These requests contain the user's selected bidding zone
     (not location) and date range. No authentication tokens are sent to third-party
     APIs (the ENTSO-E token is a general API key, not user-specific).
   - **No personal data collected** — no accounts, no analytics, no tracking, no
     device identifiers, no crash reporting.
   - **Data stored locally only** — prices cached on-device, appliances in
     SharedPreferences.
   - **No data shared with third parties.**

3. **Ads declaration** — declare "no ads" (unless ads are added later).

4. **Government apps declaration** — not applicable.

5. **Financial features declaration** — not applicable (the app shows electricity
   prices but doesn't process payments or provide financial advice).

### Store listing localisation

The app supports 26 languages. Translate the store listing (app name, short
description, full description) for at least the major markets:
- **Priority:** English, Dutch, German, French, Slovenian (matches website)
- **Secondary:** Swedish, Norwegian, Danish, Finnish, Spanish, Italian, Polish
- **Tertiary:** remaining supported languages

Play Console supports auto-translation but quality is poor. Manual translation
is better for the short/full description.

Screenshots can be language-specific (showing the app in that language) or use
English screenshots with localised captions.

## Release process

### First release

1. Create app in Play Console
2. Fill out all store listing content, forms, and visual assets
3. Upload the `.aab` file
4. Start with **internal testing** (up to 100 testers, instant approval)
   - Verify the app installs and runs correctly from Play Store
   - Verify Wear OS companion installs correctly
   - Test in-app purchases (if implemented) using test accounts
5. Move to **closed testing** (invite-only, review takes hours)
   - Get feedback from a wider group before going public
6. Move to **open testing** or straight to **production** (review takes hours to days)
   - First review is typically stricter — expect possible rejection with feedback

### Wear OS distribution

The watch app can be distributed alongside the phone app:
- **Option A: Multi-APK** — upload the wear AAB as a separate artifact in the same
  listing. Users see one store entry; the watch app auto-installs when the phone
  app is installed. Requires `com.google.android.wearable.standalone` metadata in
  the wear manifest.
- **Option B: Separate listing** — create a separate Wear OS app listing. More
  control but users have to find and install it separately.

Option A is the standard approach and what users expect.

### Pre-launch report

Play Console runs automated tests on uploaded AABs (installs on multiple devices,
screenshots stability). Review the pre-launch report before each release to catch
crashes or UI issues on devices you don't own.

## Subsequent releases

1. Bump `versionCode` and `versionName` (via `bin/release.sh`)
2. Run `./gradlew bundleRelease`
3. Upload the new `.aab` to Play Console
4. Use **staged rollout** for production releases (e.g. 10% → 50% → 100%) to catch
   issues before they affect all users
5. Write release notes in Play Console (can reuse `docs/notes/release.md` content)

## Monetization strategy

### Recommendation: freemium with one-time unlock

A one-time in-app purchase is the best fit for SweetSpot:

- **No ongoing server costs** — the app uses free public APIs, so there's nothing to
  fund monthly. Users will (rightly) question a subscription.
- **User expectations** — utility apps with subscriptions get hammered in reviews.
  A one-time unlock feels fair and avoids subscription fatigue.
- **Simplicity** — one-time IAP lifecycle is much simpler than subscriptions (no
  grace periods, account holds, renewals, or cancellation flows).
- **GPL compatibility** — the source is freely available. A reasonable one-time price
  for convenience is easier to justify than a recurring charge.

### Free tier vs. premium

| Feature | Free | Premium (one-time €2.99–€4.99) |
|---|---|---|
| Find cheapest window | Yes | Yes |
| Quick-duration chips (1–6h) | Yes | Yes |
| Custom appliances | 3 max | Unlimited |
| Wear OS companion | Yes | Yes |
| All 30 countries / 43 zones | Yes | Yes |
| Data source order customisation | No | Yes |
| Home screen widget | No | Yes |
| Price notifications / alerts | No | Yes |
| Price history chart | No | Yes |

The free tier should be genuinely useful — not crippled. The premium features are
things that power users want and that require additional development effort (widget,
notifications, history). This way the app earns good reviews from free users and
revenue from engaged users.

### Why not subscriptions

- **Subscription fatigue** — the #1 complaint in reviews for utility apps that charge
  monthly. "Why does this need a subscription?"
- **High churn** — small utility apps see 70-80% subscription churn within 3 months.
  Revenue is unstable and the effort to re-acquire users is high.
- **No recurring cost** — there's no backend, no accounts, no ongoing API cost.
  A subscription feels unjustified when the marginal cost of serving a user is zero.
- **Open source tension** — anyone can build from source. A subscription on freely
  available code invites forks and negative sentiment.
- **Billing complexity** — subscription lifecycle (grace periods, account holds,
  pauses, cancellation, pro-rating, family sharing) requires significantly more code
  than a one-time purchase.

### Why not ads

- **Small audience** — niche EU utility app won't generate meaningful ad revenue.
  Typical eCPM for utility apps is €0.50–€2.00 per 1,000 impressions.
- **UX degradation** — ads on a screen the user checks for 10 seconds feels hostile.
- **Privacy contradiction** — the app's selling point is "no tracking." AdMob requires
  the Google Mobile Ads SDK which collects device identifiers and uses tracking.

### Why not paid-only

- **Discovery barrier** — paid apps get 10-50x fewer downloads. Users can't try before
  buying, which matters for a niche app they've never heard of.
- **No growth** — without free users, there's no word-of-mouth, no reviews, no organic
  growth.

### Implementation: Google Play Billing

#### Dependencies

```kotlin
// libs.versions.toml
billing = "7.1.1"

// shared/build.gradle.kts or app/build.gradle.kts
implementation(libs.billing.ktx)
```

`com.android.billingclient:billing-ktx` is the Kotlin-friendly wrapper around Google
Play Billing Library.

#### Architecture

```
BillingRepository (new, in :shared or :app)
├── Connects to Google Play BillingClient
├── Queries available products (one-time IAP: "premium_unlock")
├── Launches purchase flow
├── Verifies purchases
├── Exposes isPremium: StateFlow<Boolean>
└── Persists purchase state locally (SharedPreferences)
    with server-side verification fallback (optional)
```

#### Product setup in Play Console

- **Product ID:** `premium_unlock`
- **Product type:** One-time (non-consumable)
- **Price:** €2.99–€4.99 (experiment with pricing per country)
- **Grace:** Google handles refund window (48 hours by default in EU)

#### Key implementation steps

1. **`BillingRepository`** — wraps `BillingClient`. Connects on app start, queries
   purchases, exposes `isPremium` state. Handles `onPurchasesUpdated` callback.

2. **Purchase verification** — for a small app without a backend, local verification
   (checking the purchase token signature) is sufficient. If revenue grows, add
   server-side verification via Google Play Developer API.

3. **Feature gating** — `ViewModel` reads `isPremium` from `BillingRepository` and
   conditionally enables premium features. UI shows an upgrade prompt (bottom sheet
   or dialog) when the user hits a premium-only feature.

4. **Restore purchases** — on first launch or reinstall, query `BillingClient` for
   existing purchases. Users shouldn't have to re-purchase.

5. **Testing** — Play Console supports license testing. Add test accounts that can
   "purchase" without being charged. Test: purchase flow, restore, refund handling.

#### Considerations

- **Offline access** — purchases should work offline once verified. Cache the premium
  state in SharedPreferences; re-verify on next connection.
- **Family sharing** — Google Play supports family library for paid apps but not for
  in-app purchases. If this matters, consider making it a paid app variant instead.
- **Regional pricing** — Play Console lets you set per-country prices. Set lower
  prices for lower-income EU countries (e.g. Baltic states, Balkans).
- **Free trial period** — not applicable for one-time purchases, but you could offer
  a time-limited premium preview (e.g. "Premium features free for 7 days") using
  local state. This is simpler than a subscription trial and doesn't require billing
  integration for the trial itself.
- **Wear OS** — premium state should sync to watch via the existing Data Layer path
  (add `isPremium` to the `/settings` sync). Gate watch-only premium features (if
  any) on the synced state.

## Marketing

### Pre-launch

- Set up the website (done: sweetspot.today)
- Write a blog post or landing page explaining dynamic electricity pricing and how
  SweetSpot helps
- Prepare social media posts for launch day
- Reach out to energy bloggers / reviewers in target markets (NL, DE, Nordic)

### Launch

- Post on relevant subreddits: r/thenetherlands, r/germany, r/finland, r/norway,
  r/electricvehicles, r/homeautomation, r/selfhosted (open source angle)
- Post on Hacker News (show HN — open source, no tracking, niche utility)
- Dutch/German energy forums and communities
- Twitter/X, Mastodon — tag energy influencers

### Ongoing

- **ASO (App Store Optimization)** — keywords in the title and description. Target:
  "electricity prices", "cheapest energy", "dynamic pricing", "day-ahead prices",
  "ENTSO-E", country-specific terms.
- **Respond to reviews** — especially negative ones. Fast, helpful responses improve
  ratings and show the app is actively maintained.
- **Regular updates** — even small improvements signal the app is alive. Play Store
  algorithm favours recently updated apps.
- **Cross-promotion** — link from the web app to the Play Store listing. Add the Play
  Store badge to the website (already done).
- **Open source community** — encourage contributions on GitHub. Contributors become
  advocates.

## Checklist

### Before first submission

- [ ] Google Play Developer account created and verified
- [ ] Monochrome icon layer added (phone + watch)
- [ ] AAB builds successfully (`./gradlew bundleRelease`)
- [ ] Privacy policy live at sweetspot.today/privacy
- [ ] Store listing text written (all priority languages)
- [ ] Screenshots captured (phone, optionally tablet and watch)
- [ ] Feature graphic designed (1024×500)
- [ ] High-res icon exported (512×512)
- [ ] Content rating questionnaire completed
- [ ] Data safety form completed
- [ ] Billing integration implemented and tested (if launching with freemium)
- [ ] Wear OS manifest metadata added for paired distribution
- [ ] Internal testing verified

### Before each release

- [ ] Version bumped
- [ ] AAB built and signed
- [ ] Release notes written
- [ ] Pre-launch report reviewed (after upload)
- [ ] Staged rollout configured
