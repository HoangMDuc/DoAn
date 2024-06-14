package com.example.doan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.doan.database.entity.FolderEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface FolderDao {


    @Query("SELECT * FROM folders where parent_id is NULL")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: String): FolderEntity


    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFolder(folderEntity: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFolders(folders: List<FolderEntity>)
    @Query("Update folders set file_quantity = :quantity where id = :id")
    suspend fun update(quantity: Int, id: String)
}