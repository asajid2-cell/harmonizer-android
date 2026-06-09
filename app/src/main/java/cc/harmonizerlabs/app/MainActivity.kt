package cc.harmonizerlabs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cc.harmonizerlabs.app.navigation.HarmonizerNavGraph
import cc.harmonizerlabs.app.ui.theme.Black
import cc.harmonizerlabs.app.ui.theme.HarmonizerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
