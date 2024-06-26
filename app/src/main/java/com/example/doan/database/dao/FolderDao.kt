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
    fun getAllRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: String): FolderEntity

    @Query("SELECT * FROM folders WHERE parent_id = :id")
    suspend fun getAllChildOf(id: String): List<FolderEntity>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folderEntity: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolders(folders: List<FolderEntity>)
    @Query("Update folders set file_quantity = :quantity where id = :id")
    suspend fun update(quantity: Int, id: String)
    @Query("Update folders set file_quantity = file_quantity + 1 where id = :folderId")
    suspend fun increaseQuantity(folderId: String)
    @Query("Update folders set file_quantity = file_quantity - 1 where id = :folderId")
    suspend fun decreaseQuantity(folderId: String)

    @Query("DELETE FROM folders WHERE id = :id AND parent_id is NOT NULL")
    suspend fun deleteFolder(id: String)

}