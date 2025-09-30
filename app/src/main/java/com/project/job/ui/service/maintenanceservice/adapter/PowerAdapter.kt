package com.project.job.ui.service.maintenanceservice.adapter

import android.annotation.SuppressLint
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
import com.project.job.utils.addFadeClickEffect

data class PowerItem(
    val powerName: String,
    var quantity: Int = 0,
    var isExpanded: Boolean = false,
    var isMaintenanceEnabled: Boolean = false,
    var maintenanceQuantity: Int = 0,
    val maintenanceName: String = ""
)

class PowerAdapter(
    private val onQuantityChanged: (List<PowerItem>) -> Unit
) : RecyclerView.Adapter<PowerAdapter.ViewHolder>() {
    private val items = mutableListOf<PowerItem>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPowerName: TextView = itemView.findViewById(R.id.tv_power_name)
        private val tvPowerNameChild: TextView = itemView.findViewById(R.id.tv_power_name_child)

        private val ivNarrowDown: ImageView =
            itemView.findViewById(R.id.iv_narrow_down_what_is_tasker_favorite)
        private val ivNarrowUp: ImageView =
            itemView.findViewById(R.id.iv_narrow_up_what_is_tasker_favorite)

        private val cardViewQuantitySub: CardView =
            itemView.findViewById(R.id.card_view_quantity_sub)
        private val cardViewQuantityAdd: CardView =
            itemView.findViewById(R.id.card_view_quantity_add)
        private val tvNumberQuantity: TextView = itemView.findViewById(R.id.tv_number_quantity)

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

            // Hiển thị/ẩn expanded section
            llMaintenanceSection.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            ivNarrowDown.visibility = if (item.isExpanded) View.GONE else View.VISIBLE
            ivNarrowUp.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

            // Switch state
            switchPower.isChecked = item.isMaintenanceEnabled
            llQuantityChildSection.visibility =
                if (item.isMaintenanceEnabled) View.VISIBLE else View.GONE

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
                    onQuantityChanged(items)
                }
            }

            cardViewQuantityAdd.addFadeClickEffect {
                item.quantity++
                tvNumberQuantity.text = item.quantity.toString()
                onQuantityChanged(items)
            }

            // Maintenance switch
            switchPower.setOnCheckedChangeListener { _, isChecked ->
                item.isMaintenanceEnabled = isChecked
                llQuantityChildSection.visibility = if (isChecked) View.VISIBLE else View.GONE
                if (!isChecked) {
                    item.maintenanceQuantity = 0
                }
                onQuantityChanged(items)
            }

            // Maintenance quantity controls
            cardViewQuantityChildSub.addFadeClickEffect {
                if (item.maintenanceQuantity > 0) {
                    item.maintenanceQuantity--
                    tvNumberQuantityChild.text = item.maintenanceQuantity.toString()
                    onQuantityChanged(items)
                }
            }

            cardViewQuantityChildAdd.addFadeClickEffect {
                item.maintenanceQuantity++
                tvNumberQuantityChild.text = item.maintenanceQuantity.toString()
                onQuantityChanged(items)
            }
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

    fun submitList(newItems: List<String>, maintenanceText: String = "") {
        items.clear()
        items.addAll(newItems.map { PowerItem(powerName = it, maintenanceName = maintenanceText) })
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<PowerItem> = items.filter { it.quantity > 0 }
}