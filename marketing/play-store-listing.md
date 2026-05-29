# Touchgrass — Play Store listing

> Final copy for the Google Play Console listing.
> Reflects the free-for-start monetization pivot (no Plus tier at launch).

---

## App title (50 chars max)

```
Touchgrass: Block Reels & Shorts
```

(33 chars — well under the cap.)

## Short description (80 chars max)

```
Block doomscrolling on Reels, Shorts, TikTok. No ads. No subscription. Free.
```

(78 chars.)

## Long description (~4000 chars max)

```
The reel blocker that actually works.

Touchgrass blocks the parts of Instagram, TikTok, YouTube, Facebook and Snapchat that are designed to keep you scrolling forever. You can still message friends, post, search, watch a video someone sends you. You just can't fall into the pit.

WHAT MAKES TOUCHGRASS DIFFERENT

✓ Reliable. No "stopped working after 3 days." Touchgrass has a watchdog that catches itself if it ever stops, and an OEM-specific setup that handles Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, and Huawei battery killers correctly.

✓ Honest about privacy. Open the Trust Dashboard inside the app to see exactly what Touchgrass can and cannot see. We don't watch your whole screen. We don't read your messages. Nothing leaves your phone except — only if you choose to — a one-time code to your own email for the commitment lock.

✓ Free. The whole app. No ads, ever. No subscription, ever. No upgrade screen nagging you. You install it, it works.

✓ Open source. Audit the code yourself: github.com/nikolozturkishvili1/touchgrass-android. Licensed GPL-3.0.

WHAT GETS BLOCKED

• YouTube Shorts
• Instagram Reels (main feed, Explore, Reels tab, DM-shared)
• TikTok For You and Following feeds
• Facebook Reels (the immersive viewer)
• Snapchat Spotlight
• YouTube Shorts in Chrome
• YouTube Shorts in Samsung Internet

The rest of those apps still works normally. Touchgrass is a scalpel, not a sledgehammer.

THINGS YOU CAN DO

• Take a one-off "peek" if you really need to (configurable friction: wait 5s, solve a math problem, type a 30-character code, or breathe for 30s)
• Turn on Quick Peek to allow one reel per app per session (great for the one a friend sent you in a DM, while still blocking the scroll-to-next)
• Set a daily pause budget so you can't peek your way to a binge
• Enable the commitment lock (free) so disabling Touchgrass requires an email code — saved hundreds of competitor users when impulse struck

SETUP TAKES 3 SCREENS

1. Grant the Accessibility permission (Touchgrass uses it for one thing: noticing reels).
2. Disable battery optimization (we walk you through your specific phone's settings — Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, and Huawei all included).
3. Pick which apps to block (defaults: all of them).

Open source: github.com/nikolozturkishvili1/touchgrass-android
Privacy: gettouchgrass.app/privacy
```

(~2.0k chars — leaves room for a couple of reviews quote-bumps after launch.)

## ASO keyword targets

Order roughly by intent strength:

1. block reels
2. block shorts
3. block tiktok
4. stop scrolling
5. doomscroll
6. screen time
7. digital wellbeing
8. focus app
9. dopamine detox
10. scroll blocker
11. no scroll
12. instagram reels blocker
13. youtube shorts blocker
14. brainrot blocker

Tags to set in Play Console:
- Primary category: **Productivity** (NOT "Lifestyle" — Productivity has the screen-time apps cluster and the Search competitors)
- Secondary category: **Health & Fitness** (digital-wellbeing collection placement)
- Content rating: **Everyone**
- Contains ads: **No**
- In-app purchases: **No** (V1 is free; if a paid tier returns later, switch this)

## Graphics requirements

| Asset | Size | Status |
|---|---|---|
| App icon (high-res) | 512 × 512 PNG (32-bit, with alpha) | TODO — replace placeholder vector |
| Feature graphic | 1024 × 500 PNG / JPG (no transparency) | TODO — hero scene of "Touchgrass is on" home screen + a blurred-out reel in the background, blade-of-grass mark on the right |
| Phone screenshots (min 2, max 8) | 1080 × 1920 portrait, PNG/JPG | TODO — see below for which screens to capture |
| Tablet screenshots (optional) | 1200 × 1920 portrait | skip for V1 |

### Screenshot plan (8 screens)

1. **Home — On** with a `Saves today: 47` stat. Caption: "Reels and shorts are blocked. Nothing else changes."
2. **Stats screen** with the big "Today" card + top surfaces breakdown. Caption: "See what you stopped."
3. **Onboarding step 2 (Accessibility)** with both status cards ✓. Caption: "One permission. Plain English about what it sees."
4. **Trust Dashboard** scrolled to "What Touchgrass cannot see". Caption: "Audit it for yourself. The whole app is open source."
5. **Pause picker** with `Daily cap: 18 min left today`. Caption: "Pause if you have to. Friction if you mean it."
6. **Friction (breathing)** mid-cycle, circle expanded. Caption: "Breathe before you scroll."
7. **Commitment lock** OTP entry screen. Caption: "Lock yourself out. Get back in via email."
8. **Settings — Battery walkthrough card** for Xiaomi. Caption: "We know your phone. The 7 worst battery killers, all handled."

## Pricing & distribution

- **Price:** Free
- **Countries:** All — start with worldwide availability; tighten only if Play Console flags a region
- **Distribution:** Initial release to **Closed Testing** track (≥20 testers required for production graduation since Nov 2023)
- **Device compatibility:**
  - Min Android 10 (API 29)
  - Target Android 15 (API 35) — bump to 16 once stable to keep ahead of Play deadlines
  - Phone only at V1 (no tablets / TV / Wear / Auto)
- **Content rating:** Self-assessed via Play Console questionnaire — answer "no" to all sensitive-content categories

## What's New (first release)

```
v0.1.0 — first public release

• Blocks YouTube Shorts, Instagram Reels (feed, Explore, tab, DM), TikTok For You + Following,
  Facebook Reels, Snapchat Spotlight, and shorts-in-Chrome / Samsung Internet.
• Per-OEM battery setup for Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, Huawei.
• Watchdog catches the service if it stops and alerts you.
• Pause flow with four friction modes (wait, math, code, breathe).
• Free commitment lock with email OTP.
• Trust Dashboard inside the app — see exactly what it can and can't see.
• No ads. No subscription. No analytics. Open source.

Found a bug? github.com/nikolozturkishvili1/touchgrass-android/issues
```

## Pre-submission gates

Per Play Console policy, before you can hit "Publish":

- [ ] Privacy policy URL set (use `https://gettouchgrass.app/privacy`)
- [ ] Data safety form completed:
  - Data collected: **email address** (only if user enables commitment lock; not linked to identity; not shared; processed on transactional-email service; user can delete by disabling the lock)
  - Data shared: **none**
  - Data security: **data encrypted in transit** (HTTPS for OTP send only); **user can request deletion** (disable lock or uninstall)
- [ ] Target API level statement (35 — current Play requirement)
- [ ] **Accessibility Use Declaration** (required for any app using AccessibilityService outside a11y purposes):
  - Description: "Touchgrass detects when the user opens short-form-feed UI (Reels, Shorts, TikTok, Spotlight) in apps they explicitly added to a block list, and intercepts back to exit the feed. The Accessibility service is the only Android API that can both observe foreground app UI events on third-party apps and inject a back gesture without invasive permissions."
  - Required disclosure shown to user: "We open the Trust Dashboard during onboarding, before Accessibility is requested. The on-device accessibility-service description string also explains the scope."
  - Why this functionality isn't possible without Accessibility: "Android does not expose foreground-app UI events to third-party apps through any other API. Apps like Usage Stats only report aggregate time, not which screen is open. No other API can perform `GLOBAL_ACTION_BACK` to intercept a feed."
  - Open source proof: link to repo.
- [ ] 20+ internal testers opted in through Closed Testing for 14+ days before promotion to Production (Play's new rule since 2023)
- [ ] Closed Testing → Production promotion gate satisfied
