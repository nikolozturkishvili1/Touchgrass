package com.touchgrass.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.touchgrass.app.ui.TouchgrassNavGraph
import com.touchgrass.app.ui.theme.TouchgrassTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. All navigation between screens lives inside Compose
 * (`androidx.navigation:navigation-compose`) — see [TouchgrassNavGraph].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TouchgrassApp() }
    }
}

@Composable
private fun TouchgrassApp() {
    TouchgrassTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            TouchgrassNavGraph()
        }
    }
}
