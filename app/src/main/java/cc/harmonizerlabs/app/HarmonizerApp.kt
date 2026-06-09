package cc.harmonizerlabs.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HarmonizerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_PLAYBACK,
                    getString(R.string.notification_channel_playback),
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Music playback controls" }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_RENDER,
                    getString(R.string.notification_channel_render),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Background render progress" }
            )
        }
    }

    companion object {
        const val CHANNEL_PLAYBACK = "harmonizer_playback"
        const val CHANNEL_RENDER = "harmonizer_render"
    }
}
