package com.draw.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.draw.R

class ImageAdapter(private val context: Context, private val imagePaths: List<String>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imagePath = imagePaths[position]
        Glide.with(context)
            .load(imagePath)
            .placeholder(R.drawable.frame_a_creator_10) // Hình ảnh placeholder
            .error(R.drawable.frame_an_angry_gift_7) // Hình ảnh lỗi
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imagePaths.size
    }
}
