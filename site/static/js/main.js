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
