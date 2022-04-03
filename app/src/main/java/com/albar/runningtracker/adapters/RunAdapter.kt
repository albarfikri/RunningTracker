package com.albar.runningtracker.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.albar.runningtracker.databinding.ItemRunBinding
import com.albar.runningtracker.db.Run
import com.albar.runningtracker.other.TrackingUtility
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    // do it asynchronously
    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val binding = ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RunViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.bind(run as Run)
    }


    inner class RunViewHolder(private val binding: ItemRunBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(run: Run) {
            binding.ivRunImage.setImageBitmap(run.img)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timeStamp
            }

            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

            binding.tvDate.text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKMH} km/h"
            binding.tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${run.distanceInMeters / 1000f} km"
            binding.tvDistance.text = distanceInKm

            binding.tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned} kcal"
            binding.tvCalories.text = caloriesBurned

        }
    }
}