package com.jw.railstatistics

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.opencsv.CSVReader
import java.io.BufferedReader
import java.io.InputStreamReader
import android.os.Build
import android.graphics.Color
import android.view.WindowManager
import com.google.android.material.card.MaterialCardView
import androidx.core.view.updateLayoutParams
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

class NationalRailActivity : AppCompatActivity() {

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
        val headerContainer = findViewById<LinearLayout>(R.id.headerContainer)
        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)
        
        ViewCompat.setOnApplyWindowInsetsListener(headerContainer) { view, windowInsets ->
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            // Update header padding to account for status bar
            view.updatePadding(
                top = statusBars.top + resources.getDimensionPixelSize(R.dimen.header_top_padding)
            )
            
            // Update content padding to account for navigation bar
            contentContainer.updatePadding(bottom = navigationBars.bottom)
            
            windowInsets
        }

        // Set up click listeners for header buttons
        findViewById<MaterialCardView>(R.id.backButton).setOnClickListener {
            onBackPressed()
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
                val intent = Intent(this, StationDetailActivity::class.java)
                intent.putExtra("station", station)
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

    private fun showSettingsDropdown(anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.menu_settings, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_add_station -> {
                    Toast.makeText(this, "Add Station selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_import -> {
                    importCSV()
                    true
                }
                R.id.menu_import_template -> {
                    importTemplate()
                    true
                }
                R.id.menu_export -> {
                    exportCSV()
                    true
                }
                R.id.menu_clear_data -> {
                    stationList.clear()
                    adapter.notifyDataSetChanged()
                    clearDataFromDisk()
                    Toast.makeText(this, "Data cleared", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun importCSV() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("text/csv", "application/vnd.ms-excel", "text/plain", "text/comma-separated-values")
            )
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        csvImportLauncher.launch(Intent.createChooser(intent, "Select CSV file"))
    }

    private fun parseCSV(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                CSVReader(InputStreamReader(inputStream)).use { reader ->
                    var isFirstLine = true
                    
                    reader.forEach { tokens ->
                        if (isFirstLine) {
                            isFirstLine = false
                            return@forEach
                        }
                        
                        if (tokens.size >= 9) { // Minimum columns needed
                            // Create yearlyUsage map for years 2024-1998 (columns 9-35)
                            val yearlyUsage = mutableMapOf<Int, String>()
                            var year = 2024
                            for (i in 9..35) {
                                val usage = tokens.getOrNull(i)?.trim() ?: ""
                                yearlyUsage[year] = usage
                                year--
                            }
                            
                            // Normalize visit status to "Visited" or "Not Visited"
                            val rawVisitStatus = tokens[4].trim().lowercase()
                            val visitStatus = when {
                                rawVisitStatus == "yes" || rawVisitStatus == "visited" -> "Visited"
                                else -> "Not Visited"
                            }
                            
                            val station = Station(
                                name = tokens[0].trim(),
                                country = tokens[1].trim(),
                                county = tokens[2].trim(),
                                trainOperator = tokens[3].trim(),
                                visitStatus = visitStatus,
                                visitedDate = tokens[5].trim(),
                                favorite = tokens[6].trim().equals("yes", ignoreCase = true),
                                latitude = tokens[7].trim(),
                                longitude = tokens[8].trim(),
                                yearlyUsage = yearlyUsage
                            )
                            stationList.add(station)
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "CSV imported successfully", Toast.LENGTH_SHORT).show()
            saveStationsToDisk()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to import CSV", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStationsToDisk() {
        try {
            val gson = Gson()
            val json = gson.toJson(stationList)
            openFileOutput("stations.json", MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
            Toast.makeText(this, "Data saved to disk", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearDataFromDisk() {
        if (deleteFile("stations.json")) {
            Toast.makeText(this, "Data cleared from disk", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to clear data from disk", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadStationsFromDisk() {
        try {
            val fileInput = openFileInput("stations.json")
            val json = fileInput.bufferedReader().use { it.readText() }
            fileInput.close()
            val gson = Gson()
            val type = object : TypeToken<List<Station>>() {}.type
            val savedStations: List<Station> = gson.fromJson(json, type)
            stationList.clear()
            stationList.addAll(savedStations)
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            // It's fine if the file doesn't exist yet
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
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.UK).format(java.util.Date())
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
            // Clear existing data
            stationList.clear()
            
            // Read and parse the template file
            assets.open("template_stations.csv").use { inputStream ->
                CSVReader(InputStreamReader(inputStream)).use { reader ->
                    var isFirstLine = true
                    
                    reader.forEach { tokens ->
                        if (isFirstLine) {
                            isFirstLine = false
                            return@forEach
                        }
                        
                        if (tokens.size >= 9) { // Minimum columns needed
                            // Create yearlyUsage map for years 2024-1998 (columns 9-35)
                            val yearlyUsage = mutableMapOf<Int, String>()
                            var year = 2024
                            for (i in 9..35) {
                                val usage = tokens.getOrNull(i)?.trim() ?: ""
                                yearlyUsage[year] = usage
                                year--
                            }
                            
                            // Normalize visit status to "Visited" or "Not Visited"
                            val rawVisitStatus = tokens[4].trim().lowercase()
                            val visitStatus = when {
                                rawVisitStatus == "yes" || rawVisitStatus == "visited" -> "Visited"
                                else -> "Not Visited"
                            }
                            
                            val station = Station(
                                name = tokens[0].trim(),
                                country = tokens[1].trim(),
                                county = tokens[2].trim(),
                                trainOperator = tokens[3].trim(),
                                visitStatus = visitStatus,
                                visitedDate = tokens[5].trim(),
                                favorite = tokens[6].trim().equals("yes", ignoreCase = true),
                                latitude = tokens[7].trim(),
                                longitude = tokens[8].trim(),
                                yearlyUsage = yearlyUsage
                            )
                            stationList.add(station)
                        }
                    }
                }
            }
            
            adapter.notifyDataSetChanged()
            saveStationsToDisk()
            Toast.makeText(this, "Template imported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to import template", Toast.LENGTH_SHORT).show()
        }
    }
}