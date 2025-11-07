package com.permissionflow.core

import android.os.Build
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PermissionLoggerTest {

    @Before
    fun setup() {
        // Reset logger state before each test
        PermissionLogger.isEnabled = false
        PermissionLogger.logHandler = null
    }

    @After
    fun tearDown() {
        // Clean up after each test
        PermissionLogger.isEnabled = false
        PermissionLogger.logHandler = null
    }

    @Test
    fun `logger is disabled by default`() {
        // Then
        assertFalse(PermissionLogger.isEnabled)
    }

    @Test
    fun `enableDebugLogging enables logger`() {
        // When
        PermissionLogger.enableDebugLogging()

        // Then
        assertTrue(PermissionLogger.isEnabled)
    }

    @Test
    fun `custom log handler can be set`() {
        // Given
        var loggedMessage: String? = null
        val handler: (PermissionLogger.LogLevel, String, String, Throwable?) -> Unit = { _, _, msg, _ ->
            loggedMessage = msg
        }

        // When
        PermissionLogger.setLogHandler(handler)
        PermissionLogger.isEnabled = true

        // Note: Actual logging would happen in the implementation
        // This just tests that the handler can be set
        assertNotNull(PermissionLogger.logHandler)
    }

    @Test
    fun `log levels are defined correctly`() {
        // When
        val debug = PermissionLogger.LogLevel.DEBUG
        val info = PermissionLogger.LogLevel.INFO
        val warning = PermissionLogger.LogLevel.WARNING
        val error = PermissionLogger.LogLevel.ERROR

        // Then
        assertNotNull(debug)
        assertNotNull(info)
        assertNotNull(warning)
        assertNotNull(error)
    }

    @Test
    fun `logger state can be toggled`() {
        // Given
        assertFalse(PermissionLogger.isEnabled)

        // When
        PermissionLogger.isEnabled = true

        // Then
        assertTrue(PermissionLogger.isEnabled)

        // When
        PermissionLogger.isEnabled = false

        // Then
        assertFalse(PermissionLogger.isEnabled)
    }
}
