package com.permissionflow.core

import android.Manifest
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PermissionAnalyticsTest {

    private lateinit var mockTracker: PermissionAnalyticsTracker

    @Before
    fun setup() {
        mockTracker = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        // Clear all trackers after each test
        PermissionAnalytics.clearTrackers()
    }

    @Test
    fun `registerTracker adds tracker`() {
        // When
        PermissionAnalytics.registerTracker(mockTracker)

        // Then
        // Tracker is registered (tested by tracking events)
        val event = PermissionEvent(
            permission = Manifest.permission.CAMERA,
            eventType = PermissionEvent.EventType.REQUESTED
        )
        PermissionAnalytics.track(event)

        verify { mockTracker.trackEvent(event) }
    }

    @Test
    fun `unregisterTracker removes tracker`() {
        // Given
        PermissionAnalytics.registerTracker(mockTracker)

        // When
        PermissionAnalytics.unregisterTracker(mockTracker)

        // Then
        val event = PermissionEvent(
            permission = Manifest.permission.CAMERA,
            eventType = PermissionEvent.EventType.REQUESTED
        )
        PermissionAnalytics.track(event)

        // Tracker should not receive events after unregistering
        verify(exactly = 0) { mockTracker.trackEvent(event) }
    }

    @Test
    fun `multiple trackers can be registered`() {
        // Given
        val tracker1 = mockk<PermissionAnalyticsTracker>(relaxed = true)
        val tracker2 = mockk<PermissionAnalyticsTracker>(relaxed = true)

        PermissionAnalytics.registerTracker(tracker1)
        PermissionAnalytics.registerTracker(tracker2)

        // When
        val event = PermissionEvent(
            permission = Manifest.permission.CAMERA,
            eventType = PermissionEvent.EventType.REQUESTED
        )
        PermissionAnalytics.track(event)

        // Then
        verify { tracker1.trackEvent(event) }
        verify { tracker2.trackEvent(event) }
    }

    @Test
    fun `PermissionEvent has correct structure`() {
        // When
        val event = PermissionEvent(
            permission = Manifest.permission.CAMERA,
            eventType = PermissionEvent.EventType.GRANTED,
            result = PermissionResult.Granted,
            timestamp = 1234567890L
        )

        // Then
        assertEquals(Manifest.permission.CAMERA, event.permission)
        assertEquals(PermissionEvent.EventType.GRANTED, event.eventType)
        assertEquals(PermissionResult.Granted, event.result)
        assertEquals(1234567890L, event.timestamp)
    }

    @Test
    fun `PermissionEvent EventTypes are defined correctly`() {
        // When
        val requested = PermissionEvent.EventType.REQUESTED
        val granted = PermissionEvent.EventType.GRANTED
        val denied = PermissionEvent.EventType.DENIED
        val permanentlyDenied = PermissionEvent.EventType.PERMANENTLY_DENIED
        val rationaleShown = PermissionEvent.EventType.RATIONALE_SHOWN
        val settingsOpened = PermissionEvent.EventType.SETTINGS_OPENED
        val historyCleared = PermissionEvent.EventType.HISTORY_CLEARED

        // Then
        assertNotNull(requested)
        assertNotNull(granted)
        assertNotNull(denied)
        assertNotNull(permanentlyDenied)
        assertNotNull(rationaleShown)
        assertNotNull(settingsOpened)
        assertNotNull(historyCleared)
    }

    @Test
    fun `track event with result`() {
        // Given
        PermissionAnalytics.registerTracker(mockTracker)

        // When
        val event = PermissionEvent(
            permission = Manifest.permission.CAMERA,
            eventType = PermissionEvent.EventType.GRANTED,
            result = PermissionResult.Granted
        )
        PermissionAnalytics.track(event)

        // Then
        verify {
            mockTracker.trackEvent(match { e ->
                e.permission == Manifest.permission.CAMERA &&
                e.eventType == PermissionEvent.EventType.GRANTED &&
                e.result == PermissionResult.Granted
            })
        }
    }

    @Test
    fun `track multiple permission event`() {
        // Given
        PermissionAnalytics.registerTracker(mockTracker)
        val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        val result = MultiPermissionResult(
            granted = permissions,
            denied = emptyList(),
            permanentlyDenied = emptyList(),
            results = mapOf(
                Manifest.permission.CAMERA to PermissionResult.Granted,
                Manifest.permission.RECORD_AUDIO to PermissionResult.Granted
            )
        )

        // When
        PermissionAnalytics.trackMultiple(permissions, result)

        // Then
        verify {
            mockTracker.trackMultiPermissionEvent(permissions, result, any())
        }
    }

    @Test
    fun `clearTrackers removes all trackers`() {
        // Given
        val tracker1 = mockk<PermissionAnalyticsTracker>(relaxed = true)
        val tracker2 = mockk<PermissionAnalyticsTracker>(relaxed = true)

        PermissionAnalytics.registerTracker(tracker1)
        PermissionAnalytics.registerTracker(tracker2)

        // When
        PermissionAnalytics.clearTrackers()

        // Then
        val event = PermissionEvent(
            permission = Manifest.permission.CAMERA,
            eventType = PermissionEvent.EventType.REQUESTED
        )
        PermissionAnalytics.track(event)

        verify(exactly = 0) { tracker1.trackEvent(any()) }
        verify(exactly = 0) { tracker2.trackEvent(any()) }
    }

    @Test
    fun `PermissionEvent timestamp is set automatically`() {
        // When
        val event = PermissionEvent(
            permission = Manifest.permission.CAMERA,
            eventType = PermissionEvent.EventType.REQUESTED
        )

        // Then
        assertTrue(event.timestamp > 0)
    }
}
