package com.project.job.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.project.job.R

class BannerAdapter(
    private val listimg: List<Int>
) : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {
    class ViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_item_view_pager)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_in_banner, parent, false) as ViewGroup
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = listimg[position]
        Glide.with(holder.imageView.context)
            .load(photo)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return listimg.size
    }


}