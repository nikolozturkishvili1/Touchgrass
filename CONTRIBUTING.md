# Contributing to Touchgrass

## Welcome

Touchgrass is open source because trust is the product. We tell users we don't watch their whole screen, we don't ship analytics SDKs, and we don't put ads in their faces — and then we hand them the source so they can verify it. Every contribution that lands in this repo helps us keep that promise. Whether you're fixing a typo, adding a detector for an app that's currently bypassing the block, or writing a battery-optimization walkthrough for a phone we've never touched, you're helping someone take their brain back. Thank you.

## Code of Conduct

This project follows the [Contributor Covenant](./CODE_OF_CONDUCT.md). By participating — in issues, PRs, discussions, or anywhere else — you agree to uphold it. Be kind, be precise, assume good faith. Report incidents to the email in `CODE_OF_CONDUCT.md`.

## What we want help with

The two highest-leverage contribution paths, in order:

1. **Add a `Detector` for an app or surface that currently bypasses Touchgrass.** Examples: Pinterest's pin-board infinite scroll, BeReal-style feeds, future Snapchat redesigns, X/Twitter's "For You" surface, LinkedIn video feed, Reddit shorts. Each detector is one file plus its tests — see "Adding a new Detector" below.
2. **Add an OEM battery-optimization walkthrough.** We ship walkthroughs for Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, and Huawei. Everything beyond that — Honor, Nothing, ASUS, Sony, Fairphone, Tecno, Infinix, Lenovo, Motorola variants, regional skins — is the long tail we need help filling in.

Other very welcome contributions:

- **Bug reports**, especially of the form *"Touchgrass stopped working on my {device} running {ROM} after {time}"*. Include `Build.MANUFACTURER`, `Build.MODEL`, Android version, and steps. Use the `bug` label.
- **Detector regressions** when target apps update. Reels and Shorts UIs change; detectors need to keep up. Reproduce, file an issue with the target app's version code, and ideally open a PR.
- **Translations.** V1.1 plans Spanish, Portuguese, and Hindi (per spec §3.2). If you're fluent and want to own a locale's `strings.xml`, open an issue first so we can coordinate.

## What we don't want

Some contributions, no matter how well-intentioned, break the brand promises that make Touchgrass worth using. We will close these PRs:

- **No analytics SDKs, crash reporters, or any networked dependency** not already in `gradle/libs.versions.toml`. Privacy is a brand promise. New network calls require an issue and explicit maintainer sign-off — see spec §11.3.
- **No subscription-model code paths.** V1 ships fully free; if a paid tier returns later it will be a one-time price. No recurring billing, no trial timers, no "upgrade after N uses" nags.
- **No ads.** Not banner, not interstitial, not "house ads", not affiliate links inside the app. Ever.
- **No broadening of the AccessibilityService `packageNames`** to apps the user hasn't asked us to block. Every package in `accessibility_service_config.xml` is a privacy claim we have to defend in the Trust Dashboard. Scope strictly to feed surfaces.

If you're unsure whether your change fits, open an issue first. We'd rather discuss than reject.

## Development setup

Same as the README, abbreviated here:

1. Install Android Studio (latest stable) and JDK 17.
2. Clone the repo: `git clone https://github.com/nikolozturkishvili1/Touchgrass.git`
3. Open in Android Studio. Let Gradle sync.
4. Run `./gradlew assembleDebug` to confirm a clean build.
5. Run on a real device — the AccessibilityService cannot be meaningfully tested in the emulator for most target apps.

You'll want a couple of test devices if possible: a Pixel (stock Android baseline) and at least one aggressive-OEM device (Xiaomi, Samsung) for the reliability paths.

## Code style

- **Idiomatic modern Kotlin.** Coroutines + Flow over RxJava. Sealed classes for state. Data classes for models. No Java unless a binding strictly requires it.
- **Jetpack Compose for all UI.** No XML layouts unless an Android API demands it (e.g., notification custom layouts).
- **Version catalog is the single source of truth for dependency versions.** Add to `gradle/libs.versions.toml`. Never hardcode versions in `build.gradle.kts`.
- **Detekt + ktlint must pass.** Run `./gradlew detekt ktlintCheck` before pushing. CI enforces it.
- **Unit tests for non-trivial logic.** This is a hard expectation for detectors, the `EventDebouncer`, OEM detection, and watchdog logic. Other areas: use judgement, but err toward testing.
- **No `println`.** Use Timber. It's wrapped so debug logs strip from release builds.
- **No `TODO` in `main` without a linked GitHub issue.** Per spec §11.2. Format: `// TODO(#123): explain`. Bare `TODO`s will be flagged in review.
- **KDoc on public classes and non-trivial functions.** Explain *why*, not *what*.

## Adding a new Detector

This is the single most valuable thing you can contribute. The pattern is deliberately small so it's hard to get wrong.

1. **Create the class** under `app/src/main/kotlin/com/touchgrass/app/accessibility/detectors/`. Name it `{AppName}{Surface}Detector.kt` — e.g., `PinterestPinBoardDetector.kt`.
2. **Implement the `Detector` interface.** The interface lands in `main` during Week 3 of the build (reference spec §12.4 Recipe 1 for the contract). A detector takes an `AccessibilityEvent`, decides if the user is in the target feed surface, and returns a `BlockingDecision`. Do not perform the block yourself — return a decision and let `BlockingStrategy` handle it.
3. **Write unit tests** under `app/src/test/kotlin/com/touchgrass/app/accessibility/detectors/`. Mock `AccessibilityEvent` (or use the test helpers if they exist by the time you're contributing). Cover: positive detection, negative detection (similar-looking but wrong surface), and edge cases like the user backing out mid-scroll.
4. **Register the package name** in BOTH `app/src/main/res/xml/accessibility_service_config.xml` AND the `<queries>` block of `AndroidManifest.xml`. **Be deliberate here.** Scope the detector strictly to the feed surface — never the whole app. The Trust Dashboard promises users we only observe feeds, not the entire app.
5. **Update the README's "What it blocks" list** with the new surface.
6. **Open a PR with the `[detector]` label.** Title format: `detector: add {App} {Surface} detector`.

In review we'll check: scoping, false-positive risk, debounce behavior, and whether the detection still works after a hypothetical UI shuffle by the target app (i.e., is your detection brittle to a single resource-ID rename?).

## Adding an OEM walkthrough

OEM walkthroughs are pure data, no Kotlin required. If you own one of these devices, you can ship a contribution in an afternoon.

1. **Create the JSON file** at `app/src/main/assets/oem/{manufacturer}.json` matching the schema in spec §4.5. Use the lowercase manufacturer name (`honor.json`, `nothing.json`, `motorola.json`).
2. **Include the deep-link intent** if one exists for that OEM's battery settings (e.g., for MIUI it's `miui.intent.action.POWER_HIDE_MODE_APP_LIST`). If you can't find one, omit the field — fallback navigation handles it.
3. **Cite your sources** in a top-of-file comment block: forum post URLs, OEM documentation links, XDA threads, the version of the ROM you verified against. This is the difference between a walkthrough that ages well and one that confuses users in six months.
4. **Provide screenshots** if you have access to the device: `app/src/main/res/drawable/oem_{manufacturer}_step{n}.png`. Crop to the relevant UI, redact any personal info. PNG, reasonable resolution (1080px wide is plenty).
5. **Open a PR with the `[oem]` label.** Title format: `oem: add {Manufacturer} battery walkthrough`.

In review we'll check: accuracy against current ROM, source citations, and that the steps actually work end-to-end.

## PR process

**Branch naming:**

- `feat/...` — new feature
- `fix/...` — bug fix
- `detector/...` — new or updated detector
- `oem/...` — OEM walkthrough
- `docs/...` — documentation only

**Commits:** Imperative mood, scope prefix. Examples:

- `detector: add Pinterest pin-board detector`
- `oem: add Honor MagicOS battery walkthrough`
- `fix: debounce TikTok re-entry within 1.5s`
- `docs: clarify accessibility scoping in trust dashboard`

**PR description must include:**

- **What changed** — one or two sentences.
- **Why** — link the issue, or describe the user-facing problem.
- **Manual test steps** — exactly what you did on a real device to confirm it works. "Opened Instagram, tapped Reels tab, observed block fires within 200ms" is the right shape.
- **New permissions or dependencies** — if any. New dependencies require justification per spec §11.3. Expect questions; we keep the dependency graph small on purpose.

**CI must be green.** Detekt, ktlint, unit tests, debug build. No exceptions for "just a docs change" — CI is fast.

**Be patient.** This is a solo-maintainer project. Triage within a week, merge within two weeks for clean PRs. Tag `@nikolozturkishvili1` if a PR sits without a reply past those windows — Git notifications get noisy.

## Issue templates and labels

We use the following labels — please apply them when filing or, if you can't, leave a hint in the title:

- `bug` — something is broken
- `feature` — new capability
- `oem` — anything related to OEM battery handling
- `detector` — anything related to a per-app detector
- `accessibility` — AccessibilityService behavior, scoping, permissions
- `good-first-issue` — small, well-scoped, friendly to first-time contributors

## License

Touchgrass is licensed under **GPL-3.0**. By contributing, you agree that your contribution is licensed under GPL-3.0 as well. This is deliberate: it keeps Touchgrass open, prevents predatory closed-source forks, and protects the trust positioning that makes the app worth using.

Thanks for being here.
