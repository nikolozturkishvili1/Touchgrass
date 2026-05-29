# Touchgrass — Play Console Release (MOCK)

> Mock release/version details for updating **Touchgrass** in **Google Play Console**.
> All values are placeholders for testing the release flow — replace before any real publish.

---

## App identity (mock)

| Field | Value |
|---|---|
| App name | Touchgrass — Go Outside & Reset |
| Package name | `com.touchgrass.app` |
| Default language | English (United States) – en-US |
| Category | Health & Fitness |
| Current production version | 1.3.2 (132) |

---

## New release — version details

| Field | Value |
|---|---|
| Track | Production |
| Release name | `1.4.0 (140)` |
| Version name | `1.4.0` |
| Version code | `140` |
| App bundle | `app-release.aab` (signed by Play App Signing) |
| Min SDK / Target SDK | 24 / 34 |
| Countries / regions | All (as already configured for the track) |
| Staged rollout | Start at **20%** |

### Staged rollout plan
- **Phase 1:** 20% of users on day 0
- **Phase 2:** 50% after 48 h if crash-free sessions ≥ 99.3%
- **Phase 3:** 100% after 5 days, no P0/P1 regressions
- **Halt criteria:** crash-free < 99.3% or ANR rate > 0.5% → pause rollout

---

## Release notes — "What's new in this version"

Paste into the **Release notes** box, inside the `<en-US>…</en-US>` language tags Play Console provides.

### en-US (primary)
```
🌱 What's new in 1.4.0

• Grass Streaks 2.0 — your streak now survives one rest day
• Green Map — find nearby parks and grassy spots fast
• Smarter nudges that learn your daily routine
• New "Sunset Walk" daily challenge
• Redesigned home screen widget
• Faster launch + fixed a bug that reset streaks after midnight

Thanks for touching grass with us 💚
```

### en-US (short — for testing tracks / character-tight locales)
```
🌱 1.4.0: Grass Streaks 2.0 (one rest day allowed), Green Map to find parks nearby, smarter reminders, a new Sunset Walk challenge, a fresh widget, and faster launch.
```

### es-ES (sample localization)
```
🌱 Novedades de la versión 1.4.0

• Rachas 2.0 — tu racha sobrevive a un día de descanso
• Mapa Verde — encuentra parques y zonas con césped cerca
• Recordatorios más inteligentes según tu rutina
• Nuevo reto diario "Paseo al atardecer"
• Widget rediseñado
• Inicio más rápido y corrección de un error que reiniciaba las rachas

Gracias por pisar el césped con nosotros 💚
```

---

## Prior version history (mock — for context)

| Version | Code | Notes |
|---|---|---|
| 1.4.0 | 140 | Grass Streaks 2.0, Green Map, Sunset Walk challenge, widget redesign |
| 1.3.2 | 132 | Bug fixes & performance improvements |
| 1.3.1 | 131 | Minor fixes for reminder scheduling |
| 1.3.0 | 130 | Friends list & weekly leaderboard |
| 1.2.0 | 120 | Photo check-ins — "prove you touched grass" |
| 1.1.0 | 110 | Home screen widgets + smart reminders |
| 1.0.0 | 100 | Initial release |

---

## How to apply in Play Console

1. **Play Console → select app → Release → Production** (or Testing track).
2. **Create new release**.
3. Upload the **app bundle** (or pick from the library).
4. Set **Release name** → `1.4.0 (140)` (auto-fills from the bundle).
5. Paste **Release notes** into the `<en-US>` block (add `<es-ES>` if localizing).
6. **Next → review** warnings, then **Save** (stays a draft) or **Start rollout to Production** to go live.
7. For staged rollout, set the rollout percentage to **20%** on the review screen.
