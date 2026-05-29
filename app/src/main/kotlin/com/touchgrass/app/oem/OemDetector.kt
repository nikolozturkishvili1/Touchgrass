package com.touchgrass.app.oem

import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Identifies the current device's OEM family from [Build.MANUFACTURER] and [Build.BRAND]
 * (spec §4.5). Used at onboarding to pick which walkthrough to show.
 *
 * Brand fallback is important for Xiaomi sub-brands (Redmi, POCO) which sometimes report
 * `MANUFACTURER=Xiaomi` but `BRAND=Redmi` and sometimes the inverse, depending on the model
 * and ROM version.
 */
@Singleton
class OemDetector
    @Inject
    constructor() {
        // Flat manufacturer/brand matching across 7 OEM vendors; the branching is inherent.
        @Suppress("CyclomaticComplexMethod")
        fun detect(
            manufacturer: String = Build.MANUFACTURER.orEmpty(),
            brand: String = Build.BRAND.orEmpty(),
        ): OemId {
            val m = manufacturer.lowercase()
            val b = brand.lowercase()
            return when {
                "samsung" in m || "samsung" in b -> OemId.Samsung
                "xiaomi" in m || "xiaomi" in b || "redmi" in b || "poco" in b -> OemId.Xiaomi
                "oneplus" in m || "oneplus" in b -> OemId.OnePlus
                "oppo" in m || "oppo" in b -> OemId.Oppo
                "vivo" in m || "vivo" in b -> OemId.Vivo
                "realme" in m || "realme" in b -> OemId.Realme
                "huawei" in m || "huawei" in b -> OemId.Huawei
                "honor" in m || "honor" in b -> OemId.Honor
                "google" in m || "pixel" in b -> OemId.Google
                else -> OemId.Generic
            }
        }
    }
