package com.jw.railstatistics

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class StationDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_detail)

        // Retrieve the passed Station object
        val station = intent.getSerializableExtra("station") as? Station

        // Setup Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.detailToolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.menu_edit) {
                Toast.makeText(this, "Edit clicked", Toast.LENGTH_SHORT).show()
                true
            } else false
        }

        station?.let {
            // Basic Information
            findViewById<TextView>(R.id.tvStationName).text = it.name
            findViewById<TextView>(R.id.tvCountry).text = it.county
            findViewById<TextView>(R.id.tvCounty).text = it.trainOperator
            findViewById<TextView>(R.id.tvTOC).text = it.visitedDate

            // Coordinates (Longitude: Column 8, Latitude: Column 9)
            findViewById<TextView>(R.id.tvLatitude).text = "Latitude: ${it.extraData.getOrNull(2) ?: "N/A"}"
            findViewById<TextView>(R.id.tvLongitude).text = "Longitude: ${it.extraData.getOrNull(3) ?: "N/A"}"

            // Visit Status and Favorite (Columns 5-7)
            findViewById<TextView>(R.id.tvVisitStatus).text = "Visited: ${it.visitStatus}"
            findViewById<TextView>(R.id.tvVisitDate).text = "Visit Date: ${it.extraData.getOrNull(0) ?: "N/A"}"

            val favouriteStatus = it.extraData.getOrNull(1)?.lowercase() == "yes"
            val ivFavourite = findViewById<ImageView>(R.id.ivFavourite)
            ivFavourite.setImageResource(
                if (favouriteStatus) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )

            // Populate Usage Data (Columns 10-36, Years 2024 to 1997)
            val usageLayout = findViewById<LinearLayout>(R.id.layoutUsageData)
            usageLayout.removeAllViews()

            val years = (2024 downTo 1997).toList()
            years.forEachIndexed { index, year ->
                val usageTextView = TextView(this).apply {
                    val usageValue = it.extraData.getOrNull(index + 4) ?: "N/A"
                    text = "$year: $usageValue"
                    textSize = 16f
                    setPadding(0, 4, 0, 4)
                }
                usageLayout.addView(usageTextView)
            }
        } ?: run {
            Toast.makeText(this, "No station data available.", Toast.LENGTH_SHORT).show()
        }
    }
}