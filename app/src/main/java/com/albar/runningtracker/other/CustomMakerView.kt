package com.albar.runningtracker.other

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.albar.runningtracker.R
import com.albar.runningtracker.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

// check our list of run
@SuppressLint("ViewConstructor")
class CustomMakerView(
    private val runs: List<Run>,
    c: Context,
    layoutId: Int,
    ) : MarkerView(c, layoutId) {

    // MPPointF point of x and y
    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }

        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }

        val tvDateMarker = findViewById<TextView>(R.id.tvDateMarker)
        val tvAvgSpeedMarker = findViewById<TextView>(R.id.tvAvgSpeedMarker)
        val tvDistanceMarker = findViewById<TextView>(R.id.tvDistanceMarker)
        val tvDurationMarker = findViewById<TextView>(R.id.tvDurationMarker)
        val tvCaloriesBurnedMarker = findViewById<TextView>(R.id.tvCaloriesBurnedMarker)


        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDateMarker.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKMH} km/h"
        tvAvgSpeedMarker.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f} km"
        tvDistanceMarker.text = distanceInKm

        tvDurationMarker.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurned} kcal"
        tvCaloriesBurnedMarker.text = caloriesBurned
    }
}