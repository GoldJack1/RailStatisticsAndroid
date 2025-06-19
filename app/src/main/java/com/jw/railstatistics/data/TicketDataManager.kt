package com.jw.railstatistics.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class TicketDataManager(private val context: Context) {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    companion object {
        private const val TICKETS_FILE = "tickets.json"
    }

    // Save tickets to internal storage
    suspend fun saveTicketsToDisk(tickets: List<TicketRecord>) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(tickets)
            val file = File(context.filesDir, TICKETS_FILE)
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Load tickets from internal storage
    suspend fun loadTicketsFromDisk(): List<TicketRecord> = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, TICKETS_FILE)
            if (!file.exists()) return@withContext emptyList()
            
            val json = file.readText()
            val type = object : TypeToken<List<TicketRecord>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Parse CSV file
    suspend fun parseCSV(uri: Uri): Pair<List<TicketRecord>, List<String>> = withContext(Dispatchers.IO) {
        val tickets = mutableListOf<TicketRecord>()
        val errors = mutableListOf<String>()

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = InputStreamReader(inputStream)
            val csvReader = CSVReader(reader)

            // Skip header row
            val header = csvReader.readNext()
            if (header == null) {
                errors.add("Empty CSV file")
                return@withContext Pair(tickets, errors)
            }

            var rowIndex = 1
            var row: Array<String>?
            while (csvReader.readNext().also { row = it } != null) {
                rowIndex++
                val rowData = row!!
                
                // Create a map of column names to values
                val rowMap = header.zip(rowData.toList()).toMap()
                
                // Validate required fields
                val origin = rowMap["Origin"] ?: ""
                val destination = rowMap["Destination"] ?: ""
                val price = rowMap["Price"] ?: ""
                val outboundDate = rowMap["OutboundDate"] ?: ""
                val outboundTime = rowMap["OutboundTime"] ?: ""
                val ticketType = rowMap["TicketType"] ?: ""
                val classType = rowMap["ClassType"] ?: ""

                if (origin.isEmpty() || destination.isEmpty() || outboundDate.isEmpty() || 
                    outboundTime.isEmpty() || ticketType.isEmpty() || classType.isEmpty()) {
                    errors.add("Row $rowIndex: Missing required fields.")
                    continue
                }

                // Validate and include only numeric loyalty data
                val virginPoints = validateNumericField(rowMap["VirginPoints"])
                val lnerPerks = validateNumericField(rowMap["LNERperks"])
                val clubAvantiJourneys = validateNumericField(rowMap["ClubAvantiJourneys"])

                val loyaltyProgram = if (virginPoints != null || lnerPerks != null || clubAvantiJourneys != null) {
                    LoyaltyProgram(
                        virginPoints = virginPoints,
                        lnerCashValue = lnerPerks,
                        clubAvantiJourneys = clubAvantiJourneys
                    )
                } else null

                val railcard = rowMap["Railcard"]?.takeIf { it.isNotEmpty() }
                val coach = rowMap["Coach"]?.takeIf { it.isNotEmpty() }
                val seat = rowMap["Seat"]?.takeIf { it.isNotEmpty() }
                val tocRouteRestriction = rowMap["TOC/Route-Restriction"]?.takeIf { it.isNotEmpty() }

                val returnDate = rowMap["ReturnDate"] ?: ""
                val returnTime = rowMap["ReturnTime"] ?: ""

                // Determine ticketFormat based on ticketType
                val typeLower = ticketType.lowercase()
                val ticketFormat = when {
                    typeLower.contains("contactless") -> "Contactless cards"
                    typeLower.contains("travelcard") -> "Travelcards"
                    typeLower.contains("ranger") || typeLower.contains("rover") -> "Rangers/Rovers"
                    else -> "Tickets"
                }

                // If both outbound and return info are present, create two linked tickets
                if (returnDate.isNotEmpty() && returnTime.isNotEmpty()) {
                    val returnGroupID = UUID.randomUUID().toString()
                    
                    // Outbound ticket
                    val outboundTicket = TicketRecord(
                        origin = origin,
                        destination = destination,
                        price = if (price.startsWith("£")) price else "£$price",
                        ticketType = ticketType,
                        classType = classType,
                        toc = rowMap["TOC"],
                        outboundDate = outboundDate,
                        outboundTime = outboundTime,
                        wasDelayed = (rowMap["WasDelayed"] ?: "No").lowercase() == "yes",
                        delayDuration = rowMap["DelayDuration"] ?: "",
                        pendingCompensation = (rowMap["PendingCompensation"] ?: "No").lowercase() == "yes",
                        compensation = rowMap["Compensation"] ?: "",
                        loyaltyProgram = loyaltyProgram,
                        railcard = railcard,
                        coach = coach,
                        seat = seat,
                        tocRouteRestriction = tocRouteRestriction,
                        returnGroupID = returnGroupID,
                        isReturn = false,
                        ticketFormat = ticketFormat
                    )
                    
                    // Return ticket
                    val returnTicket = TicketRecord(
                        origin = destination,
                        destination = origin,
                        price = "£0.00",
                        ticketType = ticketType,
                        classType = classType,
                        toc = rowMap["TOC"],
                        outboundDate = returnDate,
                        outboundTime = returnTime,
                        loyaltyProgram = loyaltyProgram,
                        railcard = railcard,
                        coach = coach,
                        seat = seat,
                        tocRouteRestriction = tocRouteRestriction,
                        returnGroupID = returnGroupID,
                        isReturn = true,
                        ticketFormat = ticketFormat
                    )
                    
                    tickets.add(outboundTicket)
                    tickets.add(returnTicket)
                } else {
                    // Single ticket
                    val ticket = TicketRecord(
                        origin = origin,
                        destination = destination,
                        price = if (price.startsWith("£")) price else "£$price",
                        ticketType = ticketType,
                        classType = classType,
                        toc = rowMap["TOC"],
                        outboundDate = outboundDate,
                        outboundTime = outboundTime,
                        wasDelayed = (rowMap["WasDelayed"] ?: "No").lowercase() == "yes",
                        delayDuration = rowMap["DelayDuration"] ?: "",
                        pendingCompensation = (rowMap["PendingCompensation"] ?: "No").lowercase() == "yes",
                        compensation = rowMap["Compensation"] ?: "",
                        loyaltyProgram = loyaltyProgram,
                        railcard = railcard,
                        coach = coach,
                        seat = seat,
                        tocRouteRestriction = tocRouteRestriction,
                        ticketFormat = ticketFormat
                    )
                    tickets.add(ticket)
                }
            }
            
            csvReader.close()
        } catch (e: Exception) {
            errors.add("Error parsing CSV: ${e.message}")
        }

        Pair(tickets, errors)
    }

    // Load newest ticket from disk
    suspend fun loadNewestTicketFromDisk(): TicketRecord? = withContext(Dispatchers.IO) {
        val allTickets = loadTicketsFromDisk()
        if (allTickets.isEmpty()) return@withContext null

        allTickets.maxByOrNull { ticket ->
            try {
                dateTimeFormat.parse("${ticket.outboundDate} ${ticket.outboundTime}")?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }

    // Export tickets to CSV
    suspend fun exportCSV(tickets: List<TicketRecord>, uri: Uri) = withContext(Dispatchers.IO) {
        try {
            val csvContent = StringBuilder()
            csvContent.append("Origin,Destination,Price,TicketType,ClassType,TOC,OutboundDate,OutboundTime,WasDelayed,DelayDuration,PendingCompensation,Compensation,VirginPoints,LNERperks,ClubAvantiJourneys,Railcard,Coach,Seat,TOC/Route-Restriction,ReturnGroupID,IsReturn,TicketFormat\n")

            for (ticket in tickets) {
                val row = listOf(
                    ticket.origin,
                    ticket.destination,
                    ticket.price,
                    ticket.ticketType,
                    ticket.classType,
                    ticket.toc ?: "",
                    ticket.outboundDate,
                    ticket.outboundTime,
                    if (ticket.wasDelayed) "Yes" else "No",
                    ticket.delayDuration,
                    if (ticket.pendingCompensation) "Yes" else "No",
                    ticket.compensation,
                    ticket.loyaltyProgram?.virginPoints ?: "",
                    ticket.loyaltyProgram?.lnerCashValue ?: "",
                    ticket.loyaltyProgram?.clubAvantiJourneys ?: "",
                    ticket.railcard ?: "",
                    ticket.coach ?: "",
                    ticket.seat ?: "",
                    ticket.tocRouteRestriction ?: "",
                    ticket.returnGroupID ?: "",
                    if (ticket.isReturn) "Yes" else "No",
                    ticket.ticketFormat
                ).joinToString(",")

                csvContent.append(row).append("\n")
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvContent.toString().toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun validateNumericField(value: String?): String? {
        return if (!value.isNullOrEmpty() && value.toDoubleOrNull() != null) value else null
    }
} 