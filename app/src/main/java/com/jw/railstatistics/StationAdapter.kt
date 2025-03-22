package com.jw.railstatistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class StationAdapter(
    private val stations: List<Station>,
    private val onItemClick: (Station) -> Unit
) : RecyclerView.Adapter<StationAdapter.StationViewHolder>() {

    inner class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStationName)
        val tvCounty: TextView = itemView.findViewById(R.id.tvCounty)
        val tvOperator: TextView = itemView.findViewById(R.id.tvTrainOperator)
        val tvVisitedDate: TextView = itemView.findViewById(R.id.tvVisitedDate)
        val tvVisitStatus: TextView = itemView.findViewById(R.id.tvVisitStatus)
        val cardView: CardView = itemView.findViewById<CardView>(R.id.cardStation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_station, parent, false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = stations[position]
        holder.tvName.text = station.name
        holder.tvCounty.text = station.county
        holder.tvOperator.text = station.trainOperator
        holder.tvVisitedDate.text = station.visitedDate
        holder.tvVisitStatus.text = station.visitStatus
        holder.cardView.setOnClickListener { onItemClick(station) }
    }

    override fun getItemCount(): Int = stations.size
}