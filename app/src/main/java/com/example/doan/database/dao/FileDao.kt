package com.example.doan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.doan.database.entity.FileEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface FileDao {


    @Query("SELECT * FROM files where type = :type and parent_id = :folderId")
    fun getAllOfType(type: String, folderId: String) : Flow<List<FileEntity>>

    @Insert
    suspend fun insertFile(fileEntity: FileEntity) : Long

    @Query("SELECT * FROM files where parent_id = :id")
    suspend fun getFileOf(id: String) : List<FileEntity>

    @Query("DELETE FROM files where id = :id")
    suspend fun deleteLockedFile(id: String)
}