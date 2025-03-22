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
import com.google.android.material.appbar.MaterialToolbar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.opencsv.CSVReader
import java.io.BufferedReader
import java.io.InputStreamReader

class NationalRailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StationAdapter
    private val stationList = mutableListOf<Station>()

    private lateinit var csvLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_national_rail)

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

        // Setup toolbar and handle clicks
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_settings) {
                // Use the toolbar's view for the anchor
                showSettingsDropdown(toolbar.findViewById(R.id.action_settings))
                true
            } else {
                false
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