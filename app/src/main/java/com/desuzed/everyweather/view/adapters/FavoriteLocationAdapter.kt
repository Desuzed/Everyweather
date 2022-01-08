package com.desuzed.everyweather.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.desuzed.everyweather.data.room.FavoriteLocationDto
import com.desuzed.everyweather.databinding.FavoriteLocationItemBinding

class FavoriteLocationAdapter(
    private val onItemClickListener: OnItemClickListener,
) :
    ListAdapter<FavoriteLocationDto, FavoriteLocationAdapter.FavoriteLocationVH>(LocationsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteLocationVH {
        return FavoriteLocationVH.create(parent)
    }

    override fun onBindViewHolder(holder: FavoriteLocationVH, position: Int) {
        val current = getItem(position)
        holder.bind(current, onItemClickListener)
    }

    class FavoriteLocationVH(binding: FavoriteLocationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val tvFavoriteLocationName: TextView = binding.tvFavoriteLocationName
        private val tvFavoriteLocationRegionCountry: TextView =
            binding.tvFavoriteLocationRegionCountry

        @SuppressLint("SetTextI18n")
        fun bind(
            favoriteLocationDto: FavoriteLocationDto,
            onItemClickListener: OnItemClickListener
        ) {
            tvFavoriteLocationName.text = favoriteLocationDto.cityName
            tvFavoriteLocationRegionCountry.text = favoriteLocationDto.toString()
            itemView.setOnClickListener {
                onItemClickListener.onClick(favoriteLocationDto)
            }
            itemView.setOnLongClickListener {
                onItemClickListener.onLongClick(favoriteLocationDto)
                return@setOnLongClickListener true
            }
        }


        companion object {
            fun create(parent: ViewGroup): FavoriteLocationVH {
                return FavoriteLocationVH(
                    FavoriteLocationItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    class LocationsComparator : DiffUtil.ItemCallback<FavoriteLocationDto>() {
        override fun areContentsTheSame(
            oldItem: FavoriteLocationDto,
            newItem: FavoriteLocationDto
        ): Boolean {
            return oldItem.latLon == newItem.latLon
        }

        override fun areItemsTheSame(
            oldItem: FavoriteLocationDto,
            newItem: FavoriteLocationDto
        ): Boolean {
            return oldItem == newItem
        }

    }
    interface OnItemClickListener {
        fun onClick(favoriteLocationDto: FavoriteLocationDto)
        fun onLongClick(favoriteLocationDto: FavoriteLocationDto)
    }

}