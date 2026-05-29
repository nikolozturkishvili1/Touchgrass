# Touchgrass Privacy Policy

**Nothing leaves your phone.**

That is the whole policy. The rest of this document explains what that means, what Touchgrass can and cannot see, and what the one exception looks like (a one-time email if you turn on the commitment lock).

If anything here ever stops being true, we will update this file and the in-app Trust Dashboard **before** the change ships.

---

## What this policy covers

This policy covers two things:

- The **Touchgrass Android app** — the app you install from Google Play, or build yourself from source.
- The **gettouchgrass.app** marketing site.

It does not cover Google Play itself, your device manufacturer, or the apps that Touchgrass helps you block. Those have their own privacy policies.

---

## What Touchgrass accesses on your device

Touchgrass is an accessibility-based blocker. To do its job it needs to see a small, specific slice of what's happening on your phone. Here is the full list.

- **The name of the foreground app — only when that app is one you chose to block.** Touchgrass needs to know "you just opened Instagram" so it can start watching for a Reels screen. It only sees this for apps on your block list. If you open WhatsApp, your bank app, or your camera, Touchgrass receives nothing.
- **On-screen UI elements inside those chosen apps, just long enough to detect a short-form feed.** This means the text labels, view IDs, and content descriptions that Android's accessibility framework exposes for the screen you're currently on. Touchgrass uses them to answer a single question: "Is this the Reels tab / Shorts player / For You feed?" The data is read, used to make that decision, and discarded. It is not logged, transmitted, or stored.
- **Your selected preferences** — which apps you've added to your block list, your friction settings, whether commitment lock is on, your Quick Peek choices. These live in a local DataStore file on your phone.
- **A local count of blocks** for the in-app stats screen ("Touchgrass stopped 47 doomscroll attempts this week"). This is kept in a local Room/SQLite database on your phone. It never leaves.

That is the entire list of what Touchgrass touches.

---

## What Touchgrass cannot access

Because the AccessibilityService is hard-scoped to a specific list of packages, Touchgrass has no way to see any of the following:

- Your **messages.** SMS, WhatsApp, Signal, Telegram, Messenger, Instagram DMs, Snapchat chats — none of these. Full stop.
- Your **keyboard input.** Touchgrass is not an input method (IME) and does not subscribe to keystroke events.
- Your **photos, files, contacts, location, microphone, or camera.** Touchgrass does not request the permissions that would be needed to read any of these.
- **Apps that aren't on your block list.** If you haven't added it, Touchgrass receives zero events from it.
- Your **banking app, password manager, email, calendar, health data**, or any other app you have not specifically added.

This is enforced in code, not just by policy. The relevant configuration lives in:

```
app/src/main/res/xml/accessibility_service_config.xml
```

The `packageNames` attribute in that file is a hard-coded list (currently: YouTube, Instagram, TikTok, Facebook, Snapchat, Chrome, Samsung Internet). Android itself filters the event stream before Touchgrass ever sees it. If a package is not in that list, the operating system will not deliver its events to us. You can read the file in our public repository to verify.

---

## What Touchgrass sends over the internet

Most of the time: **nothing.** Blocking is 100% on-device. Touchgrass does not need a network connection to detect Reels, exit a feed, or count a block.

There is exactly one case where Touchgrass uses the internet, and it is initiated by you.

### 1. Commitment lock email (only if you turn it on)

The commitment lock is an optional, free feature. When you enable it, disabling Touchgrass or pausing it requires a one-time code sent to your email. This stops you from impulse-disabling the app.

When you turn this on and use it:

- Your email address is sent to a transactional-email provider (Resend or Postmark — the actual provider will be named here once chosen) for the sole purpose of delivering the one-time code.
- The provider sees your email address and the OTP message as it passes through their system.
- **We do not store your email address on our servers.** We do not run a backend that holds a user table. The address is passed to the provider at the moment you request a code, and is not retained by Touchgrass.

If you never enable the commitment lock, your email address is never collected, transmitted, or stored.

That is the complete list of network activity. There is no analytics call. There is no crash report. There is no "anonymous usage ping." There is no in-app purchase at launch — V1 is fully free, so there is no Play Billing call either. If we ever add anything — for example, an opt-in privacy-respecting crash reporter, or a one-time supporter tier later — we will disclose it here and in the Trust Dashboard before the change ships, and it will be opt-in, not opt-out.

---

## What we don't have

It is also useful to be specific about the things we have deliberately not built.

- **No backend server holding user data.** There is no Touchgrass user account. There is no profile. There is no cloud sync. Your block list and stats live on your phone.
- **No Firebase Analytics, no Google Analytics, no Mixpanel, no Amplitude, no AppsFlyer, no Adjust, no Segment, no Branch, no Singular, no Kochava, no any-other-tracking-SDK.**
- **No third-party advertising SDKs.** No ads. Ever. That is a brand promise, not a "for now."
- **No selling or sharing of personal data**, because there is no personal data to sell or share.

---

## Open source — verify it yourself

You don't have to take our word for any of this. Touchgrass is open source under GPL-3.0. The full source code, including the accessibility configuration and every place we touch the network, lives at:

```
github.com/nikolozturkishvili1/Touchgrass
```

If anything in this policy is contradicted by what is in the code, the code is the bug, and we want to hear about it.

---

## Monetization

**V1 is free.** Every feature ships at no cost — the commitment lock, friction modes, stats, OEM walkthroughs, everything. There is no in-app purchase wired up, no Play Billing call, no Touchgrass account.

If we ever introduce a paid tier later — for example, a one-time supporter price — this section will be updated before that change ships, and it will never paywall the core blocking or the commitment lock.

---

## Children

Touchgrass is not directed at children under 13. It's a digital-wellbeing tool intended for adults and supervised teens. We do not knowingly collect any data from children, because we do not knowingly collect data from anyone — but we mention it here because Google Play asks us to be explicit.

---

## Permissions Touchgrass requests, in plain English

When you install and set up Touchgrass, Android will ask you about the following. Here is what each one is actually for.

- **Accessibility.** The big one. This is what lets Touchgrass see a Reels screen and exit it. It is scoped, in code, to only the apps on your block list. See the `accessibility_service_config.xml` file linked above.
- **Foreground Service / Foreground Service Special Use.** Lets Touchgrass keep running reliably in the background with a persistent notification. This is what fixes the "stopped working after 3 days" problem you may have seen in other blockers.
- **Post Notifications.** Lets Touchgrass show that it is on, and — importantly — alert you if Touchgrass ever stops working, so you can re-enable it before a binge instead of after.
- **Receive Boot Completed.** Lets Touchgrass start back up after you restart your phone, so you don't have to remember to turn it on again.
- **Internet.** Used only for the optional commitment-lock email OTP. Nothing else.

Battery-optimization opt-out is not a permission in the traditional sense — Touchgrass asks you to grant it through an intent during onboarding, with OEM-specific instructions. That is what keeps the service alive on Samsung, Xiaomi, OnePlus, Oppo, Vivo, Realme, and Huawei devices, which kill background apps aggressively by default.

---

## Changes to this policy

If we ever change what Touchgrass accesses, stores, or transmits — even something as small as adding an opt-in crash reporter — we will:

1. Update this file in the public repository, with the change visible in the git history.
2. Update the in-app Trust Dashboard to match.
3. Note the change in the release notes for the version that introduces it.

We will do those things **before** the change ships, not after. Material changes will not be slipped in quietly.

---

## Contact

For privacy questions, concerns, or data requests, email:

```
privacy@gettouchgrass.app
```

(Placeholder during pre-launch — this mailbox will be live before public release. If it bounces during the beta, open an issue on the GitHub repo above and we'll respond there.)

For security disclosures, see `SECURITY.md` in the repository.

---

## If you are in the EU/UK or California

Privacy laws like the GDPR, the UK GDPR, and the CCPA give you rights to access, correct, delete, and port the personal data a company holds about you, and to opt out of its sale or sharing.

We mention these laws here for completeness, but the practical answer for Touchgrass is short: **we do not hold personal data about you.** There is no user account, no profile on a server, no analytics record, no email list (unless you signed up at gettouchgrass.app, in which case you can unsubscribe from any email we send). There is nothing for us to look up, export, correct, or delete on a server, because nothing is on a server.

If you have questions about this, email the address above and we will answer honestly.

---

**Effective as of: pre-launch, last updated 2026-05-12.**
