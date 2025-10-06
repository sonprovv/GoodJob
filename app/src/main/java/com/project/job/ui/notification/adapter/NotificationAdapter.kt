package com.project.job.ui.notification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.NotificationInfo

class NotificationAdapter(
    private var notifications: List<NotificationInfo> = emptyList(),
    private val onMenuItemClick: (NotificationInfo, String) -> Unit = { _, _ -> },
//    private val onViewDetailClicked: (String) -> Unit = {}
    private val onViewDetailClicked: (NotificationInfo) -> Unit = {}
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceType = itemView.findViewById<TextView>(R.id.tvServiceNameValue)
        val content = itemView.findViewById<TextView>(R.id.tvNotificationContent)
        val time = itemView.findViewById<TextView>(R.id.tvNotificationTime)
        val container = itemView.findViewById<LinearLayout>(R.id.notificationContainer)
        val optionsButton = itemView.findViewById<ImageView>(R.id.ivOptions)
        fun bind(notification: NotificationInfo) {
//            val service = when (notification.serviceType.uppercase()) {
//                "HEALTHCARE" -> "Chăm sóc"
//                "CLEANING" -> "Dọn dẹp"
//                "MAINTENANCE" -> "Bảo trì"
//                else -> "Dịch vụ khác"
//            }
//            serviceType.text = service
            serviceType.text = notification.title
            content.text = notification.content
            time.text = notification.createdAt
            
            // Set background color based on read status
            if(notification.isRead){
                container.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
            }else{
                container.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.unreadnotification))
            }
            
            // Setup options menu
            optionsButton.setOnClickListener { view ->
                showPopupMenu(view, notification)
            }
        }
        
        private fun showPopupMenu(view: View, notification: NotificationInfo) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.nav_notification, popupMenu.menu)
            
            // Hide "Đánh dấu đã đọc" if already read
            if (notification.isRead) {
                popupMenu.menu.findItem(R.id.markread)?.isVisible = false
            }
            
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.markread -> {
                        onMenuItemClick(notification, "mark_read")
                        true
                    }
                    R.id.delete -> {
                        onMenuItemClick(notification, "delete")
                        true
                    }
                    else -> false
                }
            }
            
            popupMenu.show()
        }
    }
    fun submitList(newNotifications: List<NotificationInfo>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])

        holder.itemView.setOnClickListener {
            if (!notifications[position].isRead) {
                onMenuItemClick(notifications[position], "mark_read")
            }
//            onViewDetailClicked(notifications[position].jobID)
            onViewDetailClicked(notifications[position])
        }
    }

    override fun getItemCount(): Int = notifications.size
}