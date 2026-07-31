# Touchgrass

**An Android app that blocks Reels, Shorts, and the other infinite-scroll feeds designed to eat your day.**

> Nothing to scroll. Go look out the window.

---

## What it does

Touchgrass turns off the parts of your apps that were built to keep you there forever, while leaving the rest of those apps working normally. You can still message friends, post, search, and watch the one video someone sent you. You just can't fall into the pit.

Blocked on day one:

- YouTube Shorts
- Instagram Reels — main feed, Explore reels grid, the Reels tab, and reels shared in DM
- TikTok "For You" feed
- Facebook Reels
- Snapchat Spotlight
- YouTube Shorts on Chrome (web)
- Short-form surfaces in Samsung Internet

You keep the rest of those apps. Touchgrass only watches the apps you've asked it to block, and only enough to know when a short-form feed has opened. See the in-app Trust Dashboard for the full plain-English breakdown.

---

## Why this one is different

There are already a handful of reel and shorts blockers on the Play Store. The dominant one ships a bad update every few months, kills its background service after a couple of days, runs ads, and keeps inching its prices up. Touchgrass exists because those things are fixable product decisions, not laws of Android.

The four wedges:

- &#10003; **Reliable.** A watchdog notices when the blocking service has stopped and surfaces it as a notification before you discover it mid-binge. Onboarding walks you through your specific OEM's battery-saver settings — Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, Huawei — instead of saying "go fix Android."
- &#10003; **Honest about privacy.** Open the Trust Dashboard inside the app to see exactly what Touchgrass can and cannot see. Nothing leaves your phone. No third-party analytics SDKs. The source is right here so you can verify it yourself.
- &#10003; **No ads. Ever.** A commitment, not a current state of the app.
- &#10003; **Free, with no subscription.** V1 ships fully free — every feature, including the email-OTP commitment lock, is available to everyone. There is no subscription, no trial that flips into one, and no in-app purchase wired up at launch. If we ever introduce a paid tier later, it will be a one-time price, never a subscription, and it will never paywall the core blocking or the commitment lock.

The commitment lock — email-OTP yourself if you keep impulse-disabling — is free and always will be. The people who need it most are not the people who should have to pay for it.

---

## Status

**Beta. Current release 0.1.2 (versionCode 3), targeting Android 16 (API 36), minSdk 29.**

The app is feature-complete for V1 and in closed testing on Google Play. Blocking, the Trust Dashboard, the reliability watchdog, and the commitment lock all work. Known gap in test builds: the commitment-lock one-time code is not wired to a live mail server yet, so that email does not arrive during the beta. Everything else is functional.

---

## Install

**Direct download:** signed APKs are published to [GitHub Releases](https://github.com/nikolozturkishvili1/Touchgrass/releases) — built by GitHub Actions from the tagged commit and signed with the project's release key. *(The first public release has not been cut yet; it lands with the 0.1.2 tag.)*

**Auto-updating:** add this repository to [Obtainium](https://github.com/ImranR98/Obtainium) and it will track GitHub Releases for you.

**IzzyOnDroid / F-Droid:** inclusion request planned, not yet submitted. Once listed, add the [IzzyOnDroid repo](https://apt.izzysoft.de/fdroid/) to your F-Droid client.

**Google Play:** in closed testing. Production access is gated behind Google's 12-tester / 14-day requirement for new developer accounts.

> Note: the Play build and the GitHub build are signed with different keys (Google re-signs Play uploads with Play App Signing). You cannot update from one to the other in place — pick one source and stay on it.

**Build from source:** `./gradlew assembleRelease` with a `keystore.properties` in the project root (see `keystore.properties.example`), or `./gradlew assembleDebug` for an unsigned debug build.

---

## Build from source

You'll need Android Studio Meerkat (2024.3.1) or newer and JDK 17. AGP 8.9.2 is not supported by older Studio versions — Ladybug will fail to sync.

1. **Clone the repo:**

   ```bash
   git clone https://github.com/nikolozturkishvili1/Touchgrass.git
   cd Touchgrass
   ```

2. **Install prerequisites:**
   - [Android Studio](https://developer.android.com/studio) Meerkat (2024.3.1) or newer — required by AGP 8.9.2.
   - JDK 17. Android Studio ships one; if you build from the command line, set `JAVA_HOME` to a JDK 17 install.
   - Android SDK Platform 36 and Build-Tools 36.x. Android Studio will offer to install these on first sync.

3. **Open the project:**
   - Android Studio: *File → Open* and point at the cloned `Touchgrass` directory.
   - Let Gradle sync. The first sync downloads dependencies and may take a few minutes.

4. **Build a debug APK:**

   ```bash
   ./gradlew assembleDebug
   ```

   On Windows PowerShell:

   ```powershell
   .\gradlew.bat assembleDebug
   ```

   The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

5. **Install on a device:**
   - Enable developer options and USB debugging on the device.
   - With the device connected, run `./gradlew installDebug`, or use *Run* in Android Studio.
   - Touchgrass runs on API 29 (Android 10) and newer, and targets API 36.

6. **Grant the Accessibility permission:**
   - Open Touchgrass on the device.
   - Walk through onboarding. When prompted, open Android's Accessibility settings, find **Touchgrass**, and toggle it on.
   - Without this permission Touchgrass cannot detect or block anything. The app explains what it does and does not see before asking — see the Trust Dashboard from the home screen.

7. **Optional but recommended:** when onboarding prompts you to disable battery optimization for Touchgrass, do it. On Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, and Huawei devices this is essentially required for the blocking service to survive overnight.

---

## Architecture

Touchgrass is a single-module Kotlin + Jetpack Compose app. The blocking engine is an `AccessibilityService` that the Android OS pushes UI events into; per-app `Detector` classes recognise short-form feeds and hand off to a `BlockingStrategy` that exits the feed, optionally with an overlay. A foreground service plus a WorkManager watchdog keeps the accessibility service honest and surfaces a notification if it ever stops responding. Stats are stored in Room; preferences in DataStore; DI via Hilt. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full walkthrough.

---

## Contributing

Contributions are welcome — especially detectors for new apps and walkthroughs for OEMs we haven't covered yet.

The easiest entry points, both marked `good first issue`:

- **Add a new detector.** Each supported app has its own `Detector` class under `accessibility/detectors/`. Adding a new one is a self-contained change with a clear contract and unit tests. If your favourite app ships a new short-form feed, this is the place to start.
- **Add an OEM battery walkthrough.** Each OEM lives in its own JSON file under `app/src/main/assets/oem/`. If you own a phone whose manufacturer isn't covered, you are uniquely positioned to write the steps. Annotated screenshots welcome.

See [CONTRIBUTING.md](CONTRIBUTING.md) for code style, the PR process, and the working agreement that keeps the project honest — no ads, no analytics SDKs, no subscriptions, no growth-hack gamification.

By contributing you agree your changes will be licensed under GPL-3.0. We follow a standard Contributor Covenant — see [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

---

## Privacy

Plainly: **nothing leaves your phone.**

- Touchgrass observes UI events from the apps you've asked it to block, and only those apps. It does not watch your whole device.
- It does not read your messages, your passwords, your photos, or your keyboard input.
- There are no third-party analytics SDKs in the release build. Not Firebase Analytics, not Mixpanel, not Amplitude.
- Stats live in a local SQLite database on your device. There is no server to send them to.
- The only network call the app makes is for the optional email-OTP commitment lock — and only when you opt in. Nothing else.

Open the **Trust Dashboard** inside the app (Settings → "What can Touchgrass see?") for the full plain-English version, including what we *can* see and why we need it. For the formal policy, see [docs/PRIVACY.md](docs/PRIVACY.md).

If any of this changes, it will change in this repository in public, with a commit message that says so.

---

## Security

If you've found a security issue, please report it privately. See [SECURITY.md](SECURITY.md) for the disclosure address and what to expect. Please don't open a public issue for anything that could put users at risk.

---

## License

Touchgrass is licensed under the **GNU General Public License v3.0**. See [LICENSE](LICENSE) for the full text.

GPL-3.0 is a deliberate choice. It keeps Touchgrass and any derivative work open, which is the only way the privacy promise on the box can be verified by anyone who cares to check.

---

*Touchgrass is built by a solo developer in public. Progress, mistakes, and ship dates all live on this repo.*
