package com.project.job.ui.service.healthcareservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.ui.service.cleaningservice.adapter.DetailServiceCleaningAdapter

class DetailServiceHealthcareAdapter(private val duties: List<String>) :
    RecyclerView.Adapter<DetailServiceHealthcareAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_detail_service,
            parent,
            false
        )
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(duties[position])
    }

    override fun getItemCount(): Int = duties.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tv_content_detail_job)
        private var starImageView: ImageView = itemView.findViewById(R.id.iv_star_detail_job)

        fun bind(task: String) {
            textView.text = task
            starImageView.setImageResource(R.drawable.bg_indicator)
        }
    }
}