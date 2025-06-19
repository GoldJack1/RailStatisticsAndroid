package com.jw.railstatistics

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.widget.Button
import android.widget.ScrollView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make the app draw under system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Make system bars transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        setContentView(R.layout.activity_main)

        // Set up window insets
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply padding to account for system bars
            view.updatePadding(
                top = insets.top,
                bottom = insets.bottom
            )
            
            windowInsets
        }

        val btnTicketTracker = findViewById<Button>(R.id.btnTicketTracker)
        val btnNationalRail = findViewById<Button>(R.id.btnNationalRail)

        // Navigate to Ticket Tracking Activity
        btnTicketTracker.setOnClickListener {
            val intent = Intent(this, TicketTrackingActivity::class.java)
            startActivity(intent)
        }

        btnNationalRail.setOnClickListener {
            val intent = Intent(this, NationalRailActivity::class.java)
            startActivity(intent)
        }
    }
}