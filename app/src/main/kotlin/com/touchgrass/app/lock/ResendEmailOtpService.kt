package com.touchgrass.app.lock

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.IOException

/**
 * Sends the OTP through Resend (https://resend.com). One POST per code, no SDK.
 *
 * Why Resend? Free tier covers ~3k emails/month at launch — plenty for V1 + indie scale. The
 * commitment lock is the only network call Touchgrass makes beyond Play Billing, so adding an
 * SDK would be overkill. A bare OkHttp POST is the smallest possible footprint.
 *
 * Privacy note: Resend sees the recipient email + the email body (which contains the OTP) at
 * send time. They don't see anything else about the user or the device. Disclosed in the Trust
 * Dashboard.
 */
class ResendEmailOtpService(
    private val apiKey: String,
    private val fromEmail: String,
    private val client: OkHttpClient,
    private val json: Json,
) : EmailOtpService {
    override suspend fun sendOtp(
        email: String,
        code: String,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            val payload =
                ResendPayload(
                    from = "Touchgrass <$fromEmail>",
                    to = listOf(email),
                    subject = SUBJECT,
                    html = buildHtml(code),
                    text = buildText(code),
                )
            val body = json.encodeToString(payload).toRequestBody(JSON_MEDIA_TYPE)
            val request =
                Request
                    .Builder()
                    .url(ENDPOINT)
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Result.success(Unit)
                    } else {
                        val errBody =
                            runCatching { response.body?.string() }.getOrNull().orEmpty().take(
                                MAX_LOG_BODY_CHARS,
                            )
                        Timber.w("Resend rejected OTP send: code=%d body=%s", response.code, errBody)
                        Result.failure(IOException("Resend HTTP ${response.code}"))
                    }
                }
            } catch (e: IOException) {
                Timber.w(e, "Resend send failed")
                Result.failure(e)
            }
        }

    @Serializable
    private data class ResendPayload(
        val from: String,
        val to: List<String>,
        val subject: String,
        val html: String,
        val text: String,
    )

    private fun buildHtml(code: String): String =
        """
        <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; max-width: 480px; padding: 32px; color: #1F231C; background: #FAF8F3;">
          <h1 style="font-size: 24px; margin: 0 0 16px;">Your Touchgrass code</h1>
          <p style="font-size: 16px; line-height: 1.5;">Enter this code in Touchgrass to confirm:</p>
          <div style="font-family: 'SF Mono', Menlo, monospace; font-size: 36px; font-weight: 600; letter-spacing: 4px; padding: 16px 24px; background: #fff; border-radius: 12px; text-align: center; margin: 16px 0;">$code</div>
          <p style="font-size: 14px; color: #5C645A;">Expires in 5 minutes. If you didn't request this, ignore it — nothing else happens.</p>
        </div>
        """.trimIndent()

    private fun buildText(code: String): String =
        "Your Touchgrass code: $code\n\nExpires in 5 minutes. If you didn't request this, ignore it."

    private companion object {
        const val ENDPOINT = "https://api.resend.com/emails"
        const val SUBJECT = "Your Touchgrass code"
        const val MAX_LOG_BODY_CHARS = 200
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
