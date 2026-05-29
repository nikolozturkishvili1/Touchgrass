# Touchgrass docs index

> Navigation map for everything written about this project.
> If you're picking the codebase up cold, start here.

---

## Where everything lives

```
/                            (repo root — GitHub-convention files only)
├── README.md                ← public landing for the repo
├── LICENSE                  ← GPL-3.0
├── CONTRIBUTING.md          ← how to contribute (PR process, code style)
├── CODE_OF_CONDUCT.md       ← Contributor Covenant 2.1
├── SECURITY.md              ← responsible disclosure
│
├── docs/                    ← long-form project documentation
│   ├── INDEX.md             ← you are here
│   ├── SPEC.md              ← the locked product/engineering spec (source of truth)
│   ├── ARCHITECTURE.md      ← how the code is wired
│   ├── PRIVACY.md           ← plain-English privacy policy
│   └── RELEASE_CHECKLIST.md ← step-by-step ordered launch plan
│
├── marketing/               ← launch artifacts
│   ├── play-store-listing.md
│   └── landing-page/
│       ├── index.html       ← gettouchgrass.app
│       └── privacy.html     ← gettouchgrass.app/privacy
│
├── app/                     ← the Android app (Kotlin + Compose)
├── gradle/                  ← Gradle wrapper + version catalog
├── config/detekt/           ← static analysis config
└── .github/workflows/       ← CI
```

---

## Read-first for each audience

### 👤 New contributor

1. `README.md` — what the app is, what makes it different
2. `CONTRIBUTING.md` — how PR-review and code style work
3. `docs/ARCHITECTURE.md` — how the code is wired
4. `docs/SPEC.md` §11 — the working agreement (no ads, no analytics, etc.)

### 🔒 Privacy-conscious user

1. The **Trust Dashboard** screen inside the app (Settings → "What can Touchgrass see?")
2. `docs/PRIVACY.md` — formal policy
3. `app/src/main/res/xml/accessibility_service_config.xml` — the strict `packageNames` allowlist that backs the dashboard claims
4. `app/src/main/kotlin/com/touchgrass/app/lock/ResendEmailOtpService.kt` — the only network call the app makes

### 🤖 Future-me / future-Claude

1. **`MEMORY.md`** (in `~/.claude/.../memory/`) — quick pointers to the user / project / brand promises
2. `docs/SPEC.md` — never out of date by intent; if drift appears, the spec wins unless the user has explicitly redirected (e.g. the [free-for-start monetization pivot](../memory/project_monetization_pivot.md))
3. `docs/ARCHITECTURE.md` — read once to recall the structure
4. This file (`docs/INDEX.md`) — for "where do I find X"

### 🚢 Shipping V1

1. `docs/RELEASE_CHECKLIST.md` — ordered operational steps
2. `marketing/play-store-listing.md` — final listing copy
3. `marketing/landing-page/` — static HTML for `gettouchgrass.app`

### 📋 Spec sections — fast jumps

| Need | Spec section |
|---|---|
| Locked product decisions | `docs/SPEC.md` §0 |
| Why we're building it | §1 (vision), §2 (market research) |
| V1 feature list | §3 |
| Tech stack + module layout | §4 |
| Visual design / brand voice | §5 |
| User flows | §6 |
| Week-by-week build plan | §7 |
| GitHub + CI setup | §8 |
| Store listing copy | §9 |
| Launch & marketing plan | §10 |
| Working agreement (what Claude must/must-never do) | §11 |
| Parallel sub-agent recipes | §12 |
| Risks + mitigations | §13 |
| Definition of Done | §14 |
| Kotlin-for-.NET-dev cheatsheet | §15 |
| First-session kickoff prompt | §16 |

---

## Doc-to-code crosswalk

| Document | Backed by code at... |
|---|---|
| Trust Dashboard claims (`docs/PRIVACY.md`) | `app/src/main/res/xml/accessibility_service_config.xml` (strict `packageNames`) |
| Block pipeline (`docs/ARCHITECTURE.md` §3) | `accessibility/TouchgrassAccessibilityService.kt` + `accessibility/detectors/*` + `accessibility/{BlockingStrategy,EventDebouncer}.kt` |
| Reliability layer (`docs/ARCHITECTURE.md` §7) | `service/{TouchgrassForegroundService,BootReceiver,Watchdog*}.kt` |
| OEM walkthroughs (`docs/SPEC.md` §4.5) | `oem/*` + `app/src/main/assets/oem/*.json` |
| Pause + friction modes (`docs/SPEC.md` §3.1.D) | `domain/{PauseManager,FrictionMode,PauseResult}.kt` + `ui/pause/` |
| Commitment lock (`docs/SPEC.md` §3.1.D — Paulo feature) | `lock/*` |
| Quick Peek (`docs/SPEC.md` §3.1.E) | `domain/QuickPeekManager.kt` |
| Stats screen (`docs/SPEC.md` §3.1.F) | `data/local/BlockEvent*.kt` + `data/repository/BlockEventRepository.kt` + `ui/stats/` |
| Onboarding flow (`docs/SPEC.md` §3.1.G, §6.1) | `ui/onboarding/` + `ui/onboarding/steps/` |
| Settings screen | `ui/settings/` |

---

## Known doc-vs-reality drift

Last audit: 2026-05-14. Items still to reconcile:

- **`{your-handle}` placeholder.** Across `README.md`, `docs/PRIVACY.md`, `SECURITY.md`, `docs/RELEASE_CHECKLIST.md`, `marketing/*`, `app/src/main/kotlin/com/touchgrass/app/ui/trust/TrustDashboardScreen.kt:184`. Replace once the real GitHub handle is set.
- **Inert `billing/` package.** Carried over from pre-pivot scaffolding: the `billing/` package, `BillingModule`, `BillingRepository`, and `BILLING` manifest permission still ship in the codebase but are unwired. Either delete on the next cleanup pass or leave parked for a future supporter tier. Documented in `docs/ARCHITECTURE.md` §14.
- **`docs/SPEC.md`.** This is the canonical spec; the user generated it via Claude Desktop. We do not edit it unless the user redirects — instead, divergences from spec are tracked as named memory entries (see `memory/project_monetization_pivot.md`). Treat SPEC.md as the design-time artifact; the other docs are the as-built record.

Resolved 2026-05-14: stale Touchgrass Plus / $9.99 references in README, PRIVACY, ARCHITECTURE, CONTRIBUTING — all updated to reflect the free-for-start pivot.

---

## Memory pointers (Claude-only)

These live outside the repo, in `C:\Users\nikod\.claude\projects\d--My-Projects-Touchgrass\memory\`:

- `MEMORY.md` — top-level index, loaded automatically
- `user_role.md` — .NET dev learning Kotlin
- `project_touchgrass.md` — project context
- `project_locked_decisions.md` — settled tech + product decisions
- `project_monetization_pivot.md` — V1 ships free; Plus deferred
- `feedback_brand_promises.md` — non-negotiables (no ads / no subs / no analytics)
- `feedback_working_agreement.md` — code style + when to confirm with the user
- `reference_build_spec.md` — pointer back to `docs/SPEC.md`
