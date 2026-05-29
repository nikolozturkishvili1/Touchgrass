# Touchgrass — Play Console runbook (Closed Testing submission)

> **Goal:** get the signed AAB into the **Closed Testing** track and start the
> **20-tester × 14-consecutive-day** clock (required before a new personal account
> can apply for production). Every value you need is below — copy/paste it.

**Inputs you already have**
- Signed AAB: `app/build/outputs/bundle/release/app-release.aab` (versionName `0.1.0`, versionCode `1`)
- Listing graphics: `marketing/play-assets/icon-512.png`, `marketing/play-assets/feature-1024x500.png`
- Phone screenshots: produced in the device session (drop into `marketing/play-assets/`)
- Privacy policy URL: _confirmed once GitHub Pages is live_ — expected
  `https://nikolozturkishvili1.github.io/Touchgrass/privacy.html`
- Full listing copy reference: `marketing/play-store-listing.md`

> ⚠️ **Back up `keysets/touchgrass-upload.jks` + its passwords in 2+ safe places now.**
> When you upload, **opt in to Play App Signing** (Google holds the real signing key;
> your `.jks` becomes the *upload* key). Losing the upload key is recoverable; the rest isn't.

---

## 1 · Main store listing  (Grow → Store presence → Main store listing)

- [ ] **App name:** `Touchgrass: Block Reels & Shorts`
- [ ] **Short description (78/80):**
  `Block doomscrolling on Reels, Shorts, TikTok. No ads. No subscription. Free.`
- [ ] **Full description:** paste the long description block from `marketing/play-store-listing.md`.
- [ ] **App icon:** upload `icon-512.png`
- [ ] **Feature graphic:** upload `feature-1024x500.png`
- [ ] **Phone screenshots:** upload 2–8 (1080×1920 portrait). Order per the screenshot plan in the listing doc.

## 2 · Store settings  (Grow → Store presence → Store settings)

- [ ] **App category:** `Productivity`
- [ ] **Tags:** pick the closest to: block reels / block shorts / screen time / digital wellbeing / focus / dopamine detox
- [ ] **Contact email:** `nikodeveloper23@gmail.com` _(swap to `privacy@gettouchgrass.app` once the domain is live)_
- [ ] **Website (optional):** the GitHub Pages URL for now

## 3 · App content  (Policy → App content) — the gating section

- [ ] **Privacy policy:** the GitHub Pages privacy URL
- [ ] **Ads:** `No, my app does not contain ads`
- [ ] **App access:** `All functionality is available without special access`
  _(no login wall; the commitment lock is optional and doesn't gate the app)_
- [ ] **Content ratings:** start questionnaire → category **Utility/Productivity** → answer **No** to every
  violence / sexual / drugs / gambling question → expect **Everyone**.
- [ ] **Target audience & content:** target age group **18+ only**. "Appeals to children?" → **No**.
  _(Keeps you out of the Families program paperwork.)_
- [ ] **News app:** `No`
- [ ] **COVID-19 contact tracing/status:** `No`
- [ ] **Government app:** `No`
- [ ] **Financial features:** `No, my app doesn't provide any financial features`
- [ ] **Health apps:** declare none
- [ ] **Data safety:** see §4 below
- [ ] **Accessibility / sensitive permissions:** see §5 below — **this is what gets scrutinized**

## 4 · Data safety  (Policy → App content → Data safety)

- [ ] **Does your app collect or share required user data?** `Yes`
- [ ] **Data type:** *Personal info → Email address*
  - Collected: **Yes** · Shared: **No**
  - Optional (`Users can choose whether this data is collected`): **Yes** — only if they enable the commitment lock
  - Purpose: **App functionality** (sending the commitment-lock one-time code)
  - Linked to the user's identity: **No**
- [ ] **No other data** — no location, contacts, photos, device IDs, analytics, crash logs, etc.
- [ ] **Security practices:**
  - **Data is encrypted in transit:** `Yes` (HTTPS to the email service)
  - **Users can request data deletion:** `Yes` (disable the lock or uninstall)

## 5 · Accessibility use declaration (AccessibilityService)

Touchgrass uses `AccessibilityService`, which Google reviews closely. Provide this verbatim
(full text in `marketing/play-store-listing.md` → "Accessibility Use Declaration"):

- [ ] **What it does:** "Touchgrass detects when the user opens short-form-feed UI (Reels, Shorts,
  TikTok, Spotlight) in apps they explicitly added to a block list, and intercepts back to exit the feed."
- [ ] **Why no other API works:** "Android does not expose foreground-app UI events to third-party apps
  through any other API… No other API can perform `GLOBAL_ACTION_BACK` to intercept a feed."
- [ ] **User disclosure:** the Trust Dashboard is shown during onboarding *before* Accessibility is requested.
- [ ] **Open-source proof:** link the public repo `https://github.com/nikolozturkishvili1/Touchgrass`.
- [ ] **Attach** the justification PDF (Trust Dashboard screenshot + the strict `packageNames` from
  `accessibility_service_config.xml`) — generated for you into `marketing/play-assets/`.

## 6 · Closed testing release  (Test and release → Testing → Closed testing)

- [ ] Create/Use a **Closed testing** track.
- [ ] **Testers:** create an email list or link a **Google Group** (easiest — new testers self-join).
- [ ] **Create new release** → **upload** `app-release.aab` → **opt in to Play App Signing**.
- [ ] **Release name:** `0.1.0 (1)`
- [ ] **What's new:** paste the "What's New (first release)" block from the listing doc.
- [ ] **Countries/regions:** All.
- [ ] **Save → Review release → Start rollout to Closed testing** (sends it for review).

## 7 · After submission

- [ ] Copy the **tester opt-in URL** and send it to your testers (they must accept *and* install via Play).
- [ ] First review: **2–7 days**. Watch the **Pre-launch report** for device-class crashes.
- [ ] The clock: **≥20 testers opted in for 14 continuous days** → then "Apply for production access."
- [ ] Pre-production TODO (not blocking closed testing): register `gettouchgrass.app`, verify Resend so
  the commitment-lock email actually sends, then swap all URLs from github.io → gettouchgrass.app and rebuild.

---

### Tester note (paste into your recruiting message)
> Heads up for this test build: the optional "commitment lock" email feature isn't wired to a live mail
> server yet, so its code won't arrive by email during the beta — everything else is fully functional.
