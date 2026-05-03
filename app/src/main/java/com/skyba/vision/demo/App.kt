package com.skyba.vision.demo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build


/**
 * [Base Layer] Application class for initializing global app configurations.
 * Sets up Notification Channels required for Foreground Services on Android 8.0+.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Notification Channels are mandatory for Android O (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            /*
             * [UX] Audio attributes for notification sounds.
             * Used for sonification (feedback sounds) rather than long media playback.
             */
            val timerAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            /**
             * [UX] Persistent Timer Channel.
             * Set to IMPORTANCE_LOW to prevent intrusive sounds or pop-ups
             * while the timer is running in the background.
             */
            val timerChannel = NotificationChannel(
                "timer_channel",
                "Timer Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
                enableVibration(false)
                description = "Channel for persistent timer display in the notification shade"
            }

            /**
             * [UX] Phase Change Channel.
             * Set to IMPORTANCE_HIGH to ensure the user notices when a session ends
             * and it's time to switch between work and break.
             */
            val phaseChannel = NotificationChannel(
                "phase_change_channel",
                "Phase Change Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(false)
                vibrationPattern = null
                setSound(null, null)
                description = "Alerts for phase transitions: starting work or taking a break"
            }

            notificationManager.createNotificationChannel(timerChannel)
            notificationManager.createNotificationChannel(phaseChannel)
        }
    }
}
