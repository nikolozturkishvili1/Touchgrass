# STATUS — Touchgrass

**Updated: 2026-07-30.** Owner: Niko (solo).
Rewrite this file at the end of every session. If it is older than the last
commit, it is lying — fix it before doing anything else.

Current build: **0.1.2 (versionCode 3)**, targets Android 16 / API 36, minSdk 29.
Feature-complete for V1. Two distribution tracks run in parallel.

---

## Next action

**Confirm the four GitHub Actions signing secrets exist, then tag `v0.1.2`.**

Go to
`https://github.com/nikolozturkishvili1/Touchgrass/settings/secrets/actions` and
check for `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
Set any that are missing — **by hand, in the browser. Never paste them into a chat
or a file.**

Once all four are present:

```powershell
cd D:\My_Projects\Touchgrass
powershell -ExecutionPolicy Bypass -File .\RELEASE_NOW.ps1
```

The script reads the version from `app/build.gradle.kts`, commits anything
outstanding, pushes `main`, then asks you to confirm the secrets before it pushes
the tag. The tag is what triggers the signed build and the public GitHub Release.

A tag pushed without the secrets fails the build and burns that version number —
which is why the script refuses to do it unprompted.

---

## Track A — ship outside Play (unblocked, self-serve)

GitHub Releases → Obtainium → IzzyOnDroid. Does not depend on Google. Full
procedure in `docs/RELEASE.md` § "Distribution outside Play".

| # | Step | State |
|---|---|---|
| 1 | Commit the working tree | **← next action** |
| 2 | Confirm the four GitHub Actions release secrets are set | Blocked — see below |
| 3 | `git tag v0.1.2 && git push origin v0.1.2` → CI builds signed APK + AAB, publishes a **public** (not draft) Release | Waiting on 1 + 2 |
| 4 | Verify `Touchgrass-v0.1.2.apk` is attached and the Release is not a draft | Waiting on 3 |
| 5 | Open the IzzyOnDroid inclusion request (text is ready in `docs/RELEASE.md`) | Waiting on 4 |
| 6 | Post the Obtainium line wherever the app gets mentioned | Waiting on 4 |

Ready and verified: `fastlane/metadata/android/en-US/` is complete (title, short +
full description, changelog for versionCode 3, icon, feature graphic, five phone
screenshots). IzzyOnDroid and F-Droid read this layout automatically.

Timing: GitHub Release + Obtainium are live the moment the tag lands. IzzyOnDroid
is typically a few days to two weeks. F-Droid proper is weeks to months — optional,
do not wait on it.

---

## Track B — Google Play closed testing (blocked on people, not code)

The gate: **12 testers opted in for 14 consecutive days** before a new personal
developer account can apply for production. The clock only counts days with 12+
opted in simultaneously — it pauses if anyone drops out. Recruit ~15 for buffer.

| Item | State |
|---|---|
| Closed testing track | Live. Use the track named **"Alpha"**. Ignore the duplicate **"TouchGrassTrack"**. |
| Testers opted in | **1 of 12.** This is the critical path — everything else waits on it. |
| Google Group | Has 12 members, but **none of them ever accepted the opt-in link.** Membership ≠ opt-in. |
| Managed publishing | **ON.** After Google approves a release it sits in *Publishing overview → Changes ready to publish* until you click **Publish changes**. Easy to forget; testers get nothing until you do. |
| Track release version | Alpha is on **0.1.1 (versionCode 2)**, published 2026-07-17. The API-36 build (0.1.2 / 3) has **not** been uploaded yet — Play flagged the target-API-36 mandate, deadline **2026-08-31**, after which updates are blocked. |
| Screenshots | Done — five portrait shots exist (`marketing/screenshots/`, mirrored into fastlane). This blocker is cleared. |

The one thing that moves Track B: **get 11 more people to actually open the opt-in
link and accept.** Recruiting message and sources are in
`marketing/TESTER_RECRUITING.md`.

---

## Blocked

- **GitHub Actions release secrets — unverified.** `release.yml` needs
  `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` under
  Repo → Settings → Secrets and variables → Actions. Without them a `v*` tag push
  fails loudly, by design. **Niko must set these by hand — never paste them into a
  chat or a file.**
- **Commitment-lock email does not send.** `gettouchgrass.app` is not registered and
  Resend is not verified, so the one-time code never arrives in beta builds.
  Disclose this to testers. Not a blocker for either track; is a blocker for
  production launch.
- **Play production access** — gated entirely on Track B's 12 × 14 rule.
- **Play app name is 2 characters too long.**
  `fastlane/metadata/android/en-US/title.txt` is `Touchgrass: Block Reels & Shorts`
  — 32 chars. Play's app-name field caps at 30. Fine for fastlane/IzzyOnDroid; the
  Play field needs a trim (`Touchgrass: Block Reels+Shorts` is exactly 30). Niko's
  wording call.
- **Privacy policy exists in two places and they have drifted.** The page actually
  served is `marketing/landing-page/privacy.html` (published by `pages.yml`);
  `docs/PRIVACY.md` is the long form and has extra sections the live page lacks.
  Reconcile before production.

---

## Decided — do not relitigate

- **V1 is fully free.** No ads, no subscription, no paywall on blocking or the
  commitment lock. If a paid tier ever appears it is one-time, never a subscription.
- **`applicationId` stays `com.touchgrassinc.app`**, `namespace` stays
  `com.touchgrass.app`. They differ on purpose. Do not move sources.
- **Category is Productivity**, not Health & Fitness — Health & Fitness contradicts
  the Data safety answers.
- **Merchant registration is unavailable for Georgia**, so paid distribution through
  Play was never an option anyway. Verify the account country before revisiting.
- **Ship outside Play in parallel** rather than waiting on Google.

---

## Session log

Keep this short — newest first, one line each, drop entries older than ~10.

- **2026-07-30** — Docs rebuilt and **committed** (the tree had been dirty since
  Jul 22). Added `CLAUDE.md` + this file; merged five overlapping release docs into
  `docs/RELEASE.md`; corrected stale version/SDK values across
  README/SPEC/ARCHITECTURE; retired a mock Play-release doc that carried a wrong
  package name and category; rewrote the deployment skill; made `RELEASE_NOW.ps1`
  refuse to tag before the signing secrets are confirmed; fixed `PRIVACY_URL` to the
  live GitHub Pages address (it pointed at the unregistered domain). Not yet tagged.
- **2026-07-22** — Bumped to target API 36 (AGP 8.9.2, Gradle 8.11.1), released as
  0.1.2 in `CHANGELOG.md`. Still uncommitted.
- **2026-07-14** — Alt-distribution path set up: fastlane metadata, `release.yml`
  publishes public releases with an APK, IzzyOnDroid request drafted.
