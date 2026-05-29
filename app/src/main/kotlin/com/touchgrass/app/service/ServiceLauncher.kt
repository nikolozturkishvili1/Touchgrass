package com.touchgrass.app.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around starting/stopping [TouchgrassForegroundService] so the home toggle
 * (Compose UI) and [BootReceiver] don't duplicate the foreground-service start incantation.
 */
@Singleton
class ServiceLauncher
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun start() {
            val intent = Intent(context, TouchgrassForegroundService::class.java)
            runCatching { ContextCompat.startForegroundService(context, intent) }
                .onFailure { Timber.e(it, "failed to start TouchgrassForegroundService") }
        }

        fun stop() {
            val intent = Intent(context, TouchgrassForegroundService::class.java)
            runCatching { context.stopService(intent) }
                .onFailure { Timber.w(it, "stopService threw") }
        }
    }
