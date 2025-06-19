package com.jw.railstatistics

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jw.railstatistics.data.*
import com.jw.railstatistics.ui.*
import com.jw.railstatistics.ui.theme.RailStatisticsTheme

class TicketDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val ticketId = intent.getStringExtra("ticket_id") ?: ""
        
        setContent {
            RailStatisticsTheme {
                TicketDetailScreen(
                    ticketId = ticketId,
                    onBackPressed = { finish() },
                    onTicketUpdated = { /* Handle update */ },
                    onTicketDeleted = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: String,
    onBackPressed: () -> Unit,
    onTicketUpdated: (TicketRecord) -> Unit,
    onTicketDeleted: () -> Unit,
    viewModel: TicketViewModel = viewModel(
        factory = TicketViewModelFactory(
            TicketRepository(
                AppDatabase.getDatabase(LocalContext.current).ticketDao(),
                TicketDataManager(LocalContext.current)
            )
        )
    )
) {
    var ticket by remember { mutableStateOf<TicketRecord?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Load ticket data
    LaunchedEffect(ticketId) {
        ticket = viewModel.getTicketById(ticketId)
    }
    
    ticket?.let { currentTicket ->
        Scaffold(
            topBar = {
                // Custom header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = "Ticket Details",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(
                            onClick = { isEditing = !isEditing }
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Save" else "Edit",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(8.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = 0.1f))
                                    .padding(8.dp),
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            if (isEditing) {
                // Edit mode
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    TicketForm(
                        ticket = currentTicket,
                        onSave = { updatedTicket ->
                            viewModel.updateTicket(updatedTicket)
                            isEditing = false
                            onTicketUpdated(updatedTicket)
                        },
                        onCancel = { isEditing = false }
                    )
                }
            } else {
                // View mode
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Journey Details Section
                    JourneyDetailsSection(ticket = currentTicket)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Ticket Details Section
                    TicketDetailsSection(ticket = currentTicket)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Compensation Section
                    CompensationSection(ticket = currentTicket)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Additional Details Section
                    AdditionalDetailsSection(ticket = currentTicket)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Loyalty Programs Section
                    LoyaltyProgramsSection(ticket = currentTicket)
                }
            }
        }
        
        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Ticket") },
                text = { Text("Are you sure you want to delete this ticket? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTicket(currentTicket)
                            showDeleteDialog = false
                            onTicketDeleted()
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    } ?: run {
        // Loading state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun JourneyDetailsSection(ticket: TicketRecord) {
    DetailCard(title = "Journey Details") {
        DetailRow("Origin", ticket.origin)
        DetailRow("Destination", ticket.destination)
        DetailRow("Date", ticket.outboundDate)
        DetailRow("Time", ticket.outboundTime)
        
        if (ticket.returnGroupID != null) {
            DetailRow("Return Journey", "Yes")
        }
    }
}

@Composable
private fun TicketDetailsSection(ticket: TicketRecord) {
    DetailCard(title = "Ticket Details") {
        DetailRow("Price", ticket.price)
        DetailRow("Ticket Type", ticket.ticketType)
        DetailRow("Class", ticket.classType)
        ticket.toc?.let { DetailRow("Train Operator", it) }
        DetailRow("Format", ticket.ticketFormat)
    }
}

@Composable
private fun CompensationSection(ticket: TicketRecord) {
    DetailCard(title = "Delay & Compensation") {
        DetailRow("Was Delayed", if (ticket.wasDelayed) "Yes" else "No")
        if (ticket.wasDelayed && ticket.delayDuration.isNotEmpty()) {
            DetailRow("Delay Duration", ticket.delayDuration)
        }
        DetailRow("Pending Compensation", if (ticket.pendingCompensation) "Yes" else "No")
        if (ticket.compensation.isNotEmpty()) {
            DetailRow("Compensation", ticket.compensation)
        }
    }
}

@Composable
private fun AdditionalDetailsSection(ticket: TicketRecord) {
    DetailCard(title = "Additional Details") {
        ticket.railcard?.let { DetailRow("Railcard", it) }
        ticket.coach?.let { DetailRow("Coach", it) }
        ticket.seat?.let { DetailRow("Seat", it) }
        ticket.tocRouteRestriction?.let { DetailRow("Route Restriction", it) }
    }
}

@Composable
private fun LoyaltyProgramsSection(ticket: TicketRecord) {
    ticket.loyaltyProgram?.let { loyalty ->
        DetailCard(title = "Loyalty Programs") {
            loyalty.virginPoints?.let { DetailRow("Virgin Points", it) }
            loyalty.lnerCashValue?.let { DetailRow("LNER Perks", "£$it") }
            loyalty.clubAvantiJourneys?.let { DetailRow("Club Avanti Journeys", it) }
        }
    }
}

@Composable
private fun DetailCard(
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

@Composable
private fun DetailRow(label: String, value: String) {
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