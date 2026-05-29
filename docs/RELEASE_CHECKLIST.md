# Touchgrass V1 release checklist

Ordered for actually shipping. Each block is roughly one sitting of work.

---

## 0 · Pre-flight verification (do these locally before anything else)

- [ ] `./gradlew testDebugUnitTest` — all ~130 tests pass.
- [ ] `./gradlew detekt ktlintCheck` — clean (or document waived rules).
- [ ] `./gradlew assembleDebug` — succeeds. Open the APK on a real device, complete onboarding.
- [ ] **Real-device blocking smoke test** on a Pixel and an Xiaomi:
  - Open YouTube → Shorts. Touchgrass exits. ✓
  - Open Instagram → Reels tab. Exits. ✓
  - Open Instagram → DM-shared reel. Exits. ✓
  - Open TikTok For You. Exits. ✓
  - Open Facebook Reels viewer. Exits. ✓
  - Open Snapchat Spotlight. Exits. ✓
  - Open Chrome to `youtube.com/shorts/<id>`. Exits. ✓
  - Open Samsung Internet to same. Exits. ✓
- [ ] **Spam-tap defense:** repeatedly tap into Reels for 10s. Should still get out without flapping.
- [ ] **Pause flow:** all four friction modes (wait, math, code, breathing) complete end-to-end.
- [ ] **Commitment lock:** enable, send code (Fake or real Resend), receive, verify. Then try to turn off Touchgrass on home → OTP gate appears.
- [ ] **Watchdog soak test:** install, complete onboarding, leave for **48 hours** untouched on a Xiaomi or Samsung. Then open Instagram → Reels. Should still block.
- [ ] **Boot survival:** reboot the phone. The foreground service notification reappears within ~30s of unlock.
- [ ] **Battery optimization paths** for at least 3 OEMs walked through manually using the in-app walkthrough — confirm the deep-link buttons land on the right system screen.

---

## 1 · Asset / content production

- [ ] **App icon.** Replace `ic_launcher_foreground.xml` + `ic_launcher_background.xml` placeholder with the designed asset. Export adaptive icon (round + square) at all mipmap densities (mdpi → xxxhdpi).
- [ ] **Feature graphic** — 1024 × 500 PNG.
- [ ] **Play Store screenshots** — 8 frames per the plan in `marketing/play-store-listing.md`. 1080 × 1920 portrait. Use the actual on-device app (not Compose Previews) so the system status bar and gestures are realistic.
- [ ] **Notification icon.** Replace `ic_notification_touchgrass.xml` placeholder if the designed brand mark differs.
- [ ] **OEM step screenshots** for `assets/oem/*.json` — annotated images named `oem_{key}_step{n}.png`. Drop into `app/src/main/res/drawable-nodpi/` (one resolution is fine since they're illustrative). Text-only fallback already works, so this is upgrade quality, not blocker.

---

## 2 · Domains + email

- [ ] **Register `gettouchgrass.app`** through any registrar that supports `.app` (Cloudflare / Namecheap / Porkbun). `.app` is HTTPS-enforced via HSTS preload — you'll need a valid cert from day 1.
- [ ] **Register `gettouchgrass.com`** as the secondary domain.
- [ ] **DNS:** point both A/AAAA records to the host (Cloudflare Pages, GitHub Pages, Vercel, etc.).
- [ ] **301 redirect** `gettouchgrass.com` → `https://gettouchgrass.app`.
- [ ] **Resend account.** Sign up at resend.com → verify the `gettouchgrass.app` sending domain (DKIM + SPF + DMARC TXT records added at the registrar). Wait for the dashboard to show "verified".
- [ ] **Generate Resend API key** (with `emails:send` scope only) and drop it into `gradle.properties` (gitignored):
  ```
  RESEND_API_KEY=re_xxx
  RESEND_FROM_EMAIL=noreply@gettouchgrass.app
  ```
- [ ] **Set up `privacy@gettouchgrass.app`** + `security@` + `conduct@` email forwards (Cloudflare Email Routing or similar — free) so the addresses in PRIVACY/SECURITY/CODE_OF_CONDUCT actually reach you.
- [ ] **Deploy `marketing/landing-page/`** to whichever host (Cloudflare Pages is free + has the right Brotli + IPv6 + Speed defaults). Make sure `/privacy.html` resolves at `/privacy` (rewrite or just rename the file).

---

## 3 · GitHub repo prep

- [ ] **Create `touchgrass-android` repository.** Public.
- [ ] **Replace every `your-handle` placeholder** across the codebase:
  - `README.md`
  - `CONTRIBUTING.md`
  - `PRIVACY.md`
  - `SECURITY.md`
  - `CODE_OF_CONDUCT.md`
  - `app/src/main/kotlin/com/touchgrass/app/ui/trust/TrustDashboardScreen.kt` (`GITHUB_URL` const)
  - `marketing/landing-page/index.html` + `privacy.html`
- [ ] **`PRIVACY_URL` constant** in `TrustDashboardScreen.kt` → `https://gettouchgrass.app/privacy`.
- [ ] **Push the project** with the existing branch protection settings on `main`.
- [ ] **GitHub Actions secrets:**
  - `KEYSTORE_BASE64` (base64 of your signing keystore)
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS`
  - `KEY_PASSWORD`
  - `PLAY_SERVICE_ACCOUNT_JSON` (the Play Console service-account JSON)
  - `RESEND_API_KEY`
  - `RESEND_FROM_EMAIL`
- [ ] **Test CI:** open a no-op PR. The `android-ci.yml` workflow should run detekt + ktlint + tests + assemble.

---

## 4 · Google Play Console setup

- [ ] **Pay the one-time $25** developer fee (if you haven't already).
- [ ] **Create the app** in Play Console with `Touchgrass: Block Reels & Shorts`.
- [ ] **App Content questionnaire:**
  - Privacy Policy URL: `https://gettouchgrass.app/privacy`
  - Ads: **No**
  - Target audience: **18+** (avoid kids program — sidesteps a paperwork mountain)
  - News app: **No**
  - COVID-19: **No**
  - Government app: **No**
  - Financial features: **No**
  - Data safety form (use the section in `marketing/play-store-listing.md` verbatim)
  - **Accessibility Use Declaration:** fill in the description from `marketing/play-store-listing.md`. Attach a short PDF showing the Trust Dashboard + the strict `packageNames` in `accessibility_service_config.xml`.
- [ ] **App content rating:** complete the IARC questionnaire (answer "no" to everything sensitive). Should come back as "Everyone".
- [ ] **App pricing:** Free.
- [ ] **Countries:** All available.
- [ ] **Closed Testing track:** create a tester list. Per Play's 2023 rule you need **20+ testers opted-in for at least 14 consecutive days** before promoting to production. Start this clock as early as possible.

---

## 5 · Signed release build

- [ ] **Generate a release keystore** (`keytool -genkey ...`). **Back it up multiple places.** Losing it means you can never update the app under this listing again.
- [ ] **Verify your CI signing config** by running the `release.yml` workflow against a tag like `v0.1.0-rc1`.
- [ ] **Run a manual release build locally:**
  ```
  ./gradlew bundleRelease
  ```
- [ ] **Upload the AAB** to Play Console → Closed Testing → Create new release.
- [ ] Submit for review. Expected wait: **2–7 days for first review.** Accessibility apps often draw extra scrutiny — the Justification PDF helps.

---

## 6 · Beta + iteration (14+ days minimum per Play policy)

- [ ] **Recruit testers:**
  - Friends + family with diverse phones (especially Xiaomi/OnePlus/Oppo).
  - Post on `r/digitalminimalism`, `r/nosurf`, `r/decidingtobetter` asking for testers.
  - Post on Indie Hackers.
  - Reach out to digital-wellbeing newsletter writers offering early access.
- [ ] **Set up an issue template** in GitHub for bug reports — include "what phone, what app, what surface, what happened".
- [ ] **Monitor Play Console pre-launch report** for any device-class crashes.
- [ ] **Triage feedback** daily. Tag bugs P0/P1/P2; ship fixes via tag-pushed releases.
- [ ] **Lock-in detector view IDs.** If Instagram or TikTok shipped an update during beta, refresh the hint arrays in the affected `*Detector.kt` files.
- [ ] **Close the loop with testers.** Personally email them when their reported bug is fixed.

---

## 7 · Launch day

- [ ] **Promote Closed Testing → Production** in Play Console. (Requires the 20-tester × 14-day gate satisfied.)
- [ ] **Tag `v1.0.0`** in git → CI uploads signed AAB to Play production track.
- [ ] **Submit to Product Hunt** (12:01am PT, Tue/Wed for best reach).
- [ ] **Post in order through the day:**
  - 6am ET: r/digitalminimalism post
  - 9am ET: r/nosurf post
  - 11am ET: r/decidingtobetter, r/getdisciplined, r/dopaminedetoxing
  - 1pm ET: HN Show HN
  - 3pm ET: Indie Hackers
  - 6pm ET: TikTok / Reels short showing "I built a TikTok blocker. Here it is on TikTok."
- [ ] **Email the waitlist** with the Play Store link.
- [ ] **Reply to every comment / review for 7 days straight.** Even the negative ones. Especially the negative ones.

---

## 8 · First 30 days (post-launch operations)

- [ ] **Daily:** read every Play Store review. Reply in-voice. Ship a 1-line fix where possible.
- [ ] **Weekly:**
  - Two TikTok/Instagram posts (build-in-public angle).
  - Refresh Play Store keywords based on Play Console search-term data.
  - Skim the GitHub issues backlog.
- [ ] **Look for the inflection in install velocity.** If it's there at day 14, lock the product, focus on content. If it's not, instrument what's missing (NOT by adding analytics — by asking new installers in-app via a one-time prompt).
- [ ] **Re-evaluate the monetization pivot at day 60.** If the org-conversion-equivalent (Play store install → 7-day-retained user) is healthy, leave free. If you need revenue to keep going, the spec §3.3 Plus tier comes back at $9.99 one-time. See `memory/project_monetization_pivot.md`.

---

## Definition of Done — V1 ships when ALL true

(Mirrors spec §14, updated for the free-for-start pivot.)

- [ ] All 5 target apps blocked reliably on a fresh Pixel **and** a fresh Xiaomi.
- [ ] 48-hour soak test passes on the Xiaomi.
- [ ] All 7 OEMs (Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, Huawei) have an onboarding walkthrough.
- [ ] Trust Dashboard reviewed and approved as accurate.
- [ ] No third-party analytics SDKs in release build.
- [ ] No ads.
- [ ] Privacy policy live at `https://gettouchgrass.app/privacy` and linked from app + Play Store.
- [ ] At least 20 beta users have installed and used Touchgrass for at least one session.
- [ ] All P0 / P1 bugs from beta closed.
- [ ] Open-source repo published, CI green.
- [ ] Play Store listing finalized with screenshots + feature graphic + ASO-optimized copy.
- [ ] `gettouchgrass.app` live with landing page + email signup.
- [ ] `gettouchgrass.com` 301-redirects to `gettouchgrass.app`.
- [ ] Resend domain verified; OTP delivery tested end-to-end with a real release build.
