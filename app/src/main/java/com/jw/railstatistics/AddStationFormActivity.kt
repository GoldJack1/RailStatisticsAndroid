package com.jw.railstatistics

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileInputStream
import java.io.FileOutputStream
import com.jw.railstatistics.data.Station

class AddStationFormActivity : AppCompatActivity() {

    private lateinit var editTextStationName: EditText
    private lateinit var editTextCountry: EditText
    private lateinit var editTextCounty: EditText
    private lateinit var editTextTOC: EditText
    private lateinit var editTextLatitude: EditText
    private lateinit var editTextLongitude: EditText
    private lateinit var switchFavorite: Switch
    private lateinit var buttonAddStation: Button
    private val stationList = mutableListOf<Station>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_station_form)

        // Initialize views
        editTextStationName = findViewById(R.id.editTextStationName)
        editTextCountry = findViewById(R.id.editTextCountry)
        editTextCounty = findViewById(R.id.editTextCounty)
        editTextTOC = findViewById(R.id.editTextTOC)
        editTextLatitude = findViewById(R.id.editTextLatitude)
        editTextLongitude = findViewById(R.id.editTextLongitude)
        switchFavorite = findViewById(R.id.switchFavorite)
        buttonAddStation = findViewById(R.id.buttonAddStation)

        // Load existing stations
        loadStationsFromDisk()

        buttonAddStation.setOnClickListener {
            addStation()
        }
    }

    private fun loadStationsFromDisk() {
        try {
            openFileInput("stations.json").use { fileInput ->
                val json = fileInput.bufferedReader().readText()
                val type = object : TypeToken<List<Station>>() {}.type
                val savedStations: List<Station> = Gson().fromJson(json, type)
                stationList.addAll(savedStations ?: emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // It's fine if the file doesn't exist yet
        }
    }

    private fun saveStationsToDisk() {
        try {
            val json = Gson().toJson(stationList)
            openFileOutput("stations.json", MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save station", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addStation() {
        // Validate inputs
        val stationName = editTextStationName.text.toString().trim()
        val country = editTextCountry.text.toString().trim()
        val county = editTextCounty.text.toString().trim()
        val toc = editTextTOC.text.toString().trim()
        val latitude = editTextLatitude.text.toString().trim()
        val longitude = editTextLongitude.text.toString().trim()
        val isFavorite = switchFavorite.isChecked

        // Validate required fields
        when {
            stationName.isEmpty() -> {
                editTextStationName.error = "Station name is required"
                return
            }
            country.isEmpty() -> {
                editTextCountry.error = "Country is required"
                return
            }
            county.isEmpty() -> {
                editTextCounty.error = "County is required"
                return
            }
            toc.isEmpty() -> {
                editTextTOC.error = "Train Operating Company is required"
                return
            }
        }

        // Validate latitude
        if (latitude.isEmpty()) {
            editTextLatitude.error = "Latitude is required"
            return
        }
        try {
            latitude.toDouble().also {
                if (it < -90 || it > 90) {
                    editTextLatitude.error = "Latitude must be between -90 and 90"
                    return
                }
            }
        } catch (e: NumberFormatException) {
            editTextLatitude.error = "Invalid latitude format"
            return
        }

        // Validate longitude
        if (longitude.isEmpty()) {
            editTextLongitude.error = "Longitude is required"
            return
        }
        try {
            longitude.toDouble().also {
                if (it < -180 || it > 180) {
                    editTextLongitude.error = "Longitude must be between -180 and 180"
                    return
                }
            }
        } catch (e: NumberFormatException) {
            editTextLongitude.error = "Invalid longitude format"
            return
        }

        // Create yearlyUsage map (empty for new stations)
        val yearlyUsage = (2024 downTo 1998).associateWith { "" }

        // Create new station
        val newStation = Station(
            name = stationName,
            country = country,
            county = county,
            trainOperator = toc,
            visitStatus = "Not Visited", // Default to not visited
            visitedDate = "", // Empty visit date
            favorite = isFavorite,
            latitude = latitude,
            longitude = longitude,
            yearlyUsage = yearlyUsage
        )

        // Add to list and save
        stationList.add(newStation)
        saveStationsToDisk()

        Toast.makeText(this, "Station added successfully", Toast.LENGTH_SHORT).show()
        finish() // Return to previous screen
    }
} 