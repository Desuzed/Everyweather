package com.desuzed.everyweather.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.desuzed.everyweather.databinding.HourItemBinding
import com.desuzed.everyweather.model.model.Hour
import com.desuzed.everyweather.util.editor.HourStringEditor
import java.util.*
import kotlin.collections.ArrayList

class HourAdapter : RecyclerView.Adapter<HourAdapter.HourVH>() {
    private var list: List<Hour> = mutableListOf()
    private var mTimeZone : String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourVH {
        return HourVH(
            HourItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HourVH, position: Int) {
        holder.bind(list[position], mTimeZone!!)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList (newList : List <Hour>, timeZone: String){
        list = newList
        mTimeZone = timeZone
        notifyDataSetChanged()
    }

    class HourVH (binding: HourItemBinding) :RecyclerView.ViewHolder(binding.root) {
        private val hTime: TextView = binding.hTime
        private val hTempC: TextView = binding.hTempC
        private val hWind: TextView = binding.hWind
        private val hIcon: ImageView = binding.hIcon
        private val hWindDegree: ImageView = binding.hWindDegree

        fun bind(hour: Hour, timeZone: String) {
          val resultMap = HourStringEditor(hour, timeZone, itemView.context).getResultMap()
            hTime.text = resultMap["hTime"]
            hTempC.text = resultMap["hTempC"]
            hWind.text = resultMap["hWind"]
            hWindDegree.rotation = hour.windDegree.toFloat() - 180
            Glide
                .with(itemView.context)
                .load(resultMap["hIcon"])
                .into(hIcon)
        }

    }
}