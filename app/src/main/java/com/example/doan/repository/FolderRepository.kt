package com.example.doan.repository

import com.example.doan.database.AppDatabase
import com.example.doan.database.entity.FolderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FolderRepository(private val appDatabase: AppDatabase) {


    fun getAllFolder() : Flow<List<FolderEntity>> {

        return appDatabase.folderDao().getAllFolders()
    }

    suspend fun getById(id: String) : FolderEntity {
        return withContext(Dispatchers.IO) {
            appDatabase.folderDao().getById(id)
        }
    }


    suspend fun insert(folderEntity: FolderEntity) {
        return withContext(Dispatchers.IO) {
            appDatabase.folderDao().insertFolder(folderEntity)
        }
    }

    suspend fun insertAll(folderEntities: List<FolderEntity>) {
        return withContext(Dispatchers.IO) {
            appDatabase.folderDao().insertFolders(folderEntities)
        }
    }

    suspend fun update(quantity: Int, id : String) {
        return withContext(Dispatchers.IO) {
            appDatabase.folderDao().update(quantity, id)
        }
    }
}