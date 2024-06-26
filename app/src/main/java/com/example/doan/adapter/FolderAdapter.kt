package com.example.doan.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doan.R
import com.example.doan.database.Bucket
import com.example.doan.ui.fragment.FileListFragmentDirections

class FolderAdapter(private val buckets:  Map<Long, Bucket>, private val fileType: String)  : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderImage : ImageView = itemView.findViewById(R.id.folder_image)
        val folderName : TextView = itemView.findViewById(R.id.folder_name)
        val folderQuantity : TextView = itemView.findViewById(R.id.fold_quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return FolderViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return buckets.size
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val id = buckets.keys.elementAt(position)
        val bucket = buckets[id]
        if(bucket == null) {
            holder.folderImage.setImageResource(R.drawable.folder)
        }else {
//            holder.folderImage.setImageBitmap(bucket.thumbnail)
            Glide.with(holder.folderImage)
                .load(bucket.thumbnail)
                .placeholder(R.drawable.loading_img)
                .error(R.drawable.file)
                .into(holder.folderImage)
        }
        if (bucket != null) {
            holder.folderName.text = bucket.bucketName
        }
        if (bucket != null) {
            Log.d("FolderAdapter", "onBindViewHolder: ${bucket.bucketName}")
        }
        holder.folderQuantity.text = bucket?.numberOfFiles?.toString() ?: "0"

        holder.itemView.setOnClickListener {
            val action = bucket?.let { it1 -> FileListFragmentDirections.actionFileListFragmentSelf( it1.bucketName, fileType, id) }
            if (action != null) {
                holder.itemView.findNavController().navigate(action)
            }
        }
    }


}