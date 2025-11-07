package com.permissionflow.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.permissionflow.core.PermissionFlow
import com.permissionflow.core.PermissionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

/**
 * Helper object for navigating to app settings.
 */
object SettingsHelper {

    /**
     * Create an intent to open the app's settings page.
     *
     * @param context The context to use
     * @return Intent to open app settings
     */
    fun createAppSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Open the app's settings page.
     *
     * @param context The context to use
     */
    fun openAppSettings(context: Context) {
        context.startActivity(createAppSettingsIntent(context))
    }

    /**
     * Create an intent to open the notification settings page (Android 8+).
     *
     * @param context The context to use
     * @return Intent to open notification settings
     */
    fun createNotificationSettingsIntent(context: Context): Intent {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            createAppSettingsIntent(context)
        }
    }

    /**
     * Open the notification settings page.
     *
     * @param context The context to use
     */
    fun openNotificationSettings(context: Context) {
        context.startActivity(createNotificationSettingsIntent(context))
    }

    /**
     * Create an intent to open location settings.
     *
     * @return Intent to open location settings
     */
    fun createLocationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Open location settings.
     *
     * @param context The context to use
     */
    fun openLocationSettings(context: Context) {
        context.startActivity(createLocationSettingsIntent())
    }
}

/**
 * Extension function to open app settings from any Context.
 */
fun Context.openAppSettings() {
    SettingsHelper.openAppSettings(this)
}

/**
 * Extension function to open notification settings from any Context.
 */
fun Context.openNotificationSettings() {
    SettingsHelper.openNotificationSettings(this)
}

/**
 * Extension function to open location settings from any Context.
 */
fun Context.openLocationSettings() {
    SettingsHelper.openLocationSettings(this)
}

/**
 * Settings launcher for monitoring when user returns from settings.
 */
class SettingsLauncher {
    private var launcher: ActivityResultLauncher<Intent>? = null
    private var onResult: ((Unit) -> Unit)? = null

    /**
     * Register with an Activity.
     */
    fun register(activity: ComponentActivity, onResult: (Unit) -> Unit) {
        this.onResult = onResult
        launcher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            onResult(Unit)
        }
    }

    /**
     * Register with a Fragment.
     */
    fun register(fragment: Fragment, onResult: (Unit) -> Unit) {
        this.onResult = onResult
        launcher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            onResult(Unit)
        }
    }

    /**
     * Launch settings with the given intent.
     */
    fun launch(intent: Intent) {
        launcher?.launch(intent)
            ?: throw IllegalStateException("SettingsLauncher not registered")
    }

    /**
     * Unregister the launcher.
     */
    fun unregister() {
        launcher?.unregister()
        launcher = null
        onResult = null
    }
}

/**
 * Request a permission with automatic fallback to settings if permanently denied.
 *
 * This extension function will:
 * 1. Request the permission normally
 * 2. If permanently denied, automatically open app settings
 * 3. When user returns, re-check the permission status
 *
 * @param permission The permission to request
 * @param onSettingsReturn Callback when user returns from settings (optional)
 * @return Flow that emits permission results
 */
fun PermissionFlow.requestWithSettingsFallback(
    permission: String,
    context: Context,
    onSettingsReturn: (() -> Unit)? = null
): Flow<PermissionResult> = flow {
    requestPermission(permission).collect { result ->
        when (result) {
            is PermissionResult.PermanentlyDenied -> {
                // Open settings
                SettingsHelper.openAppSettings(context)

                // User needs to manually grant permission in settings
                // Emit the permanently denied result
                emit(result)

                // Optionally notify when returning
                onSettingsReturn?.invoke()
            }
            else -> {
                emit(result)
            }
        }
    }
}

/**
 * Extension function for easier settings navigation when permission is permanently denied.
 *
 * Usage:
 * ```
 * when (result) {
 *     is PermissionResult.PermanentlyDenied -> {
 *         showDialog(
 *             message = "Please enable permission in settings",
 *             onConfirm = { context.openAppSettings() }
 *         )
 *     }
 * }
 * ```
 */
fun PermissionResult.PermanentlyDenied.openSettings(context: Context) {
    SettingsHelper.openAppSettings(context)
}
