package com.touchgrass.app.lock

import com.touchgrass.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Wires the [EmailOtpService] implementation. Production builds (where `RESEND_API_KEY` is set
 * in `gradle.properties` and threaded into `BuildConfig`) get [ResendEmailOtpService]; dev
 * builds with no key configured fall back to [FakeEmailOtpService] (logs OTP to Logcat).
 *
 * Adding any other networked service requires explicit confirmation per spec §11.3 — see also
 * [[feedback-brand-promises]] in memory.
 */
@Module
@InstallIn(SingletonComponent::class)
object LockModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideEmailOtpService(
        client: OkHttpClient,
        json: Json,
    ): EmailOtpService {
        val apiKey = BuildConfig.RESEND_API_KEY
        val fromEmail = BuildConfig.RESEND_FROM_EMAIL
        return if (apiKey.isNotBlank()) {
            Timber.i("EmailOtpService: using Resend (api key configured)")
            ResendEmailOtpService(apiKey = apiKey, fromEmail = fromEmail, client = client, json = json)
        } else {
            Timber.w("EmailOtpService: no RESEND_API_KEY — falling back to FakeEmailOtpService (logs OTPs to Logcat)")
            FakeEmailOtpService()
        }
    }

    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L
}
