package com.touchgrass.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Strongly-typed wrapper around [DataStore]. Exposes app preferences as [Flow]s and provides
 * `suspend` setters.
 *
 * For .NET devs: this is roughly `IConfiguration` + `IOptionsMonitor<T>` over a JSON file —
 * reads are reactive, writes are atomic.
 *
 * Stored keys:
 *  - [KEY_ONBOARDING_COMPLETE]: has the user finished the first-run flow.
 *  - [KEY_TOUCHGRASS_ENABLED]: user-facing on/off toggle. False means the foreground service is
 *    stopped and the home screen shows "Touchgrass is off". The AccessibilityService binding
 *    (granted in system settings) is unaffected — disabling Touchgrass at the app level does
 *    not surrender the accessibility permission.
 *  - [KEY_ENABLED_PACKAGES]: the user's chosen subset of supported apps. Defaults to all.
 */
@Singleton
class PreferencesRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val onboardingComplete: Flow<Boolean> =
            dataStore.data.map { it[KEY_ONBOARDING_COMPLETE] ?: false }

        val touchgrassEnabled: Flow<Boolean> =
            dataStore.data.map { it[KEY_TOUCHGRASS_ENABLED] ?: true }

        val enabledPackages: Flow<Set<String>> =
            dataStore.data.map { it[KEY_ENABLED_PACKAGES] ?: DEFAULT_ENABLED_PACKAGES }

        suspend fun setOnboardingComplete(value: Boolean) {
            dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = value }
        }

        suspend fun setTouchgrassEnabled(value: Boolean) {
            dataStore.edit { it[KEY_TOUCHGRASS_ENABLED] = value }
        }

        suspend fun setEnabledPackages(packages: Set<String>) {
            dataStore.edit { it[KEY_ENABLED_PACKAGES] = packages }
        }

        companion object {
            /**
             * Day-1 default block list mirroring [accessibility_service_config.xml] `packageNames`.
             * Updating this list requires also updating the XML config and `AndroidManifest <queries>`.
             */
            val DEFAULT_ENABLED_PACKAGES: Set<String> =
                setOf(
                    "com.google.android.youtube",
                    "com.instagram.android",
                    "com.instagram.lite",
                    "com.zhiliaoapp.musically",
                    "com.ss.android.ugc.trill",
                    "com.facebook.katana",
                    "com.facebook.lite",
                    "com.snapchat.android",
                    "com.android.chrome",
                    "com.sec.android.app.sbrowser",
                )

            private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
            private val KEY_TOUCHGRASS_ENABLED = booleanPreferencesKey("touchgrass_enabled")
            private val KEY_ENABLED_PACKAGES = stringSetPreferencesKey("enabled_packages")
        }
    }
