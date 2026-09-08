package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Flight
import com.example.ui.TurkishAirlinesAppState
import com.example.ui.theme.TKGold
import com.example.ui.theme.TKLightGrey
import com.example.ui.theme.TKRed
import com.example.ui.theme.TKSilverBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightResultsScreen(
    state: TurkishAirlinesAppState,
    onFlightSelected: (Flight) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sortBy by remember { mutableStateOf("RECOMMENDED") } // RECOMMENDED, CHEAPEST, FASTEST, EARLIEST
    var showOnlyNonstop by remember { mutableStateOf(false) }
    var expandedFlightId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Branded Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TKRed)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "${state.search.fromAirport?.code ?: "LHR"} ➔ ${state.search.toAirport?.code ?: "IST"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.search.departureDate} • ${state.search.passengers} Passenger • ${state.search.cabinClass}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sorting & Filter bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort chip selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("RECOMMENDED", "CHEAPEST", "EARLIEST").forEach { option ->
                    val isSelected = sortBy == option
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.secondary else TKLightGrey)
                            .clickable { sortBy = option }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = option,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Nonstop toggle chip
            FilterChip(
                selected = showOnlyNonstop,
                onClick = { showOnlyNonstop = !showOnlyNonstop },
                label = { Text("Nonstop Only", fontSize = 10.sp) },
                leadingIcon = if (showOnlyNonstop) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                } else null
            )
        }

        // Flight lists
        val results = remember(state.flightResults, sortBy, showOnlyNonstop) {
            var list = state.flightResults
            if (showOnlyNonstop) {
                list = list.filter { it.stops == 0 }
            }
            when (sortBy) {
                "CHEAPEST" -> list.sortedBy { it.price }
                "EARLIEST" -> list.sortedBy { it.departureTime }
                else -> list // Recommended is default
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TKRed)
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SentimentDissatisfied,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Flights Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "We couldn't find any flights for this route. Try changing your search filters or dates.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(results) { flight ->
                    val isExpanded = expandedFlightId == flight.id
                    FlightCardItem(
                        flight = flight,
                        isExpanded = isExpanded,
                        onExpandClick = {
                            expandedFlightId = if (isExpanded) null else flight.id
                        },
                        onSelectClick = { onFlightSelected(flight) }
                    )
                }
                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }
}

@Composable
fun FlightCardItem(
    flight: Flight,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick)
            .testTag("flight_result_card_${flight.flightNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, TKSilverBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Flight ID and Aircraft
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TKRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = flight.flightNumber,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (flight.cabinClass == "Business") TKGold.copy(alpha = 0.15f) else TKLightGrey)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = flight.cabinClass.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (flight.cabinClass == "Business") TKGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time & Route Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Origin Time
                Column(modifier = Modifier.width(60.dp)) {
                    Text(
                        text = flight.departureTime,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = flight.originCode,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Route Progress Indicator
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = flight.duration,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = TKRed
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Text(
                        text = if (flight.stops == 0) "NONSTOP" else "${flight.stops} STOP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (flight.stops == 0) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }

                // Destination Time
                Column(
                    modifier = Modifier.width(60.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = flight.arrivalTime,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = flight.destCode,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing & Expand Chevron Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WorkOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Allowance: ${flight.baggageAllowance}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "£${flight.price.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TKRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable details with smooth animation
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("AIRCRAFT TYPE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(flight.aircraft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("MILES TO EARN", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+${flight.milesEarned} Miles", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TKGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("FARE CONDITIONS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Free cabin baggage allowance up to 8 kg\n• Changes permitted for an additional administrative fee\n• Fully refurnished M3 cabin hospitality", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onSelectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("select_flight_card_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = TKRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SELECT THIS FLIGHT", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
