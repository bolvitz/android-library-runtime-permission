package com.permissionflow.core

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

/**
 * Manages permission state tracking to detect permanently denied permissions.
 * Uses SharedPreferences to remember which permissions have been requested before.
 */
internal class PermissionStateManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Mark that a permission has been requested.
     */
    fun markPermissionRequested(permission: String) {
        prefs.edit()
            .putBoolean(getPermissionKey(permission), true)
            .apply()
    }

    /**
     * Check if a permission has been requested before.
     */
    fun wasPermissionRequestedBefore(permission: String): Boolean {
        return prefs.getBoolean(getPermissionKey(permission), false)
    }

    /**
     * Clear the request history for a permission.
     * Useful for testing or if the user grants permission through settings.
     */
    fun clearPermissionHistory(permission: String) {
        prefs.edit()
            .remove(getPermissionKey(permission))
            .apply()
    }

    /**
     * Clear all permission history.
     */
    fun clearAllHistory() {
        prefs.edit().clear().apply()
    }

    /**
     * Determine if a permission is permanently denied.
     * A permission is considered permanently denied if:
     * 1. It has been requested before
     * 2. It's not currently granted
     * 3. We should NOT show rationale (meaning user selected "Don't ask again")
     */
    fun isPermanentlyDenied(
        activity: Activity,
        permission: String,
        checker: PermissionChecker
    ): Boolean {
        val wasRequested = wasPermissionRequestedBefore(permission)
        val isGranted = checker.isPermissionGranted(permission)
        val shouldShowRationale = checker.shouldShowRationale(activity, permission)

        return wasRequested && !isGranted && !shouldShowRationale
    }

    /**
     * Convert a raw permission result (boolean) to a PermissionResult based on current state.
     */
    fun convertToPermissionResult(
        activity: Activity,
        permission: String,
        isGranted: Boolean,
        checker: PermissionChecker
    ): PermissionResult {
        return when {
            isGranted -> {
                // Clear history when granted to allow proper tracking if denied again later
                clearPermissionHistory(permission)
                PermissionResult.Granted
            }
            isPermanentlyDenied(activity, permission, checker) -> {
                PermissionResult.PermanentlyDenied
            }
            else -> {
                // Mark as requested for future permanent denial detection
                markPermissionRequested(permission)
                val shouldShowRationale = checker.shouldShowRationale(activity, permission)
                PermissionResult.Denied(shouldShowRationale)
            }
        }
    }

    /**
     * Convert multiple permission results to MultiPermissionResult.
     */
    fun convertToMultiPermissionResult(
        activity: Activity,
        results: Map<String, Boolean>,
        checker: PermissionChecker
    ): MultiPermissionResult {
        val granted = mutableListOf<String>()
        val denied = mutableListOf<String>()
        val permanentlyDenied = mutableListOf<String>()
        val individualResults = mutableMapOf<String, PermissionResult>()

        results.forEach { (permission, isGranted) ->
            val result = convertToPermissionResult(activity, permission, isGranted, checker)
            individualResults[permission] = result

            when (result) {
                is PermissionResult.Granted -> granted.add(permission)
                is PermissionResult.Denied -> denied.add(permission)
                is PermissionResult.PermanentlyDenied -> permanentlyDenied.add(permission)
            }
        }

        return MultiPermissionResult(
            granted = granted,
            denied = denied,
            permanentlyDenied = permanentlyDenied,
            results = individualResults
        )
    }

    private fun getPermissionKey(permission: String): String {
        return "${KEY_PREFIX}${permission}"
    }

    companion object {
        private const val PREFS_NAME = "permission_flow_prefs"
        private const val KEY_PREFIX = "permission_requested_"
    }
}
