package com.example.fridgeapp.data.ui.fridge

import com.bumptech.glide.Glide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.FridgeItem
import com.example.fridgeapp.databinding.ItemLayoutBinding

class FridgeItemAdapter(private var items: List<FridgeItem>, private val callBack: ItemListener) : RecyclerView.Adapter<FridgeItemAdapter.ItemViewHolder>() {

    interface ItemListener {
        fun onItemClick(index: Int)
        fun onItemLongClick(index: Int)
    }

    inner class ItemViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        fun bind(item: FridgeItem) {
            binding.itemName.text = item.name
            val currentTime = System.currentTimeMillis() // Current time in milliseconds
            // Calculate milliseconds until expiry milliseconds and convert it to days
            var daysUntilExpiry = (item.expiryDate - currentTime) / (1000 * 60 * 60 * 24)

            if ((item.expiryDate - currentTime) >= 0) {
                daysUntilExpiry += 1
            } else {
                daysUntilExpiry
            }

            if (daysUntilExpiry > 2) {
                binding.itemExpired.text = "Expires in: ${daysUntilExpiry} days"
                binding.itemExpired.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_green_dark))
            } else if (daysUntilExpiry > 0) {
                binding.itemExpired.text = "Expires in: ${daysUntilExpiry} days"
                binding.itemExpired.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_orange_dark))
            } else {
                if (daysUntilExpiry.toInt() == 0)
                    binding.itemExpired.text = "Expires today"
                else
                    binding.itemExpired.text = "Expired before: ${daysUntilExpiry * -1} days"
                binding.itemExpired.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_red_dark))
            }

            if (item.photoUrl != null)
                Glide.with(binding.root)
                    .load(item.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.dish) // Placeholder while loading
                    .error(R.drawable.dish)
                    .into(binding.itemImage)
            else
                Glide.with(binding.root)
                    .load(ContextCompat.getDrawable(binding.root.context, R.drawable.dish))
                    .circleCrop()
                    .into(binding.itemImage)

        }

        override fun onClick(p0: View?) {
            callBack.onItemClick(adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            callBack.onItemLongClick(adapterPosition)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    fun itemAt(index: Int) = items[index]

    // Add this method to update the list of items and notify the adapter
    fun updateItems(newItems: List<FridgeItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
