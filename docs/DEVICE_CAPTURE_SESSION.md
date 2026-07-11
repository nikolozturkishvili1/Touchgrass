# Touchgrass — device capture session (video + screenshots)

One phone session that produces **both** remaining Play blockers:
1. the **Accessibility prominent-disclosure video** (required by the Accessibility declaration form), and
2. the **8 store screenshots** (required by the Closed-testing store listing).

Do them in the order below — the video naturally walks through most of the screenshot screens.

---

## 0 · Pre-flight (5 min)

- [ ] Use a **real device** running the release/debug build with onboarding **not yet completed** (so you can film onboarding). If you've already finished onboarding, clear app data: Settings → Apps → Touchgrass → Storage → Clear data.
- [ ] Install the apps you'll demo blocking: **Instagram** and **YouTube** (TikTok optional but nice).
- [ ] Clean status bar: full battery (or plug in), Wi-Fi on, **Do Not Disturb on** so no notifications pop into a shot.
- [ ] Find your **screen recorder** (swipe down Quick Settings → Screen record) and **screenshot** gesture (Power + Volume-Down on most phones).
- [ ] Optional: silence the recorder mic and add captions later, OR narrate live — either is fine.

> **Screenshot size:** native portrait phone screenshots are accepted by Play (rule: PNG/JPG, 320–3840 px per side, aspect ratio ≤ 2:1). You do **not** have to hit exactly 1080×1920 — your phone's native portrait shot is fine. Capture the **raw screen**; marketing caption overlays are optional polish you can add later.

---

## PART A · The accessibility video (record first, ≤ 3 min)

Start the screen recorder, then:

1. **In-app disclosure (~40s).** Launch Touchgrass → go through onboarding to the **Accessibility step** → slowly show the on-screen text that explains what the service does → tap **Trust Dashboard** → scroll through "What Touchgrass can / cannot see," then back.
   - Caption/say: *"Before requesting Accessibility, Touchgrass shows this disclosure explaining exactly what it does and what it can and cannot access."*
2. **Enable the service (~30s).** Tap to enable → in system Accessibility settings toggle **Touchgrass on** → return to the app.
   - Caption/say: *"The user explicitly turns the service on."*
3. **Show it working (~60s).** Open **Instagram → Reels** → Touchgrass exits the feed. Repeat with **YouTube → Shorts**.
   - Caption/say: *"Touchgrass detects the short-form feed and exits it — only for the apps the user chose to block."*
4. **Why accessibility (~20s).** Caption/say: *"Android exposes no other API that lets a third-party app detect which screen is open in another app and exit it. Touchgrass processes this entirely on-device — nothing is collected, stored, or sent."*

Stop recording. **Upload to YouTube as Unlisted** → copy the URL → paste into Play Console → App content → **Accessibility services → Prominent disclosure**, then tick *"I acknowledge my video meets the requirements."*

---

## PART B · The 8 store screenshots

Capture each screen below (raw portrait screenshot). The "caption" is the marketing line for later — not required on the image itself. Save with the suggested filename.

| # | Screen to capture | Set it up so it shows… | Filename | Caption (optional overlay) |
|---|---|---|---|---|
| 1 | **Home — On** | the active/"on" state with a saves stat | `shot-1-home-on.png` | "Reels and shorts are blocked. Nothing else changes." |
| 2 | **Stats** | the "Today" card + top-surfaces breakdown | `shot-2-stats.png` | "See what you stopped." |
| 3 | **Onboarding · Accessibility step** | both status cards ✓ | `shot-3-accessibility.png` | "One permission. Plain English about what it sees." |
| 4 | **Trust Dashboard** | scrolled to "What Touchgrass cannot see" | `shot-4-trust.png` | "Audit it yourself. The whole app is open source." |
| 5 | **Pause picker** | the daily-cap line visible | `shot-5-pause.png` | "Pause if you have to. Friction if you mean it." |
| 6 | **Friction · breathing** | mid-cycle, circle expanded | `shot-6-breathe.png` | "Breathe before you scroll." |
| 7 | **Commitment lock · OTP entry** | the code-entry screen | `shot-7-lock.png` | "Lock yourself out. Get back in via email." |
| 8 | **Settings · Battery walkthrough** | an OEM card (e.g. Xiaomi/Samsung) | `shot-8-battery.png` | "We know your phone. The worst battery killers, handled." |

> Use the **real numbers the app shows** — don't fake stats. The captions above are aspirational marketing text, not a requirement.

Minimum to roll out Closed testing is **2** screenshots; capture all 8 if you can — more is better for conversion.

---

## PART C · After the session

- [ ] Drop all 8 PNGs into **`marketing/play-assets/`** (they're safe to commit — no PII).
- [ ] Play Console → **Grow → Store presence → Main store listing** → upload the screenshots (+ existing `icon-512.png`, `feature-1024x500.png`) → paste the short + full description.
- [ ] Play Console → App content → **Accessibility services** → paste the YouTube URL + tick the acknowledgment → Save.
- [ ] You're now unblocked for the Closed-testing rollout (see the "Play Console — live checkpoint" in the `touchgrass-deployment` skill for the remaining clicks).

> Both of these were the only hard blockers. After this session: build the AAB, upload to Closed testing, opt in to Play App Signing, complete the rest of App content, start rollout, and send the opt-in link to your testers.
