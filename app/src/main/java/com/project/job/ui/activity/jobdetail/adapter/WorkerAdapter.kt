package com.project.job.ui.activity.jobdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.WorkerOrder

class WorkerAdapter (private var workerList: List<WorkerOrder> = emptyList()) : RecyclerView.Adapter<WorkerAdapter.viewHolder>() {
    inner class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFullName = itemView.findViewById<TextView>(R.id.tv_full_name)
        private val tvPhone = itemView.findViewById<TextView>(R.id.tv_phone)
        private val tvLocation = itemView.findViewById<TextView>(R.id.tv_location)
        private val tvStatus = itemView.findViewById<TextView>(R.id.tv_status)

        fun bind(worker: WorkerOrder) {
            tvFullName.text = worker.worker.username
            tvLocation.text = worker.worker.location
            tvStatus.text = worker.status
            tvPhone.text = worker.worker.tel
        }
    }

    fun submitList(newList: List<WorkerOrder>) {
        workerList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_worker_order,
            parent,
            false
        )
        return viewHolder(view)
    }

    override fun getItemCount(): Int = workerList.size

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(workerList[position])
    }
}