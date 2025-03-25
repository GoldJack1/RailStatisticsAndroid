package com.jw.railstatistics

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.os.Build
import android.graphics.Color
import android.view.WindowManager
import com.google.android.material.card.MaterialCardView

class StationDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make the app draw under system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Enable blur effect for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.attributes.blurBehindRadius = 32
            window.setFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            )
        }
        
        // Make system bars transparent
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        
        setContentView(R.layout.activity_station_detail)

        // Set up window insets
        val headerContainer = findViewById<LinearLayout>(R.id.headerContainer)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        
        ViewCompat.setOnApplyWindowInsetsListener(headerContainer) { view, windowInsets ->
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(
                top = statusBars.top + resources.getDimensionPixelSize(R.dimen.header_top_padding)
            )
            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, windowInsets ->
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navigationBars.bottom)
            windowInsets
        }

        // Set up button clicks
        findViewById<MaterialCardView>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        findViewById<MaterialCardView>(R.id.editButton).setOnClickListener {
            // Handle edit click
            Toast.makeText(this, "Edit clicked", Toast.LENGTH_SHORT).show()
        }

        // Retrieve the passed Station object
        val station = intent.getSerializableExtra("station") as? Station

        station?.let {
            // Basic Information
            findViewById<TextView>(R.id.tvStationName).text = it.name
            findViewById<TextView>(R.id.tvCountry).text = it.country
            findViewById<TextView>(R.id.tvCounty).text = it.county
            findViewById<TextView>(R.id.tvTOC).text = it.trainOperator

            // Coordinates
            findViewById<TextView>(R.id.tvLatitude).text = "Latitude: ${it.latitude}"
            findViewById<TextView>(R.id.tvLongitude).text = "Longitude: ${it.longitude}"

            // Visit Status and Favorite
            findViewById<TextView>(R.id.tvVisitStatus).text = "Visited: ${it.visitStatus}"
            findViewById<TextView>(R.id.tvVisitDate).text = "Visit Date: ${it.visitedDate}"

            val ivFavourite = findViewById<ImageView>(R.id.ivFavourite)
            ivFavourite.setImageResource(
                if (it.favorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )

            // Populate Usage Data
            val usageLayout = findViewById<LinearLayout>(R.id.layoutUsageData)
            usageLayout.removeAllViews()

            // Create a row for each year from 2024 to 1998
            (2024 downTo 1998).forEach { year ->
                val usageTextView = TextView(this).apply {
                    // Get the usage value for this year, or "N/A" if not found or empty
                    val usageValue = it.yearlyUsage[year]?.takeIf { it.isNotEmpty() } ?: "N/A"
                    text = String.format("%-6d %s", year, usageValue)
                    textSize = 16f
                    setPadding(0, 4, 0, 4)
                    // Use monospace font to align columns
                    typeface = android.graphics.Typeface.MONOSPACE
                }
                usageLayout.addView(usageTextView)
            }
        } ?: run {
            Toast.makeText(this, "No station data available.", Toast.LENGTH_SHORT).show()
        }
    }
}