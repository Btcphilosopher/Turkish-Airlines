package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Trip
import com.example.ui.TurkishAirlinesAppState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    state: TurkishAirlinesAppState,
    onViewBoardingPass: (Trip) -> Unit,
    onStartCheckIn: (Trip) -> Unit,
    onTrackFlight: (String) -> Unit,
    onCancelTrip: (Trip) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("UPCOMING") } // UPCOMING, PAST
    var selectedTripDetail by remember { mutableStateOf<Trip?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (selectedTripDetail != null) {
            // TRIP DETAIL FULLSCREEN SHEET OVERLAY
            TripDetailView(
                trip = selectedTripDetail!!,
                onBackPressed = { selectedTripDetail = null },
                onStartCheckIn = {
                    onStartCheckIn(it)
                    selectedTripDetail = null
                },
                onViewBoardingPass = {
                    onViewBoardingPass(it)
                    selectedTripDetail = null
                },
                onTrackFlight = onTrackFlight,
                onCancelTrip = {
                    onCancelTrip(it)
                    selectedTripDetail = null
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "MY TRIPS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // UPCOMING / PAST TABS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TKLightGrey)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("UPCOMING", "PAST").forEach { tab ->
                        val isSelected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                .clickable { activeTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter lists based on tab
                val filteredTrips = remember(state.trips, activeTab) {
                    if (activeTab == "UPCOMING") {
                        state.trips // Simplification: assume all saved trips in db are upcoming
                    } else {
                        emptyList() // past trips empty mock
                    }
                }

                if (filteredTrips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AirplaneTicket,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Trips Found",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredTrips) { trip ->
                            TripCardItem(
                                trip = trip,
                                onClick = { selectedTripDetail = trip }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun TripCardItem(
    trip: Trip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("trip_item_card_${trip.bookingCode}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, TKSilverBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Booking Code: ${trip.bookingCode}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (trip.checkedIn) Color(0xFF4CAF50).copy(alpha = 0.15f) else TKRed.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (trip.checkedIn) "CHECKED IN" else "BOOKED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (trip.checkedIn) Color(0xFF4CAF50) else TKRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(trip.originCode, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Icon(
                    imageVector = Icons.Default.TrendingFlat,
                    contentDescription = null,
                    tint = TKRed,
                    modifier = Modifier.size(28.dp)
                )
                Text(trip.destCode, fontSize = 24.sp, fontWeight = FontWeight.Black, color = TKRed)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Date", fontSize = 10.sp, color = Color.Gray)
                    Text(trip.departureDate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("Departure", fontSize = 10.sp, color = Color.Gray)
                    Text(trip.departureTime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("Flight", fontSize = 10.sp, color = Color.Gray)
                    Text(trip.flightNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TripDetailView(
    trip: Trip,
    onBackPressed: () -> Unit,
    onStartCheckIn: (Trip) -> Unit,
    onViewBoardingPass: (Trip) -> Unit,
    onTrackFlight: (String) -> Unit,
    onCancelTrip: (Trip) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Detail Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TKRed)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "MY TRIP DETAILS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Card detailing route
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, TKSilverBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Route Visuals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(trip.originCode, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Text(trip.originCity, fontSize = 12.sp, color = Color.Gray)
                        Text(trip.departureTime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = TKRed,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.CenterVertically)
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(trip.destCode, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TKRed)
                        Text(trip.destCity, fontSize = 12.sp, color = Color.Gray)
                        Text(trip.arrivalTime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Code and Date row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("BOOKING CODE", fontSize = 10.sp, color = Color.Gray)
                        Text(trip.bookingCode, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("DEPARTURE DATE", fontSize = 10.sp, color = Color.Gray)
                        Text(trip.departureDate, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("FLIGHT NUMBER", fontSize = 10.sp, color = Color.Gray)
                        Text(trip.flightNumber, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Passenger Details & Seat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("PASSENGER", fontSize = 10.sp, color = Color.Gray)
                        Text("${trip.passengerFirstName} ${trip.passengerLastName}", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SEAT NUMBER", fontSize = 10.sp, color = Color.Gray)
                        Text(trip.seatNumber ?: "Not Selected", fontWeight = FontWeight.Bold, color = if (trip.seatNumber != null) TKRed else Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Baggage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("BAGGAGE COUNTER", fontSize = 10.sp, color = Color.Gray)
                        Text("${trip.baggageCount} Checked Bags", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TOTAL LOAD WEIGHT", fontSize = 10.sp, color = Color.Gray)
                        Text("${trip.baggageWeight} kg", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions grid list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("MANAGE SERVICES", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            // Dynamic Check-In / Boarding Pass trigger
            if (!trip.checkedIn) {
                Button(
                    onClick = { onStartCheckIn(trip) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("detail_start_checkin_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = TKRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("START CHECK-IN", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { onViewBoardingPass(trip) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("detail_view_boardingpass_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OPEN BOARDING PASS", fontWeight = FontWeight.Bold)
                }
            }

            // Secondary features
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onTrackFlight(trip.flightNumber) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("FLIGHT TRACK", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onCancelTrip(trip) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    border = BorderStroke(1.dp, TKRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TKRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CANCEL TRIP", fontSize = 12.sp)
                }
            }
        }
    }
}
