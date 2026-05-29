package com.touchgrass.app.oem

/**
 * Canonical identifier for each Android OEM whose battery-optimization behavior we know about
 * (spec §4.5).
 *
 * The [key] doubles as:
 *  - the asset filename: `assets/oem/{key}.json`
 *  - the drawable prefix: `oem_{key}_step{n}`
 *  - the analytics-free internal label
 *
 * V1 Week 4 ships walkthroughs for Samsung, Xiaomi, OnePlus (≈70% of affected users per spec).
 * Oppo / Vivo / Realme / Huawei follow in Week 5 via parallel sub-agents (spec §12.4 Recipe 2).
 * [Generic] is the fallback for devices we don't have a tailored walkthrough for — show the
 * stock Android battery-optimization opt-out flow.
 */
enum class OemId(
    val key: String,
) {
    Samsung("samsung"),
    Xiaomi("xiaomi"),
    OnePlus("oneplus"),
    Oppo("oppo"),
    Vivo("vivo"),
    Realme("realme"),
    Huawei("huawei"),
    Honor("honor"),
    Google("google"),
    Generic("generic"),
    ;

    companion object {
        fun fromKey(key: String): OemId? = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
    }
}
