# Browser-agent prompt — deploy Touchgrass to Google Play

> Paste the block below into your in-browser Claude extension. It is self-contained:
> every exact value, file path, and stop-condition is included. Generated 2026-05-31.

---

```
You are operating my web browser to publish my Android app, "Touchgrass", to the Google
Play Console. I am logged in as nikodeveloper23@gmail.com. Work carefully and DO NOT
fabricate any answer — every value you need is given below verbatim. If a value is not
given and you cannot derive it from this prompt, STOP and ask me rather than guessing.

=== HARD RULES ===
1. NEVER invent answers on the Data safety form or the Accessibility declaration — these
   are legal declarations. Use only the exact text I provide. If a question appears that
   I did not give an answer for, STOP and ask me.
2. Do NOT pay for anything, do NOT change account/billing settings, do NOT delete anything.
3. Do NOT publish to Production. We are doing Internal testing only.
4. After each major step, briefly tell me what you did and paste any URL the console gives
   you. Take a screenshot if a step looks ambiguous.
5. If you hit identity verification, payment, OTP, or a CAPTCHA, STOP and hand control to me.

=== KEY FACTS (use these exactly) ===
- App name: Touchgrass: Block Reels & Shorts
- Package / application ID: com.touchgrassinc.app
- Version: versionName 0.1.0, versionCode 1, release name "0.1.0 (1)"
- Default language: English (United States) – en-US
- App or game: App. Free or paid: Free.
- Category: Productivity
- Contact email: nikodeveloper23@gmail.com
- Public source repo: https://github.com/nikolozturkishvili1/Touchgrass
- Privacy policy URL (must be made live in STEP 0 first):
  https://nikolozturkishvili1.github.io/Touchgrass/privacy.html
- Files on this computer (use the file picker to browse to them):
  - Signed app bundle: D:\My_Projects\Touchgrass\app\build\outputs\bundle\release\app-release.aab
  - App icon (512x512): D:\My_Projects\Touchgrass\marketing\play-assets\icon-512.png
  - Feature graphic (1024x500): D:\My_Projects\Touchgrass\marketing\play-assets\feature-1024x500.png

=== STEP 0 — make the privacy policy URL live (do this FIRST) ===
The Play form requires a working privacy URL and ours currently returns 404.
1. Go to https://github.com/nikolozturkishvili1/Touchgrass/settings/pages
2. Under "Build and deployment" > "Source", choose "GitHub Actions".
3. Go to the Actions tab: https://github.com/nikolozturkishvili1/Touchgrass/actions
   Find the "Deploy Pages" workflow. If it has not run, click "Run workflow" on the main
   branch. Wait until it finishes with a green check.
4. Open https://nikolozturkishvili1.github.io/Touchgrass/privacy.html in a new tab.
   Confirm it loads a real privacy-policy page (NOT a 404). Only once it loads should you
   use this URL later. If it still 404s after the workflow is green, wait 1–2 minutes and
   refresh; if it still fails, STOP and tell me.

=== STEP 1 — prerequisite: developer account ===
Go to https://play.google.com/console . If I do NOT have a verified developer account
(it asks me to pay the $25 fee or to complete identity verification), STOP and tell me —
I must finish that personally. Only continue if the console dashboard loads with the
ability to create an app.

=== STEP 2 — create the app ===
Click "Create app". Fill:
- App name: Touchgrass: Block Reels & Shorts
- Default language: English (United States) – en-US
- App or game: App
- Free or paid: Free
- Tick both declarations (Developer Program Policies; US export laws).
Click "Create app".

=== STEP 3 — App content (Policy > App content) — REQUIRED, no screenshots needed ===
Complete every section:
- Privacy policy: paste the live URL from STEP 0.
- Ads: "No, my app does not contain ads".
- App access: "All functionality is available without special access" (there is no login wall).
- Content ratings: start the questionnaire. Email: nikodeveloper23@gmail.com.
  Category: "Utility, Productivity, Communication, or Other". Answer NO to every question
  about violence, sexuality, drugs, gambling, profanity, controlled substances, user
  interaction sharing location, etc. Submit. Expected rating: Everyone.
- Target audience and content: target age group = "18 and over" only (do NOT tick any
  younger bracket). "Is your app designed for or appealing to children?" = No.
- News app: No.
- COVID-19 contact tracing or status app: No (not one of these).
- Data safety: see STEP 4 — fill it exactly.
- Government apps: No.
- Financial features: "My app doesn't provide any financial features".
- Health: declare none / not a health app.
- Advertising ID: app does NOT use an advertising ID (No).

=== STEP 4 — Data safety (Policy > App content > Data safety) — EXACT, legal declaration ===
- "Does your app collect or share any of the required user data types?" = Yes.
- Add ONE data type only: Personal info > Email address.
  - Is this data collected, shared, or both? Collected = Yes. Shared = No.
  - Is this data processed ephemerally? No.
  - Is collection required or optional? Optional ("Users can choose whether this data is
    collected") — it is only collected if the user enables the optional commitment lock.
  - Why is this collected? Tick "App functionality" only. (It sends a one-time code for
    the commitment-lock feature.)
  - Is this data linked to the user's identity? No.
  - Is this data used for tracking? No.
- Do NOT add any other data type. No location, contacts, photos/videos, files, messages,
  device/other IDs, app activity, analytics, crash logs, or financial info are collected.
- Security practices:
  - "Is your app's data encrypted in transit?" = Yes.
  - "Do you provide a way for users to request that their data is deleted?" = Yes (users
    delete it by disabling the commitment lock or uninstalling the app).
Save and submit the section.

=== STEP 5 — Accessibility / sensitive permission declaration ===
Touchgrass uses an AccessibilityService, so the console will require a justification (this
may appear under App content "Permissions/Sensitive app permissions", or be requested at
review). Use this text VERBATIM. Do not paraphrase.

What the app uses the AccessibilityService for:
"Touchgrass detects when the user opens short-form-feed UI (Reels, Shorts, TikTok,
Spotlight) in apps they explicitly added to a block list, and intercepts back to exit the
feed. The Accessibility service is the only Android API that can both observe foreground
app UI events on third-party apps and inject a back gesture without invasive permissions."

Why no other API works:
"Android does not expose foreground-app UI events to third-party apps through any other
API. Apps like Usage Stats only report aggregate time, not which screen is open. No other
API can perform GLOBAL_ACTION_BACK to intercept a feed."

How users are told before the permission is requested:
"We open the in-app Trust Dashboard during onboarding, before Accessibility is requested.
The on-device accessibility-service description string also explains the scope."

Open-source proof (if a link field is offered):
https://github.com/nikolozturkishvili1/Touchgrass

If it asks for a demo video URL and I have not given you one, STOP and ask me — do not invent a URL.

=== STEP 6 — upload the build to Internal testing (the "live today" track) ===
Go to: Test and release > Testing > Internal testing.
1. Click "Create new release".
2. App signing: when prompted, OPT IN to Play App Signing (let Google manage the signing
   key; our uploaded bundle uses the upload key). Accept the default Google-generated key.
3. Upload the app bundle: browse to
   D:\My_Projects\Touchgrass\app\build\outputs\bundle\release\app-release.aab
   Wait for it to finish processing (it should show versionCode 1 / versionName 0.1.0).
4. Release name: 0.1.0 (1)
5. Release notes (paste under en-US):
---
v0.1.0 — first internal test build

Blocks YouTube Shorts, Instagram Reels (feed, Explore, tab, DM), TikTok For You +
Following, Facebook Reels, Snapchat Spotlight, and shorts-in-Chrome / Samsung Internet.
Per-OEM battery setup, watchdog, pause-with-friction, free commitment lock, in-app Trust
Dashboard. No ads. No subscription. No analytics. Open source.

Note for testers: the commitment-lock email isn't wired to a live mail server yet, so its
code won't arrive during this test — everything else is fully functional.
Report bugs: https://github.com/nikolozturkishvili1/Touchgrass/issues
---
6. Click "Next" / "Save". Resolve any blocking errors it lists. If it complains about a
   missing store listing, that is fine for INTERNAL testing — store listing is not required
   for the internal track; only complete what it marks as required.
7. On the Testers tab: create an email list named "Touchgrass internal" and add my email
   nikodeveloper23@gmail.com (I will add the rest). Save.
8. Click "Review release", then "Start rollout to Internal testing". Confirm.
9. Copy the tester opt-in URL it shows and paste it back to me.

=== STEP 7 — store listing (fill what you can; it does NOT block internal testing) ===
Go to Grow > Store presence > Main store listing. Fill:
- App name: Touchgrass: Block Reels & Shorts
- Short description (paste exactly):
Block doomscrolling on Reels, Shorts, TikTok. No ads. No subscription. Free.
- Full description (paste exactly):
The reel blocker that actually works.

Touchgrass blocks the parts of Instagram, TikTok, YouTube, Facebook and Snapchat that are designed to keep you scrolling forever. You can still message friends, post, search, watch a video someone sends you. You just can't fall into the pit.

WHAT MAKES TOUCHGRASS DIFFERENT

✓ Reliable. No "stopped working after 3 days." Touchgrass has a watchdog that catches itself if it ever stops, and an OEM-specific setup that handles Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, and Huawei battery killers correctly.

✓ Honest about privacy. Open the Trust Dashboard inside the app to see exactly what Touchgrass can and cannot see. We don't watch your whole screen. We don't read your messages. Nothing leaves your phone except — only if you choose to — a one-time code to your own email for the commitment lock.

✓ Free. The whole app. No ads, ever. No subscription, ever. No upgrade screen nagging you. You install it, it works.

✓ Open source. Audit the code yourself: github.com/nikolozturkishvili1/Touchgrass. Licensed GPL-3.0.

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
• Enable the commitment lock (free) so disabling Touchgrass requires an email code

SETUP TAKES 3 SCREENS

1. Grant the Accessibility permission (Touchgrass uses it for one thing: noticing reels).
2. Disable battery optimization (we walk you through your specific phone's settings — Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, and Huawei all included).
3. Pick which apps to block (defaults: all of them).

Open source: github.com/nikolozturkishvili1/Touchgrass
Privacy: gettouchgrass.app/privacy
- App icon: upload D:\My_Projects\Touchgrass\marketing\play-assets\icon-512.png
- Feature graphic: upload D:\My_Projects\Touchgrass\marketing\play-assets\feature-1024x500.png
- Phone screenshots: I do NOT have these yet. Play requires at least 2 to PUBLISH the
  store listing (this blocks Closed testing, NOT internal testing). Save the listing as a
  draft with what you have and TELL ME the listing cannot be fully published until I supply
  2–8 phone screenshots (1080x1920 portrait). Do not invent or generate screenshots.

=== STEP 8 — store settings (Grow > Store presence > Store settings) ===
- App category: Productivity
- Tags: choose the closest available to: block reels, block shorts, screen time, digital
  wellbeing, focus, dopamine detox.
- Contact email: nikodeveloper23@gmail.com
- Website (optional): https://nikolozturkishvili1.github.io/Touchgrass/

=== FINISH ===
Summarize for me: which sections are complete, the internal-testing rollout status, the
tester opt-in URL, and a checklist of anything still blocked (especially: phone screenshots
for the public store listing, and Closed testing which needs them).
```
