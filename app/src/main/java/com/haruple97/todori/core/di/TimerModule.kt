package com.haruple97.todori.core.di

import com.haruple97.todori.core.data.TimerStateStore
import com.haruple97.todori.core.timer.AndroidTimerPlatform
import com.haruple97.todori.core.timer.SystemTimeSource
import com.haruple97.todori.core.timer.TimeSource
import com.haruple97.todori.core.timer.TimerPersistence
import com.haruple97.todori.core.timer.TimerPlatform
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TimerModule {
    @Binds
    abstract fun bindTimeSource(impl: SystemTimeSource): TimeSource

    @Binds
    abstract fun bindTimerPlatform(impl: AndroidTimerPlatform): TimerPlatform

    @Binds
    abstract fun bindTimerPersistence(impl: TimerStateStore): TimerPersistence
}
