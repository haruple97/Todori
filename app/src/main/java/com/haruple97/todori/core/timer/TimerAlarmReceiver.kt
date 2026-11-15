package com.haruple97.todori.core.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.haruple97.todori.core.common.DefaultDispatcher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var engine: TimerEngine

    @Inject @field:DefaultDispatcher lateinit var dispatcher: CoroutineDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + dispatcher).launch {
            try {
                engine.restore()
                engine.onAlarm()
            } finally {
                pending.finish()
            }
        }
    }
}
