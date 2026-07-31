# Touchgrass — Production Launch Campaign Plan

Solo-dev, $0-budget, organic-only launch plan. This covers the marketing; the
mechanics of shipping are in `RELEASE.md` and current state is in `../STATUS.md`.

> **Dates below were written on 2026-07-14 and have slipped.** The plan's structure
> and channel strategy still hold; re-date the calendar against `../STATUS.md`
> before working from it.

## 1. Overview

- **Campaign name**: "Go Touch Grass" — v1.0 production launch
- **Summary**: Launch Touchgrass on Google Play as a free app and convert the "I hate that I doomscroll" audience into installs, reviews, and a retention baseline.
- **Primary objective**: 1,000 installs + 25 reviews at ≥4.5★ within 30 days of production launch (early August → early September 2026).
- **Secondary**: establish day-7 retention baseline (target ≥25%), collect feature signals, 100+ community karma/engagement to seed future launches.

## 2. Target audience

- **Primary**: 18–35 Android users who feel their Reels/Shorts/TikTok use is compulsive and have tried willpower, Digital Wellbeing timers, or uninstalling — and relapsed. Pain: shame + lost hours. Motivation: control without giving up the apps entirely (Touchgrass blocks the *feed*, not the app).
- **Secondary**: parents managing teens' phones; productivity/self-improvement community members.
- **Where they are**: r/nosurf, r/digitalminimalism, r/getdisciplined, r/androidapps, r/productivity; TikTok/YouTube Shorts (ironically — "the app that kicks you out of Shorts" performs *on* Shorts); X/Twitter build-in-public; Product Hunt; Hacker News (Show HN).
- **Stage**: problem-aware, solution-frustrated. Message accordingly — don't explain doomscrolling is bad; they know.

## 3. Key messages

- **Core**: "Touchgrass kicks you out of Reels and Shorts the moment you open them — you keep the apps, lose the doomscroll."
- Supporting:
  1. **It's not another timer.** Timers ask you to stop; Touchgrass just doesn't let the feed load. (Proof: blocking demo GIF.)
  2. **Privacy-first.** Sees only the apps you choose to block; nothing leaves the phone. (Proof: Trust Dashboard screenshot; strictly-scoped accessibility config.)
  3. **The Quick Peek.** One free peek per app per session — agency, not a cage. Commitment lock for when you don't trust future-you.
  4. **Free.** No ads, no account, no subscription (v1).
- Tone per channel: Reddit = honest solo-dev story, zero marketing-speak; Shorts/TikTok = ironic, punchy; Product Hunt = crisp feature framing.

## 4. Channel strategy (all owned/earned, $0)

| Channel | Why | Format | Effort |
|---|---|---|---|
| Reddit (r/nosurf, r/digitalminimalism, r/androidapps, r/SideProject) | Exact pain-point communities | "I built this because I lost 3h/day" story post + comments | Medium |
| Product Hunt | Early-adopter installs + backlink | Launch day listing, GIF gallery, first-comment story | Medium |
| Show HN | Tech credibility, feedback | "Show HN: Android app that ejects you from short-form feeds" | Low |
| TikTok/Shorts/Reels | The irony is the hook | 15–30s screen-recordings of the app blocking the very platform | Medium |
| X/Twitter build-in-public | Compounding audience | Launch thread + weekly metrics updates | Low |
| Play Store listing (ASO) | Converts all of the above | Keywords: "block reels", "block shorts", "stop doomscrolling", "screen time" | Low |

Rule of thumb: every channel links to the Play listing; the listing's screenshots/feature graphic carry the conversion.

## 5. Calendar (aligned to deployment phases)

| Week | Dates | Focus | Output |
|---|---|---|---|
| 1 | Jul 14–20 | Testers + assets | 12+ testers opted in; record blocking demo GIF/video; draft listing copy v2 |
| 2 | Jul 21–27 | ASO + content bank | Final screenshots, feature graphic; 3 Shorts clips; Reddit post drafts; PH assets |
| 3 | Jul 28–Aug 3 | Apply for production | Questionnaire submitted; X launch thread drafted; line up 5 friends for PH launch-day support |
| 4 | Launch week | Publish + push | Prod rollout → PH launch (Tue/Wed) → Show HN same day → Reddit posts spaced over 3 days → Shorts daily |
| 5–8 | Post-launch | Sustain | Weekly build-in-public updates; reply to every review; 2 Shorts/week; iterate on feedback |

Dependencies: listing assets before production submission; PH/HN/Reddit only after the app is actually live.

## 6. Content pieces needed

Must-have: blocking demo GIF (10s, the money shot) — **already produced**, see `../marketing/gifs/`; Play Store screenshots (five exist in `../marketing/screenshots/`; Play accepts 2–8) with captions; feature graphic; Reddit story post (one canonical version, adapted per sub's rules); PH listing (tagline ≤60 chars: "Kicks you out of Reels and Shorts, automatically"); launch thread.
Nice-to-have: 30s launch video; landing page (currently on GitHub Pages — `gettouchgrass.app` is **not registered**); press blurb for Android blogs (androidpolice tips line).

## 7. Success metrics

- **Primary KPI**: 1,000 installs in 30 days (Play Console → Statistics).
- Secondary: listing conversion ≥25%; day-7 retention ≥25%; ≥25 reviews at ≥4.5★; accessibility-permission grant rate ≥70% of activations (custom funnel signal — it's the make-or-break step); uninstall rate <40% at day 30.
- Cadence: check Play stats weekly; deeper look monthly.

## 8. Monetization note (constraint, verified July 2026)

- App is published FREE — can never become paid-upfront (Play policy, irreversible). **Locked decision: no subscription, ever** (see `../STATUS.md` → Decided). Any future paid tier is a one-time IAP.
- Play Billing requires a merchant account. **Georgia does not support merchant registration** — if the developer account is Georgia-registered, monetization needs an entity in a supported country first (Armenia/Azerbaijan/Türkiye/Estonia all supported; Estonian e-residency company is the common route). Verify account country in Play Console → Settings before planning revenue.
- Strategy: launch free and stay free for V1. Revisit a **one-time** paid tier no earlier than 60–90 days post-launch, using retention + feature-request data (candidates: multiple profiles, schedules, stats history). Never a subscription, and never a paywall on blocking or the commitment lock. Competitor context: ScreenZen free/donate; Opal $99/yr; one sec ~$25/yr.

## 9. Risks

1. **Accessibility-permission drop-off** — users bounce at the scary system dialog. Mitigation: onboarding already explains + Trust Dashboard; watch grant-rate metric, iterate copy.
2. **Reddit self-promo removal** — mods delete pitchy posts. Mitigation: follow each sub's self-promo rules, story-first framing, engage before posting, stagger subs.
3. **Play policy flag on accessibility use** — the declaration is drafted (`RELEASE.md` §3) but **not yet submitted or approved**. Mitigation: keep `packageNames` strictly scoped, keep the Trust Dashboard disclosure before the permission prompt, be ready to supply a demo video.
4. **Launch spike, retention cliff** — expected; the 30-day plan optimizes for learning, not vanity installs.

## 10. Next steps

1. Recruit 11 more testers to actually opt in (see `../STATUS.md` → Track B) — everything else waits on this.
2. Blocking demo GIFs already exist (`../marketing/gifs/`) — cut them into the per-channel formats.
3. Verify developer-account country → decides the monetization roadmap.
4. Draft Reddit post + PH listing during the 14-day test window.
