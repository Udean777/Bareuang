"use strict";

// Year
const yearEl = document.getElementById("year");
if (yearEl) yearEl.textContent = String(new Date().getFullYear());

// Mobile nav
const hamburger = document.getElementById("hamburger");
const navLinks = document.getElementById("navLinks");
if (hamburger && navLinks) {
  hamburger.addEventListener("click", () => navLinks.classList.toggle("open"));
  navLinks
    .querySelectorAll("a")
    .forEach((a) =>
      a.addEventListener("click", () => navLinks.classList.remove("open")),
    );
}

// Nav shadow on scroll
const nav = document.querySelector(".nav");
if (nav) {
  const onScroll = () => nav.classList.toggle("scrolled", window.scrollY > 8);
  window.addEventListener("scroll", onScroll, { passive: true });
  onScroll();
}

// Reveal on scroll
const reveals = document.querySelectorAll(".reveal");
if ("IntersectionObserver" in window && reveals.length) {
  const io = new IntersectionObserver(
    (entries) => {
      for (const e of entries)
        if (e.isIntersecting) {
          e.target.classList.add("in");
          io.unobserve(e.target);
        }
    },
    { threshold: 0.12, rootMargin: "0px 0px -40px 0px" },
  );
  reveals.forEach((el) => io.observe(el));
} else {
  reveals.forEach((el) => el.classList.add("in"));
}


// Download — via same-origin /api/download proxy (fix stuck 100% & rate-limit)
const DL_URL = "/api/download";
const GH_FALLBACK = "https://github.com/Udean777/Bareuang/releases/latest";
const toastEl = document.getElementById("toast");
let toastTimer;
function showToast(msg){
  if(!toastEl) { alert(msg); return; }
  toastEl.textContent = msg;
  toastEl.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(()=> toastEl.classList.remove("show"), 3500);
}
for(const a of document.querySelectorAll("a.js-download")){
  a.setAttribute("href", DL_URL);
  a.setAttribute("download", "Bareuang-latest.apk");
  a.addEventListener("click", ()=>{
    // fire-and-forget HEAD check — tidak block download, hanya toast jika 404
    fetch(DL_URL, {method:"HEAD"}).then(r=>{
      if(!r.ok) {
        const lang = document.documentElement.lang === "en" ? "en" : "id";
        showToast(lang==="en" ? "Download unavailable — try GitHub Releases." : "Download belum tersedia — coba di GitHub Releases.");
        // fallback buka releases page
        setTimeout(()=> window.open(GH_FALLBACK, "_blank", "noopener"), 800);
      }
    }).catch(()=>{});
  });
}

// i18n — lightweight, no dependencies
// dict loaded from i18n.js (window.dict)

function applyLang(lang) {
  const d = dict[lang] || dict.id;
  document.documentElement.lang = lang;
  for (const el of document.querySelectorAll("[data-i18n]")) {
    const key = el.getAttribute("data-i18n");
    const val = d[key];
    if (val == null) continue;
    const hasSvg = el.querySelector("svg");
    if (val.includes("<")) {
      el.innerHTML = val;
      continue;
    }
    if (hasSvg && el.childNodes.length > 1) {
      const svgHtml = hasSvg.outerHTML;
      el.innerHTML = `${svgHtml} ${val}`;
    } else {
      el.textContent = val;
    }
  }
  // hero title/desc contain HTML already handled above
  for (const b of document.querySelectorAll(".lang-switch button")) {
    const active = b.dataset.lang === lang;
    b.classList.toggle("active", active);
    b.setAttribute("aria-pressed", String(active));
  }
  try {
    localStorage.setItem("bareuang_lang", lang);
  } catch {}
  document.title =
    lang === "en"
      ? "Bareuang — Your cozy money companion"
      : "Bareuang — Teman cozy buat uangmu";
  const metaDesc = document.querySelector('meta[name="description"]');
  if (metaDesc) {
    metaDesc.content =
      lang === "en"
        ? "Know how long your money lasts. Core financial data stays local; optional receipt OCR is online and consent-gated."
        : "Tahu sampai kapan uangmu tahan. Data keuangan utama tersimpan lokal; OCR struk online bersifat opsional dan membutuhkan consent.";
  }
}

const saved = (() => {
  try {
    return localStorage.getItem("bareuang_lang");
  } catch {
    return null;
  }
})();
const initial =
  saved === "en" || saved === "id"
    ? saved
    : (navigator.language || "").toLowerCase().startsWith("en")
      ? "en"
      : "id";
applyLang(initial);
for (const b of document.querySelectorAll(".lang-switch button"))
  b.addEventListener("click", () => applyLang(b.dataset.lang));
