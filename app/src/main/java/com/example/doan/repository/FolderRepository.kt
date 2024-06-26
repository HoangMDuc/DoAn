package com.example.doan.repository

import com.example.doan.database.AppDatabase
import com.example.doan.database.entity.FolderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FolderRepository(private val appDatabase: AppDatabase) {


    fun getAllRootFolder() : Flow<List<FolderEntity>> {

        return appDatabase.folderDao().getAllRootFolders()
    }

    suspend fun getAllChildFolder(id: String) : List<FolderEntity> {
        return withContext(Dispatchers.IO) {
            appDatabase.folderDao().getAllChildOf(id)
        }
    }
    suspend fun getById(id: String) : FolderEntity {
        return withContext(Dispatchers.IO) {
            appDatabase.folderDao().getById(id)
        }
    }

    suspend fun decreaseQuantity(folderID: String) {
        return withContext(Dispatchers.IO) {
            appDatabase.folderDao().decreaseQuantity(folderID)
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

    suspend fun increaseOne(id: String) {
        withContext(Dispatchers.IO) {
            appDatabase.folderDao().increaseQuantity(id)
        }
    }

    suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            appDatabase.folderDao().deleteFolder(id)
        }
    }
}