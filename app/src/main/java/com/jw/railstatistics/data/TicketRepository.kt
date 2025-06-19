package com.jw.railstatistics.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class TicketRepository(
    private val ticketDao: TicketDao,
    private val ticketDataManager: TicketDataManager
) {
    // Get all tickets as Flow
    fun getAllTickets(): Flow<List<TicketRecord>> = ticketDao.getAllTickets()

    // Get tickets by year
    fun getTicketsByYear(year: String): Flow<List<TicketRecord>> = ticketDao.getTicketsByYear(year)

    // Get tickets by TOC
    fun getTicketsByTOC(toc: String): Flow<List<TicketRecord>> = ticketDao.getTicketsByTOC(toc)

    // Get tickets by type
    fun getTicketsByType(ticketType: String): Flow<List<TicketRecord>> = ticketDao.getTicketsByType(ticketType)

    // Get delayed tickets
    fun getDelayedTickets(): Flow<List<TicketRecord>> = ticketDao.getDelayedTickets()

    // Get pending compensation tickets
    fun getPendingCompensationTickets(): Flow<List<TicketRecord>> = ticketDao.getPendingCompensationTickets()

    // Get ticket by ID
    suspend fun getTicketById(id: String): TicketRecord? = ticketDao.getTicketById(id)

    // Get newest ticket
    suspend fun getNewestTicket(): TicketRecord? = ticketDao.getNewestTicket()

    // Insert ticket
    suspend fun insertTicket(ticket: TicketRecord) = ticketDao.insertTicket(ticket)

    // Insert multiple tickets
    suspend fun insertTickets(tickets: List<TicketRecord>) = ticketDao.insertTickets(tickets)

    // Update ticket
    suspend fun updateTicket(ticket: TicketRecord) = ticketDao.updateTicket(ticket)

    // Delete ticket
    suspend fun deleteTicket(ticket: TicketRecord) = ticketDao.deleteTicket(ticket)

    // Delete all tickets
    suspend fun deleteAllTickets() = ticketDao.deleteAllTickets()

    // Get tickets by return group
    suspend fun getTicketsByReturnGroup(returnGroupId: String): List<TicketRecord> = 
        ticketDao.getTicketsByReturnGroup(returnGroupId)

    // CSV operations
    suspend fun parseCSV(uri: Uri): Pair<List<TicketRecord>, List<String>> = 
        ticketDataManager.parseCSV(uri)

    suspend fun exportCSV(tickets: List<TicketRecord>, uri: Uri) = 
        ticketDataManager.exportCSV(tickets, uri)

    // Statistics
    suspend fun getTotalTicketCount(): Int = ticketDao.getTotalTicketCount()

    suspend fun getTicketCountByYear(year: String): Int = ticketDao.getTicketCountByYear(year)

    suspend fun getTotalSpent(): Double = ticketDao.getTotalSpent() ?: 0.0

    suspend fun getTotalCompensation(): Double = ticketDao.getTotalCompensation() ?: 0.0

    suspend fun getTOCDistribution(): List<TOCDistribution> = ticketDao.getTOCDistribution()

    suspend fun getTicketTypeDistribution(): List<TicketTypeDistribution> = ticketDao.getTicketTypeDistribution()

    // Get filtered tickets for statistics
    fun getFilteredTickets(year: String? = null): Flow<List<TicketRecord>> {
        return if (year.isNullOrEmpty()) {
            getAllTickets()
        } else {
            getTicketsByYear(year)
        }
    }

    // Calculate statistics for filtered tickets
    suspend fun calculateStatistics(year: String? = null): TicketStatistics {
        val tickets = if (year.isNullOrEmpty()) {
            getAllTickets().map { it }
        } else {
            getTicketsByYear(year).map { it }
        }

        // Collect the tickets from the Flow
        val ticketList = tickets.first()
        
        val totalSpent = ticketList.sumOf { ticket ->
            ticket.price.replace("£", "").replace(",", "").toDoubleOrNull() ?: 0.0
        }
        
        val totalCompensation = ticketList.sumOf { ticket ->
            ticket.compensation.replace("£", "").replace(",", "").toDoubleOrNull() ?: 0.0
        }
        
        val totalVirginPoints = ticketList.sumOf { ticket ->
            ticket.loyaltyProgram?.virginPoints?.toIntOrNull() ?: 0
        }
        
        val totalLNERPerks = ticketList.sumOf { ticket ->
            ticket.loyaltyProgram?.lnerCashValue?.toDoubleOrNull() ?: 0.0
        }
        
        val totalClubAvantiJourneys = ticketList.sumOf { ticket ->
            ticket.loyaltyProgram?.clubAvantiJourneys?.toIntOrNull() ?: 0
        }

        return TicketStatistics(
            totalTickets = ticketList.size,
            totalSpent = totalSpent,
            totalCompensation = totalCompensation,
            adjustedTotal = totalSpent - totalCompensation,
            totalVirginPoints = totalVirginPoints,
            totalLNERPerks = totalLNERPerks,
            totalClubAvantiJourneys = totalClubAvantiJourneys
        )
    }
}

// Data class for statistics
data class TicketStatistics(
    val totalTickets: Int,
    val totalSpent: Double,
    val totalCompensation: Double,
    val adjustedTotal: Double,
    val totalVirginPoints: Int,
    val totalLNERPerks: Double,
    val totalClubAvantiJourneys: Int
) 