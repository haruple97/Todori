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
        CoroutineScope(SupervisorJob() + dispatcher).launch {
            sessionRepository.sessions
                .map { sessions -> sessions.maxOfOrNull { it.completedAtMillis } }
                .distinctUntilChanged()
                .drop(1)
                .collect { WeekWidget().updateAll(this@TodoriApp) }
        }
    }
}
