package com.project.job.ui.service.cleaningservice.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R

class DetailServiceAdapter(private val detailJob: Array<String>) : RecyclerView.Adapter<DetailServiceAdapter.ViewHolder>() {
    class ViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        val tvDetailService: TextView = view.findViewById(R.id.tv_content_detail_job)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailServiceAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail_service, parent, false) as ViewGroup
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailServiceAdapter.ViewHolder, position: Int) {
        val content = detailJob[position]
        holder.tvDetailService.text = content
    }

    override fun getItemCount(): Int = detailJob.size

}