package com.haruple97.todori.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.haruple97.todori.feature.customize.CustomizeScreen
import com.haruple97.todori.feature.record.RecordScreen
import com.haruple97.todori.feature.stats.StatsScreen
import com.haruple97.todori.feature.timer.TimerScreen
import kotlinx.serialization.Serializable

@Serializable
data object TimerRoute

@Serializable
data object RecordRoute

@Serializable
data object CustomizeRoute

@Serializable
data object StatsRoute

/**
 * 화면 전환은 "열어본다 / 닫는다"로 통일한다.
 * 들어갈 때는 새 화면이 아래에서 살짝 떠오르며 나타나고 이전 화면은 살짝 작아지며 물러난다.
 * 나갈 때는 현재 화면이 아래로 내려가며 사라지고 이전 화면이 원래 크기로 돌아온다.
 * 이 방향은 예측형 뒤로가기 제스처의 축소 효과와도 같은 결이라 제스처와 버튼이 어긋나 보이지 않는다.
 */
private object TodoriTransitions {
    private const val DURATION = 340
    private const val FADE_DURATION = 220
    private const val SETTLED_SCALE = 0.96f
    private const val SLIDE_FRACTION = 14
    private val ease = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    val enter: EnterTransition =
        slideInVertically(tween(DURATION, easing = ease)) { it / SLIDE_FRACTION } +
            fadeIn(tween(FADE_DURATION))

    val exit: ExitTransition =
        scaleOut(tween(DURATION, easing = ease), targetScale = SETTLED_SCALE) +
            fadeOut(tween(FADE_DURATION))

    val popEnter: EnterTransition =
        scaleIn(tween(DURATION, easing = ease), initialScale = SETTLED_SCALE) +
            fadeIn(tween(FADE_DURATION))

    val popExit: ExitTransition =
        slideOutVertically(tween(DURATION, easing = ease)) { it / SLIDE_FRACTION } +
            fadeOut(tween(FADE_DURATION))
}

@Composable
fun TodoriNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = TimerRoute,
        modifier = modifier,
        enterTransition = { TodoriTransitions.enter },
        exitTransition = { TodoriTransitions.exit },
        popEnterTransition = { TodoriTransitions.popEnter },
        popExitTransition = { TodoriTransitions.popExit }
    ) {
        composable<TimerRoute> {
            TimerScreen(
                onOpenRecord = { navController.navigate(RecordRoute) },
                onOpenCustomize = { navController.navigate(CustomizeRoute) }
            )
        }
        composable<StatsRoute> {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        composable<CustomizeRoute> {
            CustomizeScreen(onBack = { navController.popBackStack() })
        }
        composable<RecordRoute>(deepLinks = listOf(navDeepLink<RecordRoute>(basePath = "todori://record"))) {
            RecordScreen(
                onBack = { navController.popBackStack() },
                onOpenStats = { navController.navigate(StatsRoute) }
            )
        }
    }
}
