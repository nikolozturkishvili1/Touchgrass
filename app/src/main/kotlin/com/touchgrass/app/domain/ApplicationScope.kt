package com.touchgrass.app.domain

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Process-lifetime coroutine scope, injectable so singletons can launch fire-and-forget work
 * (like the [PauseManager] DataStore observer) without owning a Job themselves.
 *
 * For .NET devs: this is the equivalent of taking an `IServiceProvider`-scoped
 * `IHostApplicationLifetime.ApplicationStopping` `CancellationToken` and spawning work tied
 * to that. `SupervisorJob` means one failing child doesn't cancel siblings.
 *
 * Use sparingly — most coroutines should be `viewModelScope`-bound. This is for components
 * that need to outlive any individual screen.
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
