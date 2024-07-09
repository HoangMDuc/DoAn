package com.example.doan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan.R
import com.example.doan.database.entity.FileEntity
import com.example.doan.repository.KeysRepository
import com.example.doan.utils.AUDIO_MEDIA
import com.example.doan.utils.DOCUMENT
import com.example.doan.utils.IMAGE_MEDIA
import com.example.doan.utils.VIDEO_MEDIA
import com.example.doan.utils.getFileInfo
import com.example.doan.utils.stringToBitmap
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockerFileAdapter(
    private val context: Context,
    private val files: List<FileEntity>,
    private val fileType: String,
    private val keysRepository: KeysRepository,
    private val handleClickFile: (FileEntity, String, ImageView, View) -> Unit,
    private val handleLongClickFile: (FileEntity, ImageView, View) -> Unit
) : RecyclerView.Adapter<LockerFileAdapter.FileViewHolder>() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

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
        //val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        holder.fileInfo.text = getFileInfo(file)
        holder.fileName.text = file.name
        coroutineScope.launch {
            if(keysRepository.hasKey(file.name)) {
                val encryptInformation = keysRepository.getKey(file.name)
                when (fileType) {
                    IMAGE_MEDIA -> {
                        Glide.with(holder.fileImage)
                            .load(file.thumbnail?.let { stringToBitmap(it) })
                            .placeholder(R.drawable.loading_img)
                            .error(R.drawable.file)
                            .into(holder.fileImage)

                    }

                    VIDEO_MEDIA -> {
                        Glide.with(holder.fileImage)
                            .load(file.thumbnail?.let { stringToBitmap(it) })
                            .placeholder(R.drawable.loading_img)
                            .error(R.drawable.file)
                            .into(holder.fileImage)


                    }

                    AUDIO_MEDIA -> {
                        Glide.with(holder.fileImage)
                            .load(R.drawable.baseline_audio_file_24)
                            .placeholder(R.drawable.loading_img)
                            .error(R.drawable.baseline_audio_file_24)
                            .into(holder.fileImage)
                    }

                    DOCUMENT -> {
                        Glide.with(holder.fileImage)
                            .load(R.drawable.file)
                            .placeholder(R.drawable.loading_img)
                            .error(R.drawable.file)
                            .into(holder.fileImage)
                    }
                }
                holder.view.setOnClickListener {
                    handleClickFile(file, encryptInformation, holder.fileImage, holder.view)
                }
                holder.view.setOnLongClickListener {
                    handleLongClickFile(file, holder.fileImage, holder.view)
                    true
                }
            }
            else {
                Snackbar.make(holder.view, "Something went wrong", Snackbar.LENGTH_SHORT).show()
            }

        }
    }

}