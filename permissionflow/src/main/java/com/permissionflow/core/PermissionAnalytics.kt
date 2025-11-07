package com.permissionflow.core

/**
 * Analytics event data for permission requests.
 */
data class PermissionEvent(
    val permission: String,
    val eventType: EventType,
    val result: PermissionResult? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
) {
    enum class EventType {
        REQUESTED,
        GRANTED,
        DENIED,
        PERMANENTLY_DENIED,
        RATIONALE_SHOWN,
        SETTINGS_OPENED,
        HISTORY_CLEARED
    }
}

/**
 * Interface for permission analytics tracking.
 * Implement this to integrate with your analytics solution (Firebase, Mixpanel, etc.)
 */
interface PermissionAnalyticsTracker {
    /**
     * Track a permission event.
     */
    fun trackEvent(event: PermissionEvent)

    /**
     * Track multiple permissions event.
     */
    fun trackMultiPermissionEvent(
        permissions: List<String>,
        result: MultiPermissionResult,
        metadata: Map<String, Any> = emptyMap()
    )
}

/**
 * Global analytics configuration for PermissionFlow.
 */
object PermissionAnalytics {

    private val trackers = mutableListOf<PermissionAnalyticsTracker>()

    /**
     * Register an analytics tracker.
     * You can register multiple trackers to send events to different services.
     *
     * Usage:
     * ```
     * PermissionAnalytics.registerTracker(object : PermissionAnalyticsTracker {
     *     override fun trackEvent(event: PermissionEvent) {
     *         Firebase.analytics.logEvent("permission_${event.eventType.name.lowercase()}", bundleOf(
     *             "permission" to event.permission,
     *             "result" to event.result?.toString()
     *         ))
     *     }
     * })
     * ```
     */
    fun registerTracker(tracker: PermissionAnalyticsTracker) {
        trackers.add(tracker)
    }

    /**
     * Unregister an analytics tracker.
     */
    fun unregisterTracker(tracker: PermissionAnalyticsTracker) {
        trackers.remove(tracker)
    }

    /**
     * Clear all registered trackers.
     */
    fun clearTrackers() {
        trackers.clear()
    }

    /**
     * Track a permission event to all registered trackers.
     */
    internal fun track(event: PermissionEvent) {
        trackers.forEach { tracker ->
            try {
                tracker.trackEvent(event)
            } catch (e: Exception) {
                PermissionLogger.error("Error tracking permission event", e)
            }
        }
    }

    /**
     * Track a multiple permissions event to all registered trackers.
     */
    internal fun trackMultiple(
        permissions: List<String>,
        result: MultiPermissionResult,
        metadata: Map<String, Any> = emptyMap()
    ) {
        trackers.forEach { tracker ->
            try {
                tracker.trackMultiPermissionEvent(permissions, result, metadata)
            } catch (e: Exception) {
                PermissionLogger.error("Error tracking multi-permission event", e)
            }
        }
    }
}

/**
 * Built-in console analytics tracker for debugging.
 */
class ConsoleAnalyticsTracker : PermissionAnalyticsTracker {
    override fun trackEvent(event: PermissionEvent) {
        println("""
            [Analytics] Permission Event:
            - Permission: ${event.permission}
            - Type: ${event.eventType}
            - Result: ${event.result}
            - Timestamp: ${event.timestamp}
            - Metadata: ${event.metadata}
        """.trimIndent())
    }

    override fun trackMultiPermissionEvent(
        permissions: List<String>,
        result: MultiPermissionResult,
        metadata: Map<String, Any>
    ) {
        println("""
            [Analytics] Multi-Permission Event:
            - Permissions: ${permissions.joinToString()}
            - All Granted: ${result.allGranted}
            - Granted: ${result.granted.size}
            - Denied: ${result.denied.size}
            - Permanently Denied: ${result.permanentlyDenied.size}
            - Metadata: $metadata
        """.trimIndent())
    }
}

/**
 * Simple in-memory analytics tracker for testing.
 */
class InMemoryAnalyticsTracker : PermissionAnalyticsTracker {
    private val _events = mutableListOf<PermissionEvent>()
    val events: List<PermissionEvent> get() = _events

    private val _multiPermissionEvents = mutableListOf<Triple<List<String>, MultiPermissionResult, Map<String, Any>>>()
    val multiPermissionEvents: List<Triple<List<String>, MultiPermissionResult, Map<String, Any>>>
        get() = _multiPermissionEvents

    override fun trackEvent(event: PermissionEvent) {
        _events.add(event)
    }

    override fun trackMultiPermissionEvent(
        permissions: List<String>,
        result: MultiPermissionResult,
        metadata: Map<String, Any>
    ) {
        _multiPermissionEvents.add(Triple(permissions, result, metadata))
    }

    fun clear() {
        _events.clear()
        _multiPermissionEvents.clear()
    }

    fun getEventsForPermission(permission: String): List<PermissionEvent> {
        return _events.filter { it.permission == permission }
    }

    fun getEventsByType(type: PermissionEvent.EventType): List<PermissionEvent> {
        return _events.filter { it.eventType == type }
    }
}
