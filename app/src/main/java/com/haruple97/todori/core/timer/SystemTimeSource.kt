package com.haruple97.todori.core.timer

import android.os.SystemClock
import javax.inject.Inject

class SystemTimeSource @Inject constructor() : TimeSource {
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
    override fun wallClockMillis(): Long = System.currentTimeMillis()
}
