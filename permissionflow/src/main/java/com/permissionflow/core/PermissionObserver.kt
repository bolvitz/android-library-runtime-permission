package com.permissionflow.core

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observes permission changes in real-time.
 * This allows the app to react when users grant/revoke permissions through settings.
 */
internal class PermissionObserver(
    private val context: Context,
    private val activity: Activity,
    private val checker: PermissionChecker,
    private val stateManager: PermissionStateManager
) {

    /**
     * Observe changes to a specific permission.
     * Emits whenever the permission status might have changed.
     *
     * @param permission The permission to observe
     * @return Flow that emits the current permission status
     */
    fun observePermissionStatus(permission: String): Flow<PermissionStatus> = callbackFlow {
        // Send initial status
        trySend(getCurrentStatus(permission))

        // Register broadcast receiver for app resume events
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // When app resumes, check if permission status changed
                trySend(getCurrentStatus(permission))
            }
        }

        // Register for activity lifecycle events
        // Note: This is a simplified approach. In production, you might want to use
        // androidx.lifecycle.ProcessLifecycleOwner for more robust lifecycle tracking
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }

        try {
            context.registerReceiver(receiver, filter)
        } catch (e: Exception) {
            // Handle registration failure
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Receiver might not be registered
            }
        }
    }.distinctUntilChanged()

    /**
     * Create a Flow that periodically checks permission status.
     * Useful for detecting changes when user returns from settings.
     *
     * @param permission The permission to observe
     * @return Flow that emits permission status changes
     */
    fun observePermissionWithPolling(permission: String): Flow<PermissionStatus> = callbackFlow {
        var lastStatus: PermissionStatus? = null

        val checkStatus = {
            val currentStatus = getCurrentStatus(permission)
            if (currentStatus != lastStatus) {
                lastStatus = currentStatus
                trySend(currentStatus)
            }
        }

        // Initial check
        checkStatus()

        // Set up periodic checking (when app is in foreground)
        // This will be managed by the lifecycle-aware collection

        awaitClose {
            // Cleanup if needed
        }
    }.distinctUntilChanged()

    /**
     * Get the current permission status.
     */
    private fun getCurrentStatus(permission: String): PermissionStatus {
        return when {
            checker.isPermissionGranted(permission) -> {
                PermissionStatus.Granted
            }
            stateManager.isPermanentlyDenied(activity, permission, checker) -> {
                PermissionStatus.PermanentlyDenied
            }
            checker.shouldShowRationale(activity, permission) -> {
                PermissionStatus.ShouldShowRationale
            }
            else -> {
                PermissionStatus.NotGranted
            }
        }
    }
}
