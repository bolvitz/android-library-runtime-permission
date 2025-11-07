package com.permissionflow.core

import android.util.Log

/**
 * Debug logger for PermissionFlow library.
 * Enable logging to debug permission request flow and state changes.
 */
object PermissionLogger {

    private const val TAG = "PermissionFlow"

    /**
     * Enable or disable debug logging.
     * Default is false (disabled).
     */
    var isEnabled: Boolean = false

    /**
     * Custom log handler for integrating with custom logging solutions.
     * If set, all logs will be directed to this handler instead of Android Log.
     */
    var logHandler: ((level: LogLevel, tag: String, message: String, throwable: Throwable?) -> Unit)? = null

    enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }

    fun debug(message: String) {
        if (!isEnabled) return
        logHandler?.invoke(LogLevel.DEBUG, TAG, message, null)
            ?: Log.d(TAG, message)
    }

    fun info(message: String) {
        if (!isEnabled) return
        logHandler?.invoke(LogLevel.INFO, TAG, message, null)
            ?: Log.i(TAG, message)
    }

    fun warn(message: String, throwable: Throwable? = null) {
        if (!isEnabled) return
        logHandler?.invoke(LogLevel.WARNING, TAG, message, throwable)
            ?: if (throwable != null) {
                Log.w(TAG, message, throwable)
            } else {
                Log.w(TAG, message)
            }
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (!isEnabled) return
        logHandler?.invoke(LogLevel.ERROR, TAG, message, throwable)
            ?: if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
    }

    // Convenience methods for permission-specific logging
    fun logPermissionRequest(permission: String) {
        debug("Requesting permission: $permission")
    }

    fun logPermissionResult(permission: String, result: PermissionResult) {
        info("Permission result for $permission: ${result::class.simpleName}")
    }

    fun logPermissionCheck(permission: String, status: PermissionStatus) {
        debug("Permission status for $permission: ${status::class.simpleName}")
    }

    fun logMultiPermissionRequest(permissions: Array<out String>) {
        debug("Requesting multiple permissions: ${permissions.joinToString()}")
    }

    fun logMultiPermissionResult(result: MultiPermissionResult) {
        info("""
            Multi-permission result:
            - Granted: ${result.granted.size}
            - Denied: ${result.denied.size}
            - Permanently Denied: ${result.permanentlyDenied.size}
        """.trimIndent())
    }

    fun logStateChange(permission: String, from: PermissionStatus, to: PermissionStatus) {
        info("Permission $permission changed from ${from::class.simpleName} to ${to::class.simpleName}")
    }

    fun logHistoryCleared(permission: String?) {
        if (permission != null) {
            info("Permission history cleared for: $permission")
        } else {
            info("All permission history cleared")
        }
    }
}

/**
 * Extension function to enable debug logging.
 *
 * Usage:
 * ```
 * PermissionLogger.enableDebugLogging()
 * ```
 */
fun PermissionLogger.enableDebugLogging() {
    isEnabled = true
    info("PermissionFlow debug logging enabled")
}

/**
 * Extension function to disable debug logging.
 */
fun PermissionLogger.disableDebugLogging() {
    info("PermissionFlow debug logging disabled")
    isEnabled = false
}

/**
 * Set a custom log handler for integrating with crash reporting or analytics.
 *
 * Usage:
 * ```
 * PermissionLogger.setLogHandler { level, tag, message, throwable ->
 *     Timber.tag(tag).log(level.toPriority(), message, throwable)
 * }
 * ```
 */
fun PermissionLogger.setLogHandler(
    handler: (level: PermissionLogger.LogLevel, tag: String, message: String, throwable: Throwable?) -> Unit
) {
    logHandler = handler
}
