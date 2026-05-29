package com.touchgrass.app.oem

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Hilt bindings for the OEM subsystem.
 *
 * The [Json] instance is shared so any future serialized data (e.g. preferences exported for
 * backup) uses the same configuration. `ignoreUnknownKeys = true` lets us add new OEM JSON
 * fields in the future without breaking old app versions that haven't been updated.
 */
@Module
@InstallIn(SingletonComponent::class)
object OemModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = false
        prettyPrint = false
    }
}
