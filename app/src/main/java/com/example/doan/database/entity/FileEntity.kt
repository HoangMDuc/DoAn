package com.example.doan.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "files",
    foreignKeys = [ForeignKey(
        entity = FolderEntity::class,
        parentColumns = ["id"],
        childColumns = ["parent_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
data class FileEntity(

    @PrimaryKey val id: Long,
    val name: String,
    val size: Long,
    @ColumnInfo(name = "origin_path") val originPath: String,
    val type: String,
    @ColumnInfo(name = "parent_id", index = true) val parentID: String
) {
}