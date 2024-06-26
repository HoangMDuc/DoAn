package com.example.doan.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import com.example.doan.database.AudioProjection
import com.example.doan.database.Bucket
import com.example.doan.database.DocumentProjection
import com.example.doan.database.ImageProjection
import com.example.doan.database.VideoProjection
import com.example.doan.database.dao.FileDao
import com.example.doan.database.dao.FolderDao
import com.example.doan.database.entity.FileEntity
import com.example.doan.utils.formatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileRepository(
    val context: Context,
    private val fileDao: FileDao,
    val folderDao: FolderDao
    ) {

    private var ver: String = "1.0"


    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance: FileRepository? = null

        fun getInstance(context: Context, fileDao: FileDao, folderDao: FolderDao): FileRepository {
            return instance ?: FileRepository(context, fileDao, folderDao)
        }


    }

    suspend fun getAllImagesBucketsName(): Map<Long, Bucket> {
        return withContext(Dispatchers.IO) {

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            Log.d("FileRepository", formatter.format(Date()))
            val imageList = mutableMapOf<Long, Bucket>()
            val imgCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID
            )

            val cur2 = context.contentResolver.query(imgCollection, projection, null, null, null)

            if (cur2 != null) {
                val idColumn = cur2.getColumnIndex(MediaStore.Images.Media._ID)
                val bucketColumn2 = cur2.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                val bucketColumn = cur2.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                while (cur2.moveToNext()) {
                    val id = cur2.getLong(idColumn)
                    val bucketName = cur2.getString(bucketColumn)
                    val bucketId = cur2.getLong(bucketColumn2)
                    if (imageList.contains(bucketId)) {
                        imageList[bucketId]?.numberOfFiles =
                            imageList[bucketId]?.numberOfFiles!! + 1
                    } else {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        val bucket = Bucket(bucketId, bucketName, 1, contentUri)
                        imageList[bucketId] = bucket
                    }

                    //Log.d("FileRepository", "id: $id name: $bucketName bucket: $bucketId")
                }
                cur2.close()
            }

            Log.d("FileRepository", formatter.format(Date()))
            imageList

        }
    }

    suspend fun getAllVideosBucketsName(): Map<Long, Bucket> {
        //scanToUpdate()
//        val mediaVersion = MediaStore.getVersion(context)
//        if(mediaVersion != ver) {
//
//        }
        return withContext(Dispatchers.IO) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            Log.d("FileRepository Video", formatter.format(Date()))
            val videoList = mutableMapOf<Long, Bucket>()
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_ID
            )

            val cur = context.contentResolver.query(collection, projection, null, null, null)
            Log.d("OK", "OL1")
            if (cur != null) {
                val idColumn = cur.getColumnIndex(MediaStore.Video.Media._ID)
                val bucketIdColumn = cur.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
                val bucketColumn = cur.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                Log.d("OK", "OK")
                while (cur.moveToNext()) {
                    Log.d("OK", "OL")
                    val id = cur.getLong(idColumn)
                    val bucketName = cur.getString(bucketColumn)
                    val bucketId = cur.getLong(bucketIdColumn)
                    if (videoList.contains(bucketId)) {
                        videoList[bucketId]?.numberOfFiles =
                            videoList[bucketId]?.numberOfFiles!! + 1
                    } else {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        val bucket = Bucket(bucketId, bucketName, 1, contentUri)
                        videoList[bucketId] = bucket
                    }
                    Log.d("Video repo", bucketName)
                    //Log.d("FileRepository", "id: $id name: $name bucket: $bucket contentUri: $contentUri mimeType: $mimeType")
                }
                cur.close()
            }

            Log.d("FileRepository Video", formatter.format(Date()))
            videoList
        }

    }

    suspend fun getAllAudiosBucketsName(): Map<Long, Bucket> {
        //scanToUpdate()
//        val mediaVersion = MediaStore.getVersion(context)
//        if(mediaVersion != ver) {
//
//        }
        return withContext(Dispatchers.IO) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            Log.d("FileRepository Video", formatter.format(Date()))
            val audioList = mutableMapOf<Long, Bucket>()
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Audio.Media.BUCKET_ID
                )

            val cur = context.contentResolver.query(collection, projection, null, null, null)
            Log.d("OK", "OL1")
            if (cur != null) {
                val idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID)
                val bucketIdColumn = cur.getColumnIndex(MediaStore.Audio.Media.BUCKET_ID)
                val bucketColumn = cur.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

                Log.d("OK", "OK")
                while (cur.moveToNext()) {
                    Log.d("OK", "OL")
                    val id = cur.getLong(idColumn)
                    val bucketName = cur.getString(bucketColumn)
                    val bucketId = cur.getLong(bucketIdColumn)
                    if (audioList.contains(bucketId)) {
                        audioList[bucketId]?.numberOfFiles =
                            audioList[bucketId]?.numberOfFiles!! + 1
                    } else {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )


                        val bucket = Bucket( bucketId, bucketName, 1, contentUri)
                        audioList[bucketId] = bucket
                    }
                    Log.d("Video repo", bucketName)
                    //Log.d("FileRepository", "id: $id name: $name bucket: $bucket contentUri: $contentUri mimeType: $mimeType")
                }
                cur.close()
            }

            Log.d("FileRepository Video", formatter.format(Date()))
            audioList
        }

    }
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun getAllDocumentsBucketsName(): Map<Long, Bucket> {
        //scanToUpdate()
//        val mediaVersion = MediaStore.getVersion(context)
//        if(mediaVersion != ver) {
//
//        }
        return withContext(Dispatchers.IO) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            Log.d("FileRepository Doc", formatter.format(Date()))
            val documentsList = mutableMapOf<Long, Bucket>()
            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val fileExtensions = arrayOf("application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            val selection = fileExtensions.joinToString(" OR ") {
               "${MediaStore.Downloads.MIME_TYPE} = ?"
            }

            val projection = arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.BUCKET_DISPLAY_NAME,
                MediaStore.Downloads.BUCKET_ID,
                MediaStore.Downloads.MIME_TYPE
                )

            val cur = context.contentResolver.query(collection, projection, selection,
                fileExtensions, null)
            Log.d("OK", "OL1")
            if (cur != null) {
                val idColumn = cur.getColumnIndex(MediaStore.Downloads._ID)
                val nameColumn = cur.getColumnIndex(MediaStore.Downloads.DISPLAY_NAME)
                val bucketColumn = cur.getColumnIndex(MediaStore.Downloads.BUCKET_DISPLAY_NAME)
                val mimeTypeColumn = cur.getColumnIndex(MediaStore.Downloads.MIME_TYPE)
                val bucketIdColumn = cur.getColumnIndex(MediaStore.Downloads.BUCKET_ID)
                //Log.d("OK", "OK")
                while (cur.moveToNext()) {
                    //Log.d("OK", "OL")
                    val id = cur.getLong(idColumn)
                    val bucketName = cur.getString(bucketColumn)
                    val name = cur.getString(nameColumn)
                    val mimeType = cur.getString(mimeTypeColumn)
                    val bucketId = cur.getLong(bucketIdColumn)
                    if (documentsList.contains(bucketId)) {
                        documentsList[bucketId]?.numberOfFiles =
                            documentsList[bucketId]?.numberOfFiles!! + 1
                    } else {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            id
                        )
                        val bucket = Bucket(bucketId, bucketName, 1, contentUri)
                        documentsList[bucketId] = bucket
                    }
                    Log.d("Doc repo", "$name $mimeType")
                    //Log.d("FileRepository", "id: $id name: $name bucket: $bucket contentUri: $contentUri mimeType: $mimeType")
                }
                cur.close()
            }

            Log.d("FileRepository Doc", formatter.format(Date()))
            documentsList
        }

    }
    private fun scanToUpdate() {
        val file =
            java.io.File(Environment.getExternalStorageDirectory().absolutePath + "/Pictures/test")
        MediaScannerConnection.scanFile(
            context, arrayOf<String>(file.toString()), null
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "-> uri=$uri")
        }
    }

    suspend fun getImagesOfBucket(bucketID: Long): List<ImageProjection> {

        return withContext(Dispatchers.IO) {
            val images = mutableListOf<ImageProjection>()
            val imgCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.MIME_TYPE
            )

            val selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
            val selectionArgs = arrayOf(bucketID.toString())
            val query =
                context.contentResolver.query(
                    imgCollection,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val displayNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val dataNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(displayNameColumn)
                    val data = cursor.getString(dataNameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val date = cursor.getLong(dateColumn) * 1000
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val bucketName = cursor.getString(bucketColumn)
                    Log.d(
                        "From file repo",
                        "id: $id name: $name size: $size ${formatter.format(date)} $mimeType"
                    )
                    Log.d("From file repo", contentUri.toString())
                    val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        context.contentResolver.loadThumbnail(contentUri, Size(100, 100), null)
                    } else {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, contentUri)
                    }
                    images.add(
                        ImageProjection(
                            id,
                            name,
                            data,
                            bucketID,
                            size,
                            contentUri,
                            formatter.format(date),
                            mimeType,
                            thumbnail,
                            bucketName
                        )
                    )
                }
            }
            images
        }
    }

    suspend fun getVideosOfBucket(bucketID: Long): List<VideoProjection> {
        return withContext(Dispatchers.IO) {
            val version = MediaStore.getVersion(context)
            Log.d("Version", version)
            val videos = mutableListOf<VideoProjection>()

            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.MIME_TYPE
            )
            val selection = "${MediaStore.Video.Media.BUCKET_ID} = ?"
            val selectionArgs = arrayOf(bucketID.toString())
            val query =
                context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
            query?.use {
                val idColumn = it.getColumnIndex(MediaStore.Video.Media._ID)
                val nameColumn = it.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val bucketColumn = it.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val dataColumn = it.getColumnIndex(MediaStore.Video.Media.DATA)
                val sizeColumn = it.getColumnIndex(MediaStore.Video.Media.SIZE)
                val dateColumn = it.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                val mimeTypeColumn = it.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val bucketName = it.getString(bucketColumn)
                    val data = it.getString(dataColumn)
                    val size = it.getLong(sizeColumn)
                    val contentUri =
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val date = it.getLong(dateColumn) * 1000
                    val mimeType = it.getString(mimeTypeColumn)
                    Log.d("Content uri", "$contentUri")
                    val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        context.contentResolver.loadThumbnail(contentUri, Size(100, 100), null)
                        //ThumbnailUtils.createVideoThumbnail(File(data), Size(100, 100), null)
                    } else {
                        MediaStore.Video.Thumbnails.getThumbnail(
                            context.contentResolver,
                            id,
                            MediaStore.Video.Thumbnails.MINI_KIND,
                            null
                        )
                    }

                    val video = VideoProjection(
                        id,
                        name,
                        data,
                        bucketID,
                        size,
                        contentUri,
                        formatter.format(date),
                        mimeType,
                        thumbnail,
                        bucketName
                    )
                    videos.add(video)
                }
            }

            videos
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun getDocumentsOfBucket(bucketID: Long): List<DocumentProjection> {
        return withContext(Dispatchers.IO) {
            val version = MediaStore.getVersion(context)
            Log.d("Version", version)
            val documents = mutableListOf<DocumentProjection>()

            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

            val fileExtensions = arrayOf("application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            val mimeSelection = fileExtensions.joinToString(" OR ") {
                "${MediaStore.Downloads.MIME_TYPE} = ?"
            }
            val selection = "($mimeSelection) AND ${MediaStore.Downloads.BUCKET_ID} = ?"
            Log.d("Selection", selection)
            val selectionArgs = arrayOf(bucketID.toString())
            val projection = arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.BUCKET_DISPLAY_NAME,
                MediaStore.Downloads.DATA,
                MediaStore.Downloads.SIZE,
                MediaStore.Downloads.DATE_MODIFIED,
                MediaStore.Downloads.MIME_TYPE,
            )

            val query =
                context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    fileExtensions + selectionArgs,
                    null
                )
            query?.use {
                val idColumn = it.getColumnIndex(MediaStore.Downloads._ID)
                val nameColumn = it.getColumnIndex(MediaStore.Downloads.DISPLAY_NAME)
                val bucketColumn = it.getColumnIndex(MediaStore.Downloads.BUCKET_DISPLAY_NAME)
                val dataColumn = it.getColumnIndex(MediaStore.Downloads.DATA)
                val sizeColumn = it.getColumnIndex(MediaStore.Downloads.SIZE)
                val dateColumn = it.getColumnIndex(MediaStore.Downloads.DATE_MODIFIED)
                val mimeTypeColumn = it.getColumnIndex(MediaStore.Downloads.MIME_TYPE)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val bucket = it.getString(bucketColumn)
                    val data = it.getString(dataColumn)
                    val size = it.getLong(sizeColumn)
                    val contentUri =
                        ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                    val date = it.getLong(dateColumn) * 1000
                    val mimeType = it.getString(mimeTypeColumn)
                    Log.d("Content uri", "$contentUri")

                    val index = name.lastIndexOf(".")
                    val extension = if (index != -1) {
                        name.substring(index + 1)
                    } else {
                        ""
                    }
                    val video = DocumentProjection(
                        id,
                        name,
                        data,
                        bucketID,
                        size,
                        contentUri,
                        formatter.format(date),
                        extension,
                        mimeType,
                        bucket
                    )
                    documents.add(video)
                }
            }

            documents
        }
    }
    suspend fun getAudiosOfBucket(bucketID: Long): List<AudioProjection> {
        return withContext(Dispatchers.IO) {
            val version = MediaStore.getVersion(context)
            Log.d("Version", version)
            val audios = mutableListOf<AudioProjection>()

            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.MIME_TYPE
            )
            val selection = "${MediaStore.Audio.Media.BUCKET_ID} = ?"
            val selectionArgs = arrayOf(bucketID.toString())
            val query =
                context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
            query?.use {
                val idColumn = it.getColumnIndex(MediaStore.Audio.Media._ID)
                val nameColumn = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val bucketColumn = it.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
                val dataColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val sizeColumn = it.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dateColumn = it.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val mimeTypeColumn = it.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val bucket = it.getString(bucketColumn)
                    val data = it.getString(dataColumn)
                    val size = it.getLong(sizeColumn)
                    val contentUri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val date = it.getLong(dateColumn) * 1000
                    val mimeType = it.getString(mimeTypeColumn)
                    Log.d("Content uri", "$contentUri")
//                    val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        context.contentResolver.loadThumbnail(contentUri, Size(100, 100), null)
//                        //ThumbnailUtils.createVideoThumbnail(File(data), Size(100, 100), null)
//                    } else {
//                        MediaStore.Video.Thumbnails.getThumbnail(
//                            context.contentResolver,
//                            id,
//                            MediaStore.Audio.Thumbnails.MINI_KIND,
//                            null
//                        )
//                    }

                    val audio = AudioProjection(
                        id,
                        name,
                        data,
                        bucketID,
                        size,
                        contentUri,
                        formatter.format(date),
                        mimeType,
                        bucket
                    )
                    audios.add(audio)
                }
            }

            audios
        }
    }

    suspend fun getLockedFiles(parentId: String): List<FileEntity> {
        return withContext(Dispatchers.IO) {
            fileDao.getFileOf(parentId)
        }

    }
    suspend fun getAll(): List<DocumentProjection> {
        return withContext(Dispatchers.IO) {
            Log.d("TAG", "AT")
            val documents = mutableListOf<DocumentProjection>()
            val collection = MediaStore.Files.getContentUri("external")
            val fileExtensions = arrayOf("application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.BUCKET_ID,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
            )
            val selection = MediaStore.Files.FileColumns.MIME_TYPE + " IN (" + fileExtensions.joinToString(",") { "'application/$it'" } + ")"
            val query =
                context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    null,
                    null
                )
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val displayNameColumn =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataNameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val bucketIdColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID)
                val dateColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeTypeColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(displayNameColumn)
                    val data = cursor.getString(dataNameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val bucketId = cursor.getLong(bucketIdColumn)
                    val bucket = cursor.getString(bucketColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"),
                        id
                    )
                    val date = cursor.getLong(dateColumn) * 1000
                    val mimeType = cursor.getString(mimeTypeColumn)
                    Log.d(
                        "From file repo",
                        "id: $id name: $name size: $size ${formatter.format(date)} $mimeType"
                    )
                    Log.d("From file repo", contentUri.toString())
//                    val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        context.contentResolver.loadThumbnail(contentUri, Size(100, 100), null)
//                    } else {
//                        MediaStore.Images.Media.getBitmap(context.contentResolver, contentUri)
//                    }
                    val index = name.lastIndexOf(".")
                    val extension = if (index != -1) {
                        name.substring(index + 1)
                    } else {
                        ""
                    }
                    documents.add(
                        DocumentProjection(
                            id,
                            name,
                            data,
                            bucketId,
                            size,
                            contentUri,
                            formatter.format(date),
                            extension,
                            mimeType,
                            bucket
                        )
                    )
                }
            }
            documents
        }

    }

    suspend fun insertFileIntoDb(file: FileEntity) : Long {
        return withContext(Dispatchers.IO) {
            fileDao.insertFile(file)
        }
    }
    suspend fun deleteLockedFile(fileID: String) {
        return withContext(Dispatchers.IO) {
            fileDao.deleteLockedFile(fileID)
        }
    }
}