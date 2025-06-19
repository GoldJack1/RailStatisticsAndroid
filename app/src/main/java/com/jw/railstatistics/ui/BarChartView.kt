package com.jw.railstatistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartData(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun BarChartView(
    data: List<ChartData>,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (data.isNotEmpty()) {
                val maxValue = data.maxOfOrNull { it.value } ?: 1.0
                val sortedData = data.sortedByDescending { it.value }
                
                sortedData.forEach { item ->
                    BarChartItem(
                        label = item.label,
                        value = item.value,
                        maxValue = maxValue,
                        color = item.color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
private fun BarChartItem(
    label: String,
    value: Double,
    maxValue: Double,
    color: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        ) {
            // Background bar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(10.dp)
                    )
            )
            
            // Value bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (value / maxValue).toFloat())
                    .background(color, RoundedCornerShape(10.dp))
            )
            
            // Value text
            Text(
                text = value.toInt().toString(),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<Pair<String, Int>>,
    title: String,
    modifier: Modifier = Modifier
) {
    val chartData = data.map { (label, value) ->
        ChartData(
            label = label,
            value = value.toDouble(),
            color = MaterialTheme.colorScheme.primary
        )
    }
    
    BarChartView(
        data = chartData,
        title = title,
        modifier = modifier
    )
} 