package com.jw.railstatistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jw.railstatistics.data.TicketRecord
import com.jw.railstatistics.data.TOCColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TicketStatisticsScreen(
    tickets: List<TicketRecord>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedYear by remember { mutableStateOf("") }
    var mileageInput by remember { mutableStateOf("") }
    var chainsInput by remember { mutableStateOf("") }
    var costPerMile by remember { mutableStateOf<Double?>(null) }
    
    // Get unique years from tickets
    val uniqueYears = remember(tickets) {
        tickets.mapNotNull { ticket ->
            try {
                ticket.outboundDate.split("/").lastOrNull()
            } catch (e: Exception) {
                null
            }
        }.distinct().sortedDescending()
    }
    
    // Filter tickets by selected year
    val filteredTickets = remember(tickets, selectedYear) {
        if (selectedYear.isEmpty()) {
            tickets
        } else {
            tickets.filter { ticket ->
                ticket.outboundDate.endsWith(selectedYear)
            }
        }
    }
    
    // Calculate statistics
    val statistics = remember(filteredTickets) {
        calculateStatistics(filteredTickets)
    }
    
    // Group tickets by TOC
    val ticketsByTOC = remember(filteredTickets) {
        filteredTickets
            .filter { it.toc != null }
            .groupBy { it.toc!! }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }
    
    // Group tickets by type
    val ticketsByType = remember(filteredTickets) {
        filteredTickets
            .groupBy { it.ticketType }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ticket Statistics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Year filter
        YearFilterSection(
            selectedYear = selectedYear,
            uniqueYears = uniqueYears,
            onYearSelected = { selectedYear = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Overview statistics
        StatisticsOverviewSection(statistics = statistics)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cost per mile section
        CostPerMileSection(
            selectedYear = selectedYear,
            statistics = statistics,
            mileageInput = mileageInput,
            onMileageInputChange = { mileageInput = it },
            chainsInput = chainsInput,
            onChainsInputChange = { chainsInput = it },
            costPerMile = costPerMile,
            onCalculateCostPerMile = {
                costPerMile = calculateCostPerMile(
                    mileageInput,
                    chainsInput,
                    statistics.adjustedTotal
                )
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // TOC distribution
        TOCDistributionSection(ticketsByTOC = ticketsByTOC)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ticket types distribution
        TicketTypesSection(ticketsByType = ticketsByType)
    }
}

@Composable
private fun YearFilterSection(
    selectedYear: String,
    uniqueYears: List<String>,
    onYearSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filter by Year",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedYear.isEmpty(),
                    onClick = { onYearSelected("") },
                    label = { Text("All") }
                )
                
                uniqueYears.forEach { year ->
                    FilterChip(
                        selected = selectedYear == year,
                        onClick = { onYearSelected(year) },
                        label = { Text(year) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsOverviewSection(statistics: TicketStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatisticRow("Total Tickets", "${statistics.totalTickets}")
            StatisticRow("Total Spent", "£${String.format("%.2f", statistics.totalSpent)}")
            StatisticRow("Compensation Received", "£${String.format("%.2f", statistics.totalCompensation)}")
            StatisticRow("Adjusted Total", "£${String.format("%.2f", statistics.adjustedTotal)}")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loyalty",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatisticRow("Virgin Points Earned", "${statistics.totalVirginPoints}")
            StatisticRow("LNER Perks", "£${String.format("%.2f", statistics.totalLNERPerks)}")
            StatisticRow("Club Avanti Journeys", "${statistics.totalClubAvantiJourneys}")
        }
    }
}

@Composable
private fun CostPerMileSection(
    selectedYear: String,
    statistics: TicketStatistics,
    mileageInput: String,
    onMileageInputChange: (String) -> Unit,
    chainsInput: String,
    onChainsInputChange: (String) -> Unit,
    costPerMile: Double?,
    onCalculateCostPerMile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Cost Per Rail Mile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = mileageInput,
                    onValueChange = onMileageInputChange,
                    label = { Text("Miles") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = chainsInput,
                    onValueChange = onChainsInputChange,
                    label = { Text("Chains") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onCalculateCostPerMile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (costPerMile != null) {
                    "Cost per mile: £${String.format("%.2f", costPerMile)}"
                } else {
                    "Cost per mile: N/A"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (costPerMile != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TOCDistributionSection(ticketsByTOC: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "TOC Distribution",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (ticketsByTOC.isNotEmpty()) {
                ticketsByTOC.take(10).forEach { (toc, count) ->
                    val tocColor = TOCColors.getColorForTOC(toc)?.let { Color(it) } ?: Color.Gray
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(tocColor, RoundedCornerShape(6.dp))
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = toc,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp
                        )
                        
                        Text(
                            text = count.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = "No data available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TicketTypesSection(ticketsByType: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ticket Types",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (ticketsByType.isNotEmpty()) {
                ticketsByType.take(10).forEach { (type, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp
                        )
                        
                        Text(
                            text = count.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = "No data available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun calculateStatistics(tickets: List<TicketRecord>): TicketStatistics {
    val totalSpent = tickets
        .filter { !it.isReturn }
        .sumOf { parsePrice(it.price) }
    
    val totalCompensation = tickets.sumOf { parsePrice(it.compensation) }
    val adjustedTotal = totalSpent - totalCompensation
    
    val totalVirginPoints = tickets
        .mapNotNull { it.loyaltyProgram?.virginPoints?.toIntOrNull() }
        .sum()
    
    val totalLNERPerks = tickets
        .mapNotNull { it.loyaltyProgram?.lnerCashValue?.toDoubleOrNull() }
        .sum()
    
    val totalClubAvantiJourneys = tickets
        .mapNotNull { it.loyaltyProgram?.clubAvantiJourneys?.toIntOrNull() }
        .sum()
    
    return TicketStatistics(
        totalTickets = tickets.size,
        totalSpent = totalSpent,
        totalCompensation = totalCompensation,
        adjustedTotal = adjustedTotal,
        totalVirginPoints = totalVirginPoints,
        totalLNERPerks = totalLNERPerks,
        totalClubAvantiJourneys = totalClubAvantiJourneys
    )
}

private fun parsePrice(price: String): Double {
    return price.replace("£", "").replace(",", "").toDoubleOrNull() ?: 0.0
}

private fun calculateCostPerMile(miles: String, chains: String, totalSpent: Double): Double? {
    val milesValue = miles.toDoubleOrNull() ?: return null
    val chainsValue = chains.toDoubleOrNull() ?: return null
    
    if (chainsValue >= 80) return null
    
    val totalMileage = milesValue + (chainsValue / 80)
    return if (totalMileage > 0) totalSpent / totalMileage else null
} 