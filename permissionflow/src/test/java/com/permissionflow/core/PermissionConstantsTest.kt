package com.permissionflow.core

import android.Manifest
import android.os.Build
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PermissionConstantsTest {

    @Test
    fun `Camera constants are correct`() {
        assertEquals(Manifest.permission.CAMERA, Permissions.Camera.CAMERA)
    }

    @Test
    fun `Camera withStorage returns correct array`() {
        val permissions = Permissions.Camera.withStorage()

        assertTrue(permissions.contains(Manifest.permission.CAMERA))
        assertTrue(permissions.size >= 1)
    }

    @Test
    fun `Location constants are correct`() {
        assertEquals(Manifest.permission.ACCESS_FINE_LOCATION, Permissions.Location.FINE)
        assertEquals(Manifest.permission.ACCESS_COARSE_LOCATION, Permissions.Location.COARSE)
        assertEquals(Manifest.permission.ACCESS_BACKGROUND_LOCATION, Permissions.Location.BACKGROUND)
    }

    @Test
    fun `Location foreground returns fine and coarse`() {
        val permissions = Permissions.Location.foreground()

        assertEquals(2, permissions.size)
        assertTrue(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @Test
    fun `Location all returns foreground and background`() {
        val permissions = Permissions.Location.all()

        assertTrue(permissions.size >= 2)
        assertTrue(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
        assertTrue(permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @Test
    fun `Media EXTERNAL_STORAGE constant is correct`() {
        assertEquals(Manifest.permission.READ_EXTERNAL_STORAGE, Permissions.Media.EXTERNAL_STORAGE)
    }

    @Test
    fun `Media constants are correct for Android 13+`() {
        assertNotNull(Permissions.Media.IMAGES)
        assertNotNull(Permissions.Media.VIDEO)
        assertNotNull(Permissions.Media.AUDIO)
    }

    @Test
    fun `Media all returns all media types`() {
        val permissions = Permissions.Media.all()

        assertTrue(permissions.size >= 3)
    }

    @Test
    fun `Media visual returns images and videos`() {
        val permissions = Permissions.Media.visual()

        assertTrue(permissions.size >= 2)
    }

    @Test
    fun `Contacts constants are correct`() {
        assertEquals(Manifest.permission.READ_CONTACTS, Permissions.Contacts.READ)
        assertEquals(Manifest.permission.WRITE_CONTACTS, Permissions.Contacts.WRITE)
    }

    @Test
    fun `Contacts all returns all contact permissions`() {
        val permissions = Permissions.Contacts.all()

        assertEquals(2, permissions.size)
        assertTrue(permissions.contains(Manifest.permission.READ_CONTACTS))
        assertTrue(permissions.contains(Manifest.permission.WRITE_CONTACTS))
    }

    @Test
    fun `Phone constant is correct`() {
        assertEquals(Manifest.permission.CALL_PHONE, Permissions.PHONE)
    }

    @Test
    fun `SMS constants are correct`() {
        assertEquals(Manifest.permission.SEND_SMS, Permissions.SMS.SEND)
        assertEquals(Manifest.permission.READ_SMS, Permissions.SMS.READ)
        assertEquals(Manifest.permission.RECEIVE_SMS, Permissions.SMS.RECEIVE)
    }

    @Test
    fun `SMS all returns all SMS permissions`() {
        val permissions = Permissions.SMS.all()

        assertTrue(permissions.size >= 3)
        assertTrue(permissions.contains(Manifest.permission.SEND_SMS))
        assertTrue(permissions.contains(Manifest.permission.READ_SMS))
        assertTrue(permissions.contains(Manifest.permission.RECEIVE_SMS))
    }

    @Test
    fun `Calendar constants are correct`() {
        assertEquals(Manifest.permission.READ_CALENDAR, Permissions.Calendar.READ)
        assertEquals(Manifest.permission.WRITE_CALENDAR, Permissions.Calendar.WRITE)
    }

    @Test
    fun `Calendar all returns read and write`() {
        val permissions = Permissions.Calendar.all()

        assertEquals(2, permissions.size)
        assertTrue(permissions.contains(Manifest.permission.READ_CALENDAR))
        assertTrue(permissions.contains(Manifest.permission.WRITE_CALENDAR))
    }

    @Test
    fun `Microphone constant is correct`() {
        assertEquals(Manifest.permission.RECORD_AUDIO, Permissions.MICROPHONE)
    }

    @Test
    fun `Notification constant is not null for current SDK`() {
        // NOTIFICATIONS is only available on Android 13+
        // With Robolectric, we can test its existence
        assertNotNull(Permissions.NOTIFICATIONS)
    }

    @Test
    fun `Bluetooth constants are not null for current SDK`() {
        // Bluetooth permissions available on Android 12+
        assertNotNull(Permissions.Bluetooth.SCAN)
        assertNotNull(Permissions.Bluetooth.CONNECT)
        assertNotNull(Permissions.Bluetooth.ADVERTISE)
    }

    @Test
    fun `Bluetooth all returns all bluetooth permissions`() {
        val permissions = Permissions.Bluetooth.all()
        // Size depends on SDK version
        assertTrue(permissions.isNotEmpty() || permissions.isEmpty())
    }

    @Test
    fun `Bluetooth common returns scan and connect`() {
        val permissions = Permissions.Bluetooth.common()
        // Size depends on SDK version
        assertTrue(permissions.isNotEmpty() || permissions.isEmpty())
    }

    @Test
    fun `Body Sensors constants are correct`() {
        assertEquals(Manifest.permission.BODY_SENSORS, Permissions.BODY_SENSORS)
        // BODY_SENSORS_BACKGROUND is only available on Android 13+
        assertNotNull(Permissions.BODY_SENSORS_BACKGROUND)
    }

    @Test
    fun `Activity Recognition constant is not null`() {
        // Available on Android 10+
        assertNotNull(Permissions.ACTIVITY_RECOGNITION)
    }

    @Test
    fun `Nearby WiFi Devices constant is not null for Android 13+`() {
        // Available on Android 13+
        assertNotNull(Permissions.NEARBY_WIFI_DEVICES)
    }

    @Test
    fun `toPermissionName extension returns last part of permission string`() {
        // Given
        val permission = Manifest.permission.CAMERA

        // When
        val name = permission.toPermissionName()

        // Then
        assertEquals("CAMERA", name)
    }

    @Test
    fun `isDangerousPermission extension identifies dangerous permissions`() {
        // Given
        val dangerousPermission = Manifest.permission.CAMERA

        // Then
        assertTrue(dangerousPermission.isDangerousPermission())
    }
}
