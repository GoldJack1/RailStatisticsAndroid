package com.jw.railstatistics

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StationDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_detail)

        val station = intent.getSerializableExtra("station") as? Station

        val tvDetail = findViewById<TextView>(R.id.tvStationDetail)
        station?.let {
            // Build a detailed string from all station data
            val details = StringBuilder()
            details.append("Station Name: ${it.name}\n")
            details.append("County: ${it.county}\n")
            details.append("Train Operator: ${it.trainOperator}\n")
            details.append("Visited Date: ${it.visitedDate}\n")
            details.append("Visit Status: ${it.visitStatus}\n")
            if (it.extraData.isNotEmpty()) {
                details.append("\nOther Data:\n")
                it.extraData.forEachIndexed { index, value ->
                    details.append("Column ${index + 6}: $value\n")
                }
            }
            tvDetail.text = details.toString()
        } ?: run {
            tvDetail.text = "No station details available."
        }
    }
}