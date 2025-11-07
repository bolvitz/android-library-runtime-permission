package com.permissionflow.core

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PermissionStateManagerTest {

    private lateinit var context: Context
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var stateManager: PermissionStateManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        sharedPrefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.clear() } returns editor
        every { editor.apply() } just Runs

        stateManager = PermissionStateManager(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `wasPermissionRequestedBefore returns false when not requested`() {
        // Given
        val permission = android.Manifest.permission.CAMERA
        every { sharedPrefs.getBoolean(any(), false) } returns false

        // When
        val result = stateManager.wasPermissionRequestedBefore(permission)

        // Then
        assertFalse(result)
    }

    @Test
    fun `wasPermissionRequestedBefore returns true when previously requested`() {
        // Given
        val permission = android.Manifest.permission.CAMERA
        every { sharedPrefs.getBoolean(any(), false) } returns true

        // When
        val result = stateManager.wasPermissionRequestedBefore(permission)

        // Then
        assertTrue(result)
    }

    @Test
    fun `markPermissionRequested saves permission to preferences`() {
        // Given
        val permission = android.Manifest.permission.CAMERA

        // When
        stateManager.markPermissionRequested(permission)

        // Then
        verify { editor.putBoolean(any(), true) }
        verify { editor.apply() }
    }

    @Test
    fun `clearPermissionHistory removes permission from preferences`() {
        // Given
        val permission = android.Manifest.permission.CAMERA

        // When
        stateManager.clearPermissionHistory(permission)

        // Then
        verify { editor.remove(any()) }
        verify { editor.apply() }
    }

    @Test
    fun `clearAllHistory clears all preferences`() {
        // When
        stateManager.clearAllHistory()

        // Then
        verify { editor.clear() }
        verify { editor.apply() }
    }

    @Test
    fun `multiple permissions can be tracked independently`() {
        // Given
        val camera = android.Manifest.permission.CAMERA
        val audio = android.Manifest.permission.RECORD_AUDIO

        every { sharedPrefs.getBoolean(any(), false) } answers {
            val key = firstArg<String>()
            key.contains(camera.substringAfterLast('.'))
        }

        // When & Then
        assertTrue(stateManager.wasPermissionRequestedBefore(camera))
        assertFalse(stateManager.wasPermissionRequestedBefore(audio))
    }

    @Test
    fun `marking permission as requested can be verified`() {
        // Given
        val permission = android.Manifest.permission.CAMERA

        // When
        stateManager.markPermissionRequested(permission)

        // Then
        verify { editor.putBoolean(any(), true) }
        verify { editor.apply() }
    }
}
