package com.example.doan.database

import android.graphics.Bitmap
import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

open class FileProjections(
    open val id: Long,
    open val name: String,
    open val path: String,
    open val bucketId: Long,
    open val size: Long,
    open val uri: Uri,
    open val date: String,
    open val type: String,
    open val bucketName: String
    ) {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<FileProjections>() {
            override fun areItemsTheSame(oldItem: FileProjections, newItem: FileProjections) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: FileProjections, newItem: FileProjections) =
                oldItem.uri == newItem.uri
        }
    }

}


data class Bucket(
    val bucketId: Long,
    val bucketName: String,
    var numberOfFiles: Int,
    var thumbnail: Uri
) {

}

data class ImageProjection(
    override val id: Long,
    override val name: String,
    override val path: String,
    override val bucketId: Long,
    override val size: Long,
    override val uri: Uri,
    override val date: String,
    override val type: String,
    val thumbnail: Bitmap,
    override val bucketName: String


) : FileProjections(id, name, path, bucketId, size, uri, date, type, bucketName)

data class VideoProjection(
    override val id: Long,
    override val name: String,
    override val path: String,
    override val bucketId: Long,
    override val size: Long,
    override val uri: Uri,
    override val date: String,
    override val type: String,
    val thumbnail: Bitmap,
    override val bucketName: String,
) : FileProjections(id, name, path, bucketId, size, uri, date, type, bucketName)

data class AudioProjection(
    override val id: Long,
    override val name: String,
    override val path: String,
    override val bucketId: Long,
    override val size: Long,
    override val uri: Uri,
    override val date: String,
    override val type: String,
    override val bucketName: String,
) : FileProjections(id, name, path, bucketId, size, uri, date, type, bucketName)

data class DocumentProjection(
    override val id: Long,
    override val name: String,
    override val path: String,
    override val bucketId: Long,
    override val size: Long,
    override val uri: Uri,
    override val date: String,
    override val type: String,
    val mimeType: String,
    override val bucketName: String,
) : FileProjections(id, name, path, bucketId, size, uri, date, type, bucketName)
