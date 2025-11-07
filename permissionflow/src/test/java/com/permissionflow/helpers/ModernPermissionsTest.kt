package com.permissionflow.helpers

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import app.cash.turbine.test
import com.permissionflow.core.PermissionFlow
import com.permissionflow.core.PermissionResult
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
class ModernPermissionsTest {

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
    fun `requestBluetoothScan returns Granted when permission is granted`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestBluetoothScan().test {
            val result = awaitItem()
            assertTrue(result is PermissionResult.Granted)
            awaitComplete()
        }
    }

    @Test
    fun `requestBluetoothConnect returns Granted when permission is granted`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestBluetoothConnect().test {
            val result = awaitItem()
            assertTrue(result is PermissionResult.Granted)
            awaitComplete()
        }
    }

    @Test
    fun `requestBluetoothAdvertise returns Granted when permission is granted`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADVERTISE)
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestBluetoothAdvertise().test {
            val result = awaitItem()
            assertTrue(result is PermissionResult.Granted)
            awaitComplete()
        }
    }

    @Test
    fun `requestBluetoothPermissions returns AllGranted when all requested permissions are granted`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, any())
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestBluetoothPermissions(
            requestScan = true,
            requestConnect = true,
            requestAdvertise = false
        ).test {
            val result = awaitItem()
            assertTrue(result is BluetoothPermissionResult.AllGranted)
            awaitComplete()
        }
    }

    @Test
    fun `requestBodySensors returns Granted when permission is granted`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.BODY_SENSORS)
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestBodySensors().test {
            val result = awaitItem()
            assertTrue(result is PermissionResult.Granted)
            awaitComplete()
        }
    }

    @Test
    fun `requestBodySensorsPermissions returns ForegroundGranted when not requesting background`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.BODY_SENSORS)
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestBodySensorsPermissions(requestBackground = false).test {
            val result = awaitItem()
            assertTrue(result is BodySensorsPermissionResult.ForegroundGranted)
            awaitComplete()
        }
    }

    @Test
    fun `requestActivityRecognition returns Granted when permission is granted`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION)
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestActivityRecognition().test {
            val result = awaitItem()
            assertTrue(result is PermissionResult.Granted)
            awaitComplete()
        }
    }

    @Test
    fun `requestNearbyWifiDevices returns Granted when permission is granted`() = runTest {
        // Given
        every {
            ContextCompat.checkSelfPermission(activity, "android.permission.NEARBY_WIFI_DEVICES")
        } returns PackageManager.PERMISSION_GRANTED

        // When/Then
        permissionFlow.requestNearbyWifiDevices().test {
            val result = awaitItem()
            assertTrue(result is PermissionResult.Granted)
            awaitComplete()
        }
    }
}
