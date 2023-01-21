package com.aditasha.pitjarus.presentation.store_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aditasha.pitjarus.R
import com.aditasha.pitjarus.databinding.ItemListStoreBinding
import com.aditasha.pitjarus.domain.model.StoreEntity
import javax.inject.Singleton

@Singleton
class StoreListAdapter(private val clickListener: (Int) -> Unit) :
    ListAdapter<StoreEntity, StoreListAdapter.ListViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding =
            ItemListStoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data, clickListener)
    }

    inner class ListViewHolder(private var binding: ItemListStoreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StoreEntity, clickListener: (Int) -> Unit) {
            binding.apply {
                storeTitle.text = data.storeName
                storeAddress.text = data.address
                storeAreaRegion.text = itemView.resources.getString(
                    R.string.store_area_region,
                    data.areaName,
                    data.regionName
                )
                checkIcon.isVisible = data.visited != null && data.visited
                locationDistance.text = data.distance
                root.setOnClickListener { clickListener(data.roomId) }
            }
        }
    }

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<StoreEntity>() {
            override fun areItemsTheSame(oldItem: StoreEntity, newItem: StoreEntity): Boolean {
                return oldItem.roomId == newItem.roomId
            }

            override fun areContentsTheSame(oldItem: StoreEntity, newItem: StoreEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}