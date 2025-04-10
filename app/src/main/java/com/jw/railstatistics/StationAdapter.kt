package com.jw.railstatistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import androidx.recyclerview.widget.RecyclerView
import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.Color

class StationAdapter(
    private val stations: List<Station>,
    private val onItemClick: (Station) -> Unit,
    private val onUpdateStatusClick: (Station) -> Unit
) : RecyclerView.Adapter<StationAdapter.StationViewHolder>() {

    inner class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardStation)
        val colorBackground: View = itemView.findViewById(R.id.colorBackground)
        val blurOverlay: View = itemView.findViewById(R.id.blurOverlay)
        val tvName: TextView = itemView.findViewById(R.id.tvStationName)
        val tvCounty: TextView = itemView.findViewById(R.id.tvCounty)
        val tvOperator: TextView = itemView.findViewById(R.id.tvTrainOperator)
        val tvVisitedDate: TextView = itemView.findViewById(R.id.tvVisitedDate)
        val tvVisitStatus: TextView = itemView.findViewById(R.id.tvVisitStatus)
        val btnUpdateStatus: LinearLayout = itemView.findViewById(R.id.btnUpdateStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_station, parent, false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = stations[position]
        
        // Set background color based on visit status
        val backgroundColor = if (station.visitStatus == "Visited") {
            ContextCompat.getColor(holder.itemView.context, R.color.visited_station)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.not_visited_station)
        }
        holder.colorBackground.setBackgroundColor(backgroundColor)

        // Set glass overlay color based on visit status and night mode
        val isNightMode = holder.itemView.context.resources.configuration.uiMode and 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        val overlayColor = if (isNightMode) {
            if (station.visitStatus == "Visited") {
                Color.argb(26, 15, 75, 35) // Dark green overlay
            } else {
                Color.argb(26, 85, 25, 25) // Dark red overlay
            }
        } else {
            if (station.visitStatus == "Visited") {
                Color.argb(26, 52, 199, 89) // Light green overlay
            } else {
                Color.argb(26, 255, 59, 48) // Light red overlay
            }
        }
        holder.blurOverlay.setBackgroundColor(overlayColor)

        // Enable blur effect for Android 12+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            holder.blurOverlay.setRenderEffect(
                RenderEffect.createBlurEffect(
                    15f,
                    15f,
                    Shader.TileMode.CLAMP
                )
            )
        }

        // Set text values
        holder.tvName.text = station.name
        holder.tvCounty.text = station.county
        holder.tvOperator.text = station.trainOperator
        
        // Always show visit status
        holder.tvVisitStatus.text = station.visitStatus
        
        // Only show visit date if there is one
        holder.tvVisitedDate.isVisible = station.visitedDate.isNotEmpty()
        if (station.visitedDate.isNotEmpty()) {
            holder.tvVisitedDate.text = "Visited on ${station.visitedDate}"
        }

        // Set click listeners
        holder.cardView.setOnClickListener { onItemClick(station) }
        holder.btnUpdateStatus.setOnClickListener { onUpdateStatusClick(station) }
    }

    override fun getItemCount(): Int = stations.size
}