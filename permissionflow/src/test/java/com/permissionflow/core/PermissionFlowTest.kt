package com.permissionflow.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PermissionFlowTest {

    private lateinit var activityController: ActivityController<ComponentActivity>
    private lateinit var activity: ComponentActivity
    private lateinit var permissionFlow: PermissionFlow

    @Before
    fun setup() {
        mockkStatic(ContextCompat::class)

        // Create activity but don't start/resume yet (to avoid lifecycle timing issues)
        activityController = Robolectric.buildActivity(ComponentActivity::class.java)
        activity = activityController.create().get()

        // Create PermissionFlow BEFORE starting the activity (this is the correct pattern)
        permissionFlow = PermissionFlow(activity, activity)

        // Now start and resume the activity
        activityController.start().resume()
    }

    @After
    fun tearDown() {
        unmockkAll()
        if (!activityController.get().isDestroyed) {
            activityController.pause().stop().destroy()
        }
    }

    @Test
    fun `PermissionFlow can be created from ComponentActivity`() {
        // PermissionFlow is already created in setup()
        // Then
        assertNotNull(permissionFlow)
    }

    @Test
    fun `checkPermission returns Granted when permission is granted`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val status = permissionFlow.checkPermission(Manifest.permission.CAMERA)

        // Then
        assertEquals(PermissionStatus.Granted, status)
    }

    @Test
    fun `checkPermission returns NotGranted when permission is not granted`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_DENIED

        // When
        val status = permissionFlow.checkPermission(Manifest.permission.CAMERA)

        // Then
        assertTrue(status is PermissionStatus.NotGranted || status is PermissionStatus.ShouldShowRationale)
    }

    @Test
    fun `checkPermissions returns all permissions statuses`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_DENIED

        // When
        val statuses = permissionFlow.checkPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        // Then
        assertEquals(2, statuses.size)
        assertEquals(PermissionStatus.Granted, statuses[Manifest.permission.CAMERA])
        assertNotNull(statuses[Manifest.permission.RECORD_AUDIO])
    }

    @Test
    fun `isPermissionGranted returns true when permission is granted`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val isGranted = permissionFlow.isPermissionGranted(Manifest.permission.CAMERA)

        // Then
        assertTrue(isGranted)
    }

    @Test
    fun `isPermissionGranted returns false when permission is not granted`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_DENIED

        // When
        val isGranted = permissionFlow.isPermissionGranted(Manifest.permission.CAMERA)

        // Then
        assertFalse(isGranted)
    }

    @Test
    fun `areAllPermissionsGranted returns true when all permissions are granted`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, any())
        } returns PackageManager.PERMISSION_GRANTED

        // When
        val allGranted = permissionFlow.areAllPermissionsGranted(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        // Then
        assertTrue(allGranted)
    }

    @Test
    fun `areAllPermissionsGranted returns false when any permission is denied`() {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_DENIED

        // When
        val allGranted = permissionFlow.areAllPermissionsGranted(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        // Then
        assertFalse(allGranted)
    }

    @Test
    fun `requestPermission emits Granted when permission is already granted`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestPermission(Manifest.permission.CAMERA).test {
            val result = awaitItem()
            assertTrue(result is PermissionResult.Granted)
            awaitComplete()
        }
    }

    @Test
    fun `requestPermissions emits all granted when all permissions already granted`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, any())
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).test {
            val result = awaitItem()
            assertTrue(result.allGranted)
            assertEquals(2, result.granted.size)
            assertEquals(0, result.denied.size)
            assertEquals(0, result.permanentlyDenied.size)
            awaitComplete()
        }
    }
}
