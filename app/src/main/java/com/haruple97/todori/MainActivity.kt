package com.haruple97.todori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.haruple97.todori.core.designsystem.theme.TodoriTheme
import com.haruple97.todori.navigation.TodoriNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoriTheme {
                TodoriNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
