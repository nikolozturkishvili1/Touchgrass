# Touchgrass — Android App Build Specification

> **Audience:** Claude Code CLI, working with the solo developer (a .NET developer learning Kotlin + Jetpack Compose).
>
> **Mission:** Build and ship the V1 of Touchgrass — an Android app that reliably blocks Reels, Shorts, and other short-form infinite-scroll content. The wedge against the incumbent (NoScroll) is **reliability, trust, and zero ads**.
>
> **Tone for Claude Code:** Act as a senior Android engineer pairing with a developer who is strong in C#/.NET but new to Android. Explain Android-specific concepts the first time they appear. Prefer idiomatic modern Kotlin (coroutines, Flow, sealed classes). Default to Jetpack Compose for all UI.

---

## 0. Locked Project Decisions

These are settled. Do not relitigate without explicit developer instruction.

| Decision | Value |
|---|---|
| Product name | **Touchgrass** |
| Play Store title | `Touchgrass: Block Reels & Shorts` |
| Primary domain | **gettouchgrass.app** (HTTPS-enforced TLD) |
| Secondary domain | **gettouchgrass.com** (301-redirects to .app) |
| Platform priority | Android first, iOS later (V2, via Family Controls + browser extension) |
| Min Android SDK | API 29 (Android 10) |
| Target Android SDK | API 35 (Android 15) |
| Language | Kotlin 2.x |
| UI | Jetpack Compose (Material 3) |
| Source control & CI | **GitHub** (public repo, `Touchgrass`) |
| License | **GPL-3.0** (protects open-source trust positioning; prevents predatory forks) |
| Monetization | Free tier covers core blocking; **Touchgrass Plus** is one-time $9.99 for extras. No subscription. No ads. Ever. |
| Distribution | Google Play primary; F-Droid secondary (later) |

---

## 1. Product Vision

Touchgrass is the **anti-doomscroll app that actually works.** It blocks short-form infinite-scroll content (Reels, Shorts, TikTok feed, Spotlight, FB Reels) on Android, while letting users keep using the rest of those apps normally.

### Positioning statement

> *For people who can't stop doomscrolling, Touchgrass is a reel & shorts blocker that — unlike NoScroll, AppBlock, Opal — actually keeps working every day, doesn't watch your screen creepily, and never shows you an ad. It's the dignified way to take your brain back.*

### Brand personality

- **Honest, slightly self-aware, anti-corporate.** The name "Touchgrass" is a wink. We are not Calm-app earnest.
- **Trust-first.** We surface our permissions and what we do/don't see, in plain English.
- **Quietly competent.** No gimmicks, no streaks, no gamification yet. Just: the block works.
- Voice references: Bear app, Things 3, Mullvad VPN. (Calm minimalism, clear copy, no growth hacks.)

### Why now / market timing

- NoScroll (top competitor on Play Store) shipped a bad update in April-May 2026, has rolled back, users hemorrhaging trust. Reviews show defecting users actively looking for alternatives.
- Recurring complaints in NoScroll reviews across 2024–2026 about reliability ("stops working after 2-4 days"), background service death, intrusive ads, and rising prices — all *fixable* product decisions.
- iOS competition (e.g. ScrollGuard) is weak because Apple's APIs are restrictive.

---

## 2. Market Research — Synthesized Findings

These findings come from analysis of ~60 NoScroll reviews (Google Play, 2024–2026) and competitor scan of ScrollGuard (iOS). The build choices below trace back to these findings.

### 2.1 Top user complaints across competitors (ranked by frequency × severity)

| # | Complaint | Frequency | Source examples |
|---|---|---|---|
| 1 | Background service dies after 2–4 days; needs reinstall or toggling accessibility off/on | Very high | Amey (19 helpful), Vee (55 helpful), Uriel, Pavan |
| 2 | Spam-tap / fast-tap bypasses the block | High | William H., Eric B. |
| 3 | Facebook Reels / IG Lite / DM-shared reels bypass the block | High | Haneen, Saransh, Emma |
| 4 | Privacy fear — "this 3rd party is watching my whole screen always" | High | Prallad (27 helpful), Vee (55 helpful) |
| 5 | Battery drain and phone slowdown | Medium-high | pogue (36 helpful), Prallad |
| 6 | Forced ads after updates, sketchy/sexual ads slipping through | Medium-high | Jed, Ryuuyaki, Elizabeth |
| 7 | Subscription creep, price hikes (8 → 80) | Medium | Adam |
| 8 | Keyboard / text-input interference from accessibility service | Medium (specific OEMs) | Cole, Kara |
| 9 | Can't watch a single reel sent by a friend without disabling block | High demand | Stephanie, Emma, Ruby |
| 10 | No browser blocking (Chrome / Samsung Internet) | Medium | Lillian (56 helpful) |

### 2.2 What users love (do NOT break these)

- **One-time payment, no subscription** (Jan, 26 helpful)
- **Free tier that actually works** (Jan, Faith, Melek)
- **Minimalist UI**, easy setup (Jan, Emy)
- **Commitment locks** — email OTP to disable saved users from impulse-disabling (Paulo: "saved my life")
- **No data collection** reputation (thunder storm, Melek)
- **Real impact** — many reviews of the form "went from 8h screen time to 2h"

### 2.3 Strategic implications → product decisions

| Insight | Decision in Touchgrass V1 |
|---|---|
| Background service reliability is *the* unsolved problem | **Engineering investment #1:** per-OEM battery whitelist onboarding, self-healing watchdog, foreground service with persistent notification, work manager fallback |
| Spam-tap bypasses exist | Block input with a debounce + overlay (not just back-press) |
| Facebook Reels and DM-reels bypass | Day-1 support for: YouTube Shorts, Instagram Reels (feed + Explore + DM), TikTok For You, Facebook Reels, Snapchat Spotlight |
| Privacy fear is the install-killer | **Trust Dashboard** in-app showing exactly what we access. Open-source the app. Privacy policy in plain English. No analytics SDKs in V1. |
| Forced ads destroy trust | **No ads. Ever.** Hard-coded into our brand. |
| Subscription creep destroys trust | One-time premium price. No subscription option at launch. |
| Watch-one-reel from DM is requested constantly | **"Quick Peek" mode** — allow one reel, block scroll to next (V1 feature) |
| Browser blocking is missing | Chrome + Samsung Internet support via accessibility service |
| Commitment locks have huge emotional value | **Make commitment lock FREE** — Paulo's "saved my life" review shows this should not be paywalled |

---

## 3. Product Scope — V1

### 3.1 V1 Feature Set (ships in 4–8 weeks)

**A. Core blocking**

- Detect when the user is in a short-form feed inside any supported app and either (a) immediately exit the feed, (b) show a full-screen overlay with a calming message, or (c) allow one item and then block (Quick Peek mode — see §6).
- Supported targets on day 1:
  - YouTube Shorts
  - Instagram Reels (main feed, Explore reels grid, Reels tab, DM-shared reels)
  - TikTok "For You" feed
  - Facebook Reels (FB main app)
  - Snapchat Spotlight
  - YouTube Shorts on Chrome (web)
  - Samsung Internet
- Hard requirements:
  - Spam-tap-proof (debounce, no re-entry within 1.5s)
  - Survives DM-share, share-sheet entry, deep link entry
  - Works on Android 10+ (API 29+)

**B. Reliability layer (the wedge)**

- Foreground service with a persistent (but unobtrusive) notification.
- Health-check watchdog: every N minutes, verify the AccessibilityService is bound and responsive. If not, fire a high-priority local notification: "Touchgrass stopped working — tap to re-enable."
- Per-OEM onboarding: detect manufacturer (Xiaomi, Samsung, OnePlus, Realme, Oppo, Vivo, Huawei, Honor) and walk user through *their specific* battery-optimization opt-out screens with annotated screenshots.
- WorkManager periodic job as a belt-and-braces re-check.
- Restart-on-boot receiver.

**C. Trust layer**

- **Trust Dashboard** screen, accessible from settings, explaining in plain English:
  - "What Touchgrass can see: which app is in the foreground and elements on screen of the apps you ask us to block"
  - "What Touchgrass cannot see: your messages, your passwords, your photos, your keyboard input"
  - "What Touchgrass sends: nothing. All data stays on your phone."
  - "Open source: github.com/nikolozturkishvili1/Touchgrass"
- Privacy policy written in plain English.
- No third-party analytics SDKs in V1. Not Firebase Analytics, not Mixpanel, not Amplitude. (Crash reporting via a privacy-respecting tool like Sentry is OK if disclosed in the Trust Dashboard.)

**D. Pause & friction**

- Pause button (default: not visible — user enables in settings) with configurable wait time (5s / 30s / 5m / 30m).
- Daily pause budget cap (e.g., max 20 min/day total pause).
- Optional friction modes on disable: typing a randomly-generated 30-character code, solving a math problem, breathing for 30s.
- **Commitment lock (FREE):** when enabled, disabling the app requires an email OTP. This is the Paulo feature — keep it free.

**E. Quick Peek mode**

- User-toggleable per-app. When on, opening a reel/short from a DM or share-sheet allows watching the single item but blocks the scroll-to-next gesture.
- Implementation: detect entry via deep link / share, allow one feed item, then intercept scroll.

**F. Stats screen**

- Today / This week / All time.
- Blocks count ("Touchgrass stopped 47 doomscroll attempts this week").
- Estimated time saved (based on average session time).
- No streaks, no badges, no gamification in V1.

**G. Onboarding**

- 4-screen flow:
  1. Welcome + value prop ("Block reels, shorts, and infinite scroll. For real this time.")
  2. Grant accessibility permission, with a clear "why we need this" + link to Trust Dashboard
  3. Disable battery optimization, **with OEM-specific instructions**
  4. Pick which apps to block (default: all supported, all on)
- After onboarding, single-screen home: big toggle, today's block count, quick access to settings.

### 3.2 Explicitly OUT of V1 scope

- iOS version (V2)
- Whole-app blocking with time limits (defer — focus = reels only)
- Multiple profiles / schedules (V1.1)
- Social / accountability partner features
- AI features
- Streaks and gamification
- Detailed per-app analytics dashboards
- Localization beyond English (V1.1 — Spanish, Portuguese, Hindi)

### 3.3 Monetization (V1)

- **Free tier:** all core blocking, Trust Dashboard, basic stats, commitment lock (email OTP).
- **Touchgrass Plus (one-time $9.99):** Quick Peek mode, advanced friction modes (math/breathing), themes, weekly recap PDF, multiple block profiles, detailed stats.
- **No subscription. No ads. No upsells inside the app beyond a single non-intrusive "Upgrade" entry in settings.**
- Use Google Play Billing for one-time purchase.

---

## 4. Technical Architecture

### 4.1 Stack

| Layer | Choice | Why |
|---|---|---|
| Language | Kotlin 2.0+ | Idiomatic Android, clean coroutines |
| UI | Jetpack Compose (latest stable) | Declarative, similar mental model to modern React/SwiftUI; faster iteration |
| Min SDK | API 29 (Android 10) | Covers ~95% of active devices; Accessibility API matured here |
| Target SDK | API 35 (Android 15) | Required by Play Store |
| Architecture | MVVM + Repository, single-activity | Standard, easy to maintain solo |
| DI | Hilt | Google's recommended DI for Android |
| Async | Coroutines + Flow | Idiomatic Kotlin |
| Persistence | Room (for stats) + DataStore (for preferences) | Standard |
| Build | Gradle Kotlin DSL, version catalog (`libs.versions.toml`) | Modern best practice |
| CI | GitHub Actions | Free for public repos, easy |
| Crash reporting | Sentry (optional, disclosed) OR none | Privacy-first |

### 4.2 Module structure

Single-module to start (simpler for solo dev). Refactor to multi-module if/when it grows.

```
app/
├── src/main/
│   ├── kotlin/com/touchgrass/app/
│   │   ├── MainActivity.kt
│   │   ├── TouchgrassApplication.kt        // @HiltAndroidApp
│   │   ├── ui/
│   │   │   ├── theme/                       // Compose theme, colors, typography
│   │   │   ├── onboarding/
│   │   │   ├── home/
│   │   │   ├── settings/
│   │   │   ├── trust/                       // Trust Dashboard
│   │   │   ├── stats/
│   │   │   └── components/                  // Reusable Composables
│   │   ├── accessibility/
│   │   │   ├── TouchgrassAccessibilityService.kt
│   │   │   ├── detectors/                   // One detector per supported app
│   │   │   │   ├── YouTubeShortsDetector.kt
│   │   │   │   ├── InstagramReelsDetector.kt
│   │   │   │   ├── TikTokDetector.kt
│   │   │   │   ├── FacebookReelsDetector.kt
│   │   │   │   ├── SnapchatSpotlightDetector.kt
│   │   │   │   └── BrowserShortsDetector.kt
│   │   │   ├── BlockingStrategy.kt
│   │   │   └── EventDebouncer.kt
│   │   ├── service/
│   │   │   ├── TouchgrassForegroundService.kt
│   │   │   ├── WatchdogWorker.kt           // WorkManager periodic check
│   │   │   └── BootReceiver.kt
│   │   ├── data/
│   │   │   ├── local/                       // Room DAOs, entities, DataStore
│   │   │   ├── repository/
│   │   │   └── model/
│   │   ├── domain/                          // Use cases
│   │   ├── billing/                         // Play Billing wrapper
│   │   ├── oem/                             // OEM detection + battery instructions
│   │   │   ├── OemDetector.kt
│   │   │   └── instructions/                // JSON/data for each OEM
│   │   ├── lock/                            // Commitment lock, email OTP
│   │   └── util/
│   ├── res/
│   │   ├── xml/accessibility_service_config.xml
│   │   └── ...
│   └── AndroidManifest.xml
└── build.gradle.kts
```

### 4.3 Critical Android components — concept notes for a .NET dev

> Claude Code: when these come up for the first time, briefly explain in C# terms. Examples below.

- **AccessibilityService:** A background system service Android starts on your behalf when the user grants the Accessibility permission. It receives `AccessibilityEvent` objects describing UI changes in *other* apps (focus changes, window changes, view scrolled, content text). Think of it as a global event bus the OS pushes UI events into. **You do not poll; the OS pushes.** This is the heart of Touchgrass. C# analogy: like a system-wide UI Automation listener.
- **Foreground Service:** A long-running background process that the OS won't kill arbitrarily, identified by a persistent notification. Required on modern Android for anything user-facing that needs to run when the app isn't in front. C# analogy: a Windows Service with a tray icon.
- **WorkManager:** Android's official scheduler for deferrable background work that survives reboots and process death. Use for the watchdog re-check. C# analogy: Hangfire / Quartz.NET.
- **Compose:** Declarative UI. State changes → UI re-composes. C# analogy: closest to MAUI's `[ObservableProperty]` + XAML data binding, but functional and code-only.
- **Hilt:** DI with annotations. `@Inject` constructor injection, `@HiltViewModel`, `@AndroidEntryPoint`. C# analogy: `Microsoft.Extensions.DependencyInjection` with attribute-driven registration.
- **Room:** SQLite ORM with annotations. `@Entity`, `@Dao`, `@Query`. C# analogy: EF Core, lighter.
- **DataStore:** Modern key-value preferences store (replacement for SharedPreferences). C# analogy: `IConfiguration` + JSON file.
- **Flow / StateFlow:** Cold/hot reactive streams. C# analogy: `IAsyncEnumerable<T>` / `IObservable<T>`.
- **Coroutines:** Lightweight threads, `suspend` functions. C# analogy: `async/await` with `Task`.

### 4.4 AccessibilityService — the engineering heart of the app

Configuration (`res/xml/accessibility_service_config.xml`):

- `accessibilityEventTypes`: `typeWindowStateChanged | typeWindowContentChanged | typeViewScrolled | typeViewFocused`
- `accessibilityFeedbackType`: `feedbackGeneric`
- `notificationTimeout`: 100ms
- `packageNames`: comma-separated list of supported packages (youtube, instagram, tiktok, facebook, snapchat, chrome, samsung-browser, etc.). **Scope down to only these packages — never observe the whole device. Critical for the Trust Dashboard claim.**
- `canRetrieveWindowContent`: true
- `description`: clear English explanation Android will show in settings

Detection strategy per app (each `Detector` is an isolated class):

- Observe `event.packageName` to know which app is foreground.
- Observe `event.className` and view IDs to know which screen.
- Use `findAccessibilityNodeInfosByViewId()` or content-description heuristics to detect Reels/Shorts UI.
- When detected → call `BlockingStrategy.applyBlock(reason)`.

`BlockingStrategy`:

- Strategy 1 (default): `performGlobalAction(GLOBAL_ACTION_BACK)` to exit the feed.
- Strategy 2 (overlay): Show a full-screen `TYPE_ACCESSIBILITY_OVERLAY` window with a calming message. (Requires careful permission handling.)
- Strategy 3 (Quick Peek): allow one item, then intercept the next scroll event by consuming it. (Trickier — V1 stretch.)

`EventDebouncer`:

- Prevents spam-tap bypass. After a successful block, ignore the source for 1.5–2s.
- Per-package and per-action debouncing.

### 4.5 OEM battery optimization handling

Maintain a JSON resource of OEM → instruction screens. Example data shape:

```json
{
  "xiaomi": {
    "manufacturer_match": ["Xiaomi", "Redmi", "POCO"],
    "steps": [
      { "title": "Open Security app", "screenshot": "xiaomi_step1.png" },
      { "title": "Battery → App battery saver", "screenshot": "xiaomi_step2.png" },
      { "title": "Find Touchgrass → No restrictions", "screenshot": "xiaomi_step3.png" }
    ],
    "deep_link_intent": "miui.intent.action.POWER_HIDE_MODE_APP_LIST"
  },
  "samsung": { },
  "oneplus": { },
  "oppo": { },
  "vivo": { },
  "realme": { },
  "huawei": { }
}
```

Detect via `Build.MANUFACTURER`. Show the right walkthrough in onboarding step 3.

### 4.6 Watchdog mechanism

- A `WatchdogWorker` (WorkManager) runs every 15 min.
- It checks: is `TouchgrassAccessibilityService` enabled in `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`? Is our foreground service alive? Has the service handled an event in the last X hours during active hours?
- If unhealthy: post a high-importance notification: "Touchgrass paused — tap to fix." Tapping deep-links to the relevant Settings screen.
- Also a heartbeat: the AccessibilityService writes a timestamp to DataStore on every event. The Watchdog reads this and decides health.

---

## 5. Visual Design Direction

### 5.1 Design principles

1. **Calm over clinical.** Warm, natural palette (mosses, off-whites, soft sand). Not the hospital-blue typical of "wellness" apps.
2. **Generous whitespace.** Single-purpose screens.
3. **Friendly typography.** Inter or DM Sans for UI; consider a slightly playful display font for the brand mark (e.g., Fraunces) — used sparingly.
4. **No icons-as-decoration.** Every icon means something. Material 3 outlined icons by default.
5. **Dark mode equal-quality with light mode.**
6. **Microcopy is the design.** Every empty state, error, and confirmation is written with brand voice. ("Nothing to scroll. Go look out the window.")

### 5.2 Color palette (starting point — finalize during design)

- **Background light:** `#FAF8F3` (warm off-white)
- **Background dark:** `#1A1C18` (warm near-black)
- **Primary:** `#3E5E3A` (deep moss green)
- **Accent:** `#C9D5A4` (pale sage)
- **Danger / block:** `#A24E3E` (terracotta — never red)
- **Text primary light:** `#1F231C`
- **Text secondary:** `#5C645A`

### 5.3 Naming the experience

- App name: **Touchgrass**
- Play Store subtitle: **Block Reels, Shorts & TikTok**
- App icon concept: a single blade of grass, minimalist, on the moss-green primary background. Should read at 48dp.
- Internal terminology to use in UI:
  - The kill action is called a "block" or "save" (e.g., "247 saves this week")
  - Pauses are called "peeks"
  - The locked state is "Touchgrass is on" / "Touchgrass is off"

---

## 6. User Flows

### 6.1 First-run onboarding

```
[Splash] → [Welcome screen] → [Accessibility explainer + grant CTA] →
[OS Accessibility Settings — user grants] → [Detect we're enabled] →
[Battery optimization — OEM-specific walkthrough] → [Pick apps to block] →
[Done — home screen]
```

Edge cases to handle:

- User denies accessibility → friendly screen explaining why we can't work without it, single-button retry.
- User backs out of OEM battery settings without disabling → next time app opens, gentle reminder banner on home.

### 6.2 Daily use

- App is on. User opens Instagram normally — works fine.
- User taps Reels tab. Touchgrass detects, exits.
- User opens Instagram, friend sent a reel in DM. With Quick Peek on: user watches one reel, scroll-to-next is blocked.

### 6.3 Pause flow

- User opens Touchgrass, taps "Take a peek".
- If commitment lock is on: email OTP challenge first.
- Then friction (configurable): wait timer, math problem, or breathing.
- Then: a chosen duration (5min, 15min, 30min) — capped by daily budget.
- During pause: home screen shows countdown, notification shows "Touchgrass paused — 14:32 left".

### 6.4 Trust Dashboard

Accessible from Settings → "What can Touchgrass see?". A single scrollable screen with sections:

- "What we access" — bulleted plain English
- "What we don't" — bulleted plain English
- "Where your data lives" — "On your phone. Nowhere else."
- "Verify for yourself" — link to GitHub repo
- "Read our privacy policy" — link

---

## 7. Build Plan & Milestones

8-week realistic timeline for a solo dev learning Kotlin alongside building.

### Week 1–2: Learning + project setup

- Kotlin fundamentals (focused on differences from C#): null safety, data classes, sealed classes, extension functions, scope functions, coroutines/suspend.
- Jetpack Compose fundamentals: composables, state hoisting, `remember`, `LaunchedEffect`, basic layouts.
- Create the Android Studio project with the structure in §4.2. Wire up Hilt, Compose, Material 3, Room, DataStore.
- Build a stub home screen with a fake toggle.
- **Deliverable:** Empty app installs on device, opens, shows home screen, builds in CI.

### Week 3: Accessibility service skeleton

- Implement `TouchgrassAccessibilityService`. Configure `accessibility_service_config.xml`.
- Implement a single detector (YouTube Shorts) and the back-press blocking strategy.
- Manual test on real device.
- **Deliverable:** App can block YouTube Shorts when accessibility is enabled.

### Week 4: Reliability layer

- Foreground service, persistent notification, boot receiver.
- WatchdogWorker.
- OEM detection + 3 OEM walkthroughs (Samsung, Xiaomi, OnePlus — cover ~70% of users).
- **Deliverable:** Touchgrass survives a 48h soak test on a Xiaomi or Samsung device.

### Week 5: More detectors + onboarding UI

- Add detectors for Instagram Reels, TikTok, Facebook Reels, Snapchat Spotlight, Chrome (web shorts).
- Build the onboarding flow in Compose with the right screens.
- Build the Trust Dashboard screen.
- **Deliverable:** All 5 apps blocked reliably; full onboarding works end-to-end.

### Week 6: Pause, commitment lock, stats

- Pause flow with friction modes.
- Email OTP commitment lock (use a transactional email API like Resend or Postmark; cheapest tier).
- Stats screen with Room-backed event counts.
- **Deliverable:** Pause works, OTP works, stats display correctly.

### Week 7: Polish, Play Billing, store listing

- Implement Play Billing for one-time Touchgrass Plus.
- Wire up Quick Peek behind the paywall.
- Visual polish, microcopy pass, dark mode QA.
- Screenshots for Play Store.
- Write store listing (see §9).
- **Deliverable:** Internal-testing build uploaded to Play Console.

### Week 8: Beta + launch

- Closed beta with 20–50 testers (recruit from r/digitalminimalism, r/nosurf, friends).
- Bug-fix sprint based on feedback.
- Production release.
- **Deliverable:** Touchgrass is live on Play Store.

---

## 8. GitHub & CI/CD Setup

### 8.1 Repository structure

- **Repo name:** `Touchgrass`
- **Visibility:** Public (open source is a brand promise)
- **License:** GPL-3.0 (`LICENSE` file at root)
- **Default branch:** `main`
- **Branch protection on `main`:** require PR review (self-review fine for solo), require CI green, no direct push

### 8.2 Top-level files to create on day 1

- `README.md` — public-facing brand summary, screenshot, install link, build instructions, contributor pointer.
- `LICENSE` — GPL-3.0 full text.
- `CONTRIBUTING.md` — how to add a new app detector, code style, PR process.
- `PRIVACY.md` — plain English privacy policy. Linked from app and Play Store.
- `ARCHITECTURE.md` — short doc explaining the AccessibilityService → Detector → BlockingStrategy flow.
- `SECURITY.md` — responsible disclosure email.
- `CODE_OF_CONDUCT.md` — standard contributor covenant.
- `.gitignore` — Android Studio + Gradle template.
- `.editorconfig` — UTF-8, LF line endings, 4-space indentation for Kotlin.

### 8.3 GitHub Actions workflows

Create `.github/workflows/`:

**`android-ci.yml`** — runs on every PR and push to `main`:

- Set up JDK 17
- Cache Gradle
- Run `./gradlew detekt ktlintCheck`
- Run `./gradlew testDebugUnitTest`
- Run `./gradlew assembleDebug`
- Upload debug APK as workflow artifact

**`release.yml`** — runs on `v*` tag push:

- Build signed release AAB (keystore from GitHub Secrets)
- Run full test suite
- Upload AAB to Play Console internal track using `r0adkll/upload-google-play` or equivalent
- Create GitHub Release with changelog

### 8.4 Secrets to configure in GitHub Settings → Secrets

- `KEYSTORE_BASE64` — base64-encoded signing keystore
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `PLAY_SERVICE_ACCOUNT_JSON` — for Play Console upload
- `RESEND_API_KEY` (or equivalent) — for commitment-lock OTP service

### 8.5 Issues & project board

- Use GitHub Issues with labels: `bug`, `feature`, `oem`, `detector`, `accessibility`, `good-first-issue`.
- One GitHub Project board with columns: Backlog / This Week / In Progress / In Review / Done.
- Pin a "Help wanted: add a detector for X" issue once V1 ships — open-source community contribution opportunity.

### 8.6 Why GitHub, not Azure DevOps (for this project)

| Factor | Reason |
|---|---|
| Open source is a brand promise | GitHub stars are public credibility; Azure DevOps repos are invisible to the community |
| Discovery & social proof | Privacy-skeptical users check GitHub before installing |
| Android ecosystem | Every library, sample, and contributor expects GitHub |
| CI/CD maturity | Mature Android Actions for build, sign, deploy to Play; free public-repo minutes |
| Free tier sufficient | Free private repos, generous Actions minutes for solo dev |

The developer may keep using Azure Boards for personal planning if preferred — code and CI live on GitHub.

---

## 9. Play Store Listing (Draft)

**Title (50 char max):** `Touchgrass: Block Reels & Shorts`

**Short description (80 char max):** `Block doomscrolling on Reels, Shorts, TikTok. No ads. No subscription.`

**Long description (sketch — refine before launch):**

> **The reel blocker that actually works.**
>
> Touchgrass blocks the parts of Instagram, TikTok, YouTube, Facebook and Snapchat that are designed to keep you scrolling forever. You can still message friends, post, search, watch a video someone sends you. You just can't fall into the pit.
>
> **What's different about Touchgrass:**
>
> ✓ **Reliable.** No "stopped working after 3 days." Touchgrass has a watchdog that catches itself if it ever stops, and an OEM-specific setup that handles Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, Huawei battery killers correctly.
>
> ✓ **Honest about privacy.** Open the Trust Dashboard inside the app to see exactly what Touchgrass can and cannot see. We don't watch your whole screen. We don't read your messages. Nothing leaves your phone.
>
> ✓ **No ads. Ever.** We will never put an ad in this app.
>
> ✓ **No subscription.** The free tier blocks everything we promise. Touchgrass Plus is a one-time $9.99 if you want Quick Peek, themes, and weekly recaps.
>
> ✓ **Commitment lock is free.** Email-OTP yourself if you keep impulse-disabling.
>
> Blocks: YouTube Shorts • Instagram Reels (feed, Explore, DM, Reels tab) • TikTok For You • Facebook Reels • Snapchat Spotlight • Shorts in Chrome and Samsung Internet.
>
> Open source: github.com/nikolozturkishvili1/Touchgrass

**ASO keywords to target:** block reels, block shorts, block tiktok, stop scrolling, doomscroll, screen time, digital wellbeing, focus app, dopamine detox, scroll blocker, no scroll, instagram reels blocker, youtube shorts blocker, brainrot

---

## 10. Marketing & Launch Plan

Solo-dev, budget-light, organic-first.

### 10.1 Pre-launch

- Set up a landing page at **gettouchgrass.app** with email signup. Single page, brand voice, screenshot, "notify me at launch" button.
- Build in public: short Twitter/X thread once a week showing progress. Cross-post on Mastodon/Bluesky.
- Start a TikTok and an Instagram for Touchgrass. Post one short video per week with progress / dev-build clips. The meta-joke ("I'm building a TikTok blocker on TikTok") is the angle.

### 10.2 Launch day

- Submit to **Product Hunt** (Tuesday or Wednesday, 12:01am PT). Have the hunter and supporter list ready.
- Post on **r/digitalminimalism**, **r/nosurf**, **r/decidingtobetter**, **r/getdisciplined**, **r/dopaminedetoxing**, **r/productivity**, **r/Android** (different angle for each — Claude can help draft).
- Email the launch-list waitlist with a personal note + direct Play Store link.
- Submit to: Indie Hackers, Hacker News (Show HN), Reddit r/SideProject.

### 10.3 Post-launch (first 30 days)

- Daily reply to every Play Store review, in voice, no corporate-speak.
- Two TikTok/Reels per week. Tutorial videos, before/after screen-time clips, dev updates.
- Reach out to 5 digital-wellbeing newsletter writers and YouTubers per week with a personalized pitch and free Touchgrass Plus codes.
- Iterate the Play Store listing weekly based on Play Console search-term data.

### 10.4 Budget guidance

- **Months 1–3:** $0 ads. All organic. Costs: $25 Play Console dev fee (one-time), ~$45/yr domains (gettouchgrass.app + .com), ~$10/mo transactional email (Resend free tier may cover V1).
- **Month 4+:** If — and only if — organic conversion proves out (e.g., 3%+ install-to-Plus), consider $10/day on Reddit ads in r/getdisciplined and similar, OR TikTok creative-led ads. Never ads until conversion is proven.
- **AI-assisted content:** Use Claude (chat or Code) to draft Reddit posts, TikTok scripts, store listing variants, reply templates. This is where AI replaces a marketing hire for a solo dev.

---

## 11. Working Agreement with Claude Code

When the developer pairs with Claude Code on this project, the following expectations apply.

### 11.1 Default behaviors

- **Always write Kotlin idiomatically.** Coroutines + Flow over RxJava. Sealed classes for state. Data classes for models. No Java unless absolutely required for a binding.
- **Always use Jetpack Compose for UI.** Never XML layouts unless a specific Android API requires it (e.g., notification custom layouts).
- **Always use the version catalog** (`gradle/libs.versions.toml`). Never hardcode library versions in `build.gradle.kts`.
- **Always write a unit test for non-trivial logic** when touching detectors, debouncing, OEM detection, watchdog logic.
- **Always explain the first time an Android-specific concept appears** in C# terms for the developer (e.g., the first time `Flow` shows up, briefly: "Flow is the Kotlin equivalent of `IAsyncEnumerable<T>` — a cold async stream"). Don't re-explain the second time.

### 11.2 Code quality bar

- Detekt + ktlint configured in CI from week 1.
- Public classes / non-trivial functions have KDoc comments.
- No `TODO` left in main branch without an associated GitHub issue.
- No `println` debugging — use Timber (or similar) wrapped so it's stripped from release builds.

### 11.3 What Claude Code should never do silently

- Add an analytics SDK, crash reporter, or any networked dependency without confirming with the developer first. Privacy is a brand promise.
- Request Android permissions beyond what's strictly needed. The current required permissions:
  - `BIND_ACCESSIBILITY_SERVICE` (service-level)
  - `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` (Android 14+)
  - `POST_NOTIFICATIONS` (Android 13+)
  - `RECEIVE_BOOT_COMPLETED`
  - `INTERNET` (only for the email-OTP commitment lock and Play Billing — discuss before adding more network calls)
  - `BILLING` (Play Billing)
  - Battery optimization opt-out is *requested* via intent, not declared as permission.
- Reach out to any third-party domain at runtime that the user hasn't consented to.
- Add ads. Ever.

### 11.4 When to push back on the developer

- If the developer asks to skip the Trust Dashboard or reduce its honesty — push back. It's the brand.
- If the developer asks to add a subscription model — push back. Reviews show this is a trust-killer.
- If the developer asks to ship without per-OEM battery handling — push back. Reliability is the wedge.
- If the developer asks to add an analytics SDK or any non-essential network call — push back. Privacy is the brand.

---

## 12. Parallel Sub-Agent Strategy (Claude Code)

Claude Code supports launching **sub-agents** with the `Task` tool to run work in parallel. Used well, this is a significant productivity unlock for a solo dev. Used wrong, it creates merge chaos. This section defines when and how to use them on Touchgrass.

### 12.1 The core rule

**Parallelize when tasks are independent (no shared files, no shared module state, no compilation dependency). Serialize when they touch shared state.**

A good test: *if Agent A and Agent B both finish and I `git diff`, do their changes ever overlap?* If yes → serialize. If no → parallelize.

### 12.2 Tasks that SHOULD run in parallel

These have no shared state and benefit from parallel execution.

**A. Per-app detectors (week 5)**

After the `Detector` interface and `BlockingStrategy` are committed to `main`, the per-app detectors are *isolated*. Launch 5 parallel agents:

| Agent | Task |
|---|---|
| Agent 1 | Implement `YouTubeShortsDetector.kt` + unit tests |
| Agent 2 | Implement `InstagramReelsDetector.kt` + unit tests |
| Agent 3 | Implement `TikTokDetector.kt` + unit tests |
| Agent 4 | Implement `FacebookReelsDetector.kt` + unit tests |
| Agent 5 | Implement `SnapchatSpotlightDetector.kt` + unit tests |

Each agent only touches one file (and its sibling test file). No conflicts.

**B. Per-OEM battery walkthrough data (week 4)**

Each OEM's instructions live in a separate JSON/data file. Launch 7 parallel agents — one per OEM (Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, Huawei) — each researching and producing the instruction data for one manufacturer.

**C. Repo housekeeping files (day 1)**

After the repo is created, launch parallel agents for:

- Agent 1: Write `README.md`
- Agent 2: Write `CONTRIBUTING.md`
- Agent 3: Write `PRIVACY.md`
- Agent 4: Write `ARCHITECTURE.md`
- Agent 5: Write `CODE_OF_CONDUCT.md`
- Agent 6: Write `.github/workflows/android-ci.yml`

All independent files. Zero conflict risk.

**D. UI screens (week 5, after design system is locked)**

After theme + reusable Composables are committed:

- Agent 1: Onboarding screens (`ui/onboarding/`)
- Agent 2: Trust Dashboard (`ui/trust/`)
- Agent 3: Stats screen (`ui/stats/`)
- Agent 4: Settings screen (`ui/settings/`)

Each owns its own subpackage.

**E. Marketing content (any time)**

- Agent 1: Reddit launch post for r/digitalminimalism
- Agent 2: Reddit launch post for r/nosurf
- Agent 3: TikTok script batch (10 videos)
- Agent 4: Product Hunt copy
- Agent 5: Twitter/X launch thread

All independent text artifacts.

### 12.3 Tasks that MUST stay serial

Do not parallelize these. They'll fight over the same files or introduce subtle bugs.

- **Anything touching `build.gradle.kts` or `libs.versions.toml`.** Dependency management is a single-writer file.
- **Anything modifying `AndroidManifest.xml`.** Same reason.
- **The `Detector` interface, `BlockingStrategy`, `EventDebouncer`** itself. These are foundational; freeze them before fanning out per-app detectors.
- **The Compose theme / design tokens.** Locked first, then UI screens fan out.
- **The Hilt module setup.** Centralized DI configuration.
- **Database migrations.** Always serial, in version order.

### 12.4 Recommended parallel-agent recipes

#### Recipe 1: "Fan out the detectors" (week 5)

```
1. Developer + Claude Code (lead): Write Detector interface, BlockingStrategy,
   EventDebouncer. Commit to main. Push.

2. Launch 5 parallel sub-agents with identical contract:
   "Implement {AppName}Detector.kt under app/src/main/kotlin/com/touchgrass/app/
   accessibility/detectors/. It must implement the Detector interface defined
   in detectors/Detector.kt (already in main). Detect when the user is viewing
   {short-form feed} in {package name}. Add unit tests under app/src/test/.
   Do not modify any file outside this directory."

3. Each agent produces a PR / branch. Developer reviews, merges sequentially.
```

#### Recipe 2: "Onboarding research blitz" (week 4)

```
1. Launch 7 parallel sub-agents, one per OEM:
   "Research the exact steps to disable battery optimization for a third-party
   app on {manufacturer} devices running their current Android skin. Produce
   a JSON file under app/src/main/assets/oem/{manufacturer}.json matching the
   schema in §4.5 of the spec. Include the deep-link intent if one exists.
   Cite your sources in comments."

2. Developer reviews each JSON for accuracy. Commits.
```

#### Recipe 3: "Repo setup day" (day 1)

```
1. Developer (or lead Claude Code) creates the empty GitHub repo with .gitignore
   and LICENSE.

2. Launch 6 parallel sub-agents for the housekeeping markdown files (§8.2).
   Each produces one file based on this spec.

3. Developer reviews, commits all in one PR.
```

#### Recipe 4: "Marketing content sprint" (week 7)

```
1. Developer locks the final brand voice and key feature list.

2. Launch 5 parallel sub-agents to draft:
   - Reddit post for r/digitalminimalism (focus: privacy angle)
   - Reddit post for r/nosurf (focus: actually-works angle)
   - Product Hunt launch copy (focus: indie-dev underdog angle)
   - TikTok script batch (focus: visual hooks)
   - Email to digital-wellbeing newsletter writers

3. Developer reviews and personalizes.
```

### 12.5 Anti-recipes (don't do these)

- ❌ Launching parallel agents to add 5 dependencies to `build.gradle.kts` — they'll all conflict.
- ❌ Launching parallel agents on the same package while the package's shape is still being defined.
- ❌ Launching parallel agents for "build the whole feature" — each will redo foundational work.
- ❌ Launching parallel agents for refactoring across the codebase — refactors touch too many files.

### 12.6 Coordination rules

- **One agent per file.** Hard rule.
- **Brief each agent in writing** with the exact files it owns and the contract it must implement.
- **Reference this spec** (the file you're reading) in every agent brief so each agent has the full product context.
- **Merge sequentially**, not in parallel. Review one PR at a time.
- **Run the test suite after each merge**, not just at the end.

---

## 13. Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Google Play rejects the AccessibilityService usage as policy violation | Medium | Critical | Submit with a detailed accessibility-use justification PDF. Trust Dashboard helps. Open-source the app to demonstrate good faith. Scope `packageNames` strictly. |
| Instagram/TikTok change their UI and detectors break | High | High (recoverable) | Modular detector design. Version checks. Crash-light fallbacks. Ship updates fast. |
| Background service still dies on some OEM despite all efforts | Medium | High | Watchdog notification surfaces the failure proactively rather than letting users discover it during a binge. Honesty in store listing about Android limitations on certain ROMs. |
| Low download volume despite quality product | Medium | High (for goal of "lots of downloads") | Heavy organic content investment. Open-source as a marketing story. Beta with niche communities first. Be prepared to iterate the ASO listing for 3–6 months before judging. |
| Developer burns out learning Kotlin + shipping | Medium | Critical | Realistic 8-week timeline above. Cut V1 scope further if needed before cutting quality. |
| iOS users keep asking for a version we can't build well | Medium | Low | Have a clear "iOS coming, Family Controls only, sign up here" page from day 1 on gettouchgrass.app. |

---

## 14. Definition of Done — V1

V1 ships when ALL of the following are true:

- [ ] All 5 target apps blocked reliably on a fresh Pixel and a fresh Xiaomi running stock MIUI.
- [ ] 48-hour soak test passes: app left alone for 48h, blocking still works without re-toggle.
- [ ] All 7 supported OEMs (Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, Huawei) have a complete onboarding walkthrough.
- [ ] Trust Dashboard reviewed and approved by developer as accurate.
- [ ] No third-party analytics SDKs in release build.
- [ ] No ads.
- [ ] Privacy policy live and linked from app + Play Store + gettouchgrass.app.
- [ ] Play Billing tested end-to-end for one-time purchase.
- [ ] At least 20 beta users have installed and reported back.
- [ ] All P0 / P1 bugs from beta closed.
- [ ] Open-source repo published, CI green, README polished.
- [ ] Play Store listing finalized with screenshots, feature graphic, ASO-optimized copy.
- [ ] gettouchgrass.app live with landing page and email signup.
- [ ] gettouchgrass.com 301-redirects to gettouchgrass.app.

---

## 15. Appendix A: Quick reference — Kotlin for the .NET developer

| C# concept | Kotlin equivalent |
|---|---|
| `var x = 5;` (mutable) | `var x = 5` |
| `const`/`readonly` | `val x = 5` |
| `string?` (nullable) | `String?` |
| `null!.Foo` | `x!!.foo` (use sparingly) |
| `x?.Foo` | `x?.foo` |
| `??` operator | `?:` (elvis) |
| `using` | `use { }` extension on Closeable |
| `record` | `data class` |
| `enum` with data | `enum class` or `sealed class` |
| `async/await` + `Task<T>` | `suspend fun` + coroutines, returns `T` directly |
| `IAsyncEnumerable<T>` | `Flow<T>` |
| `IObservable<T>` | `StateFlow<T>` / `SharedFlow<T>` |
| LINQ `.Where().Select()` | `.filter { }.map { }` |
| string interpolation `$"..."` | `"...${expr}..."` |
| `Action<T>` / `Func<T,R>` | `(T) -> Unit` / `(T) -> R` |
| Extension methods | Extension functions: `fun String.foo() = ...` |
| Properties with getter/setter | `val`/`var` with backing field, or `get()/set()` |
| `services.AddSingleton<...>()` | Hilt: `@Singleton @Provides ...` in `@Module` |
| EF Core `DbContext` | Room `@Database` + `@Dao` |
| `appsettings.json` | `DataStore` for prefs, `BuildConfig` for build-time |

---

## 16. Appendix B: First Session with Claude Code — Suggested Prompt

When the developer opens Claude Code in `D:\My_Projects\Touchgrass`, this is the recommended kickoff:

> *"Read TOUCHGRASS_BUILD_SPEC.md in full before doing anything. We are building Touchgrass per this spec. Today is Week 1, Day 1. Before any code:*
>
> *1. Confirm you've read §0 (locked decisions), §11 (working agreement), and §12 (parallel sub-agent strategy).*
>
> *2. Initialize the project: create an Android Studio project at the current directory with package `com.touchgrass.app`, min SDK 29, target SDK 35, Kotlin + Jetpack Compose, version catalog enabled.*
>
> *3. Wire up the core dependencies (Compose, Material 3, Hilt, Room, DataStore, WorkManager, Coroutines, Timber) in the version catalog.*
>
> *4. Create the package structure from §4.2 as empty packages (placeholder `package-info.kt` is fine).*
>
> *5. Then, in parallel sub-agents (per Recipe 3 in §12.4), generate the six housekeeping files: README.md, CONTRIBUTING.md, PRIVACY.md, ARCHITECTURE.md, CODE_OF_CONDUCT.md, and .github/workflows/android-ci.yml.*
>
> *6. Stop and show me everything before I commit."*

---

**End of spec. Ship something users love.**
