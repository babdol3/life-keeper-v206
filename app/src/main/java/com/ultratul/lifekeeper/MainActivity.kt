package com.ultratul.lifekeeper

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ultratul.lifekeeper.geofence.GeofenceManager
import com.ultratul.lifekeeper.ui.LifeKeeperApp
import com.ultratul.lifekeeper.ui.LifeKeeperViewModel
import com.ultratul.lifekeeper.ui.theme.LifeKeeperTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LifeKeeperViewModel by viewModels {
        LifeKeeperViewModel.Factory(application)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        viewModel.onPermissionResult(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GeofenceManager.ensureNotificationChannel(this)
        handleIncomingShare(intent)

        setContent {
            LifeKeeperTheme {
                LifeKeeperApp(
                    viewModel = viewModel,
                    onRequestPermissions = { requestRuntimePermissions() },
                    onOpenAppSettings = { openAppSettings() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingShare(intent)
    }

    private fun handleIncomingShare(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        if (!intent.type.orEmpty().startsWith("text/")) return

        val sharedText = buildString {
            intent.getStringExtra(Intent.EXTRA_SUBJECT)?.let { subject ->
                append(subject)
                append("\n")
            }
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
                append(text)
            }
        }.trim()

        if (sharedText.isNotBlank()) {
            viewModel.importSharedPlaceText(sharedText)
        }
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    private fun requestRuntimePermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

        permissionLauncher.launch(permissions)
    }
}
