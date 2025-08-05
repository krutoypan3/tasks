package ru.kokoroyume.tasks.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.kokoroyume.tasks.database.TasksDao
import ru.kokoroyume.tasks.database.TasksDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTasksDatabase(@ApplicationContext context: Context): TasksDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TasksDatabase::class.java,
            "goal_tree_database"
        ).build()
    }

    @Provides
    fun provideTasksDao(database: TasksDatabase): TasksDao {
        return database.tasksDao()
    }
}