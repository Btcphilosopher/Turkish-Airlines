package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Airport
import com.example.data.BoardingPass
import com.example.data.Flight
import com.example.data.Trip
import com.example.ui.TurkishAirlinesViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TKBlack
import com.example.ui.theme.TKRed
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    val viewModel: TurkishAirlinesViewModel = viewModel()
    val state by viewModel.appState.collectAsState()
    val activeTracking by viewModel.activeTracking.collectAsState()

    var activeTab by remember { mutableStateOf("HOME") } // HOME, BOOK, TRIPS, MILES, PROFILE
    var subFlowState by remember { mutableStateOf<String?>(null) } // null, "RESULTS", "BOOKING", "CHECKIN", "BOARDING_PASS", "TRACKING"
    
    var selectedFlightForBooking by remember { mutableStateOf<Flight?>(null) }
    var activeCheckInTrip by remember { mutableStateOf<Trip?>(null) }
    var activeBoardingPass by remember { mutableStateOf<BoardingPass?>(null) }

    // Toast alert state for real-time disruption simulation
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Intercept state changes to pop up realistic toast announcements (Section 21)
    LaunchedEffect(state.notifications) {
        state.notifications.lastOrNull()?.let { notification ->
            if (System.currentTimeMillis() - notification.timestamp < 10000) {
                toastMessage = "[${notification.title}] ${notification.message}"
            }
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(4000)
            toastMessage = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (subFlowState == null && activeTracking == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("app_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = activeTab == "HOME",
                        onClick = { activeTab = "HOME" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = TKRed, selectedTextColor = TKRed)
                    )
                    NavigationBarItem(
                        selected = activeTab == "BOOK",
                        onClick = { activeTab = "BOOK" },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Book") },
                        label = { Text("Book", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = TKRed, selectedTextColor = TKRed)
                    )
                    NavigationBarItem(
                        selected = activeTab == "TRIPS",
                        onClick = { activeTab = "TRIPS" },
                        icon = { Icon(Icons.Default.AirplaneTicket, contentDescription = "Trips") },
                        label = { Text("Trips", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = TKRed, selectedTextColor = TKRed)
                    )
                    NavigationBarItem(
                        selected = activeTab == "MILES",
                        onClick = { activeTab = "MILES" },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Miles") },
                        label = { Text("Miles", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = TKRed, selectedTextColor = TKRed)
                    )
                    NavigationBarItem(
                        selected = activeTab == "PROFILE",
                        onClick = { activeTab = "PROFILE" },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = TKRed, selectedTextColor = TKRed)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content router
            if (activeTracking != null) {
                // Intercept Tracker full overlay (Section 23)
                FlightStatusScreen(
                    state = state,
                    trackingState = activeTracking,
                    onSearchStatus = { query, callback -> viewModel.searchFlightStatus(query, callback) },
                    onStartTracking = { query -> viewModel.startFlightTracking(query) },
                    onStopTracking = { viewModel.stopFlightTracking() }
                )
            } else {
                when (subFlowState) {
                    "RESULTS" -> {
                        FlightResultsScreen(
                            state = state,
                            onFlightSelected = { flight ->
                                selectedFlightForBooking = flight
                                viewModel.selectFlightForBooking(flight)
                                subFlowState = "BOOKING"
                            },
                            onBackPressed = { subFlowState = null }
                        )
                    }
                    "BOOKING" -> {
                        if (selectedFlightForBooking != null) {
                            BookingFlowScreen(
                                state = state,
                                selectedFlight = selectedFlightForBooking!!,
                                onPaymentConfirmed = { card, onResult ->
                                    viewModel.processBookingPayment(card) { success ->
                                        onResult(success)
                                    }
                                },
                                onNavigateToTrips = {
                                    subFlowState = null
                                    activeTab = "TRIPS"
                                },
                                onBackPressed = { subFlowState = "RESULTS" }
                            )
                        }
                    }
                    "CHECKIN" -> {
                        if (activeCheckInTrip != null) {
                            CheckInScreen(
                                trip = activeCheckInTrip!!,
                                onCheckInConfirmed = { trip, seat, bags, onResult ->
                                    viewModel.performCheckIn(trip, seat, bags) { success ->
                                        onResult(success)
                                        if (success) {
                                            viewModel.getBoardingPassForTrip(trip.id) { bp ->
                                                if (bp != null) {
                                                    activeBoardingPass = bp
                                                    subFlowState = "BOARDING_PASS"
                                                } else {
                                                    subFlowState = null
                                                    activeTab = "TRIPS"
                                                }
                                            }
                                        }
                                    }
                                },
                                onBackPressed = { subFlowState = null }
                            )
                        }
                    }
                    "BOARDING_PASS" -> {
                        if (activeBoardingPass != null) {
                            BoardingPassScreen(
                                boardingPass = activeBoardingPass!!,
                                onBackPressed = {
                                    subFlowState = null
                                    activeTab = "TRIPS"
                                }
                            )
                        }
                    }
                    else -> {
                        // Regular Tab screen views
                        when (activeTab) {
                            "HOME" -> {
                                HomeScreen(
                                    state = state,
                                    onNavigateToSearch = { activeTab = "BOOK" },
                                    onNavigateToTrips = { activeTab = "TRIPS" },
                                    onViewTripDetails = { trip ->
                                        // Open details drawer
                                        activeTab = "TRIPS"
                                    },
                                    onQuickSearch = { from, to ->
                                        viewModel.updateSearchAirport(true, from)
                                        viewModel.updateSearchAirport(false, to)
                                        activeTab = "BOOK"
                                    }
                                )
                            }
                            "BOOK" -> {
                                SearchScreen(
                                    state = state,
                                    onAirportSelected = { isFrom, airport ->
                                        viewModel.updateSearchAirport(isFrom, airport)
                                    },
                                    onCabinSelected = { cabin, count, type ->
                                        viewModel.updateSearchCabinAndPassengers(cabin, count, type)
                                    },
                                    onDatesChanged = { dep, ret ->
                                        viewModel.updateSearchDates(dep, ret)
                                    },
                                    onSearchClicked = {
                                        viewModel.executeFlightSearch()
                                        subFlowState = "RESULTS"
                                    },
                                    airportsList = listOf(
                                        Airport("LHR", "London Heathrow", "London", "United Kingdom"),
                                        Airport("IST", "Istanbul Airport", "Istanbul", "Turkey"),
                                        Airport("FCO", "Fiumicino", "Rome", "Italy"),
                                        Airport("HND", "Haneda Airport", "Tokyo", "Japan"),
                                        Airport("JFK", "John F. Kennedy", "New York", "United States")
                                    ),
                                    onSearchAirports = { query ->
                                        // Quick query filter
                                        val list = listOf(
                                            Airport("LHR", "London Heathrow", "London", "United Kingdom"),
                                            Airport("IST", "Istanbul Airport", "Istanbul", "Turkey"),
                                            Airport("FCO", "Fiumicino", "Rome", "Italy"),
                                            Airport("HND", "Haneda Airport", "Tokyo", "Japan"),
                                            Airport("JFK", "John F. Kennedy", "New York", "United States")
                                        )
                                        if (query.isBlank()) list else list.filter {
                                            it.city.contains(query, true) || it.code.contains(query, true)
                                        }
                                    },
                                    recentAirports = listOf(
                                        Airport("LHR", "London Heathrow", "London", "United Kingdom"),
                                        Airport("IST", "Istanbul Airport", "Istanbul", "Turkey")
                                    )
                                )
                            }
                            "TRIPS" -> {
                                TripsScreen(
                                    state = state,
                                    onViewBoardingPass = { trip ->
                                        viewModel.getBoardingPassForTrip(trip.id) { bp ->
                                            if (bp != null) {
                                                activeBoardingPass = bp
                                                subFlowState = "BOARDING_PASS"
                                            }
                                        }
                                    },
                                    onStartCheckIn = { trip ->
                                        activeCheckInTrip = trip
                                        subFlowState = "CHECKIN"
                                    },
                                    onTrackFlight = { code ->
                                        viewModel.startFlightTracking(code)
                                    },
                                    onCancelTrip = { trip ->
                                        viewModel.cancelTrip(trip)
                                    }
                                )
                            }
                            "MILES" -> {
                                MilesScreen(
                                    state = state,
                                    onTopUpWallet = { amt, cur ->
                                        viewModel.topUpWallet(amt, cur)
                                    }
                                )
                            }
                            "PROFILE" -> {
                                ProfileScreen(
                                    state = state,
                                    onLanguageSelected = { lang ->
                                        // Stub language
                                    },
                                    onSimulateAction = { act ->
                                        when (act) {
                                            "delay" -> viewModel.triggerSimulation("DELAYED")
                                            "gate_change" -> viewModel.triggerSimulation("GATE_CHANGE")
                                            "boarding" -> viewModel.triggerSimulation("BOARDING")
                                            "offline" -> viewModel.triggerSimulation("OFFLINE_TOGGLE")
                                            "payment_fail" -> viewModel.triggerSimulation("PAYMENT_FAILURE_TOGGLE")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Real-Time Notification Floating Overlay (Section 21)
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                if (toastMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { toastMessage = null }
                            .testTag("notification_toast"),
                        colors = CardDefaults.cardColors(containerColor = TKBlack),
                        border = BorderStroke(1.dp, TKRed),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(TKRed)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = toastMessage!!,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Alert",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
