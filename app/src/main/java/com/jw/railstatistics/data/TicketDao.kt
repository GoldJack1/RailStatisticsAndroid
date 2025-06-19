package com.jw.railstatistics.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY outboundDate DESC, outboundTime DESC")
    fun getAllTickets(): Flow<List<TicketRecord>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getTicketById(id: String): TicketRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketRecord>)

    @Update
    suspend fun updateTicket(ticket: TicketRecord)

    @Delete
    suspend fun deleteTicket(ticket: TicketRecord)

    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()

    @Query("SELECT * FROM tickets WHERE returnGroupID = :returnGroupId")
    suspend fun getTicketsByReturnGroup(returnGroupId: String): List<TicketRecord>

    @Query("SELECT * FROM tickets WHERE outboundDate LIKE :year || '%' ORDER BY outboundDate DESC, outboundTime DESC")
    fun getTicketsByYear(year: String): Flow<List<TicketRecord>>

    @Query("SELECT * FROM tickets WHERE toc = :toc ORDER BY outboundDate DESC, outboundTime DESC")
    fun getTicketsByTOC(toc: String): Flow<List<TicketRecord>>

    @Query("SELECT * FROM tickets WHERE ticketType = :ticketType ORDER BY outboundDate DESC, outboundTime DESC")
    fun getTicketsByType(ticketType: String): Flow<List<TicketRecord>>

    @Query("SELECT * FROM tickets WHERE wasDelayed = 1 ORDER BY outboundDate DESC, outboundTime DESC")
    fun getDelayedTickets(): Flow<List<TicketRecord>>

    @Query("SELECT * FROM tickets WHERE pendingCompensation = 1 ORDER BY outboundDate DESC, outboundTime DESC")
    fun getPendingCompensationTickets(): Flow<List<TicketRecord>>

    // Get newest ticket for quick access
    @Query("SELECT * FROM tickets ORDER BY outboundDate DESC, outboundTime DESC LIMIT 1")
    suspend fun getNewestTicket(): TicketRecord?

    // Statistics queries
    @Query("SELECT COUNT(*) FROM tickets")
    suspend fun getTotalTicketCount(): Int

    @Query("SELECT COUNT(*) FROM tickets WHERE outboundDate LIKE :year || '%'")
    suspend fun getTicketCountByYear(year: String): Int

    @Query("SELECT SUM(CAST(REPLACE(REPLACE(price, '£', ''), ',', '') AS REAL)) FROM tickets")
    suspend fun getTotalSpent(): Double?

    @Query("SELECT SUM(CAST(REPLACE(REPLACE(compensation, '£', ''), ',', '') AS REAL)) FROM tickets WHERE compensation != ''")
    suspend fun getTotalCompensation(): Double?

    @Query("SELECT toc, COUNT(*) as count FROM tickets GROUP BY toc ORDER BY count DESC")
    suspend fun getTOCDistribution(): List<TOCDistribution>

    @Query("SELECT ticketType, COUNT(*) as count FROM tickets GROUP BY ticketType ORDER BY count DESC")
    suspend fun getTicketTypeDistribution(): List<TicketTypeDistribution>
}

// Data classes for statistics
data class TOCDistribution(
    val toc: String,
    val count: Int
)

data class TicketTypeDistribution(
    val ticketType: String,
    val count: Int
) 