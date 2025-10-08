package com.project.job.ui.activity.jobdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.WorkerOrder
import com.project.job.ui.activity.jobdetail.viewmodel.ChoiceWorkerViewModel
import kotlinx.coroutines.launch

class WorkerAdapter (
    private var workerList: List<WorkerOrder> = emptyList(),
    private val viewModel: ChoiceWorkerViewModel,
    private val token: String,
    private val lifecycleOwner: LifecycleOwner,
    private val onWorkerStatusChanged: () -> Unit = {},
    private val onViewDetailClicked: (WorkerOrder) -> Unit = {},
    private val preferencesManager: PreferencesManager
) : RecyclerView.Adapter<WorkerAdapter.viewHolder>() {
    inner class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFullName = itemView.findViewById<TextView>(R.id.tv_full_name)
        private val tvPhone = itemView.findViewById<TextView>(R.id.tv_phone)
        private val tvLocation = itemView.findViewById<TextView>(R.id.tv_location)
        private val tvStatus = itemView.findViewById<TextView>(R.id.tv_status)
        private val cardViewDetailWorker = itemView.findViewById<CardView>(R.id.card_view_detail_worker)
        private val cardViewAccept = itemView.findViewById<CardView>(R.id.card_view_accept)

        // Rating stars
        private val ivStar1 = itemView.findViewById<ImageView>(R.id.iv_star_1)
        private val ivStar2 = itemView.findViewById<ImageView>(R.id.iv_star_2)
        private val ivStar3 = itemView.findViewById<ImageView>(R.id.iv_star_3)
        private val ivStar4 = itemView.findViewById<ImageView>(R.id.iv_star_4)
        private val ivStar5 = itemView.findViewById<ImageView>(R.id.iv_star_5)
        private val stars = listOf(ivStar1, ivStar2, ivStar3, ivStar4, ivStar5)
        
        // Comment section
        private val edtComment = itemView.findViewById<EditText>(R.id.edt_comment)
        private val cardViewComment = itemView.findViewById<CardView>(R.id.card_view_comment)
        
        // Rating state
        private var currentRating = 0

        fun bind(worker: WorkerOrder) {
            val isReview = worker.isReview
            if(isReview) {
                // Hiển thị review đã có
                val rating = worker.review?.rating ?: 0
                
                // Vô hiệu hóa click sao TRƯỚC khi setRating để tránh lỗi click
                disableStarClicks()
                
                // Hiển thị số sao đã đánh giá
                setRating(rating)
                
                itemView.findViewById<TextView>(R.id.tv_review_comment).text = worker.review?.comment ?: ""
                itemView.findViewById<TextView>(R.id.tv_review_comment).visibility = View.VISIBLE
                // Nếu đã đánh giá, ẩn phần đánh giá
                itemView.findViewById<View>(R.id.ll_review).visibility = View.VISIBLE
                itemView.findViewById<View>(R.id.ll_comment).visibility = View.GONE
                itemView.findViewById<View>(R.id.ll_action).visibility = View.GONE
            } else {
                // Nếu chưa đánh giá, hiển thị phần đánh giá
                itemView.findViewById<View>(R.id.ll_review).visibility = View.VISIBLE
                itemView.findViewById<View>(R.id.ll_comment).visibility = View.VISIBLE
                itemView.findViewById<View>(R.id.ll_action).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.tv_review_comment).visibility = View.GONE
                
                // Reset stars to clickable state
                stars.forEach { star ->
                    star.alpha = 1.0f
                    star.isClickable = true
                    star.isFocusable = true
                }
                
                setupStarRating() // Cho phép click sao
                setupCommentButton() // Setup comment button
            }
            var status = worker.status
            if(status == "Waiting") {
                status = "Chờ xác nhận"
            }
            else if(status == "Accepted") {
                status = "Đã xác nhận"
                tvStatus.setTextColor(itemView.context.getColor(R.color.xanh))
            }
            else if(status == "Rejected") {
                status = "Đã từ chối"
                tvStatus.setTextColor(itemView.context.getColor(R.color.red))
            }
            else if(status == "Completed") {
                status = "Hoàn thành"
                tvStatus.setTextColor(itemView.context.getColor(R.color.red))
            }
            else if(status == "Processing") {
                status = "Đang xử lý"
                tvStatus.setTextColor(itemView.context.getColor(R.color.cam))
            }
            tvFullName.text = worker.worker.username
            tvLocation.text = worker.worker.location
            tvStatus.text = status
            tvPhone.text = worker.worker.tel
            
            // Show/hide rating section based on status
            val llReview = itemView.findViewById<View>(R.id.ll_review)
            val llAction = itemView.findViewById<View>(R.id.ll_action)
            if(status == "Chờ xác nhận") {
                cardViewAccept.visibility = View.VISIBLE
                llReview.visibility = View.GONE
            }
            else {
                if(status == "Đã xác nhận" || status == "Đang xử lý") {
                    cardViewAccept.visibility = View.GONE
                    // Show rating section only for completed work
                }
                if(status == "Hoàn thành") {
                    llReview.visibility = View.VISIBLE
                    llAction.visibility = View.GONE
                    setupStarRating()
                } else {
                    llReview.visibility = View.GONE
                    llAction.visibility = View.VISIBLE
                }
            }
            // Xử lý sự kiện click cho card_view_detail_worker
            cardViewDetailWorker.setOnClickListener {
                // Chuyển sang màn hình chi tiết worker (không gọi onWorkerStatusChanged)
                onViewDetailClicked(worker)
            }

            // Xử lý sự kiện click cho card_view_accept
            cardViewAccept.setOnClickListener {
                // Gọi ChoideWorkerViewModel.choiceWorker với status "Accepted"
                viewModel.choiceWorker(token, worker.uid, "Accepted")

                // Ẩn card_view_accept sau khi accept
                cardViewAccept.visibility = View.GONE

                // Callback để thông báo worker đã được accept và refresh dữ liệu
                onWorkerStatusChanged()
            }
        }
        
        private fun setupStarRating() {
            // Setup click listeners for stars (only when not reviewed)
            stars.forEachIndexed { index, star ->
                star.setOnClickListener {
                    setRating(index + 1)
                }
            }
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
        
        private fun setupCommentButton() {
            // Setup comment button
            cardViewComment.setOnClickListener {
                val comment = edtComment.text.toString().trim()
                if (comment.isNotEmpty() && currentRating > 0 && currentRating <= 5) {
                    // Store rating value before any operations that might reset it
                    val ratingToSend = currentRating
                    
                    // Post review to server
                    lifecycleOwner.lifecycleScope.launch {
                        val userID = preferencesManager.getUserData()["user_id"] ?: ""
                        try {
                            viewModel.postReviewWorker(
                                token = token,
                                orderID = workerList[adapterPosition].uid,
                                rating = ratingToSend,
                                comment = comment,
                                workerID = workerList[adapterPosition].worker.uid,
                                userID = userID,
                                serviceType = workerList[adapterPosition].serviceType
                            )
                            
                            // Show success message
                            Toast.makeText(itemView.context, "Đánh giá đã được gửi thành công!", Toast.LENGTH_SHORT).show()
                            
                            // Clear comment and reset rating after successful API call
                            edtComment.text.clear()
                            setRating(0)
                            
                        } catch (e: Exception) {
                            // Show error message
                            Toast.makeText(itemView.context, "Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Show validation message
                    if (currentRating == 0 || currentRating > 5) {
                        Toast.makeText(itemView.context, "Vui lòng chọn số sao đánh giá từ 1-5!", Toast.LENGTH_SHORT).show()
                    } else if (comment.isEmpty()) {
                        Toast.makeText(itemView.context, "Vui lòng nhập nhận xét!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            // Initialize with no rating
            setRating(0)
        }
        
        private fun setRating(rating: Int) {
            // Ensure rating is within valid range (0-5)
            currentRating = when {
                rating < 0 -> 0
                rating > 5 -> 5
                else -> rating
            }
            
            // Hiển thị số sao theo đánh giá
            stars.forEachIndexed { index, star ->
                if (index < currentRating) {
                    // Filled star - màu cam
                    star.setColorFilter(ContextCompat.getColor(itemView.context, R.color.cam))
                } else {
                    // Empty star - màu xám
                    star.setColorFilter(ContextCompat.getColor(itemView.context, R.color.gray))
                }
            }
        }
        
        private fun submitRatingAndComment(rating: Int, comment: String) {
            // This method is no longer needed as API call is handled directly in click listener
            // Keeping for backward compatibility but removing the reset logic
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
//        holder.itemView.setOnClickListener {
//            onViewDetailClicked(workerList[position])
//        }
    }
}