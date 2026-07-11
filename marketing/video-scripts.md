# Touchgrass — video scripts

Three scripts. **Script 1 is a compliance video (factual, required by Google).** Scripts 2 and 3 are
**marketing** videos (emotional, lifestyle) — keep them completely separate from Script 1.

All app facts here are real (from the codebase): Touchgrass blocks short-form feeds — YouTube Shorts,
Instagram Reels (feed/Explore/Reels tab/DM-shared), TikTok For You + Following, Facebook Reels,
Snapchat Spotlight, and Shorts in Chrome + Samsung Internet — while leaving messaging, posting, and
searching in those apps working. On-device + private (Trust Dashboard shows what it can/can't see;
nothing leaves the phone except an optional email code). Free, no ads, no subscription, open source
(GPL-3.0). Tagline: **"Touch grass. For real this time."**

---

## SCRIPT 1 — Accessibility compliance video (REQUIRED, ~2 min, unlisted YouTube)

Plain screen recording of the app. No lifestyle footage, no hype. Voiceover or on-screen captions.

| # | On screen | Voiceover / caption |
|---|---|---|
| 1 | Launch app, onboarding → **Accessibility step**; show the explanation text | "Touchgrass is a focus app that blocks short-form video feeds in the apps you choose." |
| 2 | Tap **Trust Dashboard**, scroll "what it can / cannot see", go back | "Before it asks for the Accessibility permission, Touchgrass shows this disclosure — explaining exactly what the service does and what it can and cannot access." |
| 3 | Go to system Accessibility settings, toggle **Touchgrass ON**, return | "You turn the service on yourself, in your phone's Accessibility settings." |
| 4 | Open **Instagram → Reels** → it exits. Then **YouTube → Shorts** → it exits | "When you open a short-form feed in a blocked app, Touchgrass detects it and exits the feed." |
| 5 | (hold on the app's block-list screen) | "Touchgrass only watches the specific apps you add to your block list. It reads screen content on-device to detect a feed — nothing is collected, stored, or sent anywhere." |
| 6 | (end card / repo URL) | "Accessibility is the only Android API that lets an app detect which screen is open in another app and navigate back out of it. That is why Touchgrass requires it. The full source is public." |

End. Upload **Unlisted** → paste URL into App content → Accessibility services → Prominent disclosure → tick the acknowledgment.

---

## SCRIPT 2 — Short promo (vertical 9:16, 20–30s — Shorts / Reels / TikTok)

Fast, hooky, captions on screen, trendy music. Mix lifestyle B-roll with 1–2 app clips.

| Time | Visual | On-screen text / VO |
|---|---|---|
| 0–3s | Hand grabs phone at a red light; thumb opens Instagram on reflex | "You unlocked your phone to check the time." |
| 3–7s | Quick cuts: still scrolling at the red light → light turns green | "Twenty minutes of Reels later…" |
| 7–12s | Cut: two friends at dinner, both staring at their phones | "At dinner." |
| 12–16s | Cut: beautiful beach, but the person is filming/watching TikTok, not the view | "On the vacation you saved up for." |
| 16–20s | Hard cut to phone: open Reels → **Touchgrass exits the feed** | "Touchgrass blocks the feed — not the app." |
| 20–25s | App clip: messages + a friend's shared video still work | "Reels, Shorts, TikTok: gone. Messages and posts: still there." |
| 25–30s | End card: logo + grass | "Free. No ads. Open source. **Touch grass — for real this time.** → Get it on Google Play." |

---

## SCRIPT 3 — Long promo (~75–90s — store listing video + YouTube)

Tells the full story. Use this as the **one promo video** the Play listing allows (paste a YouTube URL).

**[0–8s · HOOK]**
Visual: montage — thumb scrolling in bed at 1am; scrolling at a red light; scrolling at a dinner table while a friend talks.
VO: *"You didn't download these apps to lose your life to a feed. But here we are."*

**[8–20s · THE PROBLEM]**
Visual: a real beach/vacation shot — person ignoring it, watching other people's vacations on TikTok.
VO: *"The feeds are built to keep you scrolling forever. Reels. Shorts. TikTok. You go to rest, and the scroll comes with you."*

**[20–30s · THE TURN]**
Visual: person puts the phone down, looks up — at the friend, at the view, at the road ahead.
VO: *"What if your phone just… stopped serving you the pit?"*

**[30–55s · THE SOLUTION — app demo]**
Visual: app clips.
VO, over the demo:
- *"This is Touchgrass."* (logo)
- *"Open Reels, Shorts, or TikTok — Touchgrass detects the feed and gets you out."* (show Instagram Reels + YouTube Shorts exiting)
- *"It's a scalpel, not a sledgehammer. You can still message friends, post, search, and watch a video someone sends you."* (show messaging working)
- *"Need a peek? Earn it — wait, solve a problem, or just breathe for thirty seconds first."* (show a friction screen)
- *"Lock yourself out with a commitment lock when willpower runs low."* (show OTP screen)

**[55–70s · WHY TRUST IT]**
Visual: Trust Dashboard scrolling.
VO: *"Open the Trust Dashboard and see exactly what Touchgrass can and can't see. It runs on your phone. Nothing leaves it. No ads, ever. No subscription, ever. And the whole app is open source — audit it yourself."*

**[70–85s · CLOSE]**
Visual: the three earlier scenes, replayed — but now phone-down: laughing with the friend, watching the road, feet in the sand.
VO: *"Be where you are. Touch grass — for real this time."*
End card: **Touchgrass · on Google Play · github.com/nikolozturkishvili1/Touchgrass**

---

## Taking screenshots from the recording

- You CAN pull the **app-UI frames** from your screen recording for Play screenshots (pause on a clean frame → screenshot, or export the frame). Use the 8-screen plan in `docs/DEVICE_CAPTURE_SESSION.md`.
- You CANNOT use the lifestyle B-roll (driving/beach/dinner) as Play "screenshots" — Play screenshots must show the **app itself**. Lifestyle footage is for the promo videos only.

## Production tools (Anthropic has none for video)

- **Edit / vertical / auto-captions / text-to-speech:** CapCut (free, mobile + desktop) — best fit for Shorts.
- **Desktop pro editor:** DaVinci Resolve (free).
- **Templates / quick design:** Canva.
- **Realistic AI voiceover** (if you don't want to record your voice): ElevenLabs, or CapCut's built-in TTS.
- **Free lifestyle B-roll** (if you don't film your own driving/beach/dinner shots): Pexels Videos, Pixabay, Mixkit.
- **Royalty-free music:** YouTube Audio Library, or CapCut's library.

---

## FREE production recipe ($0, no watermark) — recommended

Anthropic/Claude has **no** video tool (no plan includes it). Free AI *generators* (Veo, Sora, Runway,
Kling, Pika) cap clip length and usually **watermark** free output. The reliable free path is:
**CapCut (edit + captions + AI voice) + free stock B-roll (Pexels/Pixabay/Mixkit) + your real app recording.**

### Step 1 — gather clips (free)
- **App demo:** your existing screen recording (trim the good moments).
- **Lifestyle B-roll:** download from [pexels.com/videos](https://pexels.com/videos), pixabay.com, mixkit.co.
  These are free for commercial use, no watermark, no attribution required. Search terms per scene:

  | Scene | Search terms |
  |---|---|
  | Red-light scroll reflex | `driving phone`, `hand phone car`, `traffic jam phone`, `red light driver` |
  | Friends at dinner, both on phones | `friends phones dinner`, `phubbing restaurant`, `group phones table` |
  | Vacation, watching phone not view | `beach phone`, `vacation smartphone`, `woman beach phone` |
  | 1am scrolling in bed | `scrolling phone bed night`, `phone glow dark`, `insomnia phone` |
  | "Phone-down" payoff shots | `friends laughing talking`, `beach feet sand`, `sunset friends`, `peaceful window` |

### Step 2 — assemble in CapCut (free)
1. New project → drop clips on the timeline in script order → trim to the beat.
2. **Captions:** paste the on-screen text from the script (or use Auto-captions if you add voice first).
3. **Voiceover (no mic needed):** Text-to-speech → paste the voiceover lines → pick a natural voice.
4. **Music:** add a track from CapCut's free library; duck it under the voice.
5. **Avoid the crown 👑 icons** (those are premium/paid effects that watermark the export) and delete the default
   CapCut end-card → then **Export** is watermark-free.
6. Export **1080×1920 / 9:16** for the short promo; **1080×1920 or 1080×1350** for social; **16:9** if you also want a YouTube cut.

### Step 3 — the compliance video stays separate
Do NOT build the accessibility video this way. It must be your **real screen recording** only (Script 1) —
no stock, no AI. Upload it unlisted and paste the URL into the Accessibility declaration.

### If you insist on AI-generated scenes (free-ish)
- **Kling AI** — free daily credits, good quality (watermark on free tier).
- **Google Veo** via the Gemini app / Google AI Studio — limited free quota; best quality but the good tier is usually paid.
- **Luma Dream Machine / Pika / Hailuo** — a few free generations each; short clips, watermarks.
Generate 4–6s clips per scene from the prompts, download, then edit them in CapCut exactly as above.
