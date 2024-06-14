package com.example.doan.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.example.doan.database.Bucket
import com.example.doan.database.ImageProjection
import com.example.doan.database.VideoProjection
import com.example.doan.utils.formatter
import com.example.doan.utils.getLockedFileRootPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileRepository(val context: Context) {

    private var ver: String = "1.0"


    companion object {
        @SuppressLint("StaticFieldLeak")
        val instance: FileRepository? = null

        fun getInstance(context: Context): FileRepository {
            return instance ?: FileRepository(context)
        }


    }

    suspend fun getAllImagesBucketsName(): Map<String, Bucket> {
        return withContext(Dispatchers.IO) {
            //scanToUpdate()
//        val mediaVersion = MediaStore.getVersion(context)
//        if(mediaVersion != ver) {
//
//        }
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            Log.d("FileRepository", formatter.format(Date()))
            val imageList = mutableMapOf<String, Bucket>()
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
            )

            val cur2 = context.contentResolver.query(imgCollection, projection, null, null, null)

            if (cur2 != null) {
                val idColumn = cur2.getColumnIndex(MediaStore.Images.Media._ID)

                val bucketColumn = cur2.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                while (cur2.moveToNext()) {
                    val id = cur2.getLong(idColumn)
                    val bucketName = cur2.getString(bucketColumn)

                    if (imageList.contains(bucketName)) {
                        imageList[bucketName]?.numberOfFiles =
                            imageList[bucketName]?.numberOfFiles!! + 1
                    } else {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
//                        val thumbnail: Bitmap =
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                                context.contentResolver.loadThumbnail(
//                                    contentUri,
//                                    Size(100, 100),
//                                    null
//                                )
//                            } else {
//                                MediaStore.Images.Media.getBitmap(
//                                    context.contentResolver,
//                                    contentUri
//                                )
//                            }
                        val bucket = Bucket(bucketName, 1, contentUri)
                        imageList[bucketName] = bucket
                    }

                    //Log.d("FileRepository", "id: $id name: $name bucket: $bucket contentUri: $contentUri mimeType: $mimeType")
                }
                cur2.close()
            }

            Log.d("FileRepository", formatter.format(Date()))
            imageList

        }
    }

    suspend fun getAllVideosBucketsName(): Map<String, Bucket> {
        //scanToUpdate()
//        val mediaVersion = MediaStore.getVersion(context)
//        if(mediaVersion != ver) {
//
//        }
        return withContext(Dispatchers.IO) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            Log.d("FileRepository Video", formatter.format(Date()))
            val videoList = mutableMapOf<String, Bucket>()
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
            )

            val cur = context.contentResolver.query(collection, projection, null, null, null)
            Log.d("OK", "OL1")
            if (cur != null) {
                val idColumn = cur.getColumnIndex(MediaStore.Video.Media._ID)

                val bucketColumn = cur.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                Log.d("OK", "OK")
                while (cur.moveToNext()) {
                    Log.d("OK", "OL")
                    val id = cur.getLong(idColumn)
                    val bucketName = cur.getString(bucketColumn)

                    if (videoList.contains(bucketName)) {
                        videoList[bucketName]?.numberOfFiles =
                            videoList[bucketName]?.numberOfFiles!! + 1
                    } else {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
//                        val thumbnail: Bitmap =
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                                context.contentResolver.loadThumbnail(
//                                    contentUri,
//                                    Size(100, 100),
//                                    null
//                                )
//                            } else {
//                                MediaStore.Video.Thumbnails.getThumbnail(
//                                    context.contentResolver,
//                                    id,
//                                    MediaStore.Video.Thumbnails.MINI_KIND,
//                                    null
//                                )
//                            }
                        val bucket = Bucket(bucketName, 1, contentUri)
                        videoList[bucketName] = bucket
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

    suspend fun getAllAudiosBucketsName(): Map<String, Bucket> {
        //scanToUpdate()
//        val mediaVersion = MediaStore.getVersion(context)
//        if(mediaVersion != ver) {
//
//        }
        return withContext(Dispatchers.IO) {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            Log.d("FileRepository Video", formatter.format(Date()))
            val audioList = mutableMapOf<String, Bucket>()
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

            )

            val cur = context.contentResolver.query(collection, projection, null, null, null)
            Log.d("OK", "OL1")
            if (cur != null) {
                val idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID)

                val bucketColumn = cur.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

                Log.d("OK", "OK")
                while (cur.moveToNext()) {
                    Log.d("OK", "OL")
                    val id = cur.getLong(idColumn)
                    val bucketName = cur.getString(bucketColumn)

                    if (audioList.contains(bucketName)) {
                        audioList[bucketName]?.numberOfFiles =
                            audioList[bucketName]?.numberOfFiles!! + 1
                    } else {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )


                        val bucket = Bucket(bucketName, 1, contentUri)
                        audioList[bucketName] = bucket
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

    suspend fun getImagesOfBucket(bucketName: String): List<ImageProjection> {

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
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.MIME_TYPE
            )

            val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(bucketName)
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
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(displayNameColumn)
                    val data = cursor.getString(dataNameColumn)
                    val size = cursor.getInt(sizeColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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
                    images.add(
                        ImageProjection(
                            id,
                            name,
                            data,
                            bucketName,
                            size,
                            contentUri,
                            formatter.format(date),
                            mimeType
                        )
                    )
                }
            }
            images
        }
    }

    suspend fun getVideosOfBucket(bucketName: String): List<VideoProjection> {
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
            val selection = "${MediaStore.Video.Media.BUCKET_DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(bucketName)
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
                    val bucket = it.getString(bucketColumn)
                    val data = it.getString(dataColumn)
                    val size = it.getInt(sizeColumn)
                    val contentUri =
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
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
//                            MediaStore.Video.Thumbnails.MINI_KIND,
//                            null
//                        )
//                    }

                    val video = VideoProjection(
                        id,
                        name,
                        data,
                        bucket,
                        size,
                        contentUri,
                        formatter.format(date),
                        mimeType
                    )
                    videos.add(video)
                }
            }

            videos
        }
    }
    suspend fun getAudiosOfBucket(bucketName: String): List<VideoProjection> {
        return withContext(Dispatchers.IO) {
            val version = MediaStore.getVersion(context)
            Log.d("Version", version)
            val videos = mutableListOf<VideoProjection>()

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
            val selection = "${MediaStore.Audio.Media.BUCKET_DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(bucketName)
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
                    val size = it.getInt(sizeColumn)
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

                    val video = VideoProjection(
                        id,
                        name,
                        data,
                        bucket,
                        size,
                        contentUri,
                        formatter.format(date),
                        mimeType
                    )
                    videos.add(video)
                }
            }

            videos
        }
    }
    suspend fun getLockedFiles(folder: String, type: String?): List<File> {
        return withContext(Dispatchers.IO) {
            val path = getLockedFileRootPath(context, type)
            Log.d("Get locked file at: ", path)
            File(path).listFiles()?.asList() ?: emptyList()
        }

    }

    suspend fun readFile(path: String): ByteArray {
        return withContext(Dispatchers.IO) {
            var byteArray: ByteArray = byteArrayOf()

            val file = File(path)
            context.contentResolver.openInputStream(file.toUri())?.use {
                byteArray = it.readBytes()
                it.close()
            }

            byteArray
        }

    }

    suspend fun createAndWriteFile(
        fileName: String,
        byteArray: ByteArray,
        fileType: String
    ): Boolean {

        return withContext(Dispatchers.IO) {
            val file = File(getLockedFileRootPath(context, fileType), fileName)
            try {
                Log.d("FileRepository", "createAndWriteFile: ${file.absolutePath}")
                val outputStream = FileOutputStream(file)
                outputStream.write(byteArray)
                outputStream.close()
                outputStream.flush()
                true
            } catch (e: Exception) {
                Log.i("error in createAndWrite", e.toString())
                false
            }
        }

    }


}