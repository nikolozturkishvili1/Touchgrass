# ProGuard / R8 rules for Touchgrass release builds.
#
# Add app-specific keep rules below as needed. The Android Gradle Plugin's default rules
# (proguard-android-optimize.txt) cover the common cases for AndroidX, Hilt, Kotlin,
# and Compose, so this file should stay small.

# Keep crash-reportable stack traces meaningful.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Timber — strip Log.v / Log.d in release per spec §11.2.
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
}

# Hilt generated classes are kept by the Hilt plugin; no custom rules needed.

# Keep our AccessibilityService entry points so the OS can bind them.
-keep class com.touchgrass.app.accessibility.TouchgrassAccessibilityService { *; }
-keep class com.touchgrass.app.service.TouchgrassForegroundService { *; }
-keep class com.touchgrass.app.service.BootReceiver { *; }
