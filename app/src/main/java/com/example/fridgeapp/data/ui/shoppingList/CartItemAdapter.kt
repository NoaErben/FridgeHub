package com.example.fridgeapp.data.ui.shoppingList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fridgeapp.R
import com.example.fridgeapp.data.model.CartItem
import com.example.fridgeapp.databinding.CartLayoutBinding

class CartItemAdapter(
    private var items: List<CartItem>,
    private val callBack: ItemListener
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    interface ItemListener {
        fun onItemClick(index: Int)
        fun onItemLongClick(index: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = CartLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
    fun itemAt(index: Int) = items[index]

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }


    inner class CartItemViewHolder(private val binding: CartLayoutBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {
        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

            fun bind(item: CartItem) {
            binding.itemName.text = item.name
            binding.itemCategory.text = item.category
            binding.itemQuantity.text = "Quantity: ${item.quantity}"
            if (item.photoUrl != null) {
                Glide.with(binding.itemImage.context)
                    .load(item.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.dish)
                    .error(R.drawable.dish)
                    .into(binding.itemImage)
            } else {
                Glide.with(binding.root)
                    .load(ContextCompat.getDrawable(binding.root.context, R.drawable.dish))
                    .circleCrop()
                    .into(binding.itemImage)
            }
        }

        override fun onClick(p0: View?) {
            callBack.onItemClick(adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            callBack.onItemLongClick(adapterPosition)
            return true
        }
    }
}
