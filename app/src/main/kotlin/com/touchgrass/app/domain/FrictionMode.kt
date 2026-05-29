package com.touchgrass.app.domain

/**
 * Optional gate that must clear before a pause actually starts (spec §3.1.D).
 *
 * V1 ships [WaitTimer] as the only fully-wired mode; the others are scaffolded for the Week 6
 * Part 3 batch (math, random code, breathing exercise). They map 1:1 to UI Composables under
 * `ui/pause/friction/`.
 */
enum class FrictionMode {
    /** No friction — pause starts immediately. */
    None,

    /** Wait through a configurable timer (default 5s) before the pause begins. */
    WaitTimer,

    /** Solve a randomly-generated arithmetic problem (Plus-tier in original spec — now free per [[project-monetization-pivot]]). */
    MathProblem,

    /** Type a randomly-generated 30-character alphanumeric code (originally Plus). */
    RandomCode,

    /** Complete a 30-second breathing exercise (originally Plus). */
    Breathing,
    ;

    companion object {
        /** Safe parser tolerant of obsolete persisted values. */
        fun fromName(name: String?): FrictionMode =
            name?.let { entries.firstOrNull { it.name == name } } ?: WaitTimer
    }
}
