package com.permissionflow.core

import org.junit.Assert.*
import org.junit.Test

class PermissionResultTest {

    @Test
    fun `PermissionResult Granted is created correctly`() {
        // When
        val result = PermissionResult.Granted

        // Then
        assertTrue(result is PermissionResult.Granted)
    }

    @Test
    fun `PermissionResult Denied is created with rationale flag`() {
        // When
        val resultWithRationale = PermissionResult.Denied(shouldShowRationale = true)
        val resultWithoutRationale = PermissionResult.Denied(shouldShowRationale = false)

        // Then
        assertTrue(resultWithRationale is PermissionResult.Denied)
        assertTrue(resultWithRationale.shouldShowRationale)
        assertFalse(resultWithoutRationale.shouldShowRationale)
    }

    @Test
    fun `PermissionResult PermanentlyDenied is created correctly`() {
        // When
        val result = PermissionResult.PermanentlyDenied

        // Then
        assertTrue(result is PermissionResult.PermanentlyDenied)
    }

    @Test
    fun `PermissionStatus Granted is created correctly`() {
        // When
        val status = PermissionStatus.Granted

        // Then
        assertTrue(status is PermissionStatus.Granted)
    }

    @Test
    fun `PermissionStatus NotGranted is created correctly`() {
        // When
        val status = PermissionStatus.NotGranted

        // Then
        assertTrue(status is PermissionStatus.NotGranted)
    }

    @Test
    fun `PermissionStatus ShouldShowRationale is created correctly`() {
        // When
        val status = PermissionStatus.ShouldShowRationale

        // Then
        assertTrue(status is PermissionStatus.ShouldShowRationale)
    }

    @Test
    fun `PermissionStatus PermanentlyDenied is created correctly`() {
        // When
        val status = PermissionStatus.PermanentlyDenied

        // Then
        assertTrue(status is PermissionStatus.PermanentlyDenied)
    }

    @Test
    fun `MultiPermissionResult allGranted is true when all permissions granted`() {
        // Given
        val granted = listOf("CAMERA", "AUDIO")
        val denied = emptyList<String>()
        val permanentlyDenied = emptyList<String>()
        val results = mapOf(
            "CAMERA" to PermissionResult.Granted,
            "AUDIO" to PermissionResult.Granted
        )

        // When
        val result = MultiPermissionResult(granted, denied, permanentlyDenied, results)

        // Then
        assertTrue(result.allGranted)
        assertFalse(result.anyPermanentlyDenied)
    }

    @Test
    fun `MultiPermissionResult allGranted is false when any permission denied`() {
        // Given
        val granted = listOf("CAMERA")
        val denied = listOf("AUDIO")
        val permanentlyDenied = emptyList<String>()
        val results = mapOf(
            "CAMERA" to PermissionResult.Granted,
            "AUDIO" to PermissionResult.Denied(false)
        )

        // When
        val result = MultiPermissionResult(granted, denied, permanentlyDenied, results)

        // Then
        assertFalse(result.allGranted)
        assertFalse(result.anyPermanentlyDenied)
    }

    @Test
    fun `MultiPermissionResult anyPermanentlyDenied is true when any permission permanently denied`() {
        // Given
        val granted = listOf("CAMERA")
        val denied = emptyList<String>()
        val permanentlyDenied = listOf("AUDIO")
        val results = mapOf(
            "CAMERA" to PermissionResult.Granted,
            "AUDIO" to PermissionResult.PermanentlyDenied
        )

        // When
        val result = MultiPermissionResult(granted, denied, permanentlyDenied, results)

        // Then
        assertFalse(result.allGranted)
        assertTrue(result.anyPermanentlyDenied)
    }

    @Test
    fun `LocationPermissionResult PreciseGranted is created correctly`() {
        // When
        val result = LocationPermissionResult.PreciseGranted

        // Then
        assertTrue(result is LocationPermissionResult.PreciseGranted)
    }

    @Test
    fun `LocationPermissionResult ApproximateGranted is created correctly`() {
        // When
        val result = LocationPermissionResult.ApproximateGranted

        // Then
        assertTrue(result is LocationPermissionResult.ApproximateGranted)
    }

    @Test
    fun `LocationPermissionResult BackgroundGranted is created correctly`() {
        // When
        val result = LocationPermissionResult.BackgroundGranted

        // Then
        assertTrue(result is LocationPermissionResult.BackgroundGranted)
    }

    @Test
    fun `LocationPermissionResult Denied is created with rationale flag`() {
        // When
        val resultWithRationale = LocationPermissionResult.Denied(shouldShowRationale = true)
        val resultWithoutRationale = LocationPermissionResult.Denied(shouldShowRationale = false)

        // Then
        assertTrue(resultWithRationale.shouldShowRationale)
        assertFalse(resultWithoutRationale.shouldShowRationale)
    }

    @Test
    fun `LocationPermissionResult PermanentlyDenied is created correctly`() {
        // When
        val result = LocationPermissionResult.PermanentlyDenied

        // Then
        assertTrue(result is LocationPermissionResult.PermanentlyDenied)
    }

    @Test
    fun `MediaPermissionResult tracks all media types`() {
        // Given
        val images = PermissionResult.Granted
        val videos = PermissionResult.Denied(false)
        val audio = PermissionResult.PermanentlyDenied

        // When
        val result = MediaPermissionResult(images, videos, audio)

        // Then
        assertTrue(result.images is PermissionResult.Granted)
        assertTrue(result.videos is PermissionResult.Denied)
        assertTrue(result.audio is PermissionResult.PermanentlyDenied)
    }

    @Test
    fun `MediaPermissionResult allGranted is true when all media permissions granted`() {
        // Given
        val images = PermissionResult.Granted
        val videos = PermissionResult.Granted
        val audio = PermissionResult.Granted

        // When
        val result = MediaPermissionResult(images, videos, audio)

        // Then
        assertTrue(result.allGranted)
    }

    @Test
    fun `MediaPermissionResult allGranted is false when any media permission denied`() {
        // Given
        val images = PermissionResult.Granted
        val videos = PermissionResult.Denied(false)
        val audio = PermissionResult.Granted

        // When
        val result = MediaPermissionResult(images, videos, audio)

        // Then
        assertFalse(result.allGranted)
    }
}
