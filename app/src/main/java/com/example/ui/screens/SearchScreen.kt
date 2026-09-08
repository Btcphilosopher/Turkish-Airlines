package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Airport
import com.example.ui.TurkishAirlinesAppState
import com.example.ui.theme.TKLightGrey
import com.example.ui.theme.TKRed
import com.example.ui.theme.TKSilverBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: TurkishAirlinesAppState,
    onAirportSelected: (Boolean, Airport) -> Unit,
    onCabinSelected: (String, Int, String) -> Unit,
    onDatesChanged: (String, String) -> Unit,
    onSearchClicked: () -> Unit,
    airportsList: List<Airport>,
    onSearchAirports: (String) -> List<Airport>,
    recentAirports: List<Airport>,
    modifier: Modifier = Modifier
) {
    var searchType by remember { mutableStateOf("ROUND TRIP") } // ROUND TRIP, ONE WAY, MULTI CITY
    var cabinClass by remember { mutableStateOf("Economy") } // Economy, Business
    
    // Passenger count states
    var adults by remember { mutableStateOf(1) }
    var children by remember { mutableStateOf(0) }
    var infants by remember { mutableStateOf(0) }

    // Airport dialog states
    var isSelectingFrom by remember { mutableStateOf<Boolean?>(null) } // true for From, false for To, null for closed
    var airportQuery by remember { mutableStateOf("") }

    // Passenger dialog state
    var showPassengerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Header Title
            Text(
                text = "BOOK YOUR FLIGHT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Tabs layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TKLightGrey)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("ROUND TRIP", "ONE WAY", "MULTI CITY").forEach { type ->
                    val isSelected = searchType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { searchType = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // From Field Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSelectingFrom = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, TKSilverBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.FlightTakeoff, contentDescription = null, tint = TKRed)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("FROM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = state.search.fromAirport?.city ?: "Select Departure",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${state.search.fromAirport?.name ?: ""} (${state.search.fromAirport?.code ?: ""})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // To Field Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSelectingFrom = false },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, TKSilverBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.FlightLand, contentDescription = null, tint = TKRed)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("TO", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = state.search.toAirport?.city ?: "Select Destination",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (state.search.toAirport == null) MaterialTheme.colorScheme.onSurfaceVariant else TKRed
                        )
                        Text(
                            text = "${state.search.toAirport?.name ?: ""} (${state.search.toAirport?.code ?: ""})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date Selection row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDatesChanged("12 OCT", "19 OCT") },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, TKSilverBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DEPARTURE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(state.search.departureDate, fontWeight = FontWeight.Bold)
                    }
                }

                if (searchType == "ROUND TRIP") {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDatesChanged("12 OCT", "19 OCT") },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, TKSilverBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("RETURN", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.search.returnDate, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Passengers and Cabin details selection
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Passengers Field Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showPassengerDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, TKSilverBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PASSENGERS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${adults + children + infants} Passengers", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${adults} Adt, ${children} Chd, ${infants} Inf",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Cabin Field Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            cabinClass = if (cabinClass == "Economy") "Business" else "Economy"
                            onCabinSelected(cabinClass, adults + children + infants, searchType)
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, TKSilverBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CABIN CLASS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(cabinClass, fontWeight = FontWeight.Bold, color = if (cabinClass == "Business") TKRed else MaterialTheme.colorScheme.onSurface)
                        Text("Earn standard Miles", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Search CTA
            Button(
                onClick = onSearchClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("execute_search_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = TKRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SEARCH FLIGHTS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        // --- AIRPORT SELECTOR DIALOGUE OVERLAY ---
        if (isSelectingFrom != null) {
            val isFrom = isSelectingFrom == true
            AlertDialog(
                onDismissRequest = { isSelectingFrom = null; airportQuery = "" },
                confirmButton = {},
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxSize(),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isFrom) "Departure City" else "Destination City")
                        IconButton(onClick = { isSelectingFrom = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = airportQuery,
                            onValueChange = { airportQuery = it },
                            placeholder = { Text("Search city, country or code...") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("airport_search_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val filteredList = onSearchAirports(airportQuery)

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            if (airportQuery.isBlank()) {
                                item {
                                    Text(
                                        "RECENT SEARCHES",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(recentAirports) { airport ->
                                    AirportItem(airport) {
                                        onAirportSelected(isFrom, airport)
                                        isSelectingFrom = null
                                        airportQuery = ""
                                    }
                                }
                                item {
                                    Text(
                                        "ALL AIRPORTS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }

                            items(filteredList) { airport ->
                                AirportItem(airport) {
                                    onAirportSelected(isFrom, airport)
                                    isSelectingFrom = null
                                    airportQuery = ""
                                }
                            }
                        }
                    }
                }
            )
        }

        // --- PASSENGER COUNT SELECTOR ---
        if (showPassengerDialog) {
            AlertDialog(
                onDismissRequest = { showPassengerDialog = false },
                title = { Text("Select Passengers") },
                confirmButton = {
                    Button(onClick = {
                        showPassengerDialog = false
                        onCabinSelected(cabinClass, adults + children + infants, searchType)
                    }) {
                        Text("Confirm")
                    }
                },
                text = {
                    Column {
                        PassengerCounter(label = "Adults", count = adults, onCountChange = { adults = it.coerceAtLeast(1) })
                        Spacer(modifier = Modifier.height(12.dp))
                        PassengerCounter(label = "Children (2-11)", count = children, onCountChange = { children = it.coerceAtLeast(0) })
                        Spacer(modifier = Modifier.height(12.dp))
                        PassengerCounter(label = "Infants (Under 2)", count = infants, onCountChange = { infants = it.coerceAtLeast(0) })
                    }
                }
            )
        }
    }
}

@Composable
fun AirportItem(airport: Airport, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TKRed)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "${airport.city} (${airport.code})", fontWeight = FontWeight.Bold)
                Text(text = "${airport.name}, ${airport.country}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            text = airport.code,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
fun PassengerCounter(label: String, count: Int, onCountChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onCountChange(count - 1) }) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(text = count.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onCountChange(count + 1) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}
