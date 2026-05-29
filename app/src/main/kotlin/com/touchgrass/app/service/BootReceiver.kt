package com.touchgrass.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Restarts the foreground service after a reboot or after the app is updated (spec §4.6).
 *
 * Listens to:
 *  - [Intent.ACTION_BOOT_COMPLETED]: device finished booting.
 *  - [Intent.ACTION_MY_PACKAGE_REPLACED]: our APK was updated — receivers get this for free.
 *
 * Note: starting a foreground service from `BOOT_COMPLETED` is permitted on Android 12+ because
 * `specialUse` is one of the allowed FGS types at boot (alongside `dataSync`, `mediaPlayback`,
 * etc.). The corresponding permission is declared in AndroidManifest.xml.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var serviceLauncher: ServiceLauncher

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            -> {
                Timber.i("BootReceiver: action=%s, starting foreground service", intent.action)
                serviceLauncher.start()
            }
            else -> Timber.d("BootReceiver: ignoring action=%s", intent.action)
        }
    }
}
