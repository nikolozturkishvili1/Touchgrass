package com.touchgrass.app.oem

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads OEM walkthroughs from app assets and caches them in memory.
 *
 * Walkthroughs are static, ship-with-the-app data; one-time read per OEM is fine. Backed by
 * coroutine [Mutex] (not [java.util.concurrent.ConcurrentHashMap]) because the suspendable load
 * path must serialize per-key to avoid two readers racing the asset stream.
 */
@Singleton
class OemRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val json: Json,
    ) {
        private val cache = mutableMapOf<OemId, OemWalkthrough?>()
        private val cacheMutex = Mutex()

        /**
         * Return the walkthrough for [oemId], or `null` if we don't ship one for that OEM.
         * The onboarding code falls back to [OemId.Generic] in that case.
         */
        suspend fun walkthroughFor(oemId: OemId): OemWalkthrough? =
            withContext(Dispatchers.IO) {
                cacheMutex.withLock {
                    cache[oemId]?.let { return@withLock it }

                    val parsed: OemWalkthrough? =
                        runCatching {
                            context.assets.open("oem/${oemId.key}.json").use { stream ->
                                json.decodeFromString<OemWalkthrough>(stream.bufferedReader().readText())
                            }
                        }.onFailure {
                            Timber.d(it, "no walkthrough asset for oemId=%s", oemId.key)
                        }.getOrNull()

                    cache[oemId] = parsed
                    parsed
                }
            }
    }
