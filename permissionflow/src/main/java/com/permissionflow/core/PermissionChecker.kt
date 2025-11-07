package com.permissionflow.core

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utility class for checking permission status without requesting them.
 */
internal class PermissionChecker(private val context: Context) {

    /**
     * Check if a permission is granted.
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if we should show permission rationale.
     * This returns true if the user has previously denied the permission.
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Get the current status of a permission without requesting it.
     */
    fun getPermissionStatus(activity: Activity, permission: String): PermissionStatus {
        return when {
            isPermissionGranted(permission) -> {
                PermissionStatus.Granted
            }
            shouldShowRationale(activity, permission) -> {
                PermissionStatus.ShouldShowRationale
            }
            else -> {
                // If permission is not granted and we shouldn't show rationale,
                // it could mean either:
                // 1. First time asking (NotGranted)
                // 2. User selected "Don't ask again" (PermanentlyDenied)
                // We can't distinguish between these two states reliably,
                // so we need to track previous requests
                PermissionStatus.NotGranted
            }
        }
    }

    /**
     * Check if multiple permissions are all granted.
     */
    fun areAllPermissionsGranted(permissions: List<String>): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }

    /**
     * Get the status of multiple permissions.
     */
    fun getMultiPermissionStatus(permissions: List<String>): Map<String, Boolean> {
        return permissions.associateWith { isPermissionGranted(it) }
    }
}
