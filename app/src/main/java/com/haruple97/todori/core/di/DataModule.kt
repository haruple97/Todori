package com.haruple97.todori.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.haruple97.todori.core.data.database.SessionDao
import com.haruple97.todori.core.data.database.TodoriDatabase
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.data.repository.SessionRepositoryImpl
import com.haruple97.todori.core.data.repository.SettingsRepository
import com.haruple97.todori.core.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {
    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TodoriDatabase =
        Room.databaseBuilder(context, TodoriDatabase::class.java, "todori.db").build()

    @Provides
    fun provideSessionDao(database: TodoriDatabase): SessionDao = database.sessionDao()
}
