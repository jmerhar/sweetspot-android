# Pricing Analysis

Market research and pricing strategy for the Play Store launch (March 2026).

## Core value proposition

The main differentiator isn't multi-country coverage or API fallback — it's that **the app
finds the cheapest time window automatically**. Users don't have to stare at price graphs
and do mental math to figure out when to run their washing machine.

This matters more than it sounds, because European electricity markets are transitioning
from 60-minute to 15-minute settlement periods. At hourly resolution, eyeballing a graph
is manageable — 24 bars, pick the low cluster, estimate the total. At 15-minute resolution,
that's 96 bars per day. Finding the cheapest contiguous 2h15m window across 96 slots is
genuinely hard to do visually. This was the original trigger for building the app: the
increased cognitive load of 15-minute intervals made manual optimisation impractical.

Most competing apps just show a price graph or table and leave the user to figure it out.
SweetSpot does the actual computation — set your appliance duration, get a specific time.

## Competitive landscape

The European electricity price app market breaks into three categories:

| Category | Examples | Model |
|---|---|---|
| Energy company apps | Tibber, aWATTar, Frank Energie, Octopus, EnergyZero | Free (monetise via electricity supply) |
| Exchange/platform apps | Nord Pool, EPEX SPOT, Electricity Maps | Free (market transparency tools) |
| Independent developer apps | Stroomprijzen, Elspot, various ENTSO-E wrappers | Free with ads, €0.99–€3.99 to remove |

Most competitors are free because they're subsidised by an energy business or exchange.
Independent apps that charge are almost always in the €1.99–€2.99 range (ad removal or
"pro" unlock).

### What competitors offer vs. SweetSpot

| Feature | Competitors | SweetSpot |
|---|---|---|
| Show price graph/table | Yes (all) | Yes |
| Find cheapest window for a duration | Rare — Tibber has a basic version | Core feature, any duration, 15-min aware |
| Custom appliances with durations | No | Yes (with icons, Wear OS sync) |
| Multi-country coverage | Usually 1 country or 1 region | 30 countries, 43 zones |
| Multiple API sources with fallback | No | 5 sources, automatic fallback |
| Wear OS companion | Rare | Yes |
| No ads, no tracking | Some (energy company apps) | Yes |
| Open source | Rare | Yes (GPL v3) |

## SweetSpot's differentiators (ranked by user value)

1. **Automatic cheapest window calculation** — the killer feature. Set a duration, get a
   specific time. Works across 15-minute and 60-minute intervals. No mental math.
2. **Custom appliances** — save frequently-used durations with icons. One tap to search.
3. **Multi-country, multi-zone coverage** — 43 zones across 30 countries. Most apps cover
   one country.
4. **5 API sources with fallback** — if one API is down, the next one takes over silently.
5. **Wear OS companion** — check cheapest times from your wrist.
6. **No ads, no tracking** — privacy-first. No analytics SDK, no device identifiers.
7. **Open source** — users can verify the code, contribute, or build from source.

## Pricing model: trial + yearly subscription

See `play-store.md` for full rationale and implementation details. Summary:

- 14-day free trial (full functionality, no restrictions)
- Yearly subscription to continue using the app after trial expires
- No ads, no feature gating

## Price points by market

Four tiers based on purchasing power parity, dynamic pricing adoption, and competitive
density:

| Tier | Countries | Price | Rationale |
|---|---|---|---|
| Premium | NO, CH, DK, SE, FI, IE | €3.49 | Highest purchasing power. Dynamic pricing is mainstream (especially Nordics). Users accustomed to paying for utility apps. |
| Standard | NL, DE, AT, BE, FR, LU, IT, ES, PT, SI, GR | €2.49 | Core eurozone markets. NL and DE are the most competitive (many free alternatives), so price needs to feel trivial next to free Stroomprijzen/Tibber. |
| Value | CZ, HU, PL, SK, HR, EE, LV, LT, RO | €1.49 | Lower purchasing power. Dynamic pricing contracts are less common, so price must be low to convert the smaller audience. |
| Emerging | BG, RS, ME, MK | €0.99 | Lowest purchasing power. Dynamic pricing is niche. Goal is downloads and reviews, not revenue. |

### Why €2.49 standard instead of €2.99

- **Competitive pressure** — NL has Stroomprijzen (free), Tibber (free), Frank Energie
  (free), EnergyZero (free). Germany has aWATTar (free), Tibber (free). The price must
  feel trivial next to free alternatives.
- **Psychological pricing** — €2.49 feels like "about two euros", €2.99 feels like "about
  three euros". The perceived difference is larger than the €0.50 gap.
- **Open-source factor** — anyone can build from source. The purchase is for convenience.
  €2.49 stays in "not worth the hassle of building myself" territory.
- **Conversion rate** — slightly lower price × higher conversion likely outperforms higher
  price × lower conversion at this scale.
- Can always raise to €2.99 later if conversion is strong. Lowering is harder.

### Non-euro local prices (approximate)

| Country | Currency | Premium | Standard | Value |
|---|---|---|---|---|
| NO | NOK | 39 kr | — | — |
| SE | SEK | 39 kr | — | — |
| DK | DKK | 25 kr | — | — |
| CH | CHF | 3.50 | — | — |
| PL | PLN | — | — | 6.99 zł |
| CZ | CZK | — | — | 39 Kč |
| HU | HUF | — | — | 599 Ft |
| RO | RON | — | — | 6.99 lei |
| BG | BGN | — | — | 1.99 лв |
| RS | RSD | — | — | 119 din |

Play Console will suggest specific tier-aligned prices when the base price is set.

## Revenue expectations

Realistic estimates for a niche utility app:

- **Downloads:** 1,000–5,000/month after initial launch push
- **Trial-to-subscription conversion:** 2–5% (typical for utility apps with trials)
- **Average price after regional mix:** ~€2.20/year
- **Year 1 gross revenue:** ~€50–€550/month (same as one-time, since all subscribers are new)
- **Steady-state gross revenue:** higher than one-time if renewal rate is decent
- **Net after Google's 15% cut:** ~€40–€470/month in year 1

This is hobby-scale revenue. The subscription model provides recurring revenue at the cost
of higher churn risk, but at €2.49/year the renewal friction is minimal.

## Open questions

- Exact Play Console pricing tiers to use (set after creating the product listing)
- Whether to show a "days remaining" indicator during trial, or keep it invisible until
  the last 3 days
- Paywall screen copy and design
- Whether the watch should show trial status at all, or only show a locked message when
  the trial expires
