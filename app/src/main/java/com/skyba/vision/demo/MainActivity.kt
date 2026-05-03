package com.skyba.vision.demo

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artems_apps.vision_pause.ui.navigation.AppRootNavigation
import com.skyba.vision.demo.data.local.SettingsViewModel
import com.skyba.vision.demo.ui.components.RecommendedPopUp
import com.skyba.vision.demo.ui.theme.SKYbaVisionDemoTheme

/**
 * [UI Layer] The main entry point of the application.
 * Handles permission requests (Notifications, Activity Recognition) and sets up the Compose UI.
 */
class MainActivity : ComponentActivity() {

    /**
     * [Permission] Launcher for Physical Activity tracking.
     * Required for features like step counting or movement-based tracking on Android 10+.
     */
    private val requestActivityRecognitionPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

        }

    /*
     * [Permission] Launcher for Notifications (Android 13+ requirement).
     * Chains to Activity Recognition permission after the notification prompt is handled.
     */

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestActivityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // [UI] Enables drawing under system bars (status and navigation)
        enableEdgeToEdge()

         // [Logic] Initiates the permission request flow based on the Android OS version
        checkPermissions()

        setContent {

            val viewModel: SettingsViewModel = viewModel()
            val context = LocalContext.current

            SKYbaVisionDemoTheme {

                /*
                  * [UX] Battery Optimization Warning.
                  * Prompts the user to disable restrictions to ensure the background timer service works reliably.
                  */

                if (viewModel.showRecommendedPopUp) {
                    RecommendedPopUp(
                        isVisible = true,
                        onDismiss = { viewModel.dismissPopUp(permanently = true) },
                        onConfirm = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                            viewModel.dismissPopUp(permanently = true)
                        }
                    )
                }

                // [Navigation] The root of the application's navigation graph
                AppRootNavigation(viewModel = viewModel)

            }
        }
    }

    /**
     * [Logic] Version-specific permission handling.
     * - Android 13 (Tiramisu) and above: Requests Notifications first.
     * - Android 10 (Q) to 12: Requests Activity Recognition directly.
     */
    private fun checkPermissions() {
        when {

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                requestActivityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }
}

