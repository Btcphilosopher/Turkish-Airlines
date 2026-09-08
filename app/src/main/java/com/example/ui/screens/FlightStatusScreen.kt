package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Flight
import com.example.data.provider.FlightTrackingState
import com.example.ui.TurkishAirlinesAppState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightStatusScreen(
    state: TurkishAirlinesAppState,
    trackingState: FlightTrackingState?,
    onSearchStatus: (String, (Flight?) -> Unit) -> Unit,
    onStartTracking: (String) -> Unit,
    onStopTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    var flightQuery by remember { mutableStateOf("TK1988") }
    var lookupResult by remember { mutableStateOf<Flight?>(null) }
    var searchExecuted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (trackingState != null) {
                IconButton(onClick = onStopTracking) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TKRed)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (trackingState != null) "LIVE FLIGHT TRACKER" else "FLIGHT STATUS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (trackingState != null) {
            // --- LIVE AIRCRAFT TRACKER ANIMATED VIEW (Section 23) ---
            LiveFlightTrackerWidget(
                state = trackingState,
                onClose = onStopTracking
            )
        } else {
            // --- STANDARD LOOKUP FIELD ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Track a flight status in real-time. Enter the flight code below:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = flightQuery,
                    onValueChange = { flightQuery = it },
                    label = { Text("Flight Code") },
                    placeholder = { Text("e.g. TK1988") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Flight, contentDescription = null, tint = TKRed) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flight_status_input")
                )

                Button(
                    onClick = {
                        onSearchStatus(flightQuery) { result ->
                            lookupResult = result
                            searchExecuted = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("flight_status_search_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = TKRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CHECK STATUS", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lookup Details Result
                if (searchExecuted) {
                    val result = lookupResult
                    if (result != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("flight_status_result_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, TKSilverBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = result.flightNumber,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )

                                    // Badge Status
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (result.status) {
                                                    "BOARDING" -> TKRed.copy(alpha = 0.15f)
                                                    "DELAYED" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                                    else -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                }
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = result.status,
                                            fontWeight = FontWeight.Bold,
                                            color = when (result.status) {
                                                "BOARDING" -> TKRed
                                                "DELAYED" -> Color(0xFFFF9800)
                                                else -> Color(0xFF4CAF50)
                                            },
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("FROM", fontSize = 10.sp, color = Color.Gray)
                                        Text(result.originCity, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(result.originName, fontSize = 11.sp, color = Color.Gray)
                                        Text(result.departureTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TKBlack)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("TO", fontSize = 10.sp, color = Color.Gray)
                                        Text(result.destCity, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TKRed)
                                        Text(result.destName, fontSize = 11.sp, color = Color.Gray)
                                        Text(result.arrivalTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TKBlack)
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = { onStartTracking(result.flightNumber) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("launch_live_tracker_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.TravelExplore, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("LAUNCH REAL-TIME TRACKING", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Not found state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Flight code not recognized. Try searching 'TK1988'.",
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveFlightTrackerWidget(
    state: FlightTrackingState,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("live_tracker_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "ACTIVE ROUTE: ${state.flightNumber}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TKGold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Animated airplane path
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                TKGraphite
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(state.origin, fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)

                    // Track layout with airplane icon sliding based on state.progress
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Track background line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                        )

                        // Progress line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(state.progress)
                                .height(2.dp)
                                .background(TKRed)
                        )

                        // Moving aircraft
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(state.progress)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(TKRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Text(state.destination, fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Telemetry Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TKLightGrey)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ALTITUDE", fontSize = 10.sp, color = Color.Gray)
                        Text("${state.altitude} FT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TKLightGrey)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("GROUND SPEED", fontSize = 10.sp, color = Color.Gray)
                        Text("${state.speed} KTS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TKRed.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, TKRed.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ESTIMATED REMAINING", fontSize = 11.sp, color = Color.Gray)
                    Text(state.durationRemaining, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = TKRed)
                    Text("Status: ${state.status}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TKRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("DISMISS TRACKER", fontWeight = FontWeight.Bold)
            }
        }
    }
}
