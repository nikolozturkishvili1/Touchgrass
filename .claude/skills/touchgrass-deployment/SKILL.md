---
name: touchgrass-deployment
description: >-
  Release and publish the Touchgrass Android app — build or sign the AAB/APK, cut a
  GitHub Release, submit to IzzyOnDroid, or fill in Google Play Console (testing
  tracks, App content, Data safety, accessibility and foreground-service
  declarations, store listing). Use when asked to ship, release, publish, tag, or
  upload a build, or when asked for the package name, version code, signing keys,
  privacy URL, or what is still blocking release. Use it before quoting any
  Touchgrass identity value, and whenever a Play Console form or an
  F-Droid/IzzyOnDroid inclusion request needs an exact answer.
---

# Touchgrass deployment

Fast-recall values and guardrails. **The procedure is not here** — it is in
`docs/RELEASE.md`, and current state is in `STATUS.md`. Read those; do not
reconstruct them from memory.

## Rule zero

Version numbers change. **Read `app/build.gradle.kts` before you paste a version
anywhere.** If a number in any document disagrees with that file, the document is
wrong — fix it in the same turn.

```bash
grep -E "applicationId|namespace|versionCode|versionName|Sdk" app/build.gradle.kts
```

## Identity — permanent, do not "fix"

| Field | Value | Why it is the way it is |
|---|---|---|
| Play app name | `Touchgrass: Block Reels & Shorts` | 32 chars — **exceeds Play's 30-char app-name limit.** Fine for fastlane/IzzyOnDroid; needs a trim for the Play name field. |
| **applicationId** | **`com.touchgrassinc.app`** | `com.touchgrass.app` was taken on Play. Permanent. The AAB must match or upload is rejected. |
| Kotlin `namespace` | `com.touchgrass.app` | Independent of applicationId. **Never move sources to match the package.** |
| Category | `Productivity` | Not Health & Fitness — that contradicts the Data safety answers. Not Tools. |
| Target audience | 18+ only | Avoids the Families program paperwork. |
| Contact email | `nikodeveloper23@gmail.com` | Swap to `privacy@gettouchgrass.app` once that domain exists. |
| Source repo | `github.com/nikolozturkishvili1/Touchgrass` | Public, GPL-3.0, branch `main`. |
| Privacy URL (live) | `https://nikolozturkishvili1.github.io/Touchgrass/privacy.html` | The `#data-deletion` anchor doubles as the Data-safety deletion URL. |
| Landing (live) | `https://nikolozturkishvili1.github.io/Touchgrass/` | |
| Store listing copy | `fastlane/metadata/android/en-US/` | Single source. Do not retype descriptions. |
| Play assets | `marketing/play-assets/`, `marketing/screenshots/` | |

## Build

Windows; `java`/`keytool` are not on PATH.

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat bundleRelease      # AAB -> Play
.\gradlew.bat assembleRelease    # APK -> GitHub Releases / IzzyOnDroid
```

Signing reads `keystore.properties` (gitignored) → `keysets/*.jks` (gitignored).
Missing file ⇒ release packaging fails **by design**. That is the signal, not a bug.

## Hard guardrails

- **Never invent a Play Console answer.** Data safety and the accessibility
  declaration are legal declarations. Every approved answer is in `docs/RELEASE.md`.
  If a question is not answered there, stop and ask.
- Never commit `keystore.properties`, `*.jks`, `local.properties`, `*.aab`, `*.apk`.
- Never print a signing password, base64 keystore, `GITHUB_PAT`, or Resend key.
- Never push a `v*` tag before the four release secrets exist in GitHub Actions —
  the run fails by design (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
  `KEY_PASSWORD`). Niko sets these himself, in the browser.
- Never reuse a `versionCode` on a Play upload.
- Never invent a demo-video URL for the accessibility declaration. Record one.

## Gotchas that have cost time before

- **Managed publishing is ON.** Google approving a release does not ship it. You
  must click *Publishing overview → Publish changes* or testers get nothing.
- **Google Group membership ≠ tester opt-in.** Each tester must open the opt-in link
  and accept, individually. This has been the #1 false-progress trap on this project.
- **Two closed-testing tracks exist.** Use **"Alpha"**. Ignore "TouchGrassTrack".
- **Internal testing does not count** toward the production gate. Only closed
  testing does: **12 testers × 14 consecutive days** (lowered from 20 on 2024-12-11).
  Recruit ~15 — the clock pauses below 12.
- **Play and GitHub builds are signed with different keys** (Play re-signs). Users
  cannot update across sources in place. Say so wherever the app is linked.
- The commitment-lock OTP email does not send in beta — Resend is unverified.
  Disclose it to testers.

## Where to look next

| Need | File |
|---|---|
| What to do right now | `STATUS.md` |
| Step-by-step release procedure, all channels | `docs/RELEASE.md` |
| Verbatim Play Console legal answers | `docs/RELEASE.md` §3 |
| IzzyOnDroid inclusion request text | `docs/RELEASE.md` §2 |
| Browser-agent prompt for Play Console | `docs/RELEASE.md` §4 |
| Product behavior decisions | `docs/SPEC.md` |
| Tester recruiting message + sources | `marketing/TESTER_RECRUITING.md` |

Keep this skill accurate: when a value in the identity table changes, edit it here
in the same turn.
