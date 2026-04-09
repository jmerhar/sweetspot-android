// Auto-redirect to the user's preferred language.
// Saves the chosen language in localStorage so the preference persists across visits.
// When the user picks a language via the switcher, that choice is saved and honoured.
(function () {
  var SUPPORTED = ['bg','cs','da','de','el','es','et','fi','fr','hr','hu','it','lt','lv','mk','nb','nl','pl','pt','ro','sk','sl','sr','sv'];
  // Map browser language codes that don't match our Hugo language keys.
  var ALIASES = { 'no': 'nb', 'nn': 'nb' };
  var KEY = 'lang';

  // Extract the language prefix from a path (e.g. '/de/privacy/' → 'de', '/' → 'en').
  function langFromPath(path) {
    var match = path.match(/^\/([a-z]{2})\//);
    return match ? match[1] : 'en';
  }

  // Replace the language prefix in a path.
  function replaceLang(path, lang) {
    var currentLang = langFromPath(path);
    if (lang === 'en') {
      return currentLang === 'en' ? path : path.replace(/^\/[a-z]{2}\//, '/');
    }
    return currentLang === 'en' ? '/' + lang + path : path.replace(/^\/[a-z]{2}\//, '/' + lang + '/');
  }

  // Bail out if localStorage is unavailable — we can't persist the choice,
  // so redirecting would fire on every page load with no way to override.
  try { localStorage.setItem('_t', '1'); localStorage.removeItem('_t'); } catch (e) { return; }

  var currentLang = langFromPath(window.location.pathname);
  var saved = localStorage.getItem(KEY);
  var suffix = window.location.search + window.location.hash;

  if (saved) {
    // Redirect to the saved language if we're not already on it.
    if (saved !== currentLang) {
      window.location.replace(replaceLang(window.location.pathname, saved) + suffix);
      return;
    }
  } else {
    // First visit — detect from browser preferences.
    var langs = navigator.languages || [navigator.language];
    var target = 'en';
    for (var i = 0; i < langs.length; i++) {
      var code = langs[i].toLowerCase().split('-')[0];
      code = ALIASES[code] || code;
      if (SUPPORTED.indexOf(code) !== -1) {
        target = code;
        break;
      }
      if (code === 'en') break;
    }
    localStorage.setItem(KEY, target);
    if (target !== currentLang) {
      window.location.replace(replaceLang(window.location.pathname, target) + suffix);
      return;
    }
  }
})();

// Mobile nav toggle
document.addEventListener('DOMContentLoaded', function () {
  var toggle = document.querySelector('.nav-toggle');
  var links = document.querySelector('.nav-links');

  if (toggle && links) {
    toggle.addEventListener('click', function () {
      var expanded = toggle.getAttribute('aria-expanded') === 'true';
      toggle.setAttribute('aria-expanded', String(!expanded));
      links.classList.toggle('open');
    });
  }

  // Language switcher dropdown
  var langBtn = document.querySelector('.lang-btn');
  var langDropdown = document.querySelector('.lang-dropdown');

  if (langBtn && langDropdown) {
    langBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      var expanded = langBtn.getAttribute('aria-expanded') === 'true';
      langBtn.setAttribute('aria-expanded', String(!expanded));
      langDropdown.classList.toggle('open');
    });

    document.addEventListener('click', function () {
      langBtn.setAttribute('aria-expanded', 'false');
      langDropdown.classList.remove('open');
    });

    // Save the user's explicit language choice when they use the switcher.
    langDropdown.querySelectorAll('a').forEach(function (link) {
      link.addEventListener('click', function () {
        var href = link.getAttribute('href') || '/';
        var match = href.match(/^\/([a-z]{2})\//);
        localStorage.setItem('lang', match ? match[1] : 'en');
      });
    });
  }

  // Smooth scroll for anchor links (closes mobile nav)
  document.querySelectorAll('a[href*="#"]').forEach(function (link) {
    link.addEventListener('click', function () {
      if (links) {
        links.classList.remove('open');
        if (toggle) toggle.setAttribute('aria-expanded', 'false');
      }
    });
  });
});
