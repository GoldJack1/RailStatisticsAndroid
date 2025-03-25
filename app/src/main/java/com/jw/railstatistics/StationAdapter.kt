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

class StationAdapter(
    private val stations: List<Station>,
    private val onItemClick: (Station) -> Unit,
    private val onUpdateStatusClick: (Station) -> Unit
) : RecyclerView.Adapter<StationAdapter.StationViewHolder>() {

    inner class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardStation)
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
        holder.cardView.setCardBackgroundColor(backgroundColor)

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