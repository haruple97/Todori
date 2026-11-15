package com.haruple97.todori.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.haruple97.todori.feature.record.RecordScreen
import com.haruple97.todori.feature.timer.TimerScreen
import kotlinx.serialization.Serializable

@Serializable
data object TimerRoute

@Serializable
data object RecordRoute

@Composable
fun TodoriNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = TimerRoute, modifier = modifier) {
        composable<TimerRoute> {
            TimerScreen(onOpenRecord = { navController.navigate(RecordRoute) })
        }
        composable<RecordRoute>(deepLinks = listOf(navDeepLink<RecordRoute>(basePath = "todori://record"))) {
            RecordScreen(onBack = { navController.popBackStack() })
        }
    }
}
