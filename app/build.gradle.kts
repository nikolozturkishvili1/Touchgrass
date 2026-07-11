import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties =
    Properties().apply {
        if (keystorePropertiesFile.exists()) {
            keystorePropertiesFile.inputStream().use { load(it) }
        }
    }
val hasReleaseSigning = keystoreProperties.getProperty("storeFile")?.isNotBlank() == true

android {
    namespace = "com.touchgrass.app"
    compileSdk = 35

    defaultConfig {
        // Play Console app is registered under com.touchgrassinc.app (com.touchgrass.app was
        // unavailable), so the published applicationId must match it. The Kotlin/namespace stays
        // com.touchgrass.app — applicationId and namespace are independent, so no sources move.
        applicationId = "com.touchgrassinc.app"
        minSdk = 29
        targetSdk = 35
        versionCode = 2
        versionName = "0.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Commitment-lock email OTP. Set RESEND_API_KEY in local gradle.properties (gitignored)
        // for production builds; leave unset in dev — the FakeEmailOtpService takes over and
        // logs the OTP to Logcat. Privacy implication is disclosed in the Trust Dashboard.
        buildConfigField(
            "String",
            "RESEND_API_KEY",
            "\"${(project.findProperty("RESEND_API_KEY") as? String).orEmpty()}\"",
        )
        buildConfigField(
            "String",
            "RESEND_FROM_EMAIL",
            "\"${(project.findProperty("RESEND_FROM_EMAIL") as? String) ?: "noreply@gettouchgrass.app"}\"",
        )
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Reads signing material from rootProject/keystore.properties (gitignored).
            // CI overrides by writing keystore.properties at workflow runtime from GitHub Secrets
            // (KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD — see spec §8.4).
            // Without keystore.properties present, release builds remain unsigned and Gradle will fail
            // at the package step — the missing file is the signal, not silent debug-signing.
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs +=
            listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            kotlin.srcDir("src/main/kotlin")
        }
        getByName("test") {
            kotlin.srcDir("src/test/kotlin")
        }
        getByName("androidTest") {
            kotlin.srcDir("src/androidTest/kotlin")
        }
    }

    packaging {
        resources {
            excludes +=
                setOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "/META-INF/LICENSE*",
                    "/META-INF/NOTICE*",
                )
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose (BOM-managed)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Persistence
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization (used for OEM walkthrough JSON in assets/oem/)
    implementation(libs.kotlinx.serialization.json)

    // Logging
    implementation(libs.timber)

    // HTTP (commitment-lock OTP via Resend — only network call in V1, spec §11.3)
    implementation(libs.okhttp)

    // Billing (Touchgrass Plus) removed for V1 — re-add `implementation(libs.billing.ktx)`
    // when the Plus tier returns (2026-05-12 monetization pivot).

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.okhttp.mockwebserver)

    // Instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
}

ktlint {
    version.set("1.3.1")
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    autoCorrect = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)
        md.required.set(false)
    }
}
