package com.jw.railstatistics.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jw.railstatistics.data.TicketRecord
import com.jw.railstatistics.data.TOCColors
import java.text.SimpleDateFormat
import java.util.*

data class FilterState(
    val selectedTOC: String = "",
    val selectedClassType: String = "",
    val selectedTicketType: String = "",
    val selectedDelayMinutes: String = "",
    val startDate: String = "",
    val endDate: String = ""
)

@Composable
fun TicketFilterScreen(
    tickets: List<TicketRecord>,
    onApplyFilters: (FilterState) -> Unit,
    onDismiss: () -> Unit,
    initialFilterState: FilterState = FilterState(),
    modifier: Modifier = Modifier
) {
    var filterState by remember { mutableStateOf(initialFilterState) }
    
    // Get unique values for dropdowns
    val uniqueTOCs = remember(tickets) {
        tickets.mapNotNull { it.toc }.distinct().sorted()
    }
    
    val uniqueTicketTypes = remember(tickets) {
        tickets.map { it.ticketType }.distinct().sorted()
    }
    
    val uniqueDelayMinutes = remember(tickets) {
        tickets.mapNotNull { it.delayDuration }.distinct().sorted()
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
                text = "Filter Tickets",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                
                TextButton(
                    onClick = { onApplyFilters(filterState) }
                ) {
                    Text("Apply")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // TOC Filter Section
        FilterSection(title = "Filter by TOC") {
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                OutlinedTextField(
                    value = filterState.selectedTOC,
                    onValueChange = { filterState = filterState.copy(selectedTOC = it) },
                    label = { Text("Select TOC") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = { filterState = filterState.copy(selectedTOC = "") }
                    )
                    uniqueTOCs.forEach { toc ->
                        DropdownMenuItem(
                            text = { Text(toc) },
                            onClick = { filterState = filterState.copy(selectedTOC = toc) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Class Type Filter Section
        FilterSection(title = "Filter by Class Type") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterState.selectedClassType.isEmpty(),
                    onClick = { filterState = filterState.copy(selectedClassType = "") },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterState.selectedClassType == "Standard",
                    onClick = { filterState = filterState.copy(selectedClassType = "Standard") },
                    label = { Text("Standard") }
                )
                FilterChip(
                    selected = filterState.selectedClassType == "First",
                    onClick = { filterState = filterState.copy(selectedClassType = "First") },
                    label = { Text("First") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ticket Type Filter Section
        FilterSection(title = "Filter by Ticket Type") {
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                OutlinedTextField(
                    value = filterState.selectedTicketType,
                    onValueChange = { filterState = filterState.copy(selectedTicketType = it) },
                    label = { Text("Select Ticket Type") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = { filterState = filterState.copy(selectedTicketType = "") }
                    )
                    uniqueTicketTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = { filterState = filterState.copy(selectedTicketType = type) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Delay Minutes Filter Section
        FilterSection(title = "Filter by Delay Minutes") {
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                OutlinedTextField(
                    value = filterState.selectedDelayMinutes,
                    onValueChange = { filterState = filterState.copy(selectedDelayMinutes = it) },
                    label = { Text("Select Delay Duration") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = { filterState = filterState.copy(selectedDelayMinutes = "") }
                    )
                    uniqueDelayMinutes.forEach { delay ->
                        DropdownMenuItem(
                            text = { Text(delay) },
                            onClick = { filterState = filterState.copy(selectedDelayMinutes = delay) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date Range Filter Section
        FilterSection(title = "Date Range") {
            OutlinedTextField(
                value = filterState.startDate,
                onValueChange = { filterState = filterState.copy(startDate = it) },
                label = { Text("Start Date (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = filterState.endDate,
                onValueChange = { filterState = filterState.copy(endDate = it) },
                label = { Text("End Date (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    filterState = FilterState()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear All")
            }
            
            Button(
                onClick = { onApplyFilters(filterState) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply Filters")
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
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
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

// Helper function to apply filters to tickets
fun applyFilters(tickets: List<TicketRecord>, filterState: FilterState): List<TicketRecord> {
    return tickets.filter { ticket ->
        val matchesTOC = filterState.selectedTOC.isEmpty() || ticket.toc == filterState.selectedTOC
        val matchesClassType = filterState.selectedClassType.isEmpty() || ticket.classType == filterState.selectedClassType
        val matchesTicketType = filterState.selectedTicketType.isEmpty() || ticket.ticketType == filterState.selectedTicketType
        val matchesDelayMinutes = filterState.selectedDelayMinutes.isEmpty() || ticket.delayDuration == filterState.selectedDelayMinutes
        
        val matchesDateRange = if (filterState.startDate.isNotEmpty() && filterState.endDate.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val ticketDate = dateFormat.parse(ticket.outboundDate)
                val startDate = dateFormat.parse(filterState.startDate)
                val endDate = dateFormat.parse(filterState.endDate)
                
                ticketDate != null && startDate != null && endDate != null &&
                ticketDate >= startDate && ticketDate <= endDate
            } catch (e: Exception) {
                true // If date parsing fails, include the ticket
            }
        } else {
            true
        }
        
        matchesTOC && matchesClassType && matchesTicketType && matchesDelayMinutes && matchesDateRange
    }
} 