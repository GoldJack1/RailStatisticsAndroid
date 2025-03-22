package com.jw.railstatistics

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTicketTracker = findViewById<Button>(R.id.btnTicketTracker)
        val btnNationalRail = findViewById<Button>(R.id.btnNationalRail)

        // TODO: Implement Ticket Tracker functionality
        btnTicketTracker.setOnClickListener {
            // For now, simply show a placeholder
        }

        btnNationalRail.setOnClickListener {
            val intent = Intent(this, NationalRailActivity::class.java)
            startActivity(intent)
        }
    }
}