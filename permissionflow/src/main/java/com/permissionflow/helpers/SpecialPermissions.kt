package com.permissionflow.helpers

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Helper for managing special Android permissions that don't use the standard runtime permission system.
 * These include SYSTEM_ALERT_WINDOW, WRITE_SETTINGS, PACKAGE_USAGE_STATS, etc.
 */
object SpecialPermissions {

    /**
     * Check if the app has SYSTEM_ALERT_WINDOW permission (overlay permission).
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // No permission needed before Android M
        }
    }

    /**
     * Open settings to request SYSTEM_ALERT_WINDOW permission.
     */
    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Create a Flow for observing overlay permission.
     * Note: This requires manual polling when returning from settings.
     */
    fun observeOverlayPermission(context: Context): Flow<Boolean> = flow {
        emit(canDrawOverlays(context))
    }

    /**
     * Check if the app has WRITE_SETTINGS permission.
     */
    fun canWriteSettings(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true // No permission needed before Android M
        }
    }

    /**
     * Open settings to request WRITE_SETTINGS permission.
     */
    fun openWriteSettingsPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Check if the app has PACKAGE_USAGE_STATS permission.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false
        }

        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps?.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps?.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Open settings to request PACKAGE_USAGE_STATS permission.
     */
    fun openUsageStatsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Check if the app has REQUEST_INSTALL_PACKAGES permission.
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true // No permission needed before Android O
        }
    }

    /**
     * Open settings to request REQUEST_INSTALL_PACKAGES permission.
     */
    fun openInstallPackagesSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Check if the app has MANAGE_EXTERNAL_STORAGE permission (Android 11+).
     */
    fun hasManageExternalStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            // Check for WRITE_EXTERNAL_STORAGE on older versions
            context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Open settings to request MANAGE_EXTERNAL_STORAGE permission (Android 11+).
     */
    fun openManageExternalStorageSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Check if the app can schedule exact alarms (Android 12+).
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true // No permission needed before Android 12
        }
    }

    /**
     * Open settings to request SCHEDULE_EXACT_ALARM permission (Android 12+).
     */
    fun openScheduleExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Check all special permissions at once.
     */
    fun checkAllSpecialPermissions(context: Context): Map<String, Boolean> {
        return mapOf(
            "SYSTEM_ALERT_WINDOW" to canDrawOverlays(context),
            "WRITE_SETTINGS" to canWriteSettings(context),
            "PACKAGE_USAGE_STATS" to hasUsageStatsPermission(context),
            "REQUEST_INSTALL_PACKAGES" to canRequestPackageInstalls(context),
            "MANAGE_EXTERNAL_STORAGE" to hasManageExternalStoragePermission(context),
            "SCHEDULE_EXACT_ALARM" to canScheduleExactAlarms(context)
        )
    }
}

/**
 * Extension functions for special permissions.
 */

fun Context.canDrawOverlays(): Boolean = SpecialPermissions.canDrawOverlays(this)
fun Context.openOverlaySettings() = SpecialPermissions.openOverlaySettings(this)

fun Context.canWriteSettings(): Boolean = SpecialPermissions.canWriteSettings(this)
fun Context.openWriteSettingsPermission() = SpecialPermissions.openWriteSettingsPermission(this)

fun Context.hasUsageStatsPermission(): Boolean = SpecialPermissions.hasUsageStatsPermission(this)
fun Context.openUsageStatsSettings() = SpecialPermissions.openUsageStatsSettings(this)

fun Context.canRequestPackageInstalls(): Boolean = SpecialPermissions.canRequestPackageInstalls(this)
fun Context.openInstallPackagesSettings() = SpecialPermissions.openInstallPackagesSettings(this)

fun Context.hasManageExternalStoragePermission(): Boolean = SpecialPermissions.hasManageExternalStoragePermission(this)
fun Context.openManageExternalStorageSettings() = SpecialPermissions.openManageExternalStorageSettings(this)

fun Context.canScheduleExactAlarms(): Boolean = SpecialPermissions.canScheduleExactAlarms(this)
fun Context.openScheduleExactAlarmSettings() = SpecialPermissions.openScheduleExactAlarmSettings(this)
