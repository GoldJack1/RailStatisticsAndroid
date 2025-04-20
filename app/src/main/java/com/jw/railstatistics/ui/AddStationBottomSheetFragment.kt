package com.jw.railstatistics.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import com.jw.railstatistics.R
import com.jw.railstatistics.data.Station
import com.jw.railstatistics.databinding.FragmentAddStationBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddStationBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddStationBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var usageDataContainer: LinearLayout
    private lateinit var usageRowsContainer: LinearLayout
    private lateinit var toggleUsageButton: ImageButton
    private val usageInputs = mutableListOf<Pair<TextInputEditText, TextInputEditText>>()
    
    interface StationAddedListener {
        fun onStationAdded(station: Station)
    }

    var stationAddedListener: StationAddedListener? = null

    companion object {
        const val TAG = "AddStationBottomSheetFragment"
        
        fun newInstance() = AddStationBottomSheetFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddStationBottomSheetBinding.inflate(inflater, container, false)
        
        // Initialize usage data views
        usageDataContainer = binding.usageDataContainer
        usageRowsContainer = binding.usageRowsContainer
        toggleUsageButton = binding.toggleUsageButton
        
        setupUsageSection()
        setupButtons()
        
        return binding.root
    }

    private fun setupButtons() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.confirmButton.setOnClickListener {
            saveStation()
        }
    }

    private fun setupUsageSection() {
        // Set up toggle button click listener
        toggleUsageButton.setOnClickListener {
            val isExpanded = usageDataContainer.visibility == View.VISIBLE
            usageDataContainer.visibility = if (isExpanded) View.GONE else View.VISIBLE
            toggleUsageButton.setImageResource(
                if (isExpanded) R.drawable.ic_expand_more else R.drawable.ic_expand_less
            )
        }

        // Set up add row button
        binding.addUsageRowButton.setOnClickListener {
            addUsageRow()
        }

        // Add initial row
        addUsageRow()
    }

    private fun addUsageRow() {
        // Create horizontal layout for the row
        val rowLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.margin_small)
            }
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        // Create year input
        val yearLayout = TextInputLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.margin_small)
            }
            hint = getString(R.string.year)
            defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#BFBFBF"))
            boxStrokeColor = Color.parseColor("#BFBFBF")
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }

        val yearEditText = TextInputEditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                height = resources.getDimensionPixelSize(R.dimen.min_touch_target_size)
            }
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.WHITE)
            id = View.generateViewId()
        }

        // Create usage input
        val usageLayout = TextInputLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            hint = getString(R.string.usage)
            defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#BFBFBF"))
            boxStrokeColor = Color.parseColor("#BFBFBF")
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }

        val usageEditText = TextInputEditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                height = resources.getDimensionPixelSize(R.dimen.min_touch_target_size)
            }
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.WHITE)
            id = View.generateViewId()
        }

        yearLayout.addView(yearEditText)
        usageLayout.addView(usageEditText)
        rowLayout.addView(yearLayout)
        rowLayout.addView(usageLayout)
        usageRowsContainer.addView(rowLayout)

        usageInputs.add(Pair(yearEditText, usageEditText))
    }

    private fun saveStation() {
        val name = binding.editTextStationName.text?.toString()?.trim().orEmpty()
        val country = binding.editTextCountry.text?.toString()?.trim().orEmpty()
        val county = binding.editTextCounty.text?.toString()?.trim().orEmpty()
        val trainOperator = binding.editTextTOC.text?.toString()?.trim().orEmpty()
        val latitude = binding.editTextLatitude.text?.toString()?.trim().orEmpty()
        val longitude = binding.editTextLongitude.text?.toString()?.trim().orEmpty()
        val favorite = binding.switchFavorite.isChecked

        if (name.isEmpty() || country.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) {
            Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Collect usage data
        val yearlyUsage = mutableMapOf<Int, String>()
        for ((yearEdit, usageEdit) in usageInputs) {
            val year = yearEdit.text?.toString()?.toIntOrNull()
            val usage = usageEdit.text?.toString()?.trim() ?: ""
            if (year != null) {
                yearlyUsage[year] = usage
            }
        }

        val station = Station(
            name = name,
            country = country,
            county = county,
            trainOperator = trainOperator,
            visitStatus = "Not Visited",
            visitedDate = "",
            favorite = favorite,
            latitude = latitude,
            longitude = longitude,
            yearlyUsage = yearlyUsage
        )

        stationAddedListener?.onStationAdded(station)
        dismiss()
        Toast.makeText(context, "Station added successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 