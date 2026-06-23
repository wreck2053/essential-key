document.querySelectorAll("[data-year]").forEach((element) => {
  element.textContent = new Date().getFullYear();
});

const analyticsMeasurementId = "G-BJW6JFFGHR";
const analyticsPreferenceKey = "essential-key-analytics-consent";

function startAnalytics() {
  if (window.gtag) return;

  window.dataLayer = window.dataLayer || [];
  window.gtag = function () {
    window.dataLayer.push(arguments);
  };
  window.gtag("js", new Date());
  window.gtag("config", analyticsMeasurementId, {
    anonymize_ip: true,
  });

  const tag = document.createElement("script");
  tag.async = true;
  tag.src = `https://www.googletagmanager.com/gtag/js?id=${analyticsMeasurementId}`;
  document.head.appendChild(tag);
}

function saveAnalyticsPreference(value) {
  localStorage.setItem(analyticsPreferenceKey, value);
  document.querySelector("[data-analytics-consent]")?.remove();
  if (value === "granted") startAnalytics();
}

function showAnalyticsConsent() {
  const banner = document.createElement("aside");
  banner.className = "analytics-consent";
  banner.dataset.analyticsConsent = "";
  banner.setAttribute("aria-label", "Analytics preference");
  banner.innerHTML = `
    <p>This website uses optional Google Analytics to measure page views and APK-download clicks. No analytics is loaded unless you accept. <a href="${location.pathname.includes("/essential-key/") && location.pathname !== "/essential-key/" ? "../privacy/" : "privacy/"}">Privacy details</a>.</p>
    <div class="analytics-consent-actions">
      <button class="button secondary" type="button" data-analytics-decline>Decline</button>
      <button class="button" type="button" data-analytics-accept>Accept analytics</button>
    </div>
  `;
  document.body.appendChild(banner);
  banner.querySelector("[data-analytics-decline]").addEventListener("click", () => {
    saveAnalyticsPreference("denied");
  });
  banner.querySelector("[data-analytics-accept]").addEventListener("click", () => {
    saveAnalyticsPreference("granted");
  });
}

const analyticsPreference = localStorage.getItem(analyticsPreferenceKey);
if (analyticsPreference === "granted") {
  startAnalytics();
} else if (analyticsPreference !== "denied") {
  showAnalyticsConsent();
}

document.addEventListener("click", (event) => {
  const link = event.target.closest("a");
  if (!link || !link.href.includes("/releases/") || !link.href.includes(".apk")) return;
  if (!window.gtag) return;

  window.gtag("event", "download_apk", {
    file_name: "essential-key-remapper.apk",
    link_url: link.href,
    transport_type: "beacon",
  });
});
