# Website (sweetspot.today)

Domain registered on Namecheap. Hosted on GitHub Pages from `site/` folder in the main repo (via GitHub Actions workflow). Built with Hugo, 5 languages (en, nl, de, fr, sl).

## Done

- ✅ Landing page with app description, key features, and Google Play badges
- ✅ Privacy policy (required for Play Store)
- ✅ Changelog with per-version entries
- ✅ FAQ (countries, data sources, accuracy, offline, Wear OS, cost, languages)
- ✅ Multi-language support (English, Dutch, German, French, Slovenian) via Hugo i18n
- ✅ Hugo static site generator with i18n
- ✅ GitHub Pages deployment via Actions workflow
- ✅ Custom domain DNS setup (CNAME)
- ✅ Site validation script (`make site-validate`)

## Future Ideas

### Screenshots and media
- App screenshots (phone + watch) on the landing page
- Demo GIF or short video showing the app in action

### Live price widget
- Fetch current prices from a public API (Spot-Hinta.fi or Energy-Charts, no auth required)
- Show current cheapest window for a sample duration
- Eye-catching interactive element that demonstrates the app's value

### Blog
- Electricity market news and changes
- New country/zone support announcements
- Tips for saving on dynamic tariffs
- Technical posts about the app's architecture

### SEO optimisation
- Structured data (JSON-LD) for the app and FAQ pages
- Sitemap.xml generation via Hugo
- robots.txt tuning
- Open Graph and Twitter Card meta tags (partially done in `head.html`)
- Google Search Console setup and monitoring
- Performance audit (Core Web Vitals)

### Analytics and infrastructure
- Add privacy-friendly analytics (e.g. Plausible, Umami, or Google Analytics)
- Consider enabling Cloudflare protection
