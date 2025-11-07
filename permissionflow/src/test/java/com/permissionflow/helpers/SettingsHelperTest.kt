package com.permissionflow.helpers

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class SettingsHelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        every { context.packageName } returns "com.test.app"
    }

    @Test
    fun `createAppSettingsIntent creates correct intent`() {
        // When
        val intent = SettingsHelper.createAppSettingsIntent(context)

        // Then
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertNotNull(intent.data)
        assertTrue(intent.data.toString().contains("com.test.app"))
    }

    @Test
    fun `createNotificationSettingsIntent creates correct intent for API 26+`() {
        // When
        val intent = SettingsHelper.createNotificationSettingsIntent(context)

        // Then
        assertNotNull(intent)
        // Intent action should be either notification channel settings or app notification settings
        assertTrue(
            intent.action == Settings.ACTION_APP_NOTIFICATION_SETTINGS ||
            intent.action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        )
    }

    @Test
    fun `createLocationSettingsIntent creates correct intent`() {
        // When
        val intent = SettingsHelper.createLocationSettingsIntent()

        // Then
        assertEquals(Settings.ACTION_LOCATION_SOURCE_SETTINGS, intent.action)
    }

    @Test
    fun `openAppSettings starts activity with correct intent`() {
        // Given
        val intentSlot = mutableListOf<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit

        // When
        SettingsHelper.openAppSettings(context)

        // Then
        verify { context.startActivity(any()) }
        assertTrue(intentSlot.isNotEmpty())
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intentSlot[0].action)
    }

    @Test
    fun `openNotificationSettings starts activity`() {
        // Given
        val intentSlot = mutableListOf<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit

        // When
        SettingsHelper.openNotificationSettings(context)

        // Then
        verify { context.startActivity(any()) }
        assertTrue(intentSlot.isNotEmpty())
    }

    @Test
    fun `openLocationSettings starts activity with correct intent`() {
        // Given
        val intentSlot = mutableListOf<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit

        // When
        SettingsHelper.openLocationSettings(context)

        // Then
        verify { context.startActivity(any()) }
        assertTrue(intentSlot.isNotEmpty())
        assertEquals(Settings.ACTION_LOCATION_SOURCE_SETTINGS, intentSlot[0].action)
    }

    @Test
    fun `Context extension openAppSettings calls SettingsHelper`() {
        // Given
        val intentSlot = mutableListOf<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit

        // When
        context.openAppSettings()

        // Then
        verify { context.startActivity(any()) }
        assertTrue(intentSlot.isNotEmpty())
    }

    @Test
    fun `Context extension openNotificationSettings calls SettingsHelper`() {
        // Given
        val intentSlot = mutableListOf<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit

        // When
        context.openNotificationSettings()

        // Then
        verify { context.startActivity(any()) }
        assertTrue(intentSlot.isNotEmpty())
    }

    @Test
    fun `Context extension openLocationSettings calls SettingsHelper`() {
        // Given
        val intentSlot = mutableListOf<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit

        // When
        context.openLocationSettings()

        // Then
        verify { context.startActivity(any()) }
        assertTrue(intentSlot.isNotEmpty())
    }

    @Test
    fun `PermissionResult PermanentlyDenied openSettings extension works`() {
        // Given
        val result = com.permissionflow.core.PermissionResult.PermanentlyDenied
        val intentSlot = mutableListOf<Intent>()
        every { context.startActivity(capture(intentSlot)) } returns Unit

        // When
        result.openSettings(context)

        // Then
        verify { context.startActivity(any()) }
        assertTrue(intentSlot.isNotEmpty())
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intentSlot[0].action)
    }
}
