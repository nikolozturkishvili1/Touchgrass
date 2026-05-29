# Touchgrass — Architecture

## 1. Goal of this document

This document explains how Touchgrass is wired internally, the reasoning behind that wiring, and where a new contributor (or a privacy-conscious user auditing the source) should plug in. It is the engineering counterpart to [`SPEC.md`](SPEC.md): the spec says *what* we are building and *why*; this file says *how the code is shaped*. Read the spec first — sections referenced here as "§4", "§11", etc. point back into it.

## 2. One-paragraph mental model

Touchgrass is, at its core, a pipeline of one-way pushes. The Android OS pushes `AccessibilityEvent`s into `TouchgrassAccessibilityService` for apps the user explicitly opted into blocking. The service routes each event to a per-app `Detector`, which inspects view IDs and content descriptions and returns either `Detection.NotInteresting` or `Detection.ShortFormFeed(surface)`. If it's a hit, the `EventDebouncer` decides whether we have already blocked this same source within the last ~1.5 seconds (this defeats the spam-tap bypass). If not, a `BlockingStrategy` executes — usually `GLOBAL_ACTION_BACK`, occasionally an accessibility overlay, occasionally Quick Peek interception. The block is recorded by `StatsRepository` into Room. A `TouchgrassForegroundService` keeps the process resident with a persistent notification, and a `WatchdogWorker` (WorkManager, every ~15 min) verifies the accessibility service is still alive and serviced an event recently; if not, it raises a high-priority notification. That's the whole engine. Everything else is glue, UI, or trust theatre.

## 3. Stack

Lifted from spec §4.1; kept here as a fast reference card.

| Layer | Choice | Why |
|---|---|---|
| Language | Kotlin 2.0+ | Idiomatic Android, clean coroutines |
| UI | Jetpack Compose (latest stable) | Declarative; fast iteration |
| Min SDK | API 29 (Android 10) | ~95% device coverage; Accessibility API matured here |
| Target SDK | API 35 (Android 15) | Play Store requirement |
| Architecture | MVVM + Repository, single-activity | Standard, maintainable solo |
| DI | Hilt | Google-recommended |
| Async | Coroutines + Flow | Idiomatic Kotlin |
| Persistence | Room (stats) + DataStore (preferences) | Standard |
| Build | Gradle Kotlin DSL + `libs.versions.toml` | Modern best practice |
| CI | GitHub Actions | Free for public repos |
| Crash reporting | Sentry (optional, disclosed) or none | Privacy-first |

## 4. Module / package structure

Single-module Gradle project for V1; revisit if the codebase ever justifies splitting. Full tree in spec §4.2. Annotated summary:

```
com.touchgrass.app/
├── MainActivity.kt                  // single activity host for Compose nav
├── TouchgrassApplication.kt         // @HiltAndroidApp; app-wide init
├── ui/                              // all Compose UI lives here
│   ├── theme/                       // Material 3 colors, typography
│   ├── onboarding/                  // 4-screen first-run flow (spec §6.1)
│   ├── home/                        // big toggle + today's count
│   ├── settings/                    // settings tree
│   ├── trust/                       // Trust Dashboard (spec §3.1.C)
│   ├── stats/                       // blocks per day/week/all-time
│   └── components/                  // reusable Composables
├── accessibility/                   // the engineering heart (spec §4.4)
│   ├── TouchgrassAccessibilityService.kt
│   ├── detectors/                   // one Detector per supported app
│   ├── BlockingStrategy.kt          // sealed class: BackPress | Overlay | QuickPeek
│   └── EventDebouncer.kt            // per-package, per-action 1.5s window
├── service/                         // process-lifetime + self-heal
│   ├── TouchgrassForegroundService.kt
│   ├── WatchdogWorker.kt            // WorkManager periodic health check
│   └── BootReceiver.kt              // re-arm on device boot
├── data/
│   ├── local/                       // Room DAOs, entities, DataStore keys
│   ├── repository/                  // repository implementations
│   └── model/                       // domain models
├── domain/                          // use cases; thin, only when non-trivial
├── billing/                         // present but unused at V1 — see §15
├── oem/                             // OEM detection + battery walkthrough data
│   ├── OemDetector.kt
│   └── instructions/                // per-manufacturer data classes (if needed)
├── lock/                            // commitment lock + friction modes
└── util/                            // small helpers; resist growth

res/xml/accessibility_service_config.xml   // the trust allowlist (see §10, §14)
AndroidManifest.xml                         // permissions (spec §11.3)
```

Two hard boundaries:

- `ui/` does not import from `accessibility/` or `service/`. It talks to repositories only.
- `accessibility/detectors/*` are siblings; they do not import each other. This is what makes the parallel fan-out in spec §12.4 Recipe 1 safe.

## 5. Core flow — block-decision pipeline

```mermaid
sequenceDiagram
    actor User
    participant OS as Android OS
    participant Svc as TouchgrassAccessibilityService
    participant Det as Detector (per-package)
    participant Deb as EventDebouncer
    participant Blk as BlockingStrategy
    participant Repo as StatsRepository
    participant Room as Room DB
    participant DS as DataStore

    User->>OS: Opens blocked app (e.g. Instagram Reels tab)
    OS-->>Svc: AccessibilityEvent (packageName, className, nodes)
    Svc->>DS: write heartbeat timestamp (every event)
    Svc->>Svc: lookup detector by event.packageName
    Svc->>Det: detect(event, rootNode)
    alt Not a short-form surface
        Det-->>Svc: Detection.NotInteresting
        Svc-->>OS: (no action)
    else Short-form feed
        Det-->>Svc: Detection.ShortFormFeed(surface="reels_tab")
        Svc->>Deb: shouldBlock(package, surface)?
        alt Within debounce window (< 1.5s since last block)
            Deb-->>Svc: false
        else Outside debounce window
            Deb-->>Svc: true; record timestamp
            Svc->>Blk: applyBlock(reason)
            Blk->>OS: performGlobalAction(GLOBAL_ACTION_BACK)
            Blk-->>Svc: applied
            Svc->>Repo: recordBlock(surface, ts, detectorId)
            Repo->>Room: INSERT block_event
        end
    end
```

Notes:

- The heartbeat is written on **every** event, not only on blocks — the watchdog uses it to decide whether the service is alive at all.
- `Detection.ShortFormFeed.surface` carries a human-readable label (e.g. `"shorts_player"`, `"reels_dm"`) so stats and debugging stay useful without sacrificing user privacy (the label is the surface name, not user content).
- The OS-level `packageNames` allowlist (see §10, §14) filters events *before* they reach our code. Detectors only ever see events from apps the user already opted into blocking.

## 6. Detector contract

The `Detector` interface and `Detection` hierarchy must be frozen on `main` before per-app detectors fan out in parallel (spec §12.4 Recipe 1, §12.3). One detector per app, one file per detector, no shared mutable state.

```kotlin
package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Classifies an AccessibilityEvent from a single target package as either
 * "short-form feed" or "not interesting".
 *
 * Detectors are stateless and pure with respect to the (event, root) pair
 * they receive. Cross-event memory belongs in EventDebouncer, not here.
 */
interface Detector {
    /** The exact Android package this detector handles, e.g. "com.instagram.android". */
    val packageName: String

    /** Stable identifier persisted alongside stats (e.g. "instagram_reels_v1"). */
    val detectorId: String

    fun detect(
        event: AccessibilityEvent,
        root: AccessibilityNodeInfo?,
    ): Detection
}

sealed interface Detection {
    /** The event is not a short-form surface. Cheapest path. */
    data object NotInteresting : Detection

    /**
     * The user is inside a short-form feed.
     * @param surface human-readable label for stats and debugging
     *                (e.g. "reels_tab", "shorts_player", "dm_reel", "fyp").
     */
    data class ShortFormFeed(val surface: String) : Detection
}
```

Design intent:

- Adding support for a new app = writing one new `Detector`, registering it in `AccessibilityModule`'s multibinding set, and adding its package to `accessibility_service_config.xml`. No other file needs to change.
- Detectors are unit-testable in pure JVM tests: hand them a fake `AccessibilityEvent` (Robolectric or hand-rolled fake) and assert the returned `Detection`.
- Detectors must not perform side effects. They observe; they do not act.
- Per spec §12.4 Recipe 1, the five day-1 detectors can be developed in parallel sub-agents because they share zero files and no state.

## 7. BlockingStrategy

```kotlin
sealed class BlockingStrategy {
    data object BackPress : BlockingStrategy()
    data class Overlay(val message: String) : BlockingStrategy()
    data object QuickPeek : BlockingStrategy()
}
```

**BackPress (default).** Calls `service.performGlobalAction(GLOBAL_ACTION_BACK)`. Cheapest, least intrusive, works for nearly every supported surface. Tradeoff: a determined user can immediately tap forward again — the `EventDebouncer` is what makes this honest by enforcing a 1.5s lockout (this is the fix for the spam-tap bypass complaints in spec §2.1). This is the strategy ~95% of blocks should use.

**Overlay(message).** Adds a `TYPE_ACCESSIBILITY_OVERLAY` window with a calming full-screen Compose surface. Used when back-press isn't sufficient (e.g. a surface that doesn't pop on `BACK`, or when we want a deliberate beat of friction). Tradeoff: overlays are heavier, need careful dismissal logic, and can feel intrusive — reserve them for surfaces where back-press demonstrably fails.

**QuickPeek.** Allow exactly one item to be viewed (a reel shared in DM, a YouTube short opened from a notification) but consume the scroll-to-next gesture. Implemented by passing through the first `typeWindowContentChanged` event for the surface and then consuming subsequent `typeViewScrolled` events on the same view tree. Tradeoff: the trickiest strategy and the most fragile against UI changes. Ships free in V1 once we have a stable test rig — the spec originally placed this behind Plus, but the free-for-start pivot dropped all paywalls (see §14).

## 8. EventDebouncer

Per-package, per-action timing window. Default 1.5 seconds. Defeats the spam-tap bypass (NoScroll reviews cited in spec §2.1).

- Backed by a `ConcurrentHashMap<String, Long>` keyed on `"${packageName}|${surface}"`.
- Check-and-update happens under a single `synchronized` block on the map — write volume is tiny (handfuls per second worst case), so finer-grained locking is overkill.
- The window is configurable via DataStore (advanced setting) but defaults to `1_500L` ms.
- Injected `Clock` so unit tests don't sleep.

```kotlin
class EventDebouncer(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val windowMillis: Long = 1_500L,
) {
    private val lastBlockAt = ConcurrentHashMap<String, Long>()

    /** Returns true if we should block now; records the timestamp if so. */
    fun shouldBlock(packageName: String, surface: String): Boolean =
        synchronized(lastBlockAt) {
            val key = "$packageName|$surface"
            val now = clock.millis()
            val last = lastBlockAt[key] ?: 0L
            if (now - last < windowMillis) return false
            lastBlockAt[key] = now
            true
        }
}
```

## 9. Reliability layer

Three components keep the heart beating. Reliability is the wedge against NoScroll (spec §2.3) — invest accordingly.

- **`TouchgrassForegroundService`.** A `Service` started in the foreground with a low-importance, persistent notification ("Touchgrass is on"). Its only job is to keep the process resident so the OS does not reap us when memory pressure rises. It holds no business logic; the AccessibilityService does the actual work.
- **`WatchdogWorker` (WorkManager).** A `CoroutineWorker` scheduled as a periodic work request every ~15 minutes. On each run it checks (a) that `com.touchgrass.app/.accessibility.TouchgrassAccessibilityService` is listed in `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`, and (b) that the on-disk heartbeat timestamp (written by the AccessibilityService to DataStore on every event) is recent enough during active hours. If either check fails, it posts a high-importance notification: *"Touchgrass paused — tap to fix."* Tapping deep-links to the relevant Settings screen. The watchdog's job isn't to *prevent* failure — it's to make failure **observable within minutes** instead of days.
- **`BootReceiver`.** A `BroadcastReceiver` registered for `BOOT_COMPLETED` (and `LOCKED_BOOT_COMPLETED` where applicable). On boot it re-arms the foreground service and enqueues the watchdog if not already scheduled.

## 10. OEM layer

Per-OEM battery-killer behavior is the single biggest reason competitors stop working (spec §2.3). We treat it as a first-class problem.

- `OemDetector` reads `Build.MANUFACTURER` (falling back to `Build.BRAND` for devices that lie about manufacturer).
- It looks up a walkthrough from `app/src/main/assets/oem/{manufacturer}.json`. Schema lives in spec §4.5 — title strings, screenshot filenames, an optional `deep_link_intent` that jumps the user straight to the right Settings page where one exists.
- Onboarding step 3 shows the matched walkthrough. If no match, we show a generic "Battery → Don't optimize Touchgrass" instruction set.
- Seven manufacturers ship in V1: Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, Huawei. Each is authored independently — spec §12.4 Recipe 2 fans this out across seven parallel sub-agents.

Adding a new OEM = adding one JSON file (and optional screenshots) in `assets/oem/`. No Kotlin changes if the existing schema covers it; add a data class under `oem/instructions/` if the schema needs extending.

## 11. Data layer

**Room (stats).** A single entity to start:

```kotlin
@Entity(tableName = "block_events")
data class BlockEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val packageName: String,
    val surface: String,        // e.g. "reels_tab"
    val detectorId: String,     // e.g. "instagram_reels_v1"
)
```

**DataStore (preferences).** Typed preferences: which apps are enabled, debounce window override, friction mode, commitment-lock state, today's pause budget remaining, watchdog heartbeat timestamp. DataStore (not Room) for the heartbeat because the write must be cheap and lock-free on the event hot path.

**Repository pattern.** ViewModels depend on repositories; *nothing else does*. This keeps the AccessibilityService and Workers free of UI-framework coupling and gives us a single seam to swap in fakes for instrumented tests.

- `BlockEventRepository` — reads/writes the stats table; exposes `Flow<DailyTotals>` etc.
- `PreferencesRepository` — thin wrapper over DataStore.
- `BillingRepository` — Play Billing wrapper; present but unused at V1 (no paywalled features). See §14 for status.
- `OemRepository` — loads + caches the matched OEM walkthrough JSON.

Repositories are interfaces in `data/repository/`, with implementations bound in `RepositoryModule`.

## 12. UI layer

Single activity (`MainActivity`), Jetpack Compose end-to-end, Material 3. Navigation via `androidx.navigation:navigation-compose`.

- **ViewModels** are `@HiltViewModel`-annotated and hold `StateFlow<UiState>`, where `UiState` is a `data class` (or sealed hierarchy for screens with distinct states like `Loading | Ready | Error`).
- **Screens** own state hoisting. The top-level screen Composable takes a ViewModel; child Composables take only the slice of state they need plus event lambdas. Composables stay stateless wherever possible.
- **Theme** (`ui/theme/`) is frozen before parallel UI work fans out (spec §12.3).
- **No XML layouts** — except `accessibility_service_config.xml` (Android requires XML there) and the manifest.

## 13. DI graph (Hilt)

Modules — frozen early so per-feature work doesn't fight over them:

- **`DataModule`** — provides the Room database, DAOs, and DataStore instances. `@Singleton`.
- **`RepositoryModule`** — `@Binds` repository interfaces to their implementations. `@Singleton`.
- **`AccessibilityModule`** — provides the set of `Detector`s (multibinding via `@IntoSet`), the `EventDebouncer` singleton, and the default `BlockingStrategy`. Consumed by `TouchgrassAccessibilityService` via `@AndroidEntryPoint`.
- **`BillingModule`** — provides the `BillingClient` and Play Billing wrapper. `@Singleton`. *Currently unwired — V1 has no in-app purchases. See §14.*

ViewModels use `@HiltViewModel` + constructor injection. Workers use `@HiltWorker` + an assisted factory. `TouchgrassApplication` is `@HiltAndroidApp`.

```mermaid
graph TD
    App[TouchgrassApplication<br/>@HiltAndroidApp] --> DM[DataModule]
    App --> RM[RepositoryModule]
    App --> AM[AccessibilityModule]
    App --> BM[BillingModule]

    DM --> Room[(Room DB)]
    DM --> DS[(DataStore)]

    RM --> BER[BlockEventRepository]
    RM --> PR[PreferencesRepository]
    RM --> BR[BillingRepository]
    RM --> OR[OemRepository]

    AM --> Dets[Set&lt;Detector&gt;]
    AM --> Deb[EventDebouncer]
    AM --> Blk[BlockingStrategy]

    BER --> Room
    PR --> DS

    Svc[TouchgrassAccessibilityService] --> Dets
    Svc --> Deb
    Svc --> Blk
    Svc --> BER

    VM[ViewModels<br/>@HiltViewModel] --> BER
    VM --> PR
    VM --> BR
    VM --> OR
```

## 14. What does NOT live in the codebase

These omissions are deliberate brand promises (spec §2.3, §11.3). Any PR that adds one of them must be rejected on sight unless the developer has explicitly approved it as a disclosed, opt-in toggle.

- **No analytics SDK.** Not Firebase Analytics. Not Mixpanel. Not Amplitude. Not "just a tiny ping".
- **No crash reporter** in the default build. Sentry is allowed *later*, opt-in, disclosed in the Trust Dashboard. Not present out of the box.
- **No backend.** Touchgrass has no server. The only network call in V1 is the transactional-email API used by the optional commitment-lock OTP — disclosed in the Trust Dashboard.
- **No code path that observes apps outside the `packageNames` allowlist** declared in `res/xml/accessibility_service_config.xml`. The Trust Dashboard claim ("we only see apps you asked us to block") is enforced by the OS at the event-delivery layer, not by polite intent inside our process. The allowlist is a single file a reviewer can diff.
- **No ads. Ever.**
- **No in-app purchases at V1.** The free-for-start pivot means there is no Play Billing call, no Plus tier, and nothing paywalled. The `billing/` package, `BillingModule`, `BillingRepository`, and `BILLING` manifest permission are present from earlier scaffolding but are inert — they sit dark and may be deleted in a future cleanup. Until then, treat them as not part of the V1 product surface.

## 15. Where to plug in

| Task | Touch this |
|---|---|
| Add support for a new app | `accessibility/detectors/{App}Detector.kt`, register it in `AccessibilityModule`, add its package to `accessibility_service_config.xml` |
| Add a new OEM battery walkthrough | `app/src/main/assets/oem/{manufacturer}.json` (+ optional data class in `oem/instructions/` if the schema needs extending) |
| Add a new friction mode (math, breathing, etc.) | `lock/` — commitment lock and friction modes share this package |
| Add a new UI screen | `ui/{screen-name}/` — screen Composable + ViewModel + state class; add a route to the nav graph |
| Tune the debounce window | `accessibility/EventDebouncer.kt` (default) + `PreferencesRepository` (user override) |
| Add a new BlockingStrategy variant | `accessibility/BlockingStrategy.kt` — extend the sealed class, handle the new branch in the service's `when` |
| Add a new stat or aggregate | `data/local/BlockEventDao.kt` (query) + `BlockEventRepository` (Flow) + `ui/stats/` (display) |

Every row here is intentionally a single package or file. If a change needs to span more, the seams are wrong — flag that in the PR.

## 16. Build commands

Quick reference. Run from the repo root.

```bash
./gradlew assembleDebug         # build debug APK
./gradlew installDebug          # install on the connected device
./gradlew testDebugUnitTest     # run JVM unit tests
./gradlew detekt ktlintCheck    # static analysis + style
./gradlew assembleRelease       # build release APK (signed via env/keystore)
./gradlew bundleRelease         # build release AAB for Play upload
```

CI runs `detekt ktlintCheck`, `testDebugUnitTest`, and `assembleDebug` on every PR (spec §8.3). Keep it green.
