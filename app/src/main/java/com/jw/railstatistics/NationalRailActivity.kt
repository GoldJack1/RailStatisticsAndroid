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

class NationalRailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StationAdapter
    private val stationList = mutableListOf<Station>()

    private lateinit var csvLauncher: ActivityResultLauncher<Intent>

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
        adapter = StationAdapter(stationList) { station ->
            val intent = Intent(this, StationDetailActivity::class.java)
            intent.putExtra("station", station)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load previously saved stations from disk
        loadStationsFromDisk()

        // Register CSV import result handler
        csvLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    parseCSV(uri)
                }
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
                    Toast.makeText(this, "Import Template selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_export -> {
                    Toast.makeText(this, "Export selected", Toast.LENGTH_SHORT).show()
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
            // Set the type to "*/*" to show all files
            type = "*/*"
            // Allow CSV files by including several common CSV MIME types
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("text/csv", "application/vnd.ms-excel", "text/plain", "text/comma-separated-values")
            )
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        csvLauncher.launch(Intent.createChooser(intent, "Select CSV file"))
    }

    private fun parseCSV(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                // Use CSVReader from OpenCSV to handle CSV parsing robustly.
                CSVReader(InputStreamReader(inputStream)).use { reader ->
                    var isFirstLine = true
                    reader.forEach { tokens ->
                        if (isFirstLine) {
                            // Skip header row if needed.
                            isFirstLine = false
                            return@forEach
                        }
                        if (tokens.size >= 5) {
                            val station = Station(
                                name = tokens[0].trim(),
                                county = tokens[1].trim(),
                                trainOperator = tokens[2].trim(),
                                visitedDate = tokens[3].trim(),
                                visitStatus = tokens[4].trim(),
                                extraData = tokens.drop(5)
                            )
                            stationList.add(station)
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "CSV imported successfully", Toast.LENGTH_SHORT).show()
            // Save the imported data to disk
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
}