# Publishing to Google Play Store

## One-time setup

1. **Google Play Developer account** — $25 one-time fee at [play.google.com/console](https://play.google.com/console). Requires a Google account and identity verification (can take a few days).

2. **App signing** — Google Play manages its own signing key. You upload an AAB (Android App Bundle) instead of an APK. Google re-signs it for distribution. Your current upload key (`release.jks`) becomes the "upload key" that proves it's you.

## Code changes

Build an AAB instead of APK:

```bash
./gradlew bundleRelease
```

This produces `app/build/outputs/bundle/release/app-release.aab`.

## Store listing requirements

- **Privacy policy** — a URL to a privacy policy page (required even if you collect nothing)
- **App description** — short and full description
- **Screenshots** — at least 2 phone screenshots, optionally tablet
- **Feature graphic** — 1024x500 banner image
- **App icon** — 512x512 high-res icon
- **Content rating** — complete the IARC questionnaire in Play Console
- **Data safety form** — declare what data the app collects (network requests to EnergyZero API, no personal data)

## Release process

1. Create app in Play Console
2. Fill out store listing, content rating, and data safety
3. Upload the `.aab` file
4. Start with **internal testing** (up to 100 testers, instant approval) to verify everything works
5. Move to **production** (review takes hours to days)

## Monetization options

A few strategies that fit a small utility app like this:

### 1. Paid app (simplest)

Set a one-time price ($0.99–$2.99) in Play Console. No code changes needed. Google takes a 15% cut on the first $1M/year in revenue (30% after that). Pros: zero complexity, no ads, no server costs. Cons: paid apps get far fewer downloads, and users expect a lot for even $1.

### 2. Free with ads (most common)

Show a banner ad on the main screen or an interstitial after each search result. Requires integrating the Google Mobile Ads SDK (AdMob):

- Add `com.google.android.gms:play-services-ads` dependency
- Initialize `MobileAds` in `MainActivity`
- Place a `BannerAd` composable (or `AdView` in a `AndroidView` wrapper) at the bottom of the screen
- Typical revenue: $0.50–$2.00 per 1,000 impressions (eCPM) for utility apps

This is the easiest recurring revenue but degrades the user experience.

### 3. Freemium (free + in-app purchase)

Offer core functionality for free and charge for premium features via Google Play Billing:

- Add `com.android.billingclient:billing-ktx` dependency
- Gate premium features behind a one-time purchase ($1.99–$4.99)
- Possible premium features:
  - Unlimited appliances (free tier: 2–3)
  - Home screen widget showing next cheap window
  - Push notifications when a cheap window is about to start
  - Multiple energy providers / regions
  - Price history charts

### 4. Donations / tip jar

Add a "Buy me a coffee" style in-app purchase or link to an external service (Ko-fi, GitHub Sponsors). Low revenue but keeps the app fully free and clean. Can be implemented as a non-consumable IAP or just an external link in settings.

### 5. Open source + sponsorship

Keep the app free and open source, monetize indirectly through GitHub Sponsors or attracting freelance/consulting work. Works best if the app builds your reputation rather than generating direct income.

### Recommendation

For a niche utility app targeting Dutch/EU electricity users, **freemium** or **paid app** are the most realistic. The addressable audience is small, so ad revenue will be minimal. A one-time purchase of $1.99 with a generous free tier is probably the best balance of revenue and user experience.

## Subsequent releases

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`
2. Run `./gradlew bundleRelease`
3. Upload the new `.aab` to Play Console
4. Roll out to internal testing or production
