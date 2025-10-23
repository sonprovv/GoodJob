package com.project.job.ui.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.model.NominatimSearchResult

class SearchResultsAdapter(
    private val onItemClick: (NominatimSearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {

    private val results = mutableListOf<NominatimSearchResult>()

    fun submitList(newResults: List<NominatimSearchResult>) {
        results.clear()
        results.addAll(newResults)
        notifyDataSetChanged()
    }

    fun clearResults() {
        results.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_result_title)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_result_address)

        fun bind(result: NominatimSearchResult) {
            // Extract title from display name (usually first part before comma)
            val parts = result.displayName.split(",")
            tvTitle.text = parts.firstOrNull()?.trim() ?: result.displayName
            
            // Use formatted short address
            tvAddress.text = result.getShortAddress()

            itemView.setOnClickListener {
                onItemClick(result)
            }
        }
    }
}
