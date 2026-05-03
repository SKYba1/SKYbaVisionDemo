package com.artems_apps.vision_pause.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.artems_apps.vision_pause.ui.screens.soundScreen
import com.artems_apps.vision_pause.ui.screens.timeScreen
import com.skyba.vision.demo.data.local.SettingsViewModel

/**
 * [Navigation Layer] The Root Navigation Graph.
 *
 * Orchestrates the transition between the global navigation levels:
 * 1. [Graph.MAIN]: The core application flow with the Bottom Navigation Bar.
 * 2. [Details]: Sub-screens (settings, configurations) where the Bottom Bar is hidden.
 */
@Composable
fun AppRootNavigation(
    viewModel: SettingsViewModel
) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Graph.MAIN
    ) {
        /*
          * [Flow] Main Application Graph.
          * Contains the PrimaryNavigation (BottomBar + Tabs).
          */
        composable(
            route = Graph.MAIN,
            // Анімація повернення з деталей
            popEnterTransition = { materialSharedAxisXIn(forward = false) }
        ) {
            PrimaryNavigation(
                rootNavController = rootNavController,
                viewModel = viewModel
            )
        }

        /**
         * [UI/UX] Detail Screens Configuration.
         *
         * All sub-screens share a unified Material 3 Shared Axis X transition
         * to provide a consistent "drill-down" navigation experience.
         */
        val detailScreens = listOf("time", "sound")

        detailScreens.forEach { route ->
            composable(
                route = route,
                enterTransition = { materialSharedAxisXIn(forward = true) },
                exitTransition = { materialSharedAxisXOut(forward = true) },
                popEnterTransition = { materialSharedAxisXIn(forward = false) },
                popExitTransition = { materialSharedAxisXOut(forward = false) }
            ) {
                // Dependency Injection: Pass the shared ViewModel and back-navigation callback
                when(route) {
                    "time" -> timeScreen(onBack = { rootNavController.popBackStack() }, viewModel = viewModel)
                    "sound" -> soundScreen(onBack = { rootNavController.popBackStack() }, viewModel = viewModel)
                }
            }
        }
    }
}