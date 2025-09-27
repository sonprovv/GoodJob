package com.project.job.ui.reviewworker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.project.job.R
import com.project.job.data.source.remote.api.response.ExtendedReview

class CommentAdapter(
    private var commentList : List<ExtendedReview> = emptyList(),
) : RecyclerView.Adapter<CommentAdapter.viewHolder>()  {
    inner class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar = itemView.findViewById<ImageView>(R.id.img_avatar_reviewer)
        private val nameReviewer = itemView.findViewById<TextView>(R.id.tv_name_reviewer)
        private val service = itemView.findViewById<TextView>(R.id.tv_service_reviewer)
        private val imgStar1 = itemView.findViewById<ImageView>(R.id.iv_star_1)
        private val imgStar2 = itemView.findViewById<ImageView>(R.id.iv_star_2)
        private val imgStar3 = itemView.findViewById<ImageView>(R.id.iv_star_3)
        private val imgStar4 = itemView.findViewById<ImageView>(R.id.iv_star_4)
        private val imgStar5 = itemView.findViewById<ImageView>(R.id.iv_star_5)
        private val stars = listOf(imgStar1, imgStar2, imgStar3, imgStar4, imgStar5)
        private val comment = itemView.findViewById<TextView>(R.id.tv_comment_reviewer)
        fun bind(review: ExtendedReview) {
            disableStarClicks()
            val serviceType = when (review.serviceType.uppercase()) {
                "HEALTHCARE" -> "Chăm sóc"
                "CLEANING" -> "Dọn dẹp"
                "MAINTENANCE" -> "Bảo trì"
                else -> "Dịch vụ khác"
            }
            nameReviewer.text = review.user.username
            service.text = serviceType // Hiển thị loại dịch vụ từ API
            comment.text = review.comment
            for (i in stars.indices) {
                if (i < review.rating) {
                    stars[i].setColorFilter(ContextCompat.getColor(itemView.context, R.color.cam))
                } else {
                    stars[i].setColorFilter(ContextCompat.getColor(itemView.context, R.color.gray))
                }
            }
            Glide.with(itemView.context)
                .load(review.user.avatar)
                .placeholder(R.drawable.img_profile_picture_defaul)
                .apply(RequestOptions().transform(RoundedCorners(20)))
                .into(imgAvatar)
        }

        private fun disableStarClicks() {
            // Remove click listeners and disable stars when already reviewed
            stars.forEach { star ->
                star.setOnClickListener(null)
                star.isClickable = false
                star.isFocusable = false
                star.isEnabled = false // Vô hiệu hóa hoàn toàn
                star.background = null // Remove ripple effect
                star.alpha = 0.7f // Làm mờ để hiển thị trạng thái disabled
            }
        }
    }

    fun submitList(newList: List<ExtendedReview>) {
        commentList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_review,
            parent,
            false
        )
        return viewHolder(view)
    }

    override fun getItemCount(): Int = commentList.size

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(commentList[position])
    }
}