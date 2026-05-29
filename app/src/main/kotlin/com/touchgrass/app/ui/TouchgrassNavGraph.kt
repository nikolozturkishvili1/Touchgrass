package com.touchgrass.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.touchgrass.app.ui.home.HomeRoute
import com.touchgrass.app.ui.onboarding.OnboardingRoute
import com.touchgrass.app.ui.pause.PauseRoute
import com.touchgrass.app.ui.settings.SettingsAppPickerRoute
import com.touchgrass.app.ui.settings.SettingsRoute
import com.touchgrass.app.ui.stats.StatsRoute
import com.touchgrass.app.ui.trust.TrustDashboardScreen

/**
 * Top-level navigation graph (Jetpack Navigation Compose).
 *
 * Routes (string-based for V1 simplicity; can move to type-safe routes via kotlinx.serialization
 * later without breaking anything):
 *  - `home` — default destination. Renders one of six HomeUiState variants.
 *  - `onboarding` — the four-step first-run flow. Replaces itself with `home` on completion.
 *  - `trust` — Trust Dashboard. Reachable from home, onboarding, and settings.
 *  - `stats` — Stats screen (today / this week / all-time + top surfaces).
 *  - `pause` — Pause picker + friction. Returns to home with the pause active.
 *  - `settings` — Pause prefs + friction-mode picker + Trust Dashboard link.
 */
private object Routes {
    const val HOME = "home"
    const val ONBOARDING = "onboarding"
    const val TRUST = "trust"
    const val STATS = "stats"
    const val PAUSE = "pause"
    const val SETTINGS = "settings"
    const val SETTINGS_APPS = "settings_apps"
}

@Composable
fun TouchgrassNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        addHome(navController)
        addOnboarding(navController)
        addTrust(navController)
        addStats(navController)
        addPause(navController)
        addSettings(navController)
        addSettingsAppPicker(navController)
    }
}

private fun NavGraphBuilder.addHome(navController: NavHostController) {
    composable(Routes.HOME) {
        HomeRoute(
            onStartOnboarding = { navController.navigate(Routes.ONBOARDING) },
            onOpenTrustDashboard = { navController.navigate(Routes.TRUST) },
            onOpenStats = { navController.navigate(Routes.STATS) },
            onOpenPause = { navController.navigate(Routes.PAUSE) },
            onOpenSettings = { navController.navigate(Routes.SETTINGS) },
        )
    }
}

private fun NavGraphBuilder.addStats(navController: NavHostController) {
    composable(Routes.STATS) {
        StatsRoute(onNavigateBack = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.addPause(navController: NavHostController) {
    composable(Routes.PAUSE) {
        PauseRoute(onDismiss = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.addSettings(navController: NavHostController) {
    composable(Routes.SETTINGS) {
        SettingsRoute(
            onNavigateBack = { navController.popBackStack() },
            onOpenTrustDashboard = { navController.navigate(Routes.TRUST) },
            onOpenAppSelection = { navController.navigate(Routes.SETTINGS_APPS) },
        )
    }
}

private fun NavGraphBuilder.addSettingsAppPicker(navController: NavHostController) {
    composable(Routes.SETTINGS_APPS) {
        SettingsAppPickerRoute(onNavigateBack = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.addOnboarding(navController: NavHostController) {
    composable(Routes.ONBOARDING) {
        OnboardingRoute(
            onFinished = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onOpenTrustDashboard = { navController.navigate(Routes.TRUST) },
        )
    }
}

private fun NavGraphBuilder.addTrust(navController: NavHostController) {
    composable(Routes.TRUST) {
        TrustDashboardScreen(onNavigateBack = { navController.popBackStack() })
    }
}
