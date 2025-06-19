package com.jw.railstatistics.data

import android.graphics.Color

object TOCColors {
    // Dictionary mapping TOC names to their HEX color codes
    private val tocColors: Map<String, String> = mapOf(
        "Standard" to "#868686",
        "First" to "#9800F0",
        "Greater Anglia" to "#d70428",
        "ScotRail" to "#1e467d",
        "Avanti West Coast" to "#004354",
        "c2c" to "#b7007c",
        "Caledonian Sleeper" to "#1d2e35",
        "Chiltern Railways" to "#00bfff",
        "CrossCountry" to "#660f21",
        "East Midlands Railway" to "#713563",
        "Great Western Railway" to "#0a493e",
        "Hull Trains" to "#de005c",
        "Thameslink/Great Northern" to "#ff5aa4",
        "Heathrow Express" to "#532e63",
        "Island Line" to "#1e90ff",
        "Transport for Wales" to "#ff0000",
        "LNER" to "#ce0e2d",
        "Northern" to "#034DE2",
        "TransPennine Express" to "#09a4ec",
        "Multi-Operator" to "#F1F1F1",
        "Merseyrail" to "#fff200",
        "Gatwick Express" to "#EB1E2D",
        "Great Northern" to "#ff5aa4",
        "LNWR" to "#00bf6f",
        "South Western Railway" to "#24398c",
        "Southeastern" to "#389cff",
        "Southern" to "#8cc63e",
        "Thameslink" to "#ff5aa4",
        "West Midlands Trains" to "#ff8300",
        "Lumo" to "#2b6ef5",
        "Grand Central" to "#1d1d1b",
        "London Overground" to "#e87722",
        "London Underground" to "#10069f",
        "Elizabeth Line" to "#6950a1",
        "Bee Network" to "#FFCC33",
        "Blackpool Tramway" to "#7F2680",
        "Docklands Light Railway (DLR)" to "#00b2a9",
        "Edinburgh Trams" to "#8D122A",
        "Glasgow Subway" to "#f57c14",
        "London Tramlink" to "#78be20",
        "Manchester Metrolink" to "#edb600",
        "Nottingham Express Transit (NET)" to "#01796f",
        "Sheffield Supertram" to "#C67A1E",
        "Tyne & Wear Metro" to "#FBB10F",
        "West Midlands Metro" to "#0075c9",
        "Heritage" to "#800000",
        "International" to "#355e3b",
        "Eurostar" to "#086bfe"
    )

    // Function to get a Color for a TOC
    fun getColorForTOC(toc: String?): Int? {
        if (toc == null) return null
        val hex = tocColors[toc] ?: return null
        return try {
            Color.parseColor(hex)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    // Function to get all TOC names
    fun getAllTOCNames(): List<String> {
        return tocColors.keys.toList().sorted()
    }
} 