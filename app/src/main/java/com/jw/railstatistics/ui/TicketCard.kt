package com.jw.railstatistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jw.railstatistics.data.TOCColors
import com.jw.railstatistics.data.TicketRecord
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TicketCard(
    ticket: TicketRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tocColor = TOCColors.getColorForTOC(ticket.toc)?.let { Color(it) } ?: Color.Gray
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with TOC color indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TOC color indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(tocColor, RoundedCornerShape(4.dp))
                )
                
                // Ticket type and class
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = ticket.ticketType,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = ticket.classType,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Journey details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Origin
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "From",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ticket.origin,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Arrow
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "To",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                
                // Destination
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "To",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ticket.destination,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date, time, and price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date and time
                Column {
                    Text(
                        text = formatDate(ticket.outboundDate),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = ticket.outboundTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Price
                Text(
                    text = ticket.price,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Additional details
            if (ticket.toc != null || ticket.wasDelayed || ticket.pendingCompensation) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // TOC
                    ticket.toc?.let { toc ->
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = {
                                Text(
                                    text = toc,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                    
                    // Delay indicator
                    if (ticket.wasDelayed) {
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = {
                                Text(
                                    text = "Delayed",
                                    fontSize = 10.sp,
                                    color = Color.Red
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Red.copy(alpha = 0.1f)
                            )
                        )
                    }
                    
                    // Compensation indicator
                    if (ticket.pendingCompensation) {
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = {
                                Text(
                                    text = "Compensation",
                                    fontSize = 10.sp,
                                    color = Color.Yellow.copy(alpha = 0.8f)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Yellow.copy(alpha = 0.1f)
                            )
                        )
                    }
                    
                    // Return indicator
                    if (ticket.isReturn) {
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = {
                                Text(
                                    text = "Return",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
            
            // Loyalty program indicators
            ticket.loyaltyProgram?.let { loyalty ->
                if (loyalty.virginPoints != null || loyalty.lnerCashValue != null || loyalty.clubAvantiJourneys != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        loyalty.virginPoints?.let { points ->
                            FilterChip(
                                selected = false,
                                onClick = { },
                                label = {
                                    Text(
                                        text = "Virgin: $points",
                                        fontSize = 10.sp,
                                        color = Color(0xFFE31837)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFE31837).copy(alpha = 0.1f)
                                )
                            )
                        }
                        
                        loyalty.lnerCashValue?.let { value ->
                            FilterChip(
                                selected = false,
                                onClick = { },
                                label = {
                                    Text(
                                        text = "LNER: £$value",
                                        fontSize = 10.sp,
                                        color = Color(0xFFCE0E2D)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFCE0E2D).copy(alpha = 0.1f)
                                )
                            )
                        }
                        
                        loyalty.clubAvantiJourneys?.let { journeys ->
                            FilterChip(
                                selected = false,
                                onClick = { },
                                label = {
                                    Text(
                                        text = "Avanti: $journeys",
                                        fontSize = 10.sp,
                                        color = Color(0xFF004354)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF004354).copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketCardCompact(
    ticket: TicketRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tocColor = TOCColors.getColorForTOC(ticket.toc)?.let { Color(it) } ?: Color.Gray
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TOC color indicator
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(tocColor, RoundedCornerShape(3.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Journey details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${ticket.origin} → ${ticket.destination}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatDate(ticket.outboundDate)} at ${ticket.outboundTime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Price
            Text(
                text = ticket.price,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
} 