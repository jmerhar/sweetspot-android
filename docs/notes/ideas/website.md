# Website (sweetspot.today)

Domain registered on Namecheap. Hosted on GitHub Pages from `site/` folder in the main repo (via GitHub Actions workflow, since `docs/` is already used for project documentation).

## Pages

### Landing page
- App description, key features, screenshots (phone + watch)
- Download links (GitHub Releases, Play Store when published)
- Demo GIF or short video showing the app in action

### Privacy policy
- Required for Play Store listing
- No personal data collected, no accounts, no analytics
- Prices fetched from public APIs, cached locally on device only

### Changelog / what's new
- Auto-generated or manually maintained from release notes (`docs/notes/release.md`)
- One entry per version with date and highlights

### FAQ
- Supported countries and bidding zones
- Where do the prices come from? (ENTSO-E, fallback sources)
- Are prices accurate? (day-ahead only, no taxes/fees)
- Does it work offline? (cached prices)
- Wear OS requirements and setup

### Multi-language support
- At minimum: English, Dutch, German, French, Slovenian
- Could reuse existing string resources for common phrases
- Consider a static site generator with i18n support (e.g. Hugo, Jekyll)

### Live price widget
- Fetch current prices from a public API (Spot-Hinta.fi or Energy-Charts, no auth required)
- Show current cheapest window for a sample duration
- Eye-catching interactive element that demonstrates the app's value

### Blog
- Electricity market news and changes
- New country/zone support announcements
- Tips for saving on dynamic tariffs
- Technical posts about the app's architecture

## Tech decisions
- [ ] Static site generator vs plain HTML/CSS
- [ ] GitHub Pages from `site/` folder via Actions workflow
- [ ] Custom domain DNS setup (CNAME record pointing to GitHub Pages)
