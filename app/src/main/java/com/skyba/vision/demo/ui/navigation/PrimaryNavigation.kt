package com.artems_apps.vision_pause.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.artems_apps.vision_pause.data.local.StatsViewModel
import com.artems_apps.vision_pause.ui.screens.settingsScreen
import com.artems_apps.vision_pause.ui.screens.timerScreen
import com.skyba.vision.demo.R
import com.skyba.vision.demo.data.local.SettingsViewModel
import com.skyba.vision.demo.ui.components.MyAppTopAppBar
import com.skyba.vision.demo.ui.screens.statsScreen

/**
 * [Navigation Layer] The primary navigation wrapper of the application.
 * Manages the Bottom Navigation bar and switches between Main, Stats, and Settings tabs.
 */
@Composable
fun PrimaryNavigation(
    rootNavController: NavController,
    viewModel: SettingsViewModel
) {
    // Separate NavController for tab-based navigation to keep backstack clean
    val tabsNavController = rememberNavController()
    val navBackStackEntry by tabsNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        /**
         * [UI] Dynamic TopAppBar that changes its title based on the current tab.
         */

        topBar = {

            MyAppTopAppBar(
                titleRes = when (currentRoute) {
                    "main" -> R.string.main_screen_title
                    "stats" -> R.string.stats_screen_title
                    "settings" -> R.string.menu_title
                    else -> R.string.app_name
                },
                onBack = null
            )
        },

        /**
         * [UX] Custom Floating Bottom Navigation Bar.
         * Styled with Rounded Corners and Surface elevation for a modern look.
         */

        bottomBar = {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    // --- Main Tab ---
                    NavigationBarItem(
                        selected = currentRoute == "main",
                        onClick = { if (currentRoute != "main") tabsNavController.navigate("main") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.timer_button_icon),
                                contentDescription = "Головна",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(

                            indicatorColor = MaterialTheme.colorScheme.primary,

                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,

                            unselectedIconColor = MaterialTheme.colorScheme.primary
                        ),
                        label = { Text(text = stringResource(R.string.main_screen_title),
                            color = MaterialTheme.colorScheme.primary) }

                    )
                    // --- Stats Tab ---
                    NavigationBarItem(
                        selected = currentRoute == "stats",
                        onClick = { if (currentRoute != "stats") tabsNavController.navigate("stats") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.timeline_button_icon),
                                contentDescription = "Головна",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(

                            indicatorColor = MaterialTheme.colorScheme.primary,

                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,

                            unselectedIconColor = MaterialTheme.colorScheme.primary
                        ),
                        label = { Text(text = stringResource(R.string.stats_screen_title),
                            color = MaterialTheme.colorScheme.primary) }
                    )
                    // --- Settings Tab ---
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = { if (currentRoute != "settings") tabsNavController.navigate("settings") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.burger_menu_button_icon),
                                contentDescription = "Головна",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(

                            indicatorColor = MaterialTheme.colorScheme.primary,

                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,

                            unselectedIconColor = MaterialTheme.colorScheme.primary
                        ),
                        label = { Text(text = stringResource(R.string.menu_title),
                            color = MaterialTheme.colorScheme.primary) }
                    )
                }
            }
        }
    ) { innerPadding ->
        /**
         * [Navigation] Tab-level NavHost.
         * Defines the screens for each bottom navigation item.
         */
        NavHost(
            navController = tabsNavController,
            startDestination = "main",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("main") { timerScreen(viewModel) }
            composable("stats") {
                val context = LocalContext.current

                /*
                * [Architecture] Manual ViewModel injection via Factory.
                * Ensures StatsViewModel receives the required database and repository instances.
                */
                val statsVm: StatsViewModel = viewModel(
                    modelClass = StatsViewModel::class.java,
                    factory = StatsViewModel.Factory(context)
                )

                statsScreen(viewModel = statsVm)
            }
            composable("settings") {
                settingsScreen(
                    onTimeClick = { rootNavController.navigate("time") },
                    onSoundClick = { rootNavController.navigate("sound") }
                )
            }
        }
    }
}