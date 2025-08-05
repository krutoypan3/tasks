package ru.kokoroyume.tasks.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import ru.kokoroyume.tasks.model.TreeNode

@Database(
    entities = [TreeNode::class],
    version = 1,
    exportSchema = false
)
abstract class TasksDatabase : RoomDatabase() {
    abstract fun tasksDao(): TasksDao

    companion object {
        @Volatile
        private var INSTANCE: TasksDatabase? = null

        fun getDatabase(context: Context): TasksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TasksDatabase::class.java,
                    "goal_tree_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}