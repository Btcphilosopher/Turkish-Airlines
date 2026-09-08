package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    trip: Trip,
    onCheckInConfirmed: (Trip, String, Int, (Boolean) -> Unit) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var checkInStep by remember { mutableStateOf(0) } // 0: Passengers, 1: Seat, 2: Baggage, 3: Confirm
    var selectedSeat by remember { mutableStateOf(trip.seatNumber ?: "14A") }
    var bagsCount by remember { mutableStateOf(trip.baggageCount) }
    var isCheckingIn by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (checkInStep > 0) checkInStep-- else onBackPressed()
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TKRed)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ONLINE CHECK-IN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${checkInStep + 1}/4",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { (checkInStep + 1) / 4.0f },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = Color(0xFF4CAF50),
            trackColor = TKLightGrey
        )

        if (isCheckingIn) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50), modifier = Modifier.size(52.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Registering Check-In with Turkish Airlines...", fontWeight = FontWeight.Bold)
                    Text("Generating Digital Boarding Pass offline copy...", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (checkInStep) {
                    0 -> {
                        // PASSENGER CONFIRMATION
                        Text("1. CONFIRM PASSENGER", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Please select passengers eligible for online check-in:", fontSize = 12.sp, color = Color.Gray)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = TKRed, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("${trip.passengerFirstName} ${trip.passengerLastName}", fontWeight = FontWeight.Bold)
                                        Text("Passport: ${trip.bookingCode}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Checkbox(checked = true, onCheckedChange = {}, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50)))
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { checkInStep = 1 },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("checkin_passengers_next_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("CONFIRM PASSENGERS", fontWeight = FontWeight.Bold)
                        }
                    }

                    1 -> {
                        // SEAT CHOICE
                        Text("2. CONFIRM SEAT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Confirm your flight seat assignment below:", fontSize = 12.sp, color = Color.Gray)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, TKSilverBorder),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("CURRENT SEAT", fontSize = 11.sp, color = Color.Gray)
                                    Text(selectedSeat, fontSize = 24.sp, fontWeight = FontWeight.Black, color = TKRed)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("14A", "14B", "15C", "15D").forEach { s ->
                                        val isSel = selectedSeat == s
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) TKRed else TKLightGrey)
                                                .clickable { selectedSeat = s }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(s, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Black)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { checkInStep = 2 },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("checkin_seat_next_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("CONFIRM SEAT", fontWeight = FontWeight.Bold)
                        }
                    }

                    2 -> {
                        // DECLARE BAGGAGE
                        Text("3. DECLARE BAGGAGE", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Declare the total number of checked-in suitcases you will drop off at the airport counter:", fontSize = 12.sp, color = Color.Gray)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, TKSilverBorder),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("CHECKED SUITCASES", fontWeight = FontWeight.Bold)
                                    Text("Allowed load: Up to 23 kg per bag", fontSize = 12.sp, color = Color.Gray)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { bagsCount = (bagsCount - 1).coerceAtLeast(0) }) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                                    }
                                    Text(bagsCount.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp), fontSize = 18.sp)
                                    IconButton(onClick = { bagsCount = bagsCount + 1 }) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { checkInStep = 3 },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("checkin_baggage_next_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("CONFIRM BAGGAGE", fontWeight = FontWeight.Bold)
                        }
                    }

                    3 -> {
                        // CONFIRM CHECK IN
                        Text("4. DECLARATION CONFIRMATION", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Please read and accept the security terms before completing online check-in:", fontSize = 12.sp, color = Color.Gray)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = TKLightGrey)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("SECURITY WARNING", fontWeight = FontWeight.Bold, color = TKRed, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("You are strictly prohibited from carrying hazardous materials (lithium batteries, compressed gases, corrosive liquids) inside your checked luggage or hand baggage.", fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                isCheckingIn = true
                                onCheckInConfirmed(trip, selectedSeat, bagsCount) { success ->
                                    isCheckingIn = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("checkin_submit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("ACCEPT & COMPLETE CHECK-IN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
