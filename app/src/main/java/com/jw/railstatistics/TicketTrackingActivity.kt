package com.jw.railstatistics

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jw.railstatistics.data.TicketRecord
import com.jw.railstatistics.ui.*
import com.jw.railstatistics.ui.theme.RailStatisticsTheme

class TicketTrackingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RailStatisticsTheme {
                TicketTrackingScreen(
                    onBackPressed = { finish() },
                    onTicketClick = { ticket ->
                        // Navigate to ticket detail
                        val intent = Intent(this, TicketDetailActivity::class.java).apply {
                            putExtra("ticket_id", ticket.id)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketTrackingScreen(
    onBackPressed: () -> Unit,
    onTicketClick: (TicketRecord) -> Unit,
    viewModel: TicketViewModel = viewModel(
        factory = TicketViewModelFactory(
            TicketRepository(
                AppDatabase.getDatabase(LocalContext.current).ticketDao(),
                TicketDataManager(LocalContext.current)
            )
        )
    )
) {
    val tickets by viewModel.tickets.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val csvImportResult by viewModel.csvImportResult.collectAsState()
    
    var showAddTicketDialog by remember { mutableStateOf(false) }
    var showStatisticsDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var showClearDataDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // CSV import launcher
    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importCSV(it) }
    }
    
    // CSV export launcher
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportCSV(it) }
    }
    
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
    
    // Filter tickets based on search
    val filteredTickets = remember(tickets, searchText, selectedYear) {
        tickets.filter { ticket ->
            val matchesSearch = searchText.isEmpty() || 
                ticket.origin.contains(searchText, ignoreCase = true) ||
                ticket.destination.contains(searchText, ignoreCase = true) ||
                ticket.ticketType.contains(searchText, ignoreCase = true) ||
                ticket.toc?.contains(searchText, ignoreCase = true) == true
            
            val matchesYear = selectedYear.isEmpty() || 
                ticket.outboundDate.endsWith(selectedYear)
            
            matchesSearch && matchesYear
        }
    }
    
    Scaffold(
        topBar = {
            // Custom header with frosted glass effect
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(16.dp)
            ) {
                // Header row
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
                        text = "Ticket Tracker",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search and action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search Tickets") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    IconButton(
                        onClick = { showAddTicketDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Ticket",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(8.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { showStatisticsDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Statistics",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(8.dp)
                        )
                    }
                    
                    Box {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(8.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Import CSV") },
                                leadingIcon = { Icon(Icons.Default.Download, null) },
                                onClick = {
                                    csvImportLauncher.launch("text/csv")
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export CSV") },
                                leadingIcon = { Icon(Icons.Default.Upload, null) },
                                onClick = {
                                    csvExportLauncher.launch("tickets_export.csv")
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear All Data") },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                onClick = {
                                    showClearDataDialog = true
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Year filter tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedYear.isEmpty(),
                            onClick = { viewModel.setSelectedYear("") },
                            label = { Text("All") }
                        )
                    }
                    
                    items(uniqueYears) { year ->
                        FilterChip(
                            selected = selectedYear == year,
                            onClick = { viewModel.setSelectedYear(year) },
                            label = { Text(year) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (filteredTickets.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Train,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tickets available",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Import, add, or create a new ticket to get started",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTickets) { ticket ->
                        TicketCard(
                            ticket = ticket,
                            onClick = { onTicketClick(ticket) }
                        )
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAddTicketDialog) {
        AlertDialog(
            onDismissRequest = { showAddTicketDialog = false },
            title = { Text("Add New Ticket") },
            text = {
                TicketForm(
                    onSave = { ticket ->
                        viewModel.addTicket(ticket)
                        showAddTicketDialog = false
                    },
                    onCancel = { showAddTicketDialog = false }
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
    
    if (showStatisticsDialog) {
        AlertDialog(
            onDismissRequest = { showStatisticsDialog = false },
            title = { Text("Ticket Statistics") },
            text = {
                TicketStatisticsScreen(
                    tickets = tickets,
                    onDismiss = { showStatisticsDialog = false }
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
    
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("This will delete all ticket data. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllTickets()
                        showClearDataDialog = false
                    }
                ) {
                    Text("Clear All", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // CSV import result dialog
    csvImportResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.clearCSVImportResult() },
            title = { Text("Import Complete") },
            text = {
                when (result) {
                    is CSVImportResult.Success -> {
                        Text("Successfully imported ${result.importedCount} tickets.")
                    }
                    is CSVImportResult.Error -> {
                        Text("Error: ${result.message}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearCSVImportResult() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Error message
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
} 