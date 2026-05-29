package com.touchgrass.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.touchgrass.app.domain.FrictionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed persistence for pause **state** (currently paused? until when? budget used
 * today?) and pause **preferences** (button visible? friction mode? daily budget cap?).
 *
 * State is here, not in [PreferencesRepository], because the AccessibilityService needs cheap
 * synchronous access to "is paused right now?" and [com.touchgrass.app.domain.PauseManager]
 * caches the value in a `StateFlow` to give it that.
 */
@Singleton
class PauseRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        // ---- State (fast-changing) ----

        val pausedUntilMsFlow: Flow<Long> =
            dataStore.data.map { it[KEY_PAUSED_UNTIL_MS] ?: 0L }

        suspend fun pausedUntilMs(): Long = pausedUntilMsFlow.first()

        suspend fun setPausedUntilMs(value: Long) {
            dataStore.edit { it[KEY_PAUSED_UNTIL_MS] = value }
        }

        /**
         * Returns how much pause time has been used today. "Today" is identified by [todayStartMs];
         * if the stored day-start doesn't match, the budget is treated as fresh.
         */
        suspend fun budgetUsedTodayMs(todayStartMs: Long): Long {
            val prefs = dataStore.data.first()
            val storedDay = prefs[KEY_BUDGET_DAY_START_MS] ?: 0L
            return if (storedDay == todayStartMs) prefs[KEY_BUDGET_USED_MS] ?: 0L else 0L
        }

        suspend fun addBudgetConsumed(
            todayStartMs: Long,
            durationMs: Long,
        ) {
            dataStore.edit { prefs ->
                val storedDay = prefs[KEY_BUDGET_DAY_START_MS] ?: 0L
                val previousUsed = if (storedDay == todayStartMs) prefs[KEY_BUDGET_USED_MS] ?: 0L else 0L
                prefs[KEY_BUDGET_DAY_START_MS] = todayStartMs
                prefs[KEY_BUDGET_USED_MS] = previousUsed + durationMs
            }
        }

        // ---- Preferences (slow-changing) ----

        val pauseButtonVisibleFlow: Flow<Boolean> =
            dataStore.data.map { it[KEY_PAUSE_BUTTON_VISIBLE] ?: DEFAULT_PAUSE_BUTTON_VISIBLE }

        suspend fun setPauseButtonVisible(value: Boolean) {
            dataStore.edit { it[KEY_PAUSE_BUTTON_VISIBLE] = value }
        }

        val dailyBudgetMsFlow: Flow<Long> =
            dataStore.data.map { it[KEY_DAILY_BUDGET_MS] ?: DEFAULT_DAILY_BUDGET_MS }

        suspend fun setDailyBudgetMs(value: Long) {
            dataStore.edit { it[KEY_DAILY_BUDGET_MS] = value }
        }

        val frictionModeFlow: Flow<FrictionMode> =
            dataStore.data.map { FrictionMode.fromName(it[KEY_FRICTION_MODE]) }

        suspend fun setFrictionMode(value: FrictionMode) {
            dataStore.edit { it[KEY_FRICTION_MODE] = value.name }
        }

        // ---- Quick Peek (spec §3.1.E) ----

        val quickPeekEnabledFlow: Flow<Boolean> =
            dataStore.data.map { it[KEY_QUICK_PEEK_ENABLED] ?: false }

        suspend fun setQuickPeekEnabled(value: Boolean) {
            dataStore.edit { it[KEY_QUICK_PEEK_ENABLED] = value }
        }

        companion object {
            /** Per spec §3.1.D: max 20 min/day total pause. */
            const val DEFAULT_DAILY_BUDGET_MS: Long = 20L * 60L * 1_000L

            /**
             * Spec §3.1.D says "default: not visible". For V1 we deviate slightly and default to
             * **visible** because the Settings screen (where you'd toggle it on) hasn't shipped
             * yet — without this default a fresh user couldn't pause. Re-evaluate when the Settings
             * screen ships in Week 6 Part 3.
             */
            const val DEFAULT_PAUSE_BUTTON_VISIBLE: Boolean = true

            private val KEY_PAUSED_UNTIL_MS = longPreferencesKey("pause_until_ms")
            private val KEY_BUDGET_DAY_START_MS = longPreferencesKey("pause_budget_day_start_ms")
            private val KEY_BUDGET_USED_MS = longPreferencesKey("pause_budget_used_ms")
            private val KEY_PAUSE_BUTTON_VISIBLE = booleanPreferencesKey("pause_button_visible")
            private val KEY_DAILY_BUDGET_MS = longPreferencesKey("pause_daily_budget_ms")
            private val KEY_FRICTION_MODE = stringPreferencesKey("pause_friction_mode")
            private val KEY_QUICK_PEEK_ENABLED = booleanPreferencesKey("quick_peek_enabled")
        }
    }
