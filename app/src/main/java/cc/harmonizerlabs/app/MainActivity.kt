package cc.harmonizerlabs.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cc.harmonizerlabs.app.navigation.HarmonizerNavGraph
import cc.harmonizerlabs.app.ui.theme.Black
import cc.harmonizerlabs.app.ui.theme.HarmonizerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — notification is optional, audio plays either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 13+ requires explicit runtime grant for POST_NOTIFICATIONS.
        // Without it the media playback notification (and therefore lock-screen
        // controls) silently don't appear, even though the service is running.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            HarmonizerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = Black,
                ) {
                    HarmonizerNavGraph()
                }
            }
        }
    }
}
