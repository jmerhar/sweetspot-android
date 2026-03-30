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

The app supports 25 languages. Translate the store listing (app name, short
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

### Recommendation: free trial + one-time unlock

Every user gets the full app — all features, no restrictions. After a trial period
(7 or 14 days), a paywall appears requiring a one-time purchase to continue using
the app.

**Why this model works for SweetSpot:**

- **Single tier** — no awkward decisions about which features are free and which are
  premium. The app does one thing well; splitting it into tiers feels artificial.
- **The app sells itself** — users experience full value during the trial. If it's
  useful, the purchase is an easy decision. No "imagine how good it could be if you
  paid" — they already know.
- **No subscription fatigue** — one payment, done forever. Users don't resent it.
- **Simple code** — one `isUnlocked` boolean instead of per-feature gating logic.
- **Fair to users** — they can evaluate before committing. Better than paid-only
  (where you buy blind) and less annoying than freemium (where you're constantly
  reminded of what you can't do).

### Trial length

**14 days** is probably the right length:

- Long enough to experience the full value across different price days (electricity
  prices vary by day of week and weather).
- Long enough that users don't feel rushed.
- Short enough that it creates urgency — "I've been using this daily, I should just
  buy it."
- 7 days works too but might not be enough for users who only run appliances a few
  times a week.

### Price point

**€2.99** is the sweet spot (pun intended):

- Low enough for an impulse buy — less than a coffee.
- High enough to generate meaningful revenue at scale.
- Use Play Console's regional pricing to set lower prices for lower-income EU
  countries (e.g. Baltic states, Balkans).
- Can always increase later — harder to decrease without upsetting early buyers.

### User experience

#### During trial

- App works normally with no restrictions.
- Subtle indicator: "Trial: 12 days remaining" in settings or as a small banner.
  Not intrusive — the user should enjoy the app, not feel pressured.
- On the last 3 days, the indicator becomes slightly more prominent (e.g. a chip
  on the main screen: "3 days left — unlock SweetSpot").

#### When trial expires

- App opens to a paywall screen instead of the main screen.
- Clean, friendly design — not aggressive. Show what they've been using:
  "You've found the cheapest window 23 times. Keep saving with SweetSpot."
- Single "Unlock for €2.99" button that launches the Google Play purchase flow.
- "Restore purchase" link for reinstalls or device switches.
- No way to dismiss — the app is locked until purchase (or uninstall).

#### After purchase

- Paywall disappears permanently. App works exactly as before.
- "Thank you" toast or brief confirmation, then straight to the main screen.

#### Watch behaviour

- Trial state syncs to watch via existing Data Layer `/settings` path.
- When trial expires, watch shows a message: "Trial expired — open SweetSpot on
  your phone to unlock."

### Other monetization options

#### Subscriptions

Monthly or yearly recurring payment (e.g. €0.99/month or €4.99/year) that unlocks
the app. Google Play supports subscription trials natively, with automatic billing
after the trial ends.

- **Pros:** Predictable recurring revenue. Google handles trial-to-paid conversion.
  Play Console has built-in subscription analytics (MRR, churn, LTV).
- **Cons:** Subscription fatigue — the #1 complaint in reviews for utility apps that
  charge monthly. High churn (70-80% within 3 months for small utility apps). No
  recurring server cost to justify it. Billing complexity (grace periods, account
  holds, pauses, cancellation, pro-rating). Open source tension — anyone can build
  from source, making a subscription feel exploitative.

#### Ads (free with ads)

Show a banner ad on the main screen or an interstitial after each search result.
Requires integrating the Google Mobile Ads SDK (AdMob):

- Add `com.google.android.gms:play-services-ads` dependency
- Initialize `MobileAds` in `MainActivity`
- Place a `BannerAd` composable at the bottom of the screen
- Typical revenue: €0.50–€2.00 per 1,000 impressions (eCPM) for utility apps

- **Pros:** No barrier to entry — app is completely free. Revenue scales with usage.
- **Cons:** Small niche audience means minimal revenue. UX degradation on a screen
  users check for 10 seconds. Privacy contradiction — the app's selling point is "no
  tracking", but AdMob collects device identifiers. Adds a heavy SDK dependency.

#### Paid app (no trial)

Set a one-time price (€1.99–€2.99) in Play Console. No code changes needed. Google
takes a 15% cut on the first $1M/year (30% after that).

- **Pros:** Zero complexity — no billing code, no IAPs, no feature gating. Clean UX.
- **Cons:** Paid apps get 10-50x fewer downloads. Users can't try before buying, which
  matters for a niche app they've never heard of. No word-of-mouth or organic growth
  from free users. No reviews from casual users.

#### Freemium (feature gating)

Core functionality free, premium features behind a one-time in-app purchase
(€2.99–€4.99). Possible premium features: unlimited appliances (free tier: 2-3),
widget, notifications, price history, data source customisation.

- **Pros:** Free tier drives downloads and reviews. Premium features can target power
  users willing to pay. No time pressure — users upgrade when ready.
- **Cons:** Artificial split for a focused utility — either the free version is too
  crippled (bad reviews) or too generous (nobody pays). Per-feature gating throughout
  the app. Upgrade prompts in multiple places. Two user experiences to design and test.

#### Donations / tip jar

"Buy me a coffee" style in-app purchase or link to an external service (Ko-fi, GitHub
Sponsors). Can be a non-consumable IAP or just an external link in settings.

- **Pros:** App stays fully free and clean. No complexity. Goodwill from users.
- **Cons:** Very low conversion rate (typically <1% of active users). Not a viable
  primary revenue source. Better as a supplement to another model.

### Why trial + one-time unlock is the best fit

Compared to the alternatives above:

- **vs. subscriptions** — avoids subscription fatigue and churn. Same simplicity of
  a single unlock, but users pay once instead of forever.
- **vs. ads** — preserves the privacy-first brand and clean UX. No tracking SDK.
- **vs. paid-only** — the trial removes the discovery barrier. Users try first, then
  decide. Reviews come from actual users, not blind buyers.
- **vs. freemium** — no artificial feature split. Single tier, single experience,
  single code path. Much simpler to build and maintain.
- **vs. donations** — realistic revenue. Trial creates a natural purchase moment
  instead of relying on goodwill.

### Implementation

#### Trial tracking

Store trial state in SharedPreferences (`sweetspot_settings`):

```kotlin
// On first launch (if not already set)
if (!prefs.contains("trial_start")) {
    prefs.edit().putLong("trial_start", System.currentTimeMillis()).apply()
}

// Check trial status
fun isTrialExpired(): Boolean {
    val start = prefs.getLong("trial_start", System.currentTimeMillis())
    val elapsed = System.currentTimeMillis() - start
    val trialDays = 14
    return elapsed > trialDays * 24 * 60 * 60 * 1000L
}

fun trialDaysRemaining(): Int {
    val start = prefs.getLong("trial_start", System.currentTimeMillis())
    val elapsed = System.currentTimeMillis() - start
    val trialDays = 14
    val remaining = trialDays - (elapsed / (24 * 60 * 60 * 1000L)).toInt()
    return remaining.coerceAtLeast(0)
}
```

This is intentionally simple. Yes, users can reset it by clearing app data. That's
fine — anyone determined enough to do that every 14 days wasn't going to pay anyway.
Don't add server-side verification or device fingerprinting to prevent this; it adds
complexity and hostility for negligible revenue protection.

#### Google Play Billing

**Dependencies:**

```kotlin
// libs.versions.toml
billing = "7.1.1"

// app/build.gradle.kts
implementation(libs.billing.ktx)
```

`com.android.billingclient:billing-ktx` is the Kotlin-friendly wrapper around Google
Play Billing Library.

**Architecture:**

```
BillingRepository (new, in :app)
├── Connects to Google Play BillingClient on app start
├── Queries existing purchases → sets isUnlocked = true if found
├── Exposes isUnlocked: StateFlow<Boolean>
├── launchPurchaseFlow(activity) → triggers Google Play purchase UI
├── onPurchasesUpdated callback → verifies + acknowledges + updates state
└── Persists unlock state in SharedPreferences (cache for offline)
```

**Product setup in Play Console:**

- **Product ID:** `full_unlock`
- **Product type:** One-time (non-consumable)
- **Price:** €2.99 (with regional pricing adjustments)
- **Grace:** Google handles refund window (48 hours by default in EU)

**Key implementation steps:**

1. **`BillingRepository`** — wraps `BillingClient`. Connects on app start, queries
   purchases, exposes `isUnlocked: StateFlow<Boolean>`. Handles
   `onPurchasesUpdated` callback.

2. **Purchase verification** — for a small app without a backend, local verification
   (checking the purchase token signature) is sufficient. If revenue grows, add
   server-side verification via Google Play Developer API later.

3. **App lock logic** — `ViewModel` checks `isUnlocked` and `isTrialExpired()`. If
   trial expired and not unlocked, navigate to the paywall screen. Otherwise, show
   the normal app. Single check point in the ViewModel, not scattered throughout.

4. **Restore purchases** — on first launch or reinstall, `BillingClient` queries
   existing purchases. If found, set `isUnlocked = true`. Users never have to
   re-purchase.

5. **Testing** — Play Console supports license testing. Add test accounts that can
   "purchase" without being charged. Test: purchase flow, restore, trial expiry,
   paywall display, refund handling.

#### Considerations

- **Offline access** — cache unlock state in SharedPreferences. The app should never
  lock a paying user out because they're offline.
- **Clock manipulation** — users could set their clock back to extend the trial. Not
  worth defending against — same argument as clearing app data. Don't add complexity
  for edge cases.
- **Regional pricing** — Play Console lets you set per-country prices. Set lower
  prices for lower-income EU countries (e.g. €1.99 for Baltic states, €2.99 for
  Western Europe).
- **Wear OS** — sync `isUnlocked` and `trialDaysRemaining` to the watch via the
  existing Data Layer `/settings` path. Watch shows a "trial expired" message when
  locked, directing the user to unlock on the phone.
- **GPL consideration** — the app is open source. Anyone can build from source without
  the trial. The purchase is for the convenience of Play Store distribution, updates,
  and supporting development. This is a well-established model (e.g. Syncthing,
  K-9 Mail). Consider noting this in the FAQ.

## Testing the billing integration

### Prerequisites

- A Google Play Console developer account
- The app uploaded to Play Console (even as internal testing track — doesn't need to
  be published publicly)
- The `full_unlock` in-app product created in Play Console
- At least one license tester configured

### Play Console setup

1. **Create the app** in Play Console. Choose **Free** (not Paid) — the monetization
   happens through in-app purchase, which requires a free listing.
2. **Upload an AAB** to any track (internal testing is fine). Play Billing only works
   with apps that have been uploaded at least once. Build with `make bundle`.
3. **Create the in-app product:**
   - Go to Monetize > In-app products
   - Product ID: `full_unlock`
   - Type: one-time (not subscription)
   - Price: €2.99 (adjust regional pricing as needed)
   - Activate it
4. **Add license testers:**
   - Go to Settings > License testing
   - Add your Google account email(s)
   - License testers can make purchases without being charged

### Debug builds skip the paywall

The ViewModel checks `BuildConfig.DEBUG` and always skips the paywall in debug
builds. This means `make debug-phone` will never show the paywall, regardless of
trial state. To test the full billing flow, you need a **release build**.

### Testing the trial (no billing needed)

```bash
# Fresh install — verify trial starts, paywall not shown
make install-phone
```

To simulate an expired trial without waiting 14 days, temporarily patch
`SettingsRepository.kt`:

```kotlin
const val TRIAL_DAYS = 0  // was 14 — forces immediate expiry
```

Then build and install the release APK. The paywall should appear immediately.

Alternatively, use `adb` to manipulate the SharedPreferences timestamp:

```bash
# Check current prefs (release app has no .debug suffix)
adb shell "run-as today.sweetspot cat /data/data/today.sweetspot/shared_prefs/sweetspot_settings.xml"
```

### Testing the purchase flow

1. Build and install the release APK: `make install-phone`
2. If needed, set `TRIAL_DAYS = 0` to trigger the paywall immediately
3. The paywall should show "Unlock for €2.99" (or the localized price)
4. Tap "Unlock" — the Google Play purchase dialog appears
5. As a license tester, the purchase completes without charging
6. The paywall should disappear and the app should work normally

### Testing restore

1. After purchasing, uninstall: `adb uninstall today.sweetspot`
2. Reinstall: `make install-phone`
3. The paywall appears (trial expired, no local cache of purchase)
4. Tap "Restore purchase"
5. BillingClient queries Play and finds the existing purchase
6. The paywall should disappear

### Testing offline behaviour

1. Purchase the unlock while online
2. Turn off Wi-Fi and mobile data
3. Force-stop and reopen the app
4. The app should remain unlocked (cached in SharedPreferences)

### Testing the watch

```bash
# Install on both devices
make install-phone
make install-watch
```

The watch reads `is_trial_expired` and `is_unlocked` from the Data Layer:

- **Phone trial active:** Watch shows the normal appliance list
- **Phone trial expired + not unlocked:** Watch shows "Trial expired — Open
  SweetSpot on your phone to unlock."
- **Phone unlocked:** Watch shows the normal appliance list

To test, set `TRIAL_DAYS = 0` on the phone, install the release build on both
devices, and verify the watch shows the locked screen. Then purchase on the phone
and verify the watch unlocks.

### Test checklist

| Test | How | Expected |
|---|---|---|
| Fresh install, trial active | Install, open app | App works normally, no paywall |
| Trial days remaining | Check via Settings or UiState | Correct countdown (0–14) |
| Trial expired, not unlocked | Set `TRIAL_DAYS=0` + release build | Paywall blocks app |
| Purchase flow | Tap "Unlock" (license tester) | Play dialog → app unlocks |
| Restore purchase | Uninstall → reinstall → "Restore" | Unlock restored from Play |
| Offline after purchase | Purchase → airplane mode → reopen | App stays unlocked |
| Refund | Refund in Play Console → reopen app | Paywall reappears |
| Watch locked | Phone trial expired + watch connected | Watch shows locked screen |
| Watch unlocked | Phone unlocked + watch connected | Watch shows appliance list |
| Debug build | `make debug-phone` | Paywall always skipped |

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

- ⬜ Google Play Developer account created and verified
- ✅ Monochrome icon layer added (phone + watch)
- ✅ AAB builds successfully (`./gradlew bundleRelease` / `make bundle`)
- ✅ Release script builds AABs alongside APKs
- ✅ Privacy policy live at sweetspot.today/privacy
- ✅ Website live at sweetspot.today (with Play Store badges)
- ⬜ Store listing text written (short + full description, all priority languages)
- ⬜ Screenshots captured (phone, optionally tablet and watch)
- ⬜ Feature graphic designed (1024×500)
- ⬜ High-res icon exported (512×512)
- ⬜ Content rating questionnaire completed in Play Console
- ⬜ Data safety form completed in Play Console
- ✅ Trial logic implemented (first launch date tracking, trial expiry check)
- ✅ Paywall screen designed and implemented
- ✅ Google Play Billing integration (BillingRepository, purchase flow, restore)
- ⬜ Billing tested with Play Console license test accounts
- ✅ Trial + unlock state synced to watch via Data Layer
- ⬜ Wear OS manifest metadata added for paired distribution
- ⬜ Internal testing track verified on real devices
- ⬜ Store listing localised for priority languages (EN, NL, DE, FR, SL)

### Before each release

- ⬜ Version bumped
- ⬜ AAB built and signed
- ⬜ Release notes written (Play Console + website changelog)
- ⬜ Pre-launch report reviewed (after upload)
- ⬜ Staged rollout configured (10% → 50% → 100%)
