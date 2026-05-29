package com.touchgrass.app.lock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed persistence for commitment-lock state (spec §3.1.D).
 *
 * Stored fields:
 *  - **lockEnabled** — is the gate active right now? True only after a successful enrollment
 *    OTP verification.
 *  - **email** — the address used for OTP delivery. Stored on-device only; sent to Resend at
 *    each send. Cleared when the user disables the lock.
 *  - **otpHash** — SHA-256 hash (via [OtpHasher]) of the most recently-sent OTP. Cleared on
 *    successful verification.
 *  - **otpExpiresAtMs** — wall-clock epoch ms when the OTP becomes invalid.
 *  - **otpLastSentAtMs** — for resend-cooldown enforcement.
 */
@Singleton
class LockRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val lockEnabledFlow: Flow<Boolean> =
            dataStore.data.map { it[KEY_LOCK_ENABLED] ?: false }

        val emailFlow: Flow<String?> =
            dataStore.data.map { it[KEY_EMAIL] }

        suspend fun lockEnabled(): Boolean = lockEnabledFlow.first()

        suspend fun email(): String? = emailFlow.first()

        suspend fun setLockEnabled(enabled: Boolean) {
            dataStore.edit { it[KEY_LOCK_ENABLED] = enabled }
        }

        suspend fun setEmail(email: String?) {
            dataStore.edit { prefs ->
                if (email == null) prefs.remove(KEY_EMAIL) else prefs[KEY_EMAIL] = email
            }
        }

        /** Returns the stored hash + expiry + last-sent timestamp atomically. */
        suspend fun pendingOtp(): PendingOtp {
            val prefs = dataStore.data.first()
            return PendingOtp(
                hash = prefs[KEY_OTP_HASH],
                expiresAtMs = prefs[KEY_OTP_EXPIRES_AT_MS],
                lastSentAtMs = prefs[KEY_OTP_LAST_SENT_AT_MS],
            )
        }

        suspend fun storePendingOtp(
            hash: String,
            expiresAtMs: Long,
            sentAtMs: Long,
        ) {
            dataStore.edit { prefs ->
                prefs[KEY_OTP_HASH] = hash
                prefs[KEY_OTP_EXPIRES_AT_MS] = expiresAtMs
                prefs[KEY_OTP_LAST_SENT_AT_MS] = sentAtMs
            }
        }

        suspend fun clearPendingOtp() {
            dataStore.edit { prefs ->
                prefs.remove(KEY_OTP_HASH)
                prefs.remove(KEY_OTP_EXPIRES_AT_MS)
                // Note: we keep KEY_OTP_LAST_SENT_AT_MS so the cooldown survives verification —
                // matters for back-to-back gate uses (disable lock then disable Touchgrass).
            }
        }

        data class PendingOtp(
            val hash: String?,
            val expiresAtMs: Long?,
            val lastSentAtMs: Long?,
        )

        private companion object {
            val KEY_LOCK_ENABLED = booleanPreferencesKey("lock_enabled")
            val KEY_EMAIL = stringPreferencesKey("lock_email")
            val KEY_OTP_HASH = stringPreferencesKey("lock_otp_hash")
            val KEY_OTP_EXPIRES_AT_MS = longPreferencesKey("lock_otp_expires_at_ms")
            val KEY_OTP_LAST_SENT_AT_MS = longPreferencesKey("lock_otp_last_sent_at_ms")
        }
    }
