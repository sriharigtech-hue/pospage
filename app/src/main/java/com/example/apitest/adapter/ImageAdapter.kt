package com.example.apitest.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apitest.R



class PhotoAdapter(private val images: List<Uri>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return PhotoViewHolder(view)
    }


    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // Use Glide for Uri
        Glide.with(holder.itemView.context)
            .load(images[position])
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size
}

