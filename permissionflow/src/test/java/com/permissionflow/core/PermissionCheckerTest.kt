package com.permissionflow.core

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PermissionCheckerTest {

    private lateinit var context: Context
    private lateinit var permissionChecker: PermissionChecker

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        permissionChecker = PermissionChecker(context)
        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isPermissionGranted returns true when permission is granted`() {
        // Given
        val permission = android.Manifest.permission.CAMERA
        every {
            ContextCompat.checkSelfPermission(context, permission)
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = permissionChecker.isPermissionGranted(permission)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isPermissionGranted returns false when permission is denied`() {
        // Given
        val permission = android.Manifest.permission.CAMERA
        every {
            ContextCompat.checkSelfPermission(context, permission)
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = permissionChecker.isPermissionGranted(permission)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areAllPermissionsGranted returns true when all permissions are granted`() {
        // Given
        val permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
        every {
            ContextCompat.checkSelfPermission(context, any())
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = permissionChecker.areAllPermissionsGranted(permissions)

        // Then
        assertTrue(result)
    }

    @Test
    fun `areAllPermissionsGranted returns false when any permission is denied`() {
        // Given
        val permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
        every {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_DENIED

        // When
        val result = permissionChecker.areAllPermissionsGranted(permissions)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areAllPermissionsGranted returns true for empty list`() {
        // Given
        val permissions = emptyList<String>()

        // When
        val result = permissionChecker.areAllPermissionsGranted(permissions)

        // Then
        assertTrue(result)
    }

    @Test
    fun `getMultiPermissionStatus returns status map for all permissions`() {
        // Given
        val permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        every {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_DENIED
        every {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = permissionChecker.getMultiPermissionStatus(permissions)

        // Then
        assertEquals(3, result.size)
        assertTrue(result[android.Manifest.permission.CAMERA] == true)
        assertTrue(result[android.Manifest.permission.RECORD_AUDIO] == false)
        assertTrue(result[android.Manifest.permission.ACCESS_FINE_LOCATION] == true)
    }

    @Test
    fun `getMultiPermissionStatus returns empty map for empty list`() {
        // Given
        val permissions = emptyList<String>()

        // When
        val result = permissionChecker.getMultiPermissionStatus(permissions)

        // Then
        assertTrue(result.isEmpty())
    }
}
