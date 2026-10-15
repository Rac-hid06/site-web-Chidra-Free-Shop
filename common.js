const CURRENT_USER_KEY = 'chidraCurrentUser';

function getApiBase() {
  const isLocal = window.location.protocol === 'file:'
    || window.location.hostname === 'localhost'
    || window.location.hostname === '127.0.0.1';
  return isLocal ? 'https://site-web-chidra-free-shop.onrender.com/api' : '/api';
}

function getCurrentUser() {
  try {
    return JSON.parse(sessionStorage.getItem(CURRENT_USER_KEY) || 'null');
  } catch (e) {
    return null;
  }
}

function setCurrentUser(user) {
  sessionStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user));
  updateNavVisibility();
}

function logoutCurrentUser() {
  sessionStorage.removeItem(CURRENT_USER_KEY);
  updateNavVisibility();
}

function updateNavVisibility() {
  const user = getCurrentUser();
  const adminNav = document.getElementById('adminNavItem');
  const accountNav = document.getElementById('accountNavLink');
  if (adminNav) {
    adminNav.style.display = user?.role === 'admin' ? 'inline-block' : 'none';
  }
  if (accountNav) {
    accountNav.textContent = user ? `Compte (${user.name || user.email})` : 'Compte';
    accountNav.href = 'compte.html';
  }
}

function updateCurrentYear() {
  document.querySelectorAll('#currentYear').forEach(element => {
    element.textContent = new Date().getFullYear();
  });
}

function applySiteSettings() {
  let settings = {};
  try {
    settings = JSON.parse(localStorage.getItem('siteSettings') || '{}');
  } catch (e) {
    return;
  }
  if (settings.name) document.querySelectorAll('.logo').forEach(element => { element.textContent = settings.name; });
  if (settings.slogan) document.querySelectorAll('.page-slogan').forEach(element => { element.textContent = settings.slogan; });
  if (settings.phone) document.querySelectorAll('a[href^="tel:"]').forEach(element => { element.textContent = settings.phone; element.href = `tel:${settings.phone.replace(/\s+/g, '')}`; });
  if (settings.email) document.querySelectorAll('a[href^="mailto:"]').forEach(element => { element.textContent = settings.email; element.href = `mailto:${settings.email}`; });
  if (settings.description) document.querySelectorAll('.footer-brand p').forEach(element => { element.textContent = settings.description; });
}

function setupSharedPageStyle() {
  if (!document.querySelector('link[href*="font-awesome"]')) {
    const iconStylesheet = document.createElement('link');
    iconStylesheet.rel = 'stylesheet';
    iconStylesheet.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css';
    document.head.appendChild(iconStylesheet);
  }
  const style = document.createElement('style');
  style.textContent = `
    .page-header { position: relative; overflow: hidden; }
    .page-header::before { content: ''; position: absolute; inset: 0; background: linear-gradient(110deg, rgba(10,10,10,.96), rgba(26,107,255,.78)); opacity: .96; pointer-events: none; }
    .page-header > * { position: relative; z-index: 1; }
    .page-header p { color: #d7def0 !important; font-weight: 500; }
    .page-visual { max-width: 1100px; height: 240px; margin: 0 auto; background: center/cover no-repeat; border-radius: 20px; box-shadow: 0 18px 45px rgba(10,10,10,.16); }
    .page-visual + .cards, .page-visual + .content, .page-visual + .services { padding-top: 2.5rem; }
    .shared-footer { background: #0A0A0A; color: #aaa; margin-top: 4rem; padding: 3rem 2rem 1.25rem; }
    .shared-footer-inner { max-width: 1100px; margin: 0 auto; display: grid; grid-template-columns: 1.4fr 1fr 1fr 1.3fr; gap: 2rem; }
    .shared-footer h2 { color: #FAFAF8; font: 1.8rem 'Bebas Neue', sans-serif; letter-spacing: 1px; }
    .shared-footer h2 span { color: #C8FF00; }
    .shared-footer h3 { color: #FAFAF8; font-size: .8rem; text-transform: uppercase; letter-spacing: 1px; margin-bottom: .8rem; }
    .shared-footer p, .shared-footer a { color: #777; font-size: .82rem; line-height: 1.7; }
    .shared-footer a { display: block; text-decoration: none; margin-bottom: .25rem; }
    .shared-footer a:hover { color: #C8FF00; }
    .shared-footer-bottom { max-width: 1100px; margin: 2rem auto 0; padding-top: 1rem; border-top: 1px solid #222; color: #555; font-size: .75rem; }
    .shared-social { display: flex !important; gap: .55rem; margin-top: 1rem; }
    .shared-social a { background: #2A2A2A; border-radius: 8px; color: #aaa; display: grid; place-items: center; height: 32px; width: 32px; }
    .page-header,
    .content,
    .cards,
    .services,
    .container {
      animation: shared-page-enter .65s cubic-bezier(.2,.8,.2,1) both;
    }
    @keyframes shared-page-enter {
      from { opacity: 0; transform: translateY(22px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .nav-links a[aria-current="page"] { color: #C8FF00; }
    .brand-lockup { display: flex; flex-direction: column; justify-content: center; }
    .is-hidden { display: none !important; }
    .page-slogan {
      display: block;
      margin-top: .35rem;
      color: #888;
      font-size: .7rem;
      letter-spacing: .08em;
      text-transform: uppercase;
    }
    @media (max-width: 768px) {
      nav { padding: 0 1.25rem !important; }
      .page-header { padding: 3rem 1.25rem !important; }
      .page-header h1 { font-size: clamp(2.8rem, 13vw, 4rem) !important; }
      .cards, .services, .content, .container { padding-left: 1.25rem !important; padding-right: 1.25rem !important; }
      .grid { grid-template-columns: 1fr !important; }
      .page-visual { height: 180px; margin: 0 1.25rem; border-radius: 14px; }
      .shared-footer { padding-left: 1.25rem; padding-right: 1.25rem; }
      .shared-footer-inner { grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    }
    @media (max-width: 480px) { .shared-footer-inner { grid-template-columns: 1fr; } }
    @media (prefers-reduced-motion: reduce) {
      .page-header, .content, .cards, .services, .container { animation: none; }
    }
  `;
  document.head.appendChild(style);

  const currentPage = window.location.pathname.split('/').pop() || 'Chidra Free Shop.html';
  document.querySelectorAll('.nav-links a').forEach(link => {
    const linkPage = new URL(link.href, window.location.href).pathname.split('/').pop();
    if (linkPage === currentPage) link.setAttribute('aria-current', 'page');
  });

  const logo = document.querySelector('nav .logo');
  if (logo && !logo.parentElement.classList.contains('brand-lockup')) {
    const brand = document.createElement('div');
    brand.className = 'brand-lockup';
    logo.replaceWith(brand);
    brand.appendChild(logo);
    const slogan = document.createElement('span');
    slogan.className = 'page-slogan';
    slogan.textContent = "L'habilleur des grands hommes";
    brand.appendChild(slogan);
  }

  const pageData = {
    'collections.html': { image: 'https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=1400&q=85', alt: 'Collection de vêtements masculins' },
    'nouveautes.html': { image: 'https://images.unsplash.com/photo-1523398002811-999ca8dec234?auto=format&fit=crop&w=1400&q=85', alt: 'Nouveautés mode masculine' },
    'apropos.html': { image: 'https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?auto=format&fit=crop&w=1400&q=85', alt: 'Textures et vêtements de mode' },
    'services.html': { image: 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=1600&q=85', alt: 'Présentation de vêtements dans une boutique de mode' },
    'compte.html': { image: 'https://images.unsplash.com/photo-1617127365659-c47fa864d8bc?auto=format&fit=crop&w=1400&q=85', alt: 'Style masculin élégant' },
    'admin.html': { image: 'WhatsApp Image 2026-06-25 at 18.32.25.jpeg', alt: 'Collection de polos Chidra Free Shop' }
  };
  const header = document.querySelector('.page-header');
  const data = pageData[currentPage];
  if (header && data && !document.querySelector('.page-visual')) {
    const visual = document.createElement('div');
    visual.className = 'page-visual';
    visual.setAttribute('role', 'img');
    visual.setAttribute('aria-label', data.alt);
    visual.style.backgroundImage = `url("${data.image}")`;
    header.after(visual);
  }

  if (!document.querySelector('footer')) {
    const footer = document.createElement('footer');
    footer.className = 'shared-footer';
    footer.innerHTML = `
      <div class="shared-footer-inner">
        <div><h2>CHIDRA <span>FREE SHOP</span></h2><p>L'habilleur des grands hommes.</p><div class="shared-social"><a href="https://www.tiktok.com/@chidrafreeshop1" target="_blank" rel="noopener noreferrer" aria-label="TikTok"><i class="fa-brands fa-tiktok"></i></a><a href="https://www.instagram.com/chidra.free.shop/" target="_blank" rel="noopener noreferrer" aria-label="Instagram"><i class="fa-brands fa-instagram"></i></a><a href="https://www.facebook.com/chidra.fsf" target="_blank" rel="noopener noreferrer" aria-label="Facebook"><i class="fa-brands fa-facebook-f"></i></a></div></div>
        <div><h3>Boutique</h3><a href="Chidra Free Shop.html">Accueil</a><a href="collections.html">Collections</a><a href="nouveautes.html">Nouveautés</a></div>
        <div><h3>Assistance</h3><a href="compte.html">Mon compte</a><a href="services.html">Nos services</a><a href="mailto:nacanaboprinceabdoulrachid@gmail.com">Nous contacter</a></div>
        <div><h3>Contact</h3><a href="tel:+22606462164">+226 06 46 21 64</a><a href="mailto:nacanaboprinceabdoulrachid@gmail.com">nacanaboprinceabdoulrachid@gmail.com</a><a href="https://www.google.com/maps/search/Ouagadougou" target="_blank" rel="noopener noreferrer">Ouagadougou, Burkina Faso</a></div>
      </div>
      <div class="shared-footer-bottom">© <span id="currentYear"></span> Chidra Free Shop. Tous droits réservés.</div>`;
    document.body.appendChild(footer);
  }
  updateCurrentYear();
  applySiteSettings();
}

function getQueryParam(name) {
  const params = new URLSearchParams(window.location.search);
  return params.get(name);
}

function setupMobileMenu() {
  const nav = document.querySelector('nav');
  const navLinks = nav?.querySelector('.nav-links');
  if (!nav || !navLinks || nav.querySelector('.mobile-menu-toggle')) return;

  const style = document.createElement('style');
  style.textContent = `
    .mobile-menu-toggle {
      display: none;
      background: transparent;
      color: #fff;
      border: 1px solid #444;
      border-radius: 8px;
      width: 40px;
      height: 40px;
      font-size: 1.25rem;
      cursor: pointer;
    }
    @media (max-width: 768px) {
      .mobile-menu-toggle { display: inline-flex; align-items: center; justify-content: center; }
      nav.mobile-open { position: sticky; }
      nav.mobile-open .nav-links {
        display: flex;
        position: absolute;
        top: 64px;
        left: 0;
        right: 0;
        padding: 1rem 1.25rem;
        background: #0A0A0A;
        border-top: 1px solid #222;
        flex-direction: column;
        gap: 1rem;
      }
    }
  `;
  document.head.appendChild(style);

  const button = document.createElement('button');
  button.className = 'mobile-menu-toggle';
  button.type = 'button';
  button.setAttribute('aria-label', 'Ouvrir le menu');
  button.setAttribute('aria-expanded', 'false');
  button.textContent = '☰';
  button.addEventListener('click', () => {
    const isOpen = nav.classList.toggle('mobile-open');
    button.setAttribute('aria-expanded', String(isOpen));
    button.setAttribute('aria-label', isOpen ? 'Fermer le menu' : 'Ouvrir le menu');
    button.textContent = isOpen ? '✕' : '☰';
  });
  nav.querySelector('.nav-right')?.before(button);

  navLinks.addEventListener('click', event => {
    if (event.target.closest('a')) {
      nav.classList.remove('mobile-open');
      button.setAttribute('aria-expanded', 'false');
      button.setAttribute('aria-label', 'Ouvrir le menu');
      button.textContent = '☰';
    }
  });
}

function setupPageTransitions() {
  const transitionStyle = document.createElement('style');
  transitionStyle.textContent = `
    body.page-transition-ready {
      opacity: 0;
      transform: translateY(10px);
    }
    body.page-transition-visible {
      opacity: 1;
      transform: translateY(0);
      transition: opacity .35s ease, transform .35s cubic-bezier(.2,.8,.2,1);
    }
    body.page-transition-leaving {
      opacity: 0;
      transform: translateY(-10px);
      transition: opacity .22s ease, transform .22s ease;
    }
    @media (prefers-reduced-motion: reduce) {
      body.page-transition-ready,
      body.page-transition-visible,
      body.page-transition-leaving {
        opacity: 1;
        transform: none;
        transition: none;
      }
    }
  `;
  document.head.appendChild(transitionStyle);

  document.body.classList.add('page-transition-ready');
  requestAnimationFrame(() => {
    document.body.classList.remove('page-transition-ready');
    document.body.classList.add('page-transition-visible');
  });

  document.addEventListener('click', event => {
    const link = event.target.closest('a');
    if (!link || event.defaultPrevented || event.button !== 0) return;
    if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
    if (link.target === '_blank' || link.hasAttribute('download')) return;

    const url = new URL(link.href, window.location.href);
    if (url.origin !== window.location.origin || url.pathname === window.location.pathname && url.search === window.location.search) return;

    event.preventDefault();
    document.body.classList.remove('page-transition-visible');
    document.body.classList.add('page-transition-leaving');
    window.setTimeout(() => {
      window.location.href = url.href;
    }, 220);
  });
}

window.addEventListener('DOMContentLoaded', () => {
  updateNavVisibility();
  updateCurrentYear();
  setupSharedPageStyle();
  setupMobileMenu();
  setupPageTransitions();
});
