# CLAUDE.md — Touchgrass

Android app that blocks short-form feeds (Instagram Reels, YouTube Shorts, TikTok
For You, Facebook Reels, Snapchat Spotlight, Shorts on Chrome/Samsung Internet)
while leaving the rest of those apps working. Kotlin + Compose, single `:app`
module, GPL-3.0, solo developer, Windows dev machine.

**Start every session by reading `STATUS.md`. Do the one thing under "Next
action". Update it before you finish.**

---

## Working rules

These exist because this project kept re-doing the same work. Follow them.

1. **The code is the truth.** Never quote a version, package name, or SDK level
   from prose. Read `app/build.gradle.kts`. If a doc disagrees with the code, the
   doc is wrong — fix the doc in the same turn you notice it.
2. **One task per session.** `STATUS.md` names it. Do not re-scope, re-plan, or
   re-audit work that is already decided. If you think the priority is wrong, say
   so in one sentence and ask — do not silently switch tasks.
3. **Do not re-derive what is already written.** Before investigating anything,
   check `STATUS.md`, then `docs/RELEASE.md`, then the deployment skill. If the
   answer is there, use it.
4. **Finish by committing.** Uncommitted work is work that gets redone. Commit at
   the end of every session that changed a file, even if the task is unfinished —
   a WIP commit beats a dirty tree.
5. **Update `STATUS.md` before you finish.** What changed, what is next, what is
   blocked. This is the handoff to the next session. Non-negotiable.
6. **When blocked, record the blocker and stop.** Write it into `STATUS.md` under
   Blocked. Do not substitute a different, easier task to feel productive.
7. **Do not create new `.md` files.** The docs listed below are the complete set.
   New information goes into an existing one. Ask before adding a file.
8. **Never invent a value for a Play Console legal form.** Data safety and the
   accessibility declaration are legal declarations. If a value is not in
   `docs/RELEASE.md`, stop and ask.

---

## Verified facts

Verified 2026-07-30 against the working tree. Re-verify with the command shown;
do not trust this table over the file.

| Fact | Value | Verify with |
|---|---|---|
| applicationId (Play package) | `com.touchgrassinc.app` | `grep applicationId app/build.gradle.kts` |
| Kotlin namespace | `com.touchgrass.app` | `grep namespace app/build.gradle.kts` |
| versionName / versionCode | `0.1.2` / `3` | `grep version app/build.gradle.kts` |
| minSdk / target / compile | `29` / `36` / `36` | `grep Sdk app/build.gradle.kts` |
| jvmTarget | `17` | `grep jvmTarget app/build.gradle.kts` |
| Gradle / AGP / Kotlin | `8.11.1` / `8.9.2` / `2.1.20` | `gradle/wrapper/gradle-wrapper.properties`, `gradle/libs.versions.toml` |
| Play app name | `Touchgrass: Block Reels & Shorts` | `fastlane/metadata/android/en-US/title.txt` |
| Play category | `Productivity` | `docs/RELEASE.md` |
| Repo | `github.com/nikolozturkishvili1/Touchgrass` | `git remote -v` |
| Privacy policy (live) | `https://nikolozturkishvili1.github.io/Touchgrass/privacy.html` | `docs/RELEASE.md` |
| Store listing copy | `fastlane/metadata/android/en-US/` | that directory |

`applicationId` and `namespace` differ on purpose — `com.touchgrass.app` was taken
on Play. They are independent. **Do not "fix" this by moving sources.**

---

## Commands

Windows dev machine. `java` and `keytool` are not on PATH — use Android Studio's
bundled JBR.

```powershell
# PowerShell, from D:\My_Projects\Touchgrass
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

.\gradlew.bat testDebugUnitTest        # unit tests
.\gradlew.bat detekt ktlintCheck       # static analysis (CI runs both)
.\gradlew.bat ktlintFormat             # autofix formatting
.\gradlew.bat assembleDebug            # debug APK
.\gradlew.bat assembleRelease          # signed APK  -> app/build/outputs/apk/release/
.\gradlew.bat bundleRelease            # signed AAB  -> app/build/outputs/bundle/release/
```

Release builds read `keystore.properties` (gitignored). If that file is absent,
release packaging **fails by design** — the failure is the signal, not a bug. Do
not add a debug-signing fallback.

Run a single test:
`.\gradlew.bat :app:testDebugUnitTest --tests "com.touchgrass.app.<FQCN>" --console=plain`

CI (`.github/workflows/`): `android-ci.yml` on push/PR to `main` (detekt, ktlint,
tests, assemble); `release.yml` on a `v*` tag (signed AAB + APK, public GitHub
Release); `pages.yml` publishes the landing + privacy pages.

---

## Architecture

Read `docs/ARCHITECTURE.md` once for the wiring. The short version:

- Blocking is an `AccessibilityService` scoped by a strict `packageNames`
  allowlist in `app/src/main/res/xml/accessibility_service_config.xml`. Per-app
  detectors live in `com.touchgrass.app.accessibility.detectors`.
- A `FOREGROUND_SERVICE_SPECIAL_USE` watchdog keeps the accessibility service
  alive against OEM battery managers.
- Compose UI + Hilt + Room + DataStore. Navigation in Compose.
- The **only** outbound network call in the app is the optional commitment-lock
  email OTP (`lock/ResendEmailOtpService.kt`). Nothing else leaves the device.

Product decisions and their rationale are in `docs/SPEC.md`. The spec wins over
your instinct; if you think it should change, ask.

---

## Never do these

- Commit `keystore.properties`, `keysets/`, `*.jks`, `local.properties`, `*.aab`,
  or `*.apk`.
- Print a keystore password, base64 keystore, `GITHUB_PAT`, or Resend key into the
  transcript.
- Push a `v*` tag before the GitHub Actions release secrets are set (the run fails
  by design — see `STATUS.md`).
- Add an analytics or crash-reporting SDK — **including Sentry.** Zero third-party
  telemetry is a product promise, not a default.
- Run `RELEASE_NOW.ps1` without first confirming the GitHub Actions release secrets
  exist. It pushes a tag; a tag pushed without secrets burns that version number.
- Add ads, a subscription, or a paywall over blocking or the commitment lock.
- Reuse a `versionCode` on a Play upload.
- Change `namespace` to match `applicationId`.

---

## Docs — the complete set

| File | What it is |
|---|---|
| `STATUS.md` | Where the project is, what is next. **Read first, update last.** |
| `CLAUDE.md` | This file. How to work here. |
| `README.md` | Public repo landing page. User-facing, not internal notes. |
| `CHANGELOG.md` | Keep a Changelog format, one entry per released version. |
| `docs/SPEC.md` | Locked product + engineering spec. Source of truth for behavior. |
| `docs/ARCHITECTURE.md` | How the code is wired. |
| `docs/RELEASE.md` | The single release runbook — Play, GitHub Releases, IzzyOnDroid. |
| `docs/GO_TO_MARKET.md` | Launch/marketing plan. Channels and messaging. |
| `docs/PRIVACY.md` | Privacy policy, long form. **The page users and Google actually see is `marketing/landing-page/privacy.html`** — edit that too. |
| `marketing/TESTER_RECRUITING.md` | Recruiting message + sources for the Play tester gate. |
| `CONTRIBUTING.md`, `SECURITY.md`, `CODE_OF_CONDUCT.md` | GitHub convention files. |
| `.claude/skills/touchgrass-deployment/SKILL.md` | Fast-recall deployment values. |

`MobileApp/` is not a second project — see `MobileApp/CLAUDE.md`. `_to_delete/` holds
retired docs and is **gitignored** (local only; the retired versions live in git
history). Do not read them for facts; they are stale by definition. `beta-testers.local.md` is gitignored personal data — do not commit or
quote it.

@STATUS.md
