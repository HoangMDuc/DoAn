package com.example.doan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan.R
import com.example.doan.database.AudioProjection
import com.example.doan.database.FileProjections
import com.example.doan.utils.getFileInfo

class FileAdapter(
//    private val files: List<FileProjections>,
//    private val application: Application,
    val clickListener: (imageView: ImageView, file: FileProjections, view: View) -> Unit
) :
    ListAdapter<FileProjections, FileAdapter.FileViewHolder>(FileProjections.DiffCallback) {

    class FileViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val fileImage: ImageView = view.findViewById(R.id.file_image)
        val fileName: TextView = view.findViewById(R.id.file_name)
        val fileInfo : TextView = view.findViewById(R.id.file_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)
        return FileViewHolder(layout)
    }



    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
//        val file = files[position]
        val file = getItem(position)
       // holder.fileImage.setImageBitmap(file.thumbnail)

        var errorImage = if(file is AudioProjection) {
            R.drawable.baseline_audio_file_24
        }else {
            R.drawable.file
        }
        Glide.with(holder.fileImage)
            .load(file.uri)
            .placeholder(R.drawable.loading_img)
            .error(errorImage)
            .fitCenter()
            .into(holder.fileImage)
        holder.fileName.text = file.name

        holder.fileInfo.text = getFileInfo(file)
        holder.view.setOnClickListener {
            clickListener(holder.fileImage, file, holder.view)
//            val byteArray = bitmapToByteArray(file.thumbnail)
//            val string = Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
    }



}



