package com.permissionflow.core

import android.Manifest
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PermissionChainTest {

    private lateinit var permissionFlow: PermissionFlow
    private lateinit var permissionChain: PermissionChain

    @Before
    fun setup() {
        permissionFlow = mockk(relaxed = true)
        permissionChain = PermissionChain(permissionFlow)
    }

    @Test
    fun `chain then adds permission to chain`() {
        // When
        val result = permissionChain
            .then(Manifest.permission.CAMERA)
            .then(Manifest.permission.RECORD_AUDIO)

        // Then
        assertNotNull(result)
        assertTrue(result is PermissionChain)
    }

    @Test
    fun `chain execute returns AllGranted when all permissions granted`() = runTest {
        // Given
        coEvery {
            permissionFlow.requestPermission(Manifest.permission.CAMERA)
        } returns flowOf(PermissionResult.Granted)

        coEvery {
            permissionFlow.requestPermission(Manifest.permission.RECORD_AUDIO)
        } returns flowOf(PermissionResult.Granted)

        // When
        val chain = permissionChain
            .then(Manifest.permission.CAMERA)
            .then(Manifest.permission.RECORD_AUDIO)

        chain.execute().test {
            val result = awaitItem()

            // Then
            assertTrue(result is ChainResult.AllGranted)
            awaitComplete()
        }
    }

    @Test
    fun `chain stops at first denied permission`() = runTest {
        // Given
        coEvery {
            permissionFlow.requestPermission(Manifest.permission.CAMERA)
        } returns flowOf(PermissionResult.Denied(false))

        // When
        val chain = permissionChain
            .then(Manifest.permission.CAMERA)
            .then(Manifest.permission.RECORD_AUDIO)

        chain.execute().test {
            val result = awaitItem()

            // Then
            assertTrue(result is ChainResult.StoppedAtDenied)
            val stoppedResult = result as ChainResult.StoppedAtDenied
            assertEquals(Manifest.permission.CAMERA, stoppedResult.permission)
            assertEquals(0, stoppedResult.atStep)
            awaitComplete()
        }
    }

    @Test
    fun `chain stops at permanently denied permission`() = runTest {
        // Given
        coEvery {
            permissionFlow.requestPermission(Manifest.permission.CAMERA)
        } returns flowOf(PermissionResult.Granted)

        coEvery {
            permissionFlow.requestPermission(Manifest.permission.RECORD_AUDIO)
        } returns flowOf(PermissionResult.PermanentlyDenied)

        // When
        val chain = permissionChain
            .then(Manifest.permission.CAMERA)
            .then(Manifest.permission.RECORD_AUDIO)

        chain.execute().test {
            val result = awaitItem()

            // Then
            assertTrue(result is ChainResult.StoppedAtPermanentlyDenied)
            val stoppedResult = result as ChainResult.StoppedAtPermanentlyDenied
            assertEquals(Manifest.permission.RECORD_AUDIO, stoppedResult.permission)
            assertEquals(1, stoppedResult.atStep)
            assertEquals(1, stoppedResult.granted.size)
            awaitComplete()
        }
    }

    @Test
    fun `chain tracks granted permissions before stopping`() = runTest {
        // Given
        coEvery {
            permissionFlow.requestPermission(Manifest.permission.CAMERA)
        } returns flowOf(PermissionResult.Granted)

        coEvery {
            permissionFlow.requestPermission(Manifest.permission.RECORD_AUDIO)
        } returns flowOf(PermissionResult.Granted)

        coEvery {
            permissionFlow.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        } returns flowOf(PermissionResult.Denied(false))

        // When
        val chain = permissionChain
            .then(Manifest.permission.CAMERA)
            .then(Manifest.permission.RECORD_AUDIO)
            .then(Manifest.permission.ACCESS_FINE_LOCATION)

        chain.execute().test {
            val result = awaitItem()

            // Then
            assertTrue(result is ChainResult.StoppedAtDenied)
            val stoppedResult = result as ChainResult.StoppedAtDenied
            assertEquals(2, stoppedResult.granted.size)
            assertTrue(stoppedResult.granted.contains(Manifest.permission.CAMERA))
            assertTrue(stoppedResult.granted.contains(Manifest.permission.RECORD_AUDIO))
            awaitComplete()
        }
    }

    @Test
    fun `chain with callbacks invokes onGranted`() = runTest {
        // Given
        var cameraGranted = false
        var audioGranted = false

        coEvery {
            permissionFlow.requestPermission(any())
        } returns flowOf(PermissionResult.Granted)

        // When
        val chain = permissionChain
            .then(
                permission = Manifest.permission.CAMERA,
                onGranted = { cameraGranted = true }
            )
            .then(
                permission = Manifest.permission.RECORD_AUDIO,
                onGranted = { audioGranted = true }
            )

        chain.execute().test {
            awaitItem()
            awaitComplete()
        }

        // Then
        assertTrue(cameraGranted)
        assertTrue(audioGranted)
    }

    @Test
    fun `chain with callbacks invokes onDenied`() = runTest {
        // Given
        var cameraDenied = false

        coEvery {
            permissionFlow.requestPermission(Manifest.permission.CAMERA)
        } returns flowOf(PermissionResult.Denied(false))

        // When
        val chain = permissionChain
            .then(
                permission = Manifest.permission.CAMERA,
                onDenied = { cameraDenied = true }
            )

        chain.execute().test {
            awaitItem()
            awaitComplete()
        }

        // Then
        assertTrue(cameraDenied)
    }

    @Test
    fun `empty chain returns AllGranted`() = runTest {
        // When
        permissionChain.execute().test {
            val result = awaitItem()

            // Then
            assertTrue(result is ChainResult.AllGranted)
            awaitComplete()
        }
    }
}
