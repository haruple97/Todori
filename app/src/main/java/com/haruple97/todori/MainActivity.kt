package com.haruple97.todori

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.haruple97.todori.app.AppViewModel
import com.haruple97.todori.core.designsystem.theme.TodoriTheme
import com.haruple97.todori.core.designsystem.theme.isDarkFor
import com.haruple97.todori.navigation.TodoriNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appearance by appViewModel.appearance.collectAsStateWithLifecycle()
            TodoriTheme(theme = appearance.theme, skin = appearance.skin) {
                // 항상 어두운 테마에서는 시스템이 밝은 모드여도 상태바·내비게이션바 아이콘이 보이도록 맞춘다.
                val dark = isDarkFor(appearance.theme)
                DisposableEffect(dark) {
                    val style = if (dark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                    onDispose { }
                }
                TodoriNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
