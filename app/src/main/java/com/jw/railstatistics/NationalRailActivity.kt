package com.jw.railstatistics

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jw.railstatistics.data.Station
import com.jw.railstatistics.ui.AddStationBottomSheetFragment
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.InputStreamReader

class NationalRailActivity : AppCompatActivity(), AddStationBottomSheetFragment.StationAddedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StationAdapter
    private val stationList = mutableListOf<Station>()

    private lateinit var csvImportLauncher: ActivityResultLauncher<Intent>
    private lateinit var csvExportLauncher: ActivityResultLauncher<String>

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
        
        setContentView(R.layout.activity_national_rail)

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.titleRow)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top)
            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerViewStations)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = insets.bottom)
            windowInsets
        }

        // Set up click listeners for header buttons
        findViewById<MaterialCardView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<MaterialCardView>(R.id.statsButton).setOnClickListener {
            // Handle stats click
            Toast.makeText(this, "Stats clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.filterButton).setOnClickListener {
            // Handle filter click
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.settingsButton).setOnClickListener {
            showSettingsDropdown(it)
        }

        // Set up search bar click
        findViewById<MaterialCardView>(R.id.searchBar).setOnClickListener {
            // Handle search click - you might want to expand this into a full search interface
            Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show()
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewStations)
        adapter = StationAdapter(
            stationList,
            onItemClick = { station ->
                Log.d("NationalRail", "Opening detail view for station: ${station.name} with ID: ${station.id}")
                val intent = Intent(this, StationDetailActivity::class.java).apply {
                    putExtra("stationId", station.id)
                }
                startActivity(intent)
            },
            onUpdateStatusClick = { station ->
                toggleStationStatus(station)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load previously saved stations from disk
        loadStationsFromDisk()

        // Register CSV import result handler
        csvImportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    parseCSV(uri)
                }
            }
        }

        // Register CSV export result handler
        csvExportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri != null) {
                exportToCSV(uri)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the toolbar menu (which should only contain the settings icon)
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onStationAdded(station: Station) {
        // Check for duplicates
        if (stationList.any { it.name.equals(station.name, ignoreCase = true) }) {
            Toast.makeText(this, "A station with this name already exists", Toast.LENGTH_SHORT).show()
            return
        }
        
        stationList.add(station)
        sortStationList()
        adapter.notifyDataSetChanged() // Use notifyDataSetChanged since the position might have changed due to sorting
        saveStationsToDisk()
    }

    private fun sortStationList() {
        stationList.sortBy { it.name.lowercase() } // Sort case-insensitive
    }

    private fun showSettingsDropdown(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_settings, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_add_station -> {
                    val addStationFragment = AddStationBottomSheetFragment.newInstance()
                    addStationFragment.stationAddedListener = this
                    addStationFragment.show(supportFragmentManager, AddStationBottomSheetFragment.TAG)
                    true
                }
                R.id.menu_import -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        type = "*/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                            "text/csv",
                            "application/vnd.ms-excel",
                            "text/plain",
                            "text/comma-separated-values"
                        ))
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    csvImportLauncher.launch(intent)
                    true
                }
                R.id.menu_export -> {
                    val timestamp = java.text.SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.UK)
                        .format(java.util.Date())
                    csvExportLauncher.launch("stations_$timestamp.csv")
                    true
                }
                R.id.menu_import_template -> {
                    importTemplate()
                    true
                }
                R.id.menu_clear_data -> {
                    showClearDataConfirmation()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showClearDataConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("Are you sure you want to clear all station data? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllData() {
        // Clear the list
        stationList.clear()
        adapter.notifyDataSetChanged()

        // Clear the saved data
        try {
            openFileOutput("stations.json", MODE_PRIVATE).use { output ->
                output.write("[]".toByteArray())
            }
            Toast.makeText(this, "All data cleared successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to clear data", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper method to parse CSV data into Station objects
    private fun parseCSVData(reader: com.opencsv.CSVReader): List<Station> {
        // Skip header row
        reader.readNext()
        
        return reader.readAll().mapNotNull { line ->
            if (line.size >= 9) { // Minimum columns needed
                // Create yearlyUsage map for years 2024-1998 (columns 9-35)
                val yearlyUsage = mutableMapOf<Int, String>()
                var year = 2024
                for (i in 9..35) {
                    val usage = line.getOrNull(i)?.trim() ?: ""
                    yearlyUsage[year] = usage
                    year--
                }
                
                // Normalize visit status to "Visited" or "Not Visited"
                val rawVisitStatus = line[4].trim().lowercase()
                val visitStatus = when {
                    rawVisitStatus == "yes" || rawVisitStatus == "visited" -> "Visited"
                    else -> "Not Visited"
                }
                
                Station(
                    name = line[0].trim(),
                    country = line[1].trim(),
                    county = line[2].trim(),
                    trainOperator = line[3].trim(),
                    visitStatus = visitStatus,
                    visitedDate = line[5].trim(),
                    favorite = line[6].trim().equals("yes", ignoreCase = true),
                    latitude = line[7].trim(),
                    longitude = line[8].trim(),
                    yearlyUsage = yearlyUsage
                )
            } else null
        }
    }

    private fun parseCSV(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val parser = CSVParserBuilder()
                    .withSeparator(',')
                    .withIgnoreQuotations(false)
                    .build()
                
                val reader = CSVReaderBuilder(InputStreamReader(inputStream))
                    .withCSVParser(parser)
                    .build()
                
                val newStations = parseCSVData(reader)
                
                // Check for duplicates
                val duplicates = newStations.filter { newStation -> 
                    stationList.any { it.name.equals(newStation.name, ignoreCase = true) }
                }
                
                if (duplicates.isNotEmpty()) {
                    val message = if (duplicates.size == 1) {
                        "Station '${duplicates[0].name}' already exists and will be skipped"
                    } else {
                        "${duplicates.size} stations already exist and will be skipped"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
                
                // Add only non-duplicate stations
                val uniqueStations = newStations.filterNot { newStation -> 
                    stationList.any { it.name.equals(newStation.name, ignoreCase = true) }
                }
                
                if (uniqueStations.isNotEmpty()) {
                    stationList.addAll(uniqueStations)
                    sortStationList() // Sort after adding new stations
                    adapter.notifyDataSetChanged() // Use notifyDataSetChanged since positions might have changed
                    saveStationsToDisk()
                    Toast.makeText(this, "${uniqueStations.size} stations imported", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No new stations to import", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error reading CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveStationsToDisk() {
        try {
            val json = Gson().toJson(stationList)
            openFileOutput("stations.json", MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving stations: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadStationsFromDisk() {
        try {
            openFileInput("stations.json").use { input ->
                val json = input.bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<Station>>() {}.type
                val loadedStations = Gson().fromJson<List<Station>>(json, type) ?: emptyList()
                stationList.clear()
                stationList.addAll(loadedStations)
                sortStationList() // Sort when loading stations
                adapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            // File might not exist yet, that's okay
            e.printStackTrace()
        }
    }

    private fun toggleStationStatus(station: Station) {
        // Find the station in the list
        val index = stationList.indexOfFirst { it.name == station.name }
        if (index != -1) {
            // Toggle between Visited and Not Visited
            val newStatus = if (station.visitStatus == "Visited") "Not Visited" else "Visited"
            
            val updatedStation = station.copy(
                visitStatus = newStatus,
                visitedDate = if (newStatus == "Visited") {
                    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.UK)
                        .format(java.util.Date())
                } else ""
            )
            stationList[index] = updatedStation
            adapter.notifyItemChanged(index)
            saveStationsToDisk()
        }
    }

    private fun exportCSV() {
        val timestamp = java.text.SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.UK).format(java.util.Date())
        val filename = "stations_$timestamp.csv"
        csvExportLauncher.launch(filename)
    }

    private fun exportToCSV(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = outputStream.bufferedWriter()
                
                // Write header
                writer.write("Station Name,Country,County,Operator,Visited,Visit Date,Favorite,Latitude,Longitude")
                // Add year headers from 2024 to 1998
                (2024 downTo 1998).forEach { year ->
                    writer.write(",$year")
                }
                writer.newLine()

                // Write station data
                stationList.forEach { station ->
                    val line = buildString {
                        // Basic info (escape commas in fields)
                        append(escapeCsvField(station.name)).append(",")
                        append(escapeCsvField(station.country)).append(",")
                        append(escapeCsvField(station.county)).append(",")
                        append(escapeCsvField(station.trainOperator)).append(",")
                        append(if (station.visitStatus == "Visited") "Yes" else "No").append(",")
                        append(escapeCsvField(station.visitedDate)).append(",")
                        append(if (station.favorite) "Yes" else "No").append(",")
                        append(escapeCsvField(station.latitude)).append(",")
                        append(escapeCsvField(station.longitude))

                        // Usage data
                        (2024 downTo 1998).forEach { year ->
                            append(",")
                            append(escapeCsvField(station.yearlyUsage[year] ?: ""))
                        }
                    }
                    writer.write(line)
                    writer.newLine()
                }
                writer.flush()
            }
            Toast.makeText(this, "CSV exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to export CSV", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    private fun importTemplate() {
        try {
            val oldSize = stationList.size
            stationList.clear()
            adapter.notifyItemRangeRemoved(0, oldSize)
            
            assets.open("template_stations.csv").use { inputStream ->
                val parser = CSVParserBuilder()
                    .withSeparator(',')
                    .withIgnoreQuotations(false)
                    .build()
                
                val reader = CSVReaderBuilder(InputStreamReader(inputStream))
                    .withCSVParser(parser)
                    .build()
                
                val newStations = parseCSVData(reader)
                stationList.addAll(newStations)
                sortStationList() // Sort after importing template
                adapter.notifyDataSetChanged()
            }
            
            saveStationsToDisk()
            Toast.makeText(this, "Template imported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to import template", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadStationsFromDisk() // Refresh the list when returning from other activities
    }

    override fun onDestroy() {
        super.onDestroy()
        saveStationsToDisk()
    }
}