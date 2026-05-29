package com.touchgrass.app.accessibility

import com.touchgrass.app.accessibility.detectors.BrowserShortsDetector
import com.touchgrass.app.accessibility.detectors.FacebookReelsDetector
import com.touchgrass.app.accessibility.detectors.InstagramReelsDetector
import com.touchgrass.app.accessibility.detectors.SnapchatSpotlightDetector
import com.touchgrass.app.accessibility.detectors.TikTokDetector
import com.touchgrass.app.accessibility.detectors.YouTubeShortsDetector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Hilt bindings for the accessibility subsystem.
 *
 * Multibinding note for .NET devs: `@IntoSet` is the Hilt/Dagger equivalent of registering many
 * implementations of one interface — in `Microsoft.Extensions.DependencyInjection` that's
 * `services.AddSingleton<IDetector, YouTubeShortsDetector>()` repeated, and consumers resolve
 * `IEnumerable<IDetector>`. Here, consumers ask for `Set<Detector>`.
 *
 * **Per spec §12.4 Recipe 1**, each new app detector PR adds one `@Binds @IntoSet` line here
 * and one file under `accessibility/detectors/`. Multiple agents can work the detector files in
 * parallel; this module is the single coordination point and must be edited serially.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AccessibilityModule {

    @Binds
    abstract fun bindBlockingStrategy(impl: BackPressBlockingStrategy): BlockingStrategy

    @Binds
    abstract fun bindHeartbeat(
        impl: com.touchgrass.app.data.local.DataStoreHeartbeat,
    ): Heartbeat

    @Binds @IntoSet
    abstract fun bindYouTubeShortsDetector(detector: YouTubeShortsDetector): Detector

    @Binds @IntoSet
    abstract fun bindInstagramReelsDetector(detector: InstagramReelsDetector): Detector

    @Binds @IntoSet
    abstract fun bindTikTokDetector(detector: TikTokDetector): Detector

    @Binds @IntoSet
    abstract fun bindFacebookReelsDetector(detector: FacebookReelsDetector): Detector

    @Binds @IntoSet
    abstract fun bindSnapchatSpotlightDetector(detector: SnapchatSpotlightDetector): Detector

    @Binds @IntoSet
    abstract fun bindBrowserShortsDetector(detector: BrowserShortsDetector): Detector

    // Future detectors: add one @Binds @IntoSet line here per new Detector class. The class
    // itself lives in a per-app file under accessibility/detectors/ — see spec §12.4 Recipe 1.
}
