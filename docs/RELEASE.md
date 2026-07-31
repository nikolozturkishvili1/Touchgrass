# Touchgrass — release runbook

The single procedure for shipping Touchgrass, on every channel. Replaces the five
overlapping release docs that used to disagree with each other.

Current state and what to do next live in `STATUS.md`, not here. This file is the
**how**; `STATUS.md` is the **where we are**.

Every version number below is a live value — verify against `app/build.gradle.kts`
before you paste it anywhere. As of 2026-07-30: `0.1.2` / versionCode `3`.

---

## Two channels, run in parallel

| | Outside Play | Google Play |
|---|---|---|
| Channels | GitHub Releases, Obtainium, IzzyOnDroid | Closed testing → Production |
| Gated on | Nothing. Push a tag. | 12 testers × 14 consecutive days |
| Time to users | Same day | Weeks, dependent on recruiting |
| Signing | Project release key | Google re-signs (Play App Signing) |

The two builds are signed with **different keys**. A user cannot update from one to
the other in place. Say so wherever the app is linked.

---

## 1 · Pre-flight

Windows. `java`/`keytool` are not on PATH — use Android Studio's bundled JBR.

```powershell
cd D:\My_Projects\Touchgrass
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

.\gradlew.bat testDebugUnitTest
.\gradlew.bat detekt ktlintCheck
.\gradlew.bat assembleDebug
```

Then, on a real device:

- YouTube → Shorts exits. Instagram → Reels tab, Explore reels, DM-shared reel all
  exit. TikTok For You exits. Facebook Reels exits. Snapchat Spotlight exits.
  Chrome and Samsung Internet on `youtube.com/shorts/<id>` exit.
- Spam-tap into Reels for 10s — still exits, no flapping.
- All four pause/friction modes complete end to end.
- Reboot: the foreground-service notification returns within ~30s of unlock.
- 48-hour soak on an aggressive-OEM device (Xiaomi or Samsung) — still blocking
  afterwards. This is the test that catches the failure users actually report.

Bump `versionCode` in `app/build.gradle.kts` for **every** upload. Play rejects a
duplicate code. Add the matching `CHANGELOG.md` entry and a
`fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`.

### Signing

`keystore.properties` (gitignored) → `keysets/touchgrass-upload.jks` (gitignored).
If `keystore.properties` is missing, release packaging **fails by design** — see
`hasReleaseSigning` in `app/build.gradle.kts`. That failure is the signal. Never add
a debug-signing fallback.

Back the `.jks` and its passwords up in **two or more places**. Never commit the
keystore, the properties file, or a built `.aab`/`.apk`. Never print a password, a
base64 keystore, `GITHUB_PAT`, or a Resend key into a transcript.

### Build

```powershell
.\gradlew.bat bundleRelease      # -> app\build\outputs\bundle\release\app-release.aab   (Play)
.\gradlew.bat assembleRelease    # -> app\build\outputs\apk\release\                     (GitHub/IzzyOnDroid)
```

Confirm the artifact reports `com.touchgrassinc.app` and the expected versionCode
before uploading anything.

---

## 2 · Distribution outside Play

Order of value: GitHub Releases (instant) → Obtainium (free auto-updates) →
IzzyOnDroid (days to two weeks) → F-Droid proper (weeks to months, optional).

### Prerequisite — CI secrets

`.github/workflows/release.yml` requires, under
Repo → Settings → Secrets and variables → Actions:

`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

Optional, for later automation: `PLAY_SERVICE_ACCOUNT_JSON`, `RESEND_API_KEY`,
`RESEND_FROM_EMAIL`.

**Niko sets these by hand in the browser.** If any is missing the release job fails
loudly on purpose — that is correct behavior, not a bug to work around.

### Cut a release

```powershell
git add -A
git commit -m "release: <what changed>"
git push
git tag v0.1.2
git push origin v0.1.2
```

The tag triggers `release.yml`, which runs `bundleRelease assembleRelease`, renames
the APK to `Touchgrass-v<versionName>.apk`, and publishes a **public** GitHub
Release. Public matters: IzzyOnDroid and Obtainium poll the Releases API and cannot
see drafts.

After the run goes green, verify at
`https://github.com/nikolozturkishvili1/Touchgrass/releases/latest`:

- `Touchgrass-v<version>.apk` is attached
- the release is **not** marked draft

### Obtainium

Nothing to configure. Tell users:

> Obtainium: add `https://github.com/nikolozturkishvili1/Touchgrass`

### IzzyOnDroid inclusion request

`fastlane/metadata/android/en-US/` is already complete, so the listing writes
itself. Open an issue at `https://codeberg.org/IzzyOnDroid/repo/issues` with their
"Inclusion request" template.

**Title:** `Inclusion request: Touchgrass (com.touchgrassinc.app)`

**Body:**

- **App name:** Touchgrass
- **Package ID:** `com.touchgrassinc.app`
- **Source repository:** https://github.com/nikolozturkishvili1/Touchgrass
- **Releases (signed APK attached):** https://github.com/nikolozturkishvili1/Touchgrass/releases
- **License:** GPL-3.0
- **Current version:** *(read from `app/build.gradle.kts`)*
- **minSdk / targetSdk:** 29 / 36
- **Metadata:** `fastlane/metadata/android/en-US/` is present in the repo.

**What it does:** Touchgrass blocks short-form video feeds — Instagram Reels,
YouTube Shorts, TikTok's For You feed, Facebook Reels, Snapchat Spotlight, and
Shorts in Chrome and Samsung Internet — while leaving the rest of those apps
working normally.

**Inclusion policy checklist:**

- No ads, no tracking, no analytics SDKs of any kind.
- No non-free dependencies, no proprietary blobs, no Google Play Services requirement.
- No self-updater and no binary downloads at runtime.
- No user data leaves the device. The single network call is an optional,
  user-initiated email one-time code for the "commitment lock"; it is off by default
  and the app is fully functional without it.
- APKs are built by GitHub Actions from the tagged commit and signed with the
  developer's release key. Reproducible from `./gradlew assembleRelease`.

**On the AccessibilityService permission:** required to detect which screen is
foregrounded among the user's chosen block list and to issue `GLOBAL_ACTION_BACK`
to exit a feed; Android exposes no other API for either. The service config
restricts `packageNames` to the block list rather than listening system-wide, the
app shows a full plain-English disclosure ("Trust Dashboard") before requesting the
permission, and nothing the service sees leaves the device. Source is public.

---

## 3 · Google Play

### Track choice

|  | Internal testing | Closed testing |
|---|---|---|
| Full store listing + screenshots | not required | **required** |
| Country selection, permission declarations | relaxed | **required** |
| Counts toward Production access | ❌ no | ✅ yes — the 12 × 14 gate |

New personal developer accounts must complete **closed testing with 12+ testers
opted in for 14 consecutive days** before applying for production. Google lowered
this from 20 on 2024-12-11. Recruit ~15 for drop-off buffer — the clock pauses if
you fall below 12.

**Managed publishing is ON.** After Google approves a release it waits in
*Publishing overview → Changes ready to publish* until you click **Publish
changes**. Miss this and testers never receive the build.

### Store listing — Grow → Store presence → Main store listing

Copy source of truth is `fastlane/metadata/android/en-US/`. Do not retype it.

- **App name:** `Touchgrass: Block Reels & Shorts` (`title.txt`) — ⚠️ 32 characters; Play's app-name field caps at **30**. Fine as-is for fastlane/IzzyOnDroid, but the Play field needs a shorter variant (e.g. `Touchgrass: Block Reels+Shorts`, exactly 30). Niko decides the wording.
- **Short description:** `short_description.txt` (≤80 chars)
- **Full description:** `full_description.txt`
- **Icon:** `marketing/play-assets/icon-512.png`
- **Feature graphic:** `marketing/play-assets/feature-1024x500.png`
- **Phone screenshots:** `marketing/screenshots/` (mirrored in
  `fastlane/metadata/android/en-US/images/phoneScreenshots/`) — portrait, 2–8 required
- **Category:** `Productivity`. **Not** Health & Fitness — that contradicts the
  Data safety answers. Not Tools.
- **Contact email:** `nikodeveloper23@gmail.com`
- **Countries:** All

Privacy URLs must use the live GitHub Pages address until `gettouchgrass.app` is
actually registered:
`https://nikolozturkishvili1.github.io/Touchgrass/privacy.html`

### App content — Policy → App content

These are **legal declarations.** Use the text below verbatim. If a question appears
that is not answered here, **stop and ask** — do not infer an answer.

- **Privacy policy:** the live GitHub Pages privacy URL above. The page actually served is `marketing/landing-page/privacy.html` (published by `pages.yml`) — **not** `docs/PRIVACY.md`. Edit the HTML if the policy changes.
- **Ads:** No, my app does not contain ads.
- **App access:** All functionality is available without special access. (No login
  wall; the commitment lock is optional and does not gate the app.)
- **Content rating:** questionnaire → category Utility/Productivity → **No** to
  every violence / sexual / drugs / gambling question → expect **Everyone**.
- **Target audience:** 18+ only. "Appeals to children?" → **No.** (Keeps you out of
  the Families program.)
- **News app / COVID-19 / Government app / Financial features:** No.
- **Health apps:** none declared.
- **Advertising ID:** No, does not use one.

### Data safety

- **Collects user data:** Yes — exactly **one** type: *Personal info → Email address*.
  - Collected: **Yes** · Shared: **No** · Ephemeral: **No**
  - Optional ("users can choose"): **Yes** — only if they enable the commitment lock
  - Purpose: **App functionality** only (sending the one-time code)
  - Linked to identity: **No** · Used for tracking: **No**
- **No other data type.** No location, contacts, photos, files, messages, device
  IDs, app activity, analytics, or crash logs.
- **Encrypted in transit:** Yes (HTTPS to the email service).
- **Users can request deletion:** Yes — disable the lock or uninstall. Deletion URL
  is the privacy page's `#data-deletion` anchor.

### Accessibility use declaration

Reviewed closely by Google. Paste verbatim:

- **What it does:** "Touchgrass detects when the user opens short-form-feed UI
  (Reels, Shorts, TikTok, Spotlight) in apps they explicitly added to a block list,
  and intercepts back to exit the feed. The AccessibilityService is the only Android
  API that can both observe foreground-app UI events on third-party apps and inject
  a back gesture without invasive permissions."
- **Why no other API works:** "Android does not expose foreground-app UI events to
  third-party apps through any other API. Usage Stats only reports aggregate time,
  not which screen is open. No other API can perform GLOBAL_ACTION_BACK to intercept
  a feed."
- **User disclosure:** "The in-app Trust Dashboard is shown during onboarding,
  before Accessibility is requested. The on-device accessibility-service description
  string also explains the exact scope."
- **Open-source proof:** https://github.com/nikolozturkishvili1/Touchgrass

A **demo video URL** may be required (screen recording: open a feed → blocked →
Trust Dashboard). If asked and none exists, stop and record one, upload it unlisted
to YouTube, and paste that URL. **Never invent a URL.**

### Foreground service declaration

Type: **Special Use** (`FOREGROUND_SERVICE_SPECIAL_USE`, subtype
`touchgrass_reliability_watchdog`). Justification:

> Touchgrass runs a lightweight foreground service as a reliability watchdog: it
> keeps the AccessibilityService alive and re-arms it if the OS or an aggressive OEM
> battery manager kills it. The app's only function — blocking short-form feeds the
> instant they open — fails silently if the service stops. No standard FGS type fits
> (no media, location, sync, camera, mic, call, or connectivity). It performs no
> network or data transfer; it only monitors and restarts the app's own
> accessibility component.

### Ship to the closed testing track

1. **Test and release → Testing → Closed testing → "Alpha" → Create new release.**
   Use the track named **Alpha**. A duplicate track named "TouchGrassTrack" exists —
   ignore it.
2. Upload `app-release.aab`. On the first upload, **opt in to Play App Signing**.
3. Release name: `<versionName> (<versionCode>)`, e.g. `0.1.2 (3)`.
4. Paste release notes (mirror the `CHANGELOG.md` entry).
5. Next → resolve warnings → **Send for review**. First review typically 2–7 days;
   accessibility apps draw extra scrutiny.
6. After approval, **Publishing overview → Publish changes** (managed publishing).
7. Watch the **Pre-launch report** for device-class crashes.

### Testers — the actual bottleneck

Being a member of the Google Group is **not** the same as opting in. Each tester
must personally:

1. Be on the tester list under the Google account they use for Play.
2. Open the opt-in link signed in with that same account and tap **Accept invitation**.
3. Install Touchgrass from the Play link that appears.
4. Stay installed and opted in for the full 14 days, opening it occasionally —
   Google evaluates engagement, not just installs.

Get the link from the Alpha track → Testers tab → "How testers join your test".
Recruiting sources and message templates: `marketing/TESTER_RECRUITING.md`. Avoid
paid bot-tester services — Google reviews engagement quality when you apply for
production, and bots sink the application.

Disclose the known beta gap to testers:

> Heads up for this test build: the optional "commitment lock" email feature isn't
> wired to a live mail server yet, so its code won't arrive by email during the
> beta — everything else is fully functional.

---

## 4 · Driving Play Console with a browser agent

The Cowork/terminal session cannot click through Play Console. Hand the block below
to the Chrome extension, then paste its report back.

```text
You are in Google Play Console for the app "Touchgrass: Block Reels & Shorts"
(package com.touchgrassinc.app). Do these steps and REPORT what you see.
Do NOT submit, roll out, publish, or start any release.

HARD RULES
- Never invent an answer on the Data safety form or the Accessibility declaration.
  They are legal declarations. If a value was not given to you, STOP and ask.
- Do not pay for anything, change billing, or delete anything.
- On identity verification, payment, OTP, or CAPTCHA: STOP and hand back control.
- After each step, say what you did and paste any URL the console shows.

PART A — report current state, quoting exact labels:
1. Publishing overview / Dashboard: list every task still incomplete or erroring.
2. Test and release -> Testing -> Closed testing -> "Alpha": is there a release?
   which version? what does it say is required before I can create a new one?
   Quote any error text.
3. Grow -> Store presence -> Main store listing: which fields are filled vs empty?

PART B — testers (safe, reversible; do NOT start a rollout):
4. Closed testing -> Testers tab: report how many testers are currently OPTED IN
   (not just listed / not just group members).
5. Copy the "How testers join your test" opt-in link, or say it is unavailable.

PART C — report back verbatim: incomplete tasks, opted-in tester count, the opt-in
link, and anything blocking a new release.
```

---

## 5 · Before production launch

Not blocking either track today; all of it blocks a real production release.

- Register `gettouchgrass.app` (HSTS-preloaded, needs a valid cert day one) and
  `gettouchgrass.com`; 301 the `.com` to the `.app`.
- Verify the sending domain in Resend (DKIM + SPF + DMARC) so the commitment-lock
  OTP actually delivers. Put `RESEND_API_KEY` / `RESEND_FROM_EMAIL` in gitignored
  `gradle.properties` and in GitHub Secrets.
- Set up `privacy@`, `security@`, `conduct@` forwards so the addresses in
  `docs/PRIVACY.md`, `SECURITY.md`, and `CODE_OF_CONDUCT.md` reach a human.
- Swap every `github.io` URL to `gettouchgrass.app` — and rebuild.
  **Note the direction:** `PRIVACY_URL` in `TrustDashboardScreen.kt` currently points
  at `gettouchgrass.app/privacy`, which does **not resolve**. Until the domain is
  registered that constant must point at the live GitHub Pages URL — see `STATUS.md`.
- Refresh detector view-ID hints if Instagram or TikTok shipped an update during
  beta (`accessibility/detectors/*Detector.kt`).

---

## 6 · Definition of done for V1

Mirrors `docs/SPEC.md` §14, updated for the free-at-launch decision.

- All target apps blocked reliably on a fresh Pixel **and** a fresh Xiaomi.
- 48-hour soak passes on the Xiaomi.
- All seven OEMs (Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, Huawei) have an
  onboarding battery walkthrough.
- Trust Dashboard reviewed and confirmed accurate.
- No third-party analytics SDKs in the release build. No ads.
- Privacy policy live and linked from both the app and the store listing.
- All P0/P1 beta bugs closed.
- Repo public, CI green, release signed and reproducible.
- Resend verified and OTP delivery tested end to end with a real release build.
