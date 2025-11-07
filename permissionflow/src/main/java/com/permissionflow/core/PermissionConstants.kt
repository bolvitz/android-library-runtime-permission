package com.permissionflow.core

import android.Manifest
import android.os.Build

/**
 * Convenient constants for common permission groups.
 * Makes it easier to request multiple related permissions.
 */
object Permissions {

    /**
     * Camera-related permissions.
     */
    object Camera {
        val CAMERA = Manifest.permission.CAMERA

        /**
         * Camera + Storage for older Android versions.
         */
        fun withStorage(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(CAMERA)
            } else {
                arrayOf(CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * Location-related permissions.
     */
    object Location {
        val FINE = Manifest.permission.ACCESS_FINE_LOCATION
        val COARSE = Manifest.permission.ACCESS_COARSE_LOCATION

        /**
         * Background location (Android 10+).
         */
        val BACKGROUND = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        } else {
            null
        }

        /**
         * All foreground location permissions.
         */
        fun foreground(): Array<String> = arrayOf(FINE, COARSE)

        /**
         * All location permissions including background.
         */
        fun all(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(FINE, COARSE, BACKGROUND!!)
            } else {
                arrayOf(FINE, COARSE)
            }
        }
    }

    /**
     * Media-related permissions (Android 13+).
     */
    object Media {
        /**
         * Read images permission (Android 13+).
         */
        val IMAGES = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            null
        }

        /**
         * Read videos permission (Android 13+).
         */
        val VIDEO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            null
        }

        /**
         * Read audio permission (Android 13+).
         */
        val AUDIO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            null
        }

        /**
         * Legacy storage permission for Android 12 and below.
         */
        val EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE

        /**
         * All media permissions for current Android version.
         */
        fun all(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(IMAGES!!, VIDEO!!, AUDIO!!)
            } else {
                arrayOf(EXTERNAL_STORAGE)
            }
        }

        /**
         * Visual media only (images and videos).
         */
        fun visual(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(IMAGES!!, VIDEO!!)
            } else {
                arrayOf(EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * Bluetooth-related permissions (Android 12+).
     */
    object Bluetooth {
        val SCAN = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            null
        }

        val CONNECT = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            null
        }

        val ADVERTISE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_ADVERTISE
        } else {
            null
        }

        /**
         * All Bluetooth permissions for current Android version.
         */
        fun all(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(SCAN!!, CONNECT!!, ADVERTISE!!)
            } else {
                emptyArray()
            }
        }

        /**
         * Common Bluetooth permissions (scan and connect).
         */
        fun common(): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(SCAN!!, CONNECT!!)
            } else {
                emptyArray()
            }
        }
    }

    /**
     * Contact-related permissions.
     */
    object Contacts {
        val READ = Manifest.permission.READ_CONTACTS
        val WRITE = Manifest.permission.WRITE_CONTACTS

        fun all(): Array<String> = arrayOf(READ, WRITE)
    }

    /**
     * Calendar-related permissions.
     */
    object Calendar {
        val READ = Manifest.permission.READ_CALENDAR
        val WRITE = Manifest.permission.WRITE_CALENDAR

        fun all(): Array<String> = arrayOf(READ, WRITE)
    }

    /**
     * SMS-related permissions.
     */
    object SMS {
        val SEND = Manifest.permission.SEND_SMS
        val RECEIVE = Manifest.permission.RECEIVE_SMS
        val READ = Manifest.permission.READ_SMS

        fun all(): Array<String> = arrayOf(SEND, RECEIVE, READ)
    }

    /**
     * Audio recording permission.
     */
    val MICROPHONE = Manifest.permission.RECORD_AUDIO

    /**
     * Notification permission (Android 13+).
     */
    val NOTIFICATIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }

    /**
     * Phone-related permission.
     */
    val PHONE = Manifest.permission.CALL_PHONE

    /**
     * Body sensors permission.
     */
    val BODY_SENSORS = Manifest.permission.BODY_SENSORS

    /**
     * Body sensors background permission (Android 13+).
     */
    val BODY_SENSORS_BACKGROUND = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        "android.permission.BODY_SENSORS_BACKGROUND"
    } else {
        null
    }

    /**
     * Activity recognition permission (Android 10+).
     */
    val ACTIVITY_RECOGNITION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Manifest.permission.ACTIVITY_RECOGNITION
    } else {
        null
    }

    /**
     * Nearby WiFi devices permission (Android 12+).
     */
    val NEARBY_WIFI_DEVICES = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        "android.permission.NEARBY_WIFI_DEVICES"
    } else {
        null
    }
}

/**
 * Extension function to get permission name from string.
 */
fun String.toPermissionName(): String {
    return this.substringAfterLast('.')
}

/**
 * Extension function to check if permission is dangerous.
 */
fun String.isDangerousPermission(): Boolean {
    val dangerousPermissions = setOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.ADD_VOICEMAIL,
        Manifest.permission.USE_SIP,
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_WAP_PUSH,
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    return dangerousPermissions.contains(this)
}
