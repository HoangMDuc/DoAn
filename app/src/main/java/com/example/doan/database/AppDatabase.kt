package com.example.doan.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.doan.database.dao.FolderDao
import com.example.doan.database.entity.FolderEntity


@Database(entities = [FolderEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao() : FolderDao

    companion object {
        private var INSTANCE : AppDatabase? = null


        fun getInstance(context: Context) : AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }

}