package com.example.doan.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan.R
import com.example.doan.crypto.Crypto
import com.example.doan.repository.KeysRepository
import com.example.doan.utils.AUDIO_MEDIA
import com.example.doan.utils.IMAGE_MEDIA
import com.example.doan.utils.VIDEO_MEDIA
import com.example.doan.utils.getFileInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LockerFileAdapter(
    private val context: Context,
    private val files: List<File>,
    private val fileType: String,
    private val keysRepository: KeysRepository
) : RecyclerView.Adapter<LockerFileAdapter.FileViewHolder>() {
    companion object {
        private var data : ByteArray = byteArrayOf()
    }
    class FileViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val fileImage: ImageView = view.findViewById(R.id.file_image)
        val fileName: TextView = view.findViewById(R.id.file_name)
        val fileInfo: TextView = view.findViewById(R.id.file_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)
        return FileViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return files.size
    }


    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        Log.d("file adapter", formatter.format(Date()))
        holder.fileInfo.text = getFileInfo(file)
        Log.d("file adapter", file.length().toString())
        if (file.isDirectory) {
            holder.fileImage.setImageResource(R.drawable.folder)
        } else {
            val coroutineScope = CoroutineScope(Dispatchers.Main)
            coroutineScope.launch {
                val encryptInformation = keysRepository.getKey(file.name)
                Log.d("EN", encryptInformation)
                data = file.readBytes()
//                context.contentResolver.openInputStream(file.toUri())?.use {
//                    data = it.readBytes()
//                    it.close()
//                }
//                file.readBytes()
                val decryptedData = withContext(Dispatchers.IO) {
                    Crypto().restoreOriginalArray(data, encryptInformation)
                }
                when (fileType) {
                    IMAGE_MEDIA -> {
                        val options = BitmapFactory.Options()

                        // Set các thuộc tính của options
                        options.inSampleSize = 2
                        val bitmap =
                            BitmapFactory.decodeByteArray(
                                decryptedData,
                                0,
                                decryptedData.size,
                                options
                            )
                        // holder.fileImage.setImageBitmap(bitmap)
                        Glide.with(holder.fileImage)
                            .load(bitmap)
                            .placeholder(R.drawable.loading_img)
                            .error(R.drawable.file)
                            .into(holder.fileImage)

                        Log.d("file adapter", formatter.format(Date()))
                    }

                    VIDEO_MEDIA -> {
//                        val cacheFile = File(context.cacheDir, file.name)
//                        FileOutputStream(cacheFile).use { fos ->
//                            fos.write(decryptedData)
//                            fos.flush()
//                            fos.close()
//                        }
//                        Log.d("file adapter", cacheFile.absolutePath)
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                            val bitmap =
//                                ThumbnailUtils.createVideoThumbnail(cacheFile, Size(100, 100), null)
//                            holder.fileImage.setImageBitmap(bitmap)
//                        } else {
//
//                            val bitmap = getVideoThumbnail(cacheFile.absolutePath)
//                            holder.fileImage.setImageBitmap(bitmap)
//                        }
                        Glide.with(holder.fileImage)
                            .load(decryptedData)
                            .placeholder(R.drawable.loading_img)
                            .error(R.drawable.file)
                            .into(holder.fileImage)

                        Log.d("file adapter", formatter.format(Date()))

                    }
                    AUDIO_MEDIA -> {
                        Glide.with(holder.fileImage)
                            .load(decryptedData)
                            .placeholder(R.drawable.loading_img)
                            .error(R.drawable.baseline_audio_file_24)
                            .into(holder.fileImage)
                    }
                }
            }

            //val decryptedData = Crypto().restoreOriginalArray()
//            Log.d("file" , file.absolutePath)
//            val thumbnail : Bitmap = BitmapFactory.decodeFile(file.absolutePath)
//            Log.d("file" , thumbnail.toString())

            // holder.fileImage.setImageResource(R.drawable.file)
        }
        holder.fileName.text = file.name

        holder.view.setOnClickListener {
            if (file.isDirectory) {
                val path = file.absolutePath

//                val action = HomeFragmentDirections.actionHomeFragmentSelf(path)
//                holder.view.findNavController().navigate(action)
            }
            // Handle directory click
            else {
                // Handle file click
            }

        }

    }

    private suspend fun getVideoThumbnail(videoFilePath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return withContext(Dispatchers.IO) {
            try {
                retriever.setDataSource(videoFilePath)
                retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                retriever.release()
            }
        }
    }


}