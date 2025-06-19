package com.jw.railstatistics.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.UUID

@Entity(tableName = "tickets")
data class TicketRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    var origin: String = "Unknown",
    var destination: String = "Unknown",
    var price: String = "£0.00",
    var ticketType: String = "N/A",
    var classType: String = "Standard",
    var toc: String? = null,
    var outboundDate: String = "Unknown",
    var outboundTime: String = "00:00",
    var returnDate: String = "",
    var returnTime: String = "00:00",
    var wasDelayed: Boolean = false,
    var delayDuration: String = "",
    var pendingCompensation: Boolean = false,
    var compensation: String = "",
    @TypeConverters(LoyaltyProgramConverter::class)
    var loyaltyProgram: LoyaltyProgram? = null,
    var railcard: String? = null,
    var coach: String? = null,
    var seat: String? = null,
    var tocRouteRestriction: String? = null,
    var returnGroupID: String? = null,
    var isReturn: Boolean = false,
    var ticketFormat: String = "Tickets"
)

data class LoyaltyProgram(
    var virginPoints: String? = null,
    var lnerCashValue: String? = null,
    var clubAvantiJourneys: String? = null
)

// Type converter for Room database to handle LoyaltyProgram serialization
class LoyaltyProgramConverter {
    @androidx.room.TypeConverter
    fun fromLoyaltyProgram(loyaltyProgram: LoyaltyProgram?): String? {
        if (loyaltyProgram == null) return null
        return "${loyaltyProgram.virginPoints}|${loyaltyProgram.lnerCashValue}|${loyaltyProgram.clubAvantiJourneys}"
    }

    @androidx.room.TypeConverter
    fun toLoyaltyProgram(value: String?): LoyaltyProgram? {
        if (value == null) return null
        val parts = value.split("|")
        return if (parts.size == 3) {
            LoyaltyProgram(
                virginPoints = if (parts[0] == "null") null else parts[0],
                lnerCashValue = if (parts[1] == "null") null else parts[1],
                clubAvantiJourneys = if (parts[2] == "null") null else parts[2]
            )
        } else null
    }
} 