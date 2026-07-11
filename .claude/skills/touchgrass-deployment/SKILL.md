---
name: touchgrass-deployment
description: >-
  Use when deploying, releasing, or publishing the Touchgrass Android app to
  Google Play — building or signing the release AAB, configuring Play Console
  (testing tracks, App content, Data safety, accessibility / foreground-service
  declarations, store listing), or answering any question about the package name,
  version code, signing keys, privacy URL, or what is still blocking release.
---

# Touchgrass deployment (Google Play)

Single source of truth for shipping Touchgrass. Every value here is grounded in the
repo (`app/build.gradle.kts`, `AndroidManifest.xml`, the release docs). When in doubt,
re-verify against those files — and update this skill if a value changes.

**Last verified: 2026-06-04.** Longer prose lives in `docs/PLAY_CONSOLE_RUNBOOK.md`,
`docs/RELEASE_CHECKLIST.md`, and `docs/BROWSER_AGENT_PLAY_PROMPT.md` — this skill is the
condensed, fast-recall version. **To resume a deployment already in progress, jump straight
to "Play Console — live checkpoint" near the bottom.**

## Immutable facts (do not "fix" these)

| Field | Value | Note |
|---|---|---|
| Play app name | `Touchgrass: Block Reels & Shorts` | |
| **applicationId (package)** | **`com.touchgrassinc.app`** | **PERMANENT.** `com.touchgrass.app` was taken. AAB must match or upload is rejected. |
| Kotlin `namespace` | `com.touchgrass.app` | Independent of applicationId — **do NOT move sources** to match the package. |
| versionName / versionCode | `0.1.0` / `1` | Release name shown as `0.1.0 (1)`. Bump `versionCode` every upload. |
| Category | `Productivity` | NOT Health & Fitness (contradicts Data safety), NOT Tools. |
| minSdk / target / compile | 29 / 35 / 35 | |
| Contact email | `nikodeveloper23@gmail.com` | Swap to `privacy@gettouchgrass.app` once that domain is live. |
| Source repo | `https://github.com/nikolozturkishvili1/Touchgrass` | Public, GPL-3.0, branch `main`. |
| Privacy URL (LIVE) | `https://nikolozturkishvili1.github.io/Touchgrass/privacy.html` | `#data-deletion` anchor doubles as the Data-safety deletion URL. |
| Landing (LIVE) | `https://nikolozturkishvili1.github.io/Touchgrass/` | |

## Build the signed AAB

No `java`/`keytool` on PATH — use Android Studio's bundled JBR (OpenJDK 21). From repo root:

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew bundleRelease
# -> app/build/outputs/bundle/release/app-release.aab  (signed, ~4 MB)
```

Verify the build before uploading: open `app/build/outputs/apk/.../output-metadata.json`
(or the bundle metadata) and confirm `com.touchgrassinc.app`, versionCode `1`.

## Signing & secrets — security rules

- Signing reads `keystore.properties` (gitignored) → `keysets/touchgrass-upload.jks` (gitignored).
  Upload key CN=Nikoloz Turkishvili, O=Touchgrass.
- **Back up the `.jks` + passwords in 2+ places.** The upload key is recoverable via Play support;
  losing it still means pain. On upload, **opt in to Play App Signing** (Google holds the real key).
- **NEVER** commit `keystore.properties`, `*.jks`, `local.properties`, or the `.aab`.
  **NEVER** print the keystore base64, signing passwords, or `$GITHUB_PAT` into the transcript.
- If `keystore.properties` is absent, `release` builds stay **unsigned and fail at packaging** by
  design (see `app/build.gradle.kts` `hasReleaseSigning`) — the missing file is the signal, not silent debug-signing.
- CI (`release.yml`, triggered by a `v*` tag) writes `keystore.properties` at runtime from GitHub
  Secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` (+ `PLAY_SERVICE_ACCOUNT_JSON`,
  `RESEND_API_KEY`, `RESEND_FROM_EMAIL`). **Not set yet — do NOT push a `v*` tag until they are**, or the run fails by design.

## Which testing track

| | Internal testing | Closed testing |
|---|---|---|
| Finishable today | ✅ 0 errors | ❌ needs screenshots (+ likely a demo video) |
| Full store listing / screenshots | not required | **required** (icon, feature graphic, ≥2 phone shots) |
| Country selection, permission declarations | relaxed | **required** |
| Counts toward Production access | ❌ no | ✅ yes — the **12 testers × 14 consecutive days** gate |

New personal dev accounts MUST run Closed testing (≥12 testers, 14 days — Google lowered this from 20 on 2024-12-11; recruit ~15 for a drop-off buffer) before applying for
Production. **Recommended path:** Internal testing now (immediate dogfood), Closed testing the
moment screenshots exist (starts the clock). Declarations below are app-level — filling them helps both tracks.

## Play Console — App content answers

- **Privacy policy:** the live GitHub Pages privacy URL above.
- **Ads:** No, does not contain ads.
- **App access:** All functionality available without special access (commitment lock is optional, no login wall).
- **Content rating:** start questionnaire → category Utility/Productivity → No to every violence/sexual/drugs/gambling question → expect **Everyone**.
- **Target audience:** 18+ only; "appeals to children?" → No (avoids Families program).
- **News app / COVID-19 / Government app / Financial features:** No. **Health apps:** none.
- **Advertising ID:** No, does not use one.

## Data safety (legal — use verbatim)

- Collects user data? **Yes.** Exactly ONE type: **Personal info → Email address.**
  - Collected: Yes · Shared: No · Ephemeral: No
  - Optional ("users can choose") — only if they enable the commitment lock.
  - Purpose: **App functionality** only (sends the one-time code).
  - Linked to identity: No · Used for tracking: No
- **No other data type.** No location, contacts, photos, files, messages, device IDs, app activity, analytics, or crash logs.
- Security: encrypted in transit **Yes**; user can request deletion **Yes** (disable lock / uninstall — point at the `#data-deletion` URL).

## Accessibility declaration (gets scrutinized)

App uses `AccessibilityService` (`BIND_ACCESSIBILITY_SERVICE`), strictly scoped in
`accessibility_service_config.xml`. Paste verbatim:

- **What it does:** "Touchgrass detects when the user opens short-form-feed UI (Reels, Shorts, TikTok, Spotlight) in apps they explicitly added to a block list, and intercepts back to exit the feed. The AccessibilityService is the only Android API that can both observe foreground-app UI events on third-party apps and inject a back gesture without invasive permissions."
- **Why no other API:** "Android does not expose foreground-app UI events to third-party apps through any other API. Usage Stats only reports aggregate time, not which screen is open. No other API can perform GLOBAL_ACTION_BACK to intercept a feed."
- **User disclosure:** "The in-app Trust Dashboard is shown during onboarding, before Accessibility is requested. The on-device accessibility-service description string also explains the exact scope."
- **Open-source proof:** the repo URL above.
- May require a **demo video URL** (screen recording: open a feed → it gets blocked → Trust Dashboard). We don't have one — if asked, STOP and record one (unlisted YouTube). Do not invent a URL.

## Foreground Service declaration

App content → Foreground service permissions → **Yes**, type = **Special Use**
(`FOREGROUND_SERVICE_SPECIAL_USE`, subtype `touchgrass_reliability_watchdog`). Justification:

> Touchgrass runs a lightweight foreground service as a reliability watchdog: it keeps the
> AccessibilityService alive and re-arms it if the OS or an aggressive OEM battery manager kills it.
> The app's only function — blocking short-form feeds the instant they open — fails silently if the
> service stops. No standard FGS type fits (no media, location, sync, camera, mic, call, or
> connectivity). It performs no network or data transfer; it only monitors and restarts the app's own accessibility component.

## Store listing

- **Short description (≤80):** `Block doomscrolling on Reels, Shorts & TikTok. No ads. No subscription. Free.`
- **Full description:** the long block in `docs/BROWSER_AGENT_PLAY_PROMPT.md` / `marketing/play-store-listing.md`. The privacy line must use the **live github.io URL**, not `gettouchgrass.app` (not registered).
- **Assets:** icon `marketing/play-assets/icon-512.png`, feature graphic `marketing/play-assets/feature-1024x500.png`.
- **Countries:** All.

## Play Console — live checkpoint (as of 2026-06-04, audited against the repo)

**Account:** personal dev account. **Play app:** `Touchgrass: Block Reels & Shorts`
(`com.touchgrassinc.app`) — dashboard URL pattern
`play.google.com/console/u/2/developers/7718739663113484769/app/4972046019723284647/...`.

### Done / verified ✓
- Build is releasable: `versionCode 1`, `versionName 0.1.0`, signs locally (`keystore.properties` present on disk, gitignored).
- Play assets exist: `marketing/play-assets/icon-512.png` (512×512) + `feature-1024x500.png` (1024×500).
- Privacy + landing pages LIVE; `…/privacy.html#data-deletion` anchor confirmed present in the raw HTML.
- Store-listing copy written (`marketing/play-store-listing.md`); App content / Data safety / declaration answers all drafted above.
- CI: `android-ci.yml` (push/PR to main) green; `release.yml` (`v*` tag) builds a signed AAB + a draft GitHub Release.

### Blockers (ordered) ✗
1. **Phone screenshots — ZERO exist** (verified: the only raster files in the entire repo are the icon + feature graphic). Play needs **≥2** portrait 1080×1920; 8 are planned in `marketing/play-store-listing.md`. **Hard blocker for Closed/Production — must be captured on a real device, cannot be generated here.** This gates everything else.
2. **First AAB must be uploaded by hand.** `release.yml`'s auto-upload step is intentionally commented out (no `PLAY_SERVICE_ACCOUNT_JSON`); for a brand-new app the very first bundle must be uploaded manually in Play Console before the API will accept later ones.
3. **Release secrets unverified.** `release.yml` needs `KEYSTORE_BASE64 / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD` in GitHub → Settings → Secrets. Confirm present before pushing a `v*` tag (it fails loudly without them, by design). No `v*` tag pushed yet.
4. **Demo video** likely required for the accessibility declaration — none yet.
5. **`gettouchgrass.app` + Resend** not set up → commitment-lock email won't send in beta (disclose to testers). Swap github.io → gettouchgrass.app and rebuild before Production.

### Exact next clicks (resume point)
1. **One device session: record the accessibility video + capture the 8 screenshots** — follow `docs/DEVICE_CAPTURE_SESSION.md`. Drop screenshots into `marketing/play-assets/`; upload the video unlisted to YouTube and paste its URL into App content → Accessibility services → Prominent disclosure.
2. Play Console → **Grow → Store presence → Main store listing** → upload icon, feature graphic, screenshots; paste the short + full description.
3. **Test and release → Testing → Closed testing → Testers tab** → create an email list (or link a Google Group like `touchgrass-testers@googlegroups.com` so testers self-join). Recruiting can start now, in parallel with screenshots.
4. Locally `./gradlew bundleRelease` → upload `app-release.aab` to Closed testing → **opt in to Play App Signing** → release name `0.1.0 (1)`.
5. Complete **App content** (privacy URL, Data safety, accessibility + foreground-service declarations — all answers are in the sections above).
6. **Start rollout to Closed testing**, share the opt-in link with **≥12 testers** (recruit ~15). The 14-consecutive-day clock starts once they're opted in on a live release.

> **Testing-track requirement (current):** **12 testers** opted in for **14 consecutive days** before a new
> personal account can apply for production access. Google lowered it from 20 on **2024-12-11**.

## Browser-agent prompt (paste into the Chrome Claude extension)

This terminal Claude has **no browser access** — the Chrome Claude *extension* does the clicking. Hand it the
block below to inspect the console and set up the tester list, then paste its report back here to continue.

```text
You are in Google Play Console for the app "Touchgrass: Block Reels & Shorts"
(package com.touchgrassinc.app). Do these steps and REPORT what you see.
Do NOT submit, roll out, publish, or start any release.

PART A — report current state (quote exact labels):
1. Open "Publishing overview" (or Dashboard). List every task still marked
   incomplete / showing an error or a red-grey dot.
2. Open "Test and release -> Testing -> Closed testing". Report: is there a
   Closed testing track? any release on it? what does it say is required before
   I can create a release? Quote any error text.
3. Open "Grow -> Store presence -> Main store listing". Report which fields are
   filled vs empty, and especially whether any phone screenshots are uploaded.

PART B — set up the tester list (safe, reversible; do NOT start a rollout):
4. In Closed testing -> Testers tab, create an email list named
   "Touchgrass beta testers" (leave it empty for now), OR report the field name
   if there is an option to link a Google Group.
5. Find the "How testers join your test" opt-in link. Copy the URL if shown, and
   say whether it is active or "not yet available".

PART C — report back verbatim: the incomplete tasks, whether a Closed testing
track + opt-in link now exist, and anything blocking me from creating a release.
```

## Common mistakes

- Changing `namespace` to `com.touchgrassinc.app` to "match" the package — **wrong**; namespace ≠ applicationId, leave it `com.touchgrass.app`.
- Forgetting to bump `versionCode` on a re-upload (Play rejects duplicate codes).
- Picking Health & Fitness category (contradicts the "not a health app" Data safety answer).
- Committing the keystore/AAB, or echoing secrets into the transcript.
- Treating Internal testing as a path to public launch — it doesn't count toward the 20×14 gate.

## Note on how this skill was made

This is a project **reference** skill, grounded in the repo's authoritative files rather than
validated by the subagent pressure-test loop (that loop is for discipline/behavior skills, not a
private deployment reference). Keep it accurate by editing it whenever a value in the tables above changes.
