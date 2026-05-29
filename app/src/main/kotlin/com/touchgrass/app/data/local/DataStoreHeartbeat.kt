package com.touchgrass.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.touchgrass.app.accessibility.Heartbeat
import com.touchgrass.app.util.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed [Heartbeat]. Survives process death; the watchdog reads the same key on its
 * own process re-spawn.
 *
 * Kotlin note for .NET devs: `DataStore<Preferences>.data` is a `Flow<Preferences>` — a cold
 * async stream, equivalent in shape to `IAsyncEnumerable<Preferences>`. `.first()` here pulls
 * the current snapshot and unsubscribes; it does not subscribe forever.
 */
@Singleton
class DataStoreHeartbeat @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock,
) : Heartbeat {

    override suspend fun beat() {
        val now = clock.elapsedMillis()
        dataStore.edit { it[KEY_LAST_BEAT_ELAPSED_MS] = now }
    }

    override suspend fun lastBeatElapsedMillis(): Long? {
        return dataStore.data.map { it[KEY_LAST_BEAT_ELAPSED_MS] }.first()
    }

    private companion object {
        val KEY_LAST_BEAT_ELAPSED_MS = longPreferencesKey("accessibility_heartbeat_elapsed_ms")
    }
}
