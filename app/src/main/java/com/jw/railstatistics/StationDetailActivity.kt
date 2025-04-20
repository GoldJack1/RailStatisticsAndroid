package com.jw.railstatistics

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jw.railstatistics.data.Station
import com.jw.railstatistics.databinding.ActivityStationDetailBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StationDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStationDetailBinding
    private lateinit var currentStation: Station
    private lateinit var stations: MutableList<Station>
    private var isEditMode = false
    private var selectedVisitDate: Long? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
        
        // Set system bar appearance
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false  // Changed to false to match dark navigation bar style
            // Don't hide navigation bar when touched

        }

        binding = ActivityStationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        // Set up back button click listener
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set up edit button click listener
        binding.editButton.setOnClickListener {
            if (isEditMode) {
                saveChanges()
            } else {
                isEditMode = true
                updateEditMode()
                binding.editButtonIcon.setImageResource(R.drawable.ic_save)
            }
        }

        // Load stations from disk
        loadStationsFromDisk()

        // Get station ID using the correct key
        val stationId = intent.getLongExtra("stationId", -1L)
        Log.d("StationDetail", "Looking for station with ID: $stationId")
        
        // Add detailed logging of all stations
        Log.d("StationDetail", "All stations in list:")
        stations.forEach { station ->
            Log.d("StationDetail", "Station ID: ${station.id}, Name: ${station.name}")
        }
        
        if (stationId == -1L) {
            Log.e("StationDetail", "No station ID provided in intent")
            Toast.makeText(this, "Error: No station ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Find station in list
        Log.d("StationDetail", "Loaded ${stations.size} stations from disk")
        currentStation = stations.find { it.id == stationId } ?: run {
            Log.e("StationDetail", "Could not find station with ID: $stationId")
            Toast.makeText(this, "Error: Station not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d("StationDetail", "Found station: ${currentStation.name}")

        setupWindowInsets()
        setupSpinner()
        displayStationDetails()
        setupClickListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply padding to the header container for status bar
            binding.headerContainer.updatePadding(
                top = systemBars.top + resources.getDimensionPixelSize(R.dimen.screen_padding)
            )
            
            // Apply padding to the ScrollView content but allow drawing behind navigation bar
            binding.root.findViewById<ScrollView>(R.id.scrollView).updatePadding(
                bottom = resources.getDimensionPixelSize(R.dimen.screen_padding)
            )
            
            // Return the WindowInsets so that they can be consumed by child views
            windowInsets
        }
    }

    private fun setupClickListeners() {
        binding.editVisitDate.setOnClickListener {
            showDatePicker()
        }

        binding.ivFavourite.setOnClickListener {
            currentStation = currentStation.copy(favorite = !currentStation.favorite)
            updateFavoriteButtonState()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.visit_status_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVisitStatus.adapter = adapter

        binding.spinnerVisitStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isEditMode) {
                    val newStatus = adapter.getItem(position)?.toString() ?: return
                    currentStation = currentStation.copy(visitStatus = newStatus)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        selectedVisitDate?.let { timestamp -> calendar.timeInMillis = timestamp }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select visit date")
            .setSelection(calendar.timeInMillis)
            .build()

        datePicker.addOnPositiveButtonClickListener { timestamp ->
            calendar.timeInMillis = timestamp
            selectedVisitDate = calendar.timeInMillis
            val formattedDate = dateFormat.format(Date(selectedVisitDate!!))
            binding.editVisitDate.setText(formattedDate)
            currentStation = currentStation.copy(visitedDate = formattedDate)
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun displayStationDetails() {
        with(binding) {
            tvStationName.text = currentStation.name
            editStationName.setText(currentStation.name)
            
            tvCountry.text = currentStation.country
            editCountry.setText(currentStation.country)
            
            tvCounty.text = currentStation.county
            editCounty.setText(currentStation.county)
            
            tvTOC.text = currentStation.trainOperator
            editTOC.setText(currentStation.trainOperator)
            
            tvLatitude.text = currentStation.latitude
            editLatitude.setText(currentStation.latitude)
            
            tvLongitude.text = currentStation.longitude
            editLongitude.setText(currentStation.longitude)
            
            tvVisitDate.text = currentStation.visitedDate
            editVisitDate.setText(currentStation.visitedDate)
            
            tvVisitStatus.text = currentStation.visitStatus
            
            val statusPosition = resources.getStringArray(R.array.visit_status_options)
                .indexOf(currentStation.visitStatus)
            if (statusPosition != -1) {
                spinnerVisitStatus.setSelection(statusPosition)
            }
            
            // Display usage data
            displayUsageData()
            
            updateEditMode()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayUsageData() {
        // Clear existing views
        binding.layoutUsageData.removeAllViews()
        binding.layoutUsageDataEdit.removeAllViews()

        // Create a list of years from 2024 down to 1998
        val years = (2024 downTo 1998).toList()
        
        // Create views for display mode
        for (year in years) {
            val usage = currentStation.yearlyUsage[year] ?: ""
            
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = resources.getDimensionPixelSize(R.dimen.margin_small)
                }
                text = "$year: $usage"
                textSize = 16f
            }
            binding.layoutUsageData.addView(textView)
        }

        // Create edit fields for edit mode
        for (year in years) {
            val usage = currentStation.yearlyUsage[year] ?: ""
            
            val rowLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = resources.getDimensionPixelSize(R.dimen.margin_small)
                }
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
            }

            // Year display
            val yearTextView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.5f
                )
                text = year.toString()
                textSize = 16f
            }

            // Usage input
            val usageLayout = TextInputLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.5f
                )
                hint = getString(R.string.usage)
            }

            val usageEditText = TextInputEditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setText(usage)
                tag = year // Store year as tag for later retrieval
                inputType = InputType.TYPE_CLASS_NUMBER
            }

            usageLayout.addView(usageEditText)
            rowLayout.addView(yearTextView)
            rowLayout.addView(usageLayout)
            binding.layoutUsageDataEdit.addView(rowLayout)
        }
    }

    private fun updateEditMode() {
        with(binding) {
            // Toggle visibility of edit fields
            tvStationName.isVisible = !isEditMode
            inputLayoutStationName.isVisible = isEditMode
            
            tvCountry.isVisible = !isEditMode
            inputLayoutCountry.isVisible = isEditMode
            
            tvCounty.isVisible = !isEditMode
            inputLayoutCounty.isVisible = isEditMode
            
            tvTOC.isVisible = !isEditMode
            inputLayoutTOC.isVisible = isEditMode
            
            tvLatitude.isVisible = !isEditMode
            inputLayoutLatitude.isVisible = isEditMode
            
            tvLongitude.isVisible = !isEditMode
            inputLayoutLongitude.isVisible = isEditMode
            
            tvVisitDate.isVisible = !isEditMode
            inputLayoutVisitDate.isVisible = isEditMode
            
            tvVisitStatus.isVisible = !isEditMode
            spinnerVisitStatus.isVisible = isEditMode

            // Toggle usage data views
            layoutUsageData.isVisible = !isEditMode
            layoutUsageDataEdit.isVisible = isEditMode

            // Update edit button icon
            editButtonIcon.setImageResource(
                if (isEditMode) R.drawable.ic_save else R.drawable.ic_pencil
            )
        }
    }

    private fun updateFavoriteButtonState() {
        binding.ivFavourite.setImageResource(
            if (currentStation.favorite) R.drawable.ic_favorite_filled 
            else R.drawable.ic_favorite_outline
        )
    }

    private fun loadStationsFromDisk() {
        try {
            openFileInput("stations.json").use { input ->
                val json = input.bufferedReader().use { it.readText() }
                val type = object : TypeToken<MutableList<Station>>() {}.type
                stations = Gson().fromJson(json, type) ?: mutableListOf()
            }
        } catch (e: Exception) {
            // File might not exist yet, that's okay
            e.printStackTrace()
            stations = mutableListOf()
            Toast.makeText(this, "Error loading stations: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStationsToDisk() {
        try {
            val json = Gson().toJson(stations)
            openFileOutput("stations.json", MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving stations: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveChanges() {
        // Validate required fields
        if (binding.editStationName.text.toString().trim().isEmpty()) {
            binding.inputLayoutStationName.error = "Station name is required"
            return
        }

        // Start with existing usage data
        val updatedUsageData = currentStation.yearlyUsage.toMutableMap()
        Log.d("StationDetail", "Starting with existing usage data: $updatedUsageData")
        Log.d("StationDetail", "Number of usage data edit fields: ${binding.layoutUsageDataEdit.childCount}")
        
        // Process all visible edit fields
        for (i in 0 until binding.layoutUsageDataEdit.childCount) {
            val rowLayout = binding.layoutUsageDataEdit.getChildAt(i) as? LinearLayout
            if (rowLayout == null) {
                Log.e("StationDetail", "Row $i is not a LinearLayout")
                continue
            }
            
            Log.d("StationDetail", "Processing row $i with ${rowLayout.childCount} children")
            
            if (rowLayout.childCount != 2) {
                Log.e("StationDetail", "Row $i has unexpected number of children: ${rowLayout.childCount}")
                continue
            }
            
            val yearView = rowLayout.getChildAt(0) as? TextView
            if (yearView == null) {
                Log.e("StationDetail", "Year view in row $i is not a TextView")
                continue
            }
            
            val usageLayout = rowLayout.getChildAt(1) as? TextInputLayout
            if (usageLayout == null) {
                Log.e("StationDetail", "Usage layout in row $i is not a TextInputLayout")
                continue
            }
            
            // Get the EditText from the TextInputLayout using the correct method
            val usageEditText = usageLayout.editText
            if (usageEditText == null) {
                Log.e("StationDetail", "Could not get EditText from TextInputLayout in row $i")
                continue
            }
            
            // Get the year from the TextView instead of the tag
            val year = yearView.text.toString().toIntOrNull()
            if (year == null) {
                Log.e("StationDetail", "Could not parse year from TextView in row $i")
                continue
            }
            
            val usage = usageEditText.text?.toString()?.trim() ?: ""
            
            Log.d("StationDetail", "Processing year $year with usage: '$usage'")
            
            // Update or remove the usage data based on the input
            if (usage.isNotEmpty()) {
                updatedUsageData[year] = usage
                Log.d("StationDetail", "Added/Updated usage for year $year: $usage")
            } else {
                // Remove the year if the field was explicitly cleared
                updatedUsageData.remove(year)
                Log.d("StationDetail", "Removed usage for year $year (field was cleared)")
            }
        }

        Log.d("StationDetail", "Final usage data before saving: $updatedUsageData")

        // Create a separate updated station to avoid reference issues
        val updatedStation = currentStation.copy(
            name = binding.editStationName.text.toString().trim(),
            country = binding.editCountry.text.toString().trim(),
            county = binding.editCounty.text.toString().trim(),
            trainOperator = binding.editTOC.text.toString().trim(),
            latitude = binding.editLatitude.text.toString().trim(),
            longitude = binding.editLongitude.text.toString().trim(),
            visitedDate = binding.editVisitDate.text.toString().trim(),
            visitStatus = binding.spinnerVisitStatus.selectedItem.toString(),
            yearlyUsage = updatedUsageData
        )

        Log.d("StationDetail", "Previous yearly usage: ${currentStation.yearlyUsage}")
        Log.d("StationDetail", "Updated yearly usage: ${updatedStation.yearlyUsage}")
        
        // Update station in the list
        val index = stations.indexOfFirst { it.id == currentStation.id }
        if (index != -1) {
            // Update currentStation first
            currentStation = updatedStation
            stations[index] = currentStation
            
            // Save changes to disk
            stations.sortBy { it.name } // Sort stations by name before saving
            saveStationsToDisk()
            
            // Exit edit mode and refresh display
            isEditMode = false
            updateEditMode()
            displayStationDetails()
            
            Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error saving changes", Toast.LENGTH_SHORT).show()
        }
    }
}