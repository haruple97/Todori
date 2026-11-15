package com.haruple97.todori

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.haruple97.todori.core.common.DefaultDispatcher
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.widget.WeekWidget
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltAndroidApp
class TodoriApp : Application() {
    @Inject lateinit var sessionRepository: SessionRepository

    @Inject @field:DefaultDispatcher lateinit var dispatcher: CoroutineDispatcher

    override fun onCreate() {
        super.onCreate()
        // 새 완료 기록이 생기면 홈 화면 위젯을 갱신한다(알람으로 프로세스가 새로 떠도 동작).
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        scope.launch {
            sessionRepository.sessions
                // 추가뿐 아니라 삭제(·되돌리기)에도 갱신되도록 개수와 마지막 완료 시각을 함께 본다.
                .map { sessions -> sessions.size to sessions.maxOfOrNull { it.completedAtMillis } }
                .distinctUntilChanged()
                .drop(1)
                // updateAll은 위젯 세션이 닫힐 때까지 한참 대기하므로, 다음 변경이 밀리지 않게 각각 따로 실행한다.
                .collect { scope.launch { WeekWidget().updateAll(this@TodoriApp) } }
        }
    }
}
