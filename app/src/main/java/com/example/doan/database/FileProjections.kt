package com.example.doan.database

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

open class FileProjections(
    open val id: Long,
    open val name: String,
    open val path: String,
    open val bucket: String,
    open val size: Int,
    open val uri: Uri,
    open val date: String,
    open val type: String
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
    val bucketName: String,
    var numberOfFiles: Int,
    var thumbnail: Uri
) {

}

data class ImageProjection(
    override val id: Long,
    override val name: String,
    override val path: String,
    override val bucket: String,
    override val size: Int,
    override val uri: Uri,
    override val date: String,
    override val type: String
) : FileProjections(id, name, path, bucket,size, uri,date, type) {
}

data class VideoProjection(
    override val id: Long,
    override val name: String,
    override val path: String,
    override val bucket: String,
    override val size: Int,
    override val uri: Uri,
    override val date: String,
    override val type: String
) : FileProjections(id, name, path, bucket,size, uri, date, type) {

}

data class AudioProjection(
    override val id: Long,
    override val name: String,
    override val path: String,
    override val bucket: String,

    override val size: Int,
    override val uri: Uri,
    override val date: String,
    override val type: String
) : FileProjections(id, name, path, bucket,size,uri, date, type) {

}