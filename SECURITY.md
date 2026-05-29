# Security Policy

Thanks for taking the time to look at Touchgrass security. Touchgrass is a privacy-positioned Android app, and we treat security reports as a serious gift. This document explains how to send one and what you can expect back.

## Supported versions

The latest published release is the only supported version. Older builds receive no security patches; please update before reporting.

| Version | Supported |
| --- | --- |
| Latest release | Yes |
| Anything older | No |

**Pre-release notice (2026-05-12).** Touchgrass V1 is still in active development. There is no stable Play Store release yet, so "latest release" currently means the most recent tagged build in this repository. Once V1 ships, this section will name the supported version explicitly.

## Reporting a vulnerability

**Preferred channel:** email `security@gettouchgrass.app`.

If you'd rather use GitHub, "Private vulnerability reporting" is enabled on this repo when available (Security tab > Report a vulnerability). Either channel is fine; please don't open a public issue for security bugs.

**Encrypted email is welcome.** Our PGP key fingerprint is `{PGP_KEY_FINGERPRINT}` (PGP key TBD until V1 ships). If you need the key before then, ask in your first plaintext email with as few details as possible and we'll send it back.

Please don't use Twitter DMs, Discord, or any other channel for the initial report. Those aren't monitored for security.

## What to include

The more of the following you can give us, the faster we can act:

- Affected version (commit SHA or release tag).
- Reproduction steps. A minimal repro is gold.
- Impact assessment in your own words: what can an attacker actually do?
- Suggested fix or mitigation, if you have one in mind.
- Whether you want to be credited, and under what name or handle.

If you only have a partial report, send it anyway. We'd rather hear about a vague suspicion than miss a real bug.

## Response timeline

We are a solo-maintainer project. We will be honest with you about what we can do and when.

- **Acknowledgment:** best-effort within 72 hours of receipt.
- **Triage:** within 7 days, we will tell you whether we've reproduced it, what severity we think it is, and a rough fix or mitigation timeline.
- **Fix:** the triage email will commit to a target window. If something blocks that window, we will tell you, not ghost you.

If a fix is going to take longer than feels reasonable, we will say so plainly rather than dragging it out.

## Coordinated disclosure

Please don't publicly disclose the issue before either a fix has shipped or 90 days have passed, whichever comes first. If 90 days is approaching and we haven't shipped, reach back out — we'd rather negotiate an extension or coordinate the disclosure than have it appear cold on social media.

Once a fix ships, you're welcome to publish your write-up. We'll link to it from the release notes if you'd like.

## Out of scope

The following are known and not considered vulnerabilities in Touchgrass:

- **Rooted or jailbroken devices.** If the attack requires `su`, a custom recovery, or a tampered system image, it's out of scope. Android's threat model assumes an unrooted device, and so do we.
- **User-granted permissions to a malicious third-party app.** If the exploit requires the user to install another app and grant it Accessibility, device admin, or similar sensitive permissions, that's an OS-level trust decision, not a Touchgrass bug.
- **"An Accessibility-enabled app can read screen contents."** This is by design. Touchgrass uses the Accessibility Service to detect doomscroll patterns, and we disclose this in `docs/PRIVACY.md` and the in-app Trust Dashboard. Touchgrass is not a security boundary against an attacker who has physical access to an unlocked device.
- **Upstream dependency issues.** Please file those upstream with the dependency's maintainers. We pin dependency versions and will bump to a patched release once one is available.
- **Reports generated solely by automated scanners** with no demonstrated impact (e.g. "this library has a CVE" without a path to exploit in our usage).

## Scope

The following are in scope:

- Code in this repository.
- The release Touchgrass APK distributed via the Google Play Store.
- The `gettouchgrass.app` marketing site (when it exists).

Anything else — forks, mirrors, unofficial builds, scraped copies — is not our responsibility, but feel free to forward us anything interesting.

## Hall of fame

Reporters of confirmed vulnerabilities will be credited in the release notes for the fix, and (with your permission) listed here:

<!-- Add reporters below. Format: - YYYY-MM-DD: Name or handle - short description (link optional) -->

_No reports yet — be the first._

---

Maintained by `nikolozturkishvili1`. Questions about this policy that aren't themselves a vulnerability report can go to the same address: `security@gettouchgrass.app`.
