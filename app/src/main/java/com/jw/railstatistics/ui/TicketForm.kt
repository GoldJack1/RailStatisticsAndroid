package com.jw.railstatistics.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jw.railstatistics.data.LoyaltyProgram
import com.jw.railstatistics.data.TOCColors
import com.jw.railstatistics.data.TicketRecord
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TicketForm(
    ticket: TicketRecord? = null,
    onSave: (TicketRecord) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var origin by remember { mutableStateOf(ticket?.origin ?: "") }
    var destination by remember { mutableStateOf(ticket?.destination ?: "") }
    var price by remember { mutableStateOf(ticket?.price?.replace("£", "") ?: "") }
    var ticketType by remember { mutableStateOf(ticket?.ticketType ?: "") }
    var classType by remember { mutableStateOf(ticket?.classType ?: "Standard") }
    var toc by remember { mutableStateOf(ticket?.toc ?: "") }
    var outboundDate by remember { mutableStateOf(ticket?.outboundDate ?: "") }
    var outboundTime by remember { mutableStateOf(ticket?.outboundTime ?: "") }
    var hasReturnTicket by remember { mutableStateOf(ticket?.returnGroupID != null) }
    var returnDate by remember { mutableStateOf("") }
    var returnTime by remember { mutableStateOf("") }
    var wasDelayed by remember { mutableStateOf(ticket?.wasDelayed ?: false) }
    var delayDuration by remember { mutableStateOf(ticket?.delayDuration ?: "") }
    var pendingCompensation by remember { mutableStateOf(ticket?.pendingCompensation ?: false) }
    var compensation by remember { mutableStateOf(ticket?.compensation ?: "") }
    
    // Loyalty program fields
    var isVirginEnabled by remember { mutableStateOf(ticket?.loyaltyProgram?.virginPoints != null) }
    var virginPoints by remember { mutableStateOf(ticket?.loyaltyProgram?.virginPoints ?: "") }
    var isLNEREEnabled by remember { mutableStateOf(ticket?.loyaltyProgram?.lnerCashValue != null) }
    var lnerCashValue by remember { mutableStateOf(ticket?.loyaltyProgram?.lnerCashValue ?: "") }
    var isClubAvantiEnabled by remember { mutableStateOf(ticket?.loyaltyProgram?.clubAvantiJourneys != null) }
    var clubAvantiJourneys by remember { mutableStateOf(ticket?.loyaltyProgram?.clubAvantiJourneys ?: "") }
    
    // Additional fields
    var railcard by remember { mutableStateOf(ticket?.railcard ?: "") }
    var coach by remember { mutableStateOf(ticket?.coach ?: "") }
    var seat by remember { mutableStateOf(ticket?.seat ?: "") }
    var tocRouteRestriction by remember { mutableStateOf(ticket?.tocRouteRestriction ?: "") }
    
    // TOC selection
    var showTOCDropdown by remember { mutableStateOf(false) }
    val tocOptions = TOCColors.getAllTOCNames()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = if (ticket == null) "Add New Ticket" else "Edit Ticket",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Journey Details Section
        FormSection(title = "Journey Details") {
            OutlinedTextField(
                value = origin,
                onValueChange = { origin = it },
                label = { Text("Origin") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        
        // Ticket Details Section
        FormSection(title = "Ticket Details") {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (£)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = ticketType,
                onValueChange = { ticketType = it },
                label = { Text("Ticket Type") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Class Type Selection
            Text("Class Type", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = classType == "Standard",
                    onClick = { classType = "Standard" },
                    label = { Text("Standard") }
                )
                FilterChip(
                    selected = classType == "First",
                    onClick = { classType = "First" },
                    label = { Text("First") }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // TOC Selection
            ExposedDropdownMenuBox(
                expanded = showTOCDropdown,
                onExpandedChange = { showTOCDropdown = it }
            ) {
                OutlinedTextField(
                    value = toc,
                    onValueChange = { toc = it },
                    label = { Text("Train Operator (TOC)") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTOCDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = showTOCDropdown,
                    onDismissRequest = { showTOCDropdown = false }
                ) {
                    tocOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                toc = option
                                showTOCDropdown = false
                            }
                        )
                    }
                }
            }
        }
        
        // Date and Time Section
        FormSection(title = "Date & Time") {
            OutlinedTextField(
                value = outboundDate,
                onValueChange = { outboundDate = it },
                label = { Text("Outbound Date (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = outboundTime,
                onValueChange = { outboundTime = it },
                label = { Text("Outbound Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Return ticket toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasReturnTicket,
                    onCheckedChange = { hasReturnTicket = it }
                )
                Text("Return Ticket", fontSize = 16.sp)
            }
            
            if (hasReturnTicket) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = returnDate,
                    onValueChange = { returnDate = it },
                    label = { Text("Return Date (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = returnTime,
                    onValueChange = { returnTime = it },
                    label = { Text("Return Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        // Delay and Compensation Section
        FormSection(title = "Delay & Compensation") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = wasDelayed,
                    onCheckedChange = { wasDelayed = it }
                )
                Text("Was Delayed", fontSize = 16.sp)
            }
            
            if (wasDelayed) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = delayDuration,
                    onValueChange = { delayDuration = it },
                    label = { Text("Delay Duration") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = pendingCompensation,
                    onCheckedChange = { pendingCompensation = it }
                )
                Text("Pending Compensation", fontSize = 16.sp)
            }
            
            if (pendingCompensation) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = compensation,
                    onValueChange = { compensation = it },
                    label = { Text("Compensation Amount (£)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        // Loyalty Programs Section
        FormSection(title = "Loyalty Programs") {
            // Virgin Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isVirginEnabled,
                    onCheckedChange = { isVirginEnabled = it }
                )
                Text("Virgin Points", fontSize = 16.sp)
            }
            
            if (isVirginEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = virginPoints,
                    onValueChange = { virginPoints = it },
                    label = { Text("Virgin Points") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // LNER Perks
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isLNEREEnabled,
                    onCheckedChange = { isLNEREEnabled = it }
                )
                Text("LNER Perks", fontSize = 16.sp)
            }
            
            if (isLNEREEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = lnerCashValue,
                    onValueChange = { lnerCashValue = it },
                    label = { Text("LNER Cash Value (£)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Club Avanti
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isClubAvantiEnabled,
                    onCheckedChange = { isClubAvantiEnabled = it }
                )
                Text("Club Avanti Journeys", fontSize = 16.sp)
            }
            
            if (isClubAvantiEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = clubAvantiJourneys,
                    onValueChange = { clubAvantiJourneys = it },
                    label = { Text("Club Avanti Journeys") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        // Additional Details Section
        FormSection(title = "Additional Details") {
            OutlinedTextField(
                value = railcard,
                onValueChange = { railcard = it },
                label = { Text("Railcard") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = coach,
                onValueChange = { coach = it },
                label = { Text("Coach") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = seat,
                onValueChange = { seat = it },
                label = { Text("Seat") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = tocRouteRestriction,
                onValueChange = { tocRouteRestriction = it },
                label = { Text("TOC/Route Restriction") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        
        // Action Buttons
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    val loyaltyProgram = if (isVirginEnabled || isLNEREEnabled || isClubAvantiEnabled) {
                        LoyaltyProgram(
                            virginPoints = if (isVirginEnabled) virginPoints.takeIf { it.isNotEmpty() } else null,
                            lnerCashValue = if (isLNEREEnabled) lnerCashValue.takeIf { it.isNotEmpty() } else null,
                            clubAvantiJourneys = if (isClubAvantiEnabled) clubAvantiJourneys.takeIf { it.isNotEmpty() } else null
                        )
                    } else null
                    
                    val newTicket = TicketRecord(
                        id = ticket?.id ?: "",
                        origin = origin,
                        destination = destination,
                        price = if (price.startsWith("£")) price else "£$price",
                        ticketType = ticketType,
                        classType = classType,
                        toc = toc.takeIf { it.isNotEmpty() },
                        outboundDate = outboundDate,
                        outboundTime = outboundTime,
                        wasDelayed = wasDelayed,
                        delayDuration = delayDuration,
                        pendingCompensation = pendingCompensation,
                        compensation = compensation,
                        loyaltyProgram = loyaltyProgram,
                        railcard = railcard.takeIf { it.isNotEmpty() },
                        coach = coach.takeIf { it.isNotEmpty() },
                        seat = seat.takeIf { it.isNotEmpty() },
                        tocRouteRestriction = tocRouteRestriction.takeIf { it.isNotEmpty() }
                    )
                    
                    onSave(newTicket)
                },
                modifier = Modifier.weight(1f),
                enabled = origin.isNotEmpty() && destination.isNotEmpty() && 
                         outboundDate.isNotEmpty() && outboundTime.isNotEmpty() &&
                         ticketType.isNotEmpty() && price.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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