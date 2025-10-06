package com.project.job.ui.service.maintenanceservice.adapter

import android.annotation.SuppressLint
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.project.job.R
import com.project.job.data.source.remote.api.response.PowersInfo
import com.project.job.utils.addFadeClickEffect
import kotlinx.parcelize.Parcelize

@Parcelize
data class PowerItem(
    val powerName: String,
    var quantity: Int = 0,
    var isExpanded: Boolean = false,
    var isMaintenanceEnabled: Boolean = false,
    var maintenanceQuantity: Int? = 0,
    val maintenanceName: String = "",
    val price: Int? = 0,
    val priceAction: Int? = 0,
    val uid: String = ""
) : Parcelable

class PowerAdapter(
    private val onQuantityChanged: (List<PowerItem>) -> Unit
) : RecyclerView.Adapter<PowerAdapter.ViewHolder>() {
    private val items = mutableListOf<PowerItem>()

    companion object {
        // Expose PowerItem để có thể truy cập từ bên ngoài
        val PowerItemType = PowerItem::class.java
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPowerName: TextView = itemView.findViewById(R.id.tv_power_name)
        private val tvPowerNameChild: TextView = itemView.findViewById(R.id.tv_power_name_child)
        private val tvPowerPrice: TextView = itemView.findViewById(R.id.tv_power_price)
        private val tvPowerPriceChild: TextView = itemView.findViewById(R.id.tv_power_price_child)

        private val ivNarrowDown: ImageView =
            itemView.findViewById(R.id.iv_narrow_down_what_is_tasker_favorite)
        private val ivNarrowUp: ImageView =
            itemView.findViewById(R.id.iv_narrow_up_what_is_tasker_favorite)

        private val cardViewQuantitySub: CardView =
            itemView.findViewById(R.id.card_view_quantity_sub)
        private val cardViewQuantityAdd: CardView =
            itemView.findViewById(R.id.card_view_quantity_add)
        private val tvNumberQuantity: TextView = itemView.findViewById(R.id.tv_number_quantity)

        private val llChildSection : LinearLayout = itemView.findViewById(R.id.ll_child_section)
        private val llMaintenanceSection: LinearLayout =
            itemView.findViewById(R.id.ll_maintenance_section)
        private val cardViewQuantityChildSub: CardView =
            itemView.findViewById(R.id.card_view_quantity_child_sub)
        private val cardViewQuantityChildAdd: CardView =
            itemView.findViewById(R.id.card_view_quantity_child_add)
        private val tvNumberQuantityChild: TextView =
            itemView.findViewById(R.id.tv_number_quantity_child)
        private val llQuantityChildSection: LinearLayout =
            itemView.findViewById(R.id.ll_quantity_child_section)

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        private val switchPower: Switch = itemView.findViewById(R.id.switch_power)

        @SuppressLint("SetTextI18n")
        fun bind(item: PowerItem, position: Int) {
            tvPowerName.text = item.powerName
            tvPowerNameChild.text = item.maintenanceName
            tvNumberQuantity.text = item.quantity.toString()
            tvNumberQuantityChild.text = item.maintenanceQuantity.toString()

            // Hiển thị giá thông thường
            updatePriceDisplay(item)

            // Hiển thị giá maintenance (priceAction)
            updateMaintenancePriceDisplay(item)

            // Hiển thị/ẩn expanded section
            llMaintenanceSection.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            ivNarrowDown.visibility = if (item.isExpanded) View.GONE else View.VISIBLE
            ivNarrowUp.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

            // Switch state
            switchPower.isChecked = item.isMaintenanceEnabled
            llChildSection.visibility = if (item.isMaintenanceEnabled) View.VISIBLE else View.GONE

            // Expand/Collapse click
            ivNarrowDown.addFadeClickEffect {
                item.isExpanded = true
                notifyItemChanged(position)
            }

            ivNarrowUp.addFadeClickEffect {
                item.isExpanded = false
                notifyItemChanged(position)
            }

            // Main quantity controls
            cardViewQuantitySub.addFadeClickEffect {
                if (item.quantity > 0) {
                    item.quantity--
                    tvNumberQuantity.text = item.quantity.toString()
                    // Cập nhật trạng thái các nút maintenance
                    updateMaintenanceControlsState(item, cardViewQuantityChildSub, cardViewQuantityChildAdd)
                    onQuantityChanged(items)
                }
            }

            cardViewQuantityAdd.addFadeClickEffect {
                item.quantity++
                tvNumberQuantity.text = item.quantity.toString()
                // Cập nhật trạng thái các nút maintenance
                updateMaintenanceControlsState(item, cardViewQuantityChildSub, cardViewQuantityChildAdd)
                onQuantityChanged(items)
            }

            // Maintenance switch
            switchPower.setOnCheckedChangeListener { _, isChecked ->
                item.isMaintenanceEnabled = isChecked
                llChildSection.visibility = if (isChecked) View.VISIBLE else View.GONE
                if (isChecked) {
                    // Khi bật maintenance, khởi tạo maintenanceQuantity = 0 nếu chưa có
                    if (item.maintenanceQuantity == null) {
                        item.maintenanceQuantity = 0
                    }
                    tvNumberQuantityChild.text = item.maintenanceQuantity.toString()
                } else {
                    item.maintenanceQuantity = 0
                    tvNumberQuantityChild.text = item.maintenanceQuantity.toString()
                }
                updatePriceDisplay(item)
                updateMaintenancePriceDisplay(item)
                // Cập nhật trạng thái các nút maintenance
                updateMaintenanceControlsState(item, cardViewQuantityChildSub, cardViewQuantityChildAdd)
                onQuantityChanged(items)
            }

            // Maintenance quantity controls với điều kiện quantity > 0
            cardViewQuantityChildSub.addFadeClickEffect {
                if (item.quantity > 0 && (item.maintenanceQuantity ?: 0) > 0) {
                    item.maintenanceQuantity = (item.maintenanceQuantity ?: 0) - 1
                    tvNumberQuantityChild.text = item.maintenanceQuantity.toString()
                    onQuantityChanged(items)
                }
            }

            cardViewQuantityChildAdd.addFadeClickEffect {
                if (item.quantity > 0) {
                    item.maintenanceQuantity = (item.maintenanceQuantity ?: 0) + 1
                    tvNumberQuantityChild.text = item.maintenanceQuantity.toString()
                    onQuantityChanged(items)
                }
            }

            // Cập nhật trạng thái enabled/disabled của các nút maintenance
            updateMaintenanceControlsState(item, cardViewQuantityChildSub, cardViewQuantityChildAdd)
        }

        private fun updatePriceDisplay(item: PowerItem) {
            val priceToShow = item.price
            tvPowerPrice.text = if (priceToShow != null && priceToShow > 0) {
                "${String.format("%,d", priceToShow)} VND"
            } else {
                "Liên hệ"
            }
            tvPowerPrice.visibility = View.VISIBLE
        }

        private fun updateMaintenancePriceDisplay(item: PowerItem) {
            val priceActionToShow = item.priceAction
            tvPowerPriceChild.text = if (priceActionToShow != null && priceActionToShow > 0) {
                "+${String.format("%,d", priceActionToShow)}đ mỗi máy"
            } else {
                "Liên hệ"
            }
        }

        private fun updateMaintenanceControlsState(item: PowerItem, subButton: CardView, addButton: CardView) {
            val canUseMaintenance = item.quantity > 0 && item.isMaintenanceEnabled

            // Làm mờ các nút nếu không thể sử dụng
            val alpha = if (canUseMaintenance) 1.0f else 0.5f
            subButton.alpha = alpha
            addButton.alpha = alpha

            // Disable click effect nếu không thể sử dụng
            subButton.isClickable = canUseMaintenance
            addButton.isClickable = canUseMaintenance
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_maintenance_power, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    fun submitList(newItems: List<PowersInfo>, maintenanceText: String) {
        items.clear()
        items.addAll(newItems.map {
            PowerItem(
                powerName = it.name ?: "Unknown", // Xử lý trường hợp name null
                maintenanceName = maintenanceText,
                price = it.price,
                maintenanceQuantity = it.quantityAction ?: 0, // Đảm bảo không null
                uid = it.uid,
                priceAction = it.priceAction
            )
        })
        notifyDataSetChanged()
    }

    fun submitListWithPrices(newItems: List<PowerItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<PowerItem> = items.filter { it.quantity > 0 }

    fun getAllItems(): List<PowerItem> = items.toList()
}