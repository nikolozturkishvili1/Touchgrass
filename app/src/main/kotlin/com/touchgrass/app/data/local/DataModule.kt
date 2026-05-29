package com.touchgrass.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.touchgrass.app.util.Clock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the data layer: DataStore (preferences), Room (persistent stats), Clock.
 *
 * Room + KSP note: Room generates a `*_Impl` for [TouchgrassDatabase] at build time. If the
 * compile errors with "Room cannot find an implementation", check that
 * `alias(libs.plugins.ksp)` is applied in `app/build.gradle.kts` and that
 * `ksp(libs.androidx.room.compiler)` is declared.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    private const val PREFERENCES_NAME = "touchgrass_prefs"
    private const val DATABASE_NAME = "touchgrass.db"

    private val Context.touchgrassDataStore: DataStore<Preferences> by preferencesDataStore(
        name = PREFERENCES_NAME,
    )

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.touchgrassDataStore

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.System

    @Provides
    @Singleton
    fun provideTouchgrassDatabase(
        @ApplicationContext context: Context,
    ): TouchgrassDatabase =
        Room
            .databaseBuilder(
                context.applicationContext,
                TouchgrassDatabase::class.java,
                DATABASE_NAME,
            )
            // No fallbackToDestructiveMigration in V1: there are no migrations yet, and we
            // never want to silently drop user data. Add `addMigrations(...)` here when the
            // schema changes.
            .build()

    @Provides
    @Singleton
    fun provideBlockEventDao(db: TouchgrassDatabase): BlockEventDao = db.blockEventDao()
}
