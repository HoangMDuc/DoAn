package com.example.doan.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan.R
import com.example.doan.database.entity.FolderEntity

class LockedFolderAdapter(val context: Context,
    val clickListener: (FolderEntity) -> Unit
    ) : ListAdapter<FolderEntity, LockedFolderAdapter.AdapterViewHolder>(DiffCallback) {

    class AdapterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val folderImage : ImageView = itemView.findViewById(R.id.folder_image)
        val folderName : TextView = itemView.findViewById(R.id.folder_name)
        val folderQuantity : TextView = itemView.findViewById(R.id.fold_quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return AdapterViewHolder(layout)
    }

    @SuppressLint("CheckResult", "DiscouragedApi")
    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
        val fd = getItem(position)
        holder.folderName.text = fd.name
        holder.folderQuantity.text = fd.fileQuantity.toString()
        if(fd.thumbnail != null) {
            Glide.with(holder.folderImage)
                .load(context.resources.getIdentifier(fd.defaultThumbnail, "drawable", context.packageName))
                .placeholder(R.drawable.loading_img)
                .error(R.drawable.folder)
                .into(holder.folderImage)

        }else {
            Glide.with(holder.folderImage)
                .load(R.drawable.folder)
                .into(holder.folderImage)
        }

        holder.view.setOnClickListener {
            clickListener(fd)
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<FolderEntity>() {
            override fun areItemsTheSame(oldItem: FolderEntity, newItem: FolderEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: FolderEntity, newItem: FolderEntity) =
                oldItem.id == newItem.id
        }
    }
}