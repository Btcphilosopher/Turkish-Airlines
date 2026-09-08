package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Flight
import com.example.data.Trip
import com.example.ui.TurkishAirlinesAppState
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowScreen(
    state: TurkishAirlinesAppState,
    selectedFlight: Flight,
    onPaymentConfirmed: (String?, (Boolean) -> Unit) -> Unit,
    onNavigateToTrips: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stepIndex by remember { mutableStateOf(0) } // 0: Fare, 1: Passenger, 2: Seat, 3: Extras, 4: Payment, 5: Confirmed
    val scope = rememberCoroutineScope()

    // Form inputs state
    var firstName by remember { mutableStateOf(state.user?.firstName ?: "Alex") }
    var lastName by remember { mutableStateOf(state.user?.lastName ?: "Morgan") }
    var dob by remember { mutableStateOf(state.user?.dob ?: "1990-05-15") }
    var nationality by remember { mutableStateOf(state.user?.nationality ?: "British") }
    var passport by remember { mutableStateOf(state.user?.passportNumber ?: "GBR987654A") }
    var email by remember { mutableStateOf(state.user?.email ?: "alex.morgan@milesandsmiles.com") }
    var phone by remember { mutableStateOf(state.user?.phone ?: "+44 7911 123456") }

    // Passport scanning simulation state
    var isScanningPassport by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0.0f) }

    // Seat Selection state
    var selectedSeat by remember { mutableStateOf<String?>(null) }
    val seatPrice = if (selectedSeat != null) 24.0 else 0.0

    // Extras states
    var extraBags by remember { mutableStateOf(0) }
    val extraBagsPrice = extraBags * 40.0
    var travelInsuranceSelected by remember { mutableStateOf(false) }
    val insurancePrice = if (travelInsuranceSelected) 15.0 else 0.0

    // Payment States
    var selectedPaymentMethod by remember { mutableStateOf("CREDIT CARD") } // CREDIT CARD, TK WALLET, MILES
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    val basePrice = when (state.search.cabinClass) {
        "Business" -> selectedFlight.price * 3.2
        else -> selectedFlight.price
    }
    val flightPrice = if (stepIndex >= 1) {
        when (stepIndex) {
            else -> basePrice
        }
    } else basePrice

    val totalCost = basePrice + seatPrice + extraBagsPrice + insurancePrice

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Navigation Header
        if (stepIndex < 5) {
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
                        if (stepIndex > 0) stepIndex-- else onBackPressed()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TKRed)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (stepIndex) {
                            0 -> "SELECT FARE"
                            1 -> "PASSENGER DETAILS"
                            2 -> "SELECT SEAT"
                            3 -> "SELECT EXTRAS"
                            else -> "PAYMENT"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${stepIndex + 1}/5",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Step Progress Line
            LinearProgressIndicator(
                progress = { (stepIndex + 1) / 5.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = TKRed,
                trackColor = TKLightGrey
            )
        }

        // Screens content routing
        Box(modifier = Modifier.weight(1f)) {
            when (stepIndex) {
                0 -> FareSelectionScreen(
                    basePrice = selectedFlight.price,
                    onFareSelected = { fare ->
                        // selectedFlight.price can adjust
                        stepIndex = 1
                    }
                )
                1 -> {
                    if (isScanningPassport) {
                        PassportScannerView(
                            progress = scanProgress,
                            onCancel = { isScanningPassport = false }
                        )
                        // Trigger mock scanning sequence
                        LaunchedEffect(Unit) {
                            scanProgress = 0.0f
                            while (scanProgress < 1.0f) {
                                delay(100)
                                scanProgress += 0.08f
                            }
                            firstName = "Alex"
                            lastName = "Morgan"
                            dob = "1990-05-15"
                            nationality = "British"
                            passport = "GBR987" + (1000..9999).random() + "A"
                            isScanningPassport = false
                        }
                    } else {
                        PassengerForm(
                            firstName = firstName,
                            lastName = lastName,
                            dob = dob,
                            nationality = nationality,
                            passport = passport,
                            email = email,
                            phone = phone,
                            onFirstNameChange = { firstName = it },
                            onLastNameChange = { lastName = it },
                            onDobChange = { dob = it },
                            onNationalityChange = { nationality = it },
                            onPassportChange = { passport = it },
                            onEmailChange = { email = it },
                            onPhoneChange = { phone = it },
                            onScanPassportClick = { isScanningPassport = true },
                            onNext = { stepIndex = 2 }
                        )
                    }
                }
                2 -> SeatSelectionScreen(
                    cabinClass = selectedFlight.cabinClass,
                    selectedSeat = selectedSeat,
                    onSeatSelected = { selectedSeat = it },
                    onNext = { stepIndex = 3 }
                )
                3 -> ExtrasSelectionScreen(
                    extraBags = extraBags,
                    onExtraBagsChange = { extraBags = it },
                    insuranceSelected = travelInsuranceSelected,
                    onInsuranceChange = { travelInsuranceSelected = it },
                    onNext = { stepIndex = 4 }
                )
                4 -> PaymentScreen(
                    totalAmount = totalCost,
                    flightAmount = basePrice,
                    seatAmount = seatPrice,
                    bagsAmount = extraBagsPrice,
                    insuranceAmount = insurancePrice,
                    method = selectedPaymentMethod,
                    onMethodChange = { selectedPaymentMethod = it },
                    cardNumber = cardNumber,
                    onCardNumberChange = { cardNumber = it },
                    cardExpiry = cardExpiry,
                    onExpiryChange = { cardExpiry = it },
                    cvv = cardCvv,
                    onCvvChange = { cardCvv = it },
                    isProcessing = state.isLoading,
                    errorMessage = state.error,
                    onSubmitPayment = {
                        onPaymentConfirmed(if (selectedPaymentMethod == "CREDIT CARD") cardNumber else null) { success ->
                            if (success) {
                                stepIndex = 5
                            }
                        }
                    }
                )
                5 -> BookingConfirmedScreen(
                    trip = state.trips.lastOrNull() ?: Trip(
                        id = "tk_confirmed",
                        bookingCode = "TK9A2P",
                        flightNumber = selectedFlight.flightNumber,
                        originCode = selectedFlight.originCode,
                        originCity = selectedFlight.originCity,
                        destCode = selectedFlight.destCode,
                        destCity = selectedFlight.destCity,
                        departureTime = selectedFlight.departureTime,
                        arrivalTime = selectedFlight.arrivalTime,
                        departureDate = state.search.departureDate,
                        passengerFirstName = firstName,
                        passengerLastName = lastName,
                        paymentAmount = totalCost,
                        paymentMethod = selectedPaymentMethod
                    ),
                    onClose = onNavigateToTrips
                )
            }
        }
    }
}

// --- SUB-SCREEN 1: FARE SELECTION ---
@Composable
fun FareSelectionScreen(
    basePrice: Double,
    onFareSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CHOOSE YOUR FARE CLASS",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        FareCard(
            title = "ECOFLY",
            price = basePrice,
            features = listOf(
                "1 x 23kg Checked Bag Included",
                "Earn up to 3,420 Miles",
                "Exchange fee applies for updates",
                "Non-refundable ticket"
            ),
            color = MaterialTheme.colorScheme.secondary,
            onSelect = { onFareSelected("ECOFLY") }
        )

        FareCard(
            title = "FLEXIBLE",
            price = basePrice + 42.0,
            features = listOf(
                "2 x 23kg Checked Bags Included",
                "Earn up to 4,500 Miles",
                "Free flight changes",
                "Refund available (minus administrative charge)",
                "Standard Seat Selection included"
            ),
            color = TKRed,
            onSelect = { onFareSelected("FLEXIBLE") }
        )

        FareCard(
            title = "BUSINESS CLASS",
            price = basePrice * 3.2,
            features = listOf(
                "2 x 32kg Checked Bags Included",
                "Earn up to 8,420 Miles",
                "Full refunds and free flight changes",
                "Turkish Airlines CIP Lounge Access",
                "Premium lie-flat M3 bed cabins"
            ),
            color = TKGold,
            onSelect = { onFareSelected("BUSINESS") }
        )
    }
}

@Composable
fun FareCard(
    title: String,
    price: Double,
    features: List<String>,
    color: Color,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, TKSilverBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Text(
                    text = "£${price.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TKRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feature, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CHOOSE $title", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// --- SUB-SCREEN 2: PASSENGER FORM WITH CAMERA SCANNING MOCK ---
@Composable
fun PassengerForm(
    firstName: String,
    lastName: String,
    dob: String,
    nationality: String,
    passport: String,
    email: String,
    phone: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDobChange: (String) -> Unit,
    onNationalityChange: (String) -> Unit,
    onPassportChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onScanPassportClick: () -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "PASSENGER DETAILS",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            // Passport scan trigger
            Button(
                onClick = onScanPassportClick,
                colors = ButtonDefaults.buttonColors(containerColor = TKGold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SCAN PASSPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth().testTag("passenger_firstname_input")
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth().testTag("passenger_lastname_input")
        )

        OutlinedTextField(
            value = dob,
            onValueChange = onDobChange,
            label = { Text("Date of Birth (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nationality,
            onValueChange = onNationalityChange,
            label = { Text("Nationality") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = passport,
            onValueChange = onPassportChange,
            label = { Text("Passport Number") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Mobile Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("passenger_next_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = TKRed),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("CONTINUE TO SEAT MAP", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PassportScannerView(
    progress: Float,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Simulated Camera Feed Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ALIGN PASSPORT PAGE IN FRAME",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                "Turkish Airlines Secure ID Scanner",
                color = TKGold,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Passport bounding bracket
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(2.dp, if (progress < 1.0f) Color.White else Color.Green, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                // Laser scan line animating
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.Center)
                        .background(TKRed)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text("Processing OCR Extractor: ${(progress * 100).toInt()}%", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, color = TKRed, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}

// --- SUB-SCREEN 3: INTERACTIVE SEAT SELECTION ---
@Composable
fun SeatSelectionScreen(
    cabinClass: String,
    selectedSeat: String?,
    onSeatSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    val isBusiness = cabinClass == "Business"
    val columns = if (isBusiness) listOf("A", "C", "", "D", "G", "", "K") else listOf("A", "B", "C", "", "D", "E", "F", "", "G", "H", "J")
    val rows = if (isBusiness) (1..4).toList() else (10..22).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "CHOOSE SEAT",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = if (isBusiness) "BUSINESS CABIN (2-3-2)" else "ECONOMY CABIN (3-3-3)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid map scrolling
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, TKSilverBorder, RoundedCornerShape(12.dp))
                .background(TKLightGrey.copy(alpha = 0.5f))
                .padding(12.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns.size),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                rows.forEach { rowNum ->
                    columns.forEach { col ->
                        val seatCode = "$rowNum$col"
                        if (col.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.size(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(rowNum.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                            }
                        } else {
                            val isOccupied = rowNum % 3 == 0 && col == "C"
                            val isSelected = selectedSeat == seatCode
                            val isPremium = rowNum == 12 || rowNum == 10 // Extra Legroom

                            item {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                isSelected -> TKRed
                                                isOccupied -> Color.Gray
                                                isPremium -> TKGold.copy(alpha = 0.3f)
                                                else -> Color.White
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) TKRed else if (isPremium) TKGold else TKSilverBorder,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable(enabled = !isOccupied) {
                                            onSeatSelected(seatCode)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = col,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else if (isOccupied) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            LegendItem("Available", Color.White, TKSilverBorder)
            LegendItem("Selected", TKRed, TKRed)
            LegendItem("Occupied", Color.Gray, Color.Gray)
            LegendItem("Legroom", TKGold.copy(alpha = 0.3f), TKGold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selection strip
        if (selectedSeat != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SEAT SELECTED", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$selectedSeat (Extra Legroom)", fontWeight = FontWeight.Bold)
                    }
                    Text("£24", fontWeight = FontWeight.Bold, color = TKRed, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("seat_next_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = TKRed),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(if (selectedSeat != null) "CONFIRM SEAT SELECTION" else "CONTINUE WITHOUT SEAT", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LegendItem(label: String, bg: Color, border: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(bg)
                .border(1.dp, border, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// --- SUB-SCREEN 4: EXTRAS SELECTION ---
@Composable
fun ExtrasSelectionScreen(
    extraBags: Int,
    onExtraBagsChange: (Int) -> Unit,
    insuranceSelected: Boolean,
    onInsuranceChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "CUSTOMISE YOUR FLIGHT",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        // Extra Baggage Selector card
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("ADDITIONAL CHECKED BAGGAGE", fontWeight = FontWeight.Bold)
                    Text("+23 kg baggage load allowance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("£40 per additional bag", fontSize = 12.sp, color = TKRed, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onExtraBagsChange((extraBags - 1).coerceAtLeast(0)) }) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                    }
                    Text(extraBags.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { onExtraBagsChange(extraBags + 1) }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }
            }
        }

        // Travel Insurance Card
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("PREMIUM TRAVEL INSURANCE", fontWeight = FontWeight.Bold)
                    Text("Covers delay, trip cancellation, and global medical expenses.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("£15 total package", fontSize = 12.sp, color = TKRed, fontWeight = FontWeight.Bold)
                }

                Checkbox(
                    checked = insuranceSelected,
                    onCheckedChange = onInsuranceChange,
                    colors = CheckboxDefaults.colors(checkedColor = TKRed)
                )
            }
        }

        // Upgrade banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TKGold.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, TKGold)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.HotelClass, contentDescription = null, tint = TKGold)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("CABIN HOSPITALITY UPGRADE", fontWeight = FontWeight.Bold, color = TKGold)
                    Text("Enjoy luxury Business meals & priority Boarding pass lanes.", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("extras_next_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = TKRed),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("CONTINUE TO PAYMENT", fontWeight = FontWeight.Bold)
        }
    }
}

// --- SUB-SCREEN 5: PAYMENTS VIEW ---
@Composable
fun PaymentScreen(
    totalAmount: Double,
    flightAmount: Double,
    seatAmount: Double,
    bagsAmount: Double,
    insuranceAmount: Double,
    method: String,
    onMethodChange: (String) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cardExpiry: String,
    onExpiryChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit,
    isProcessing: Boolean,
    errorMessage: String?,
    onSubmitPayment: () -> Unit
) {
    val scrollState = rememberScrollState()

    if (isProcessing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = TKRed, modifier = Modifier.size(52.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Processing payment securely...", fontWeight = FontWeight.Bold)
                Text("Connecting MockPaymentProvider...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Price breakdown summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TKLightGrey)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PRICE BREAKDOWN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base Flight Fee")
                        Text("£${flightAmount.toInt()}")
                    }
                    if (seatAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Seat Selection (Legroom)")
                            Text("£${seatAmount.toInt()}")
                        }
                    }
                    if (bagsAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Baggage (+23kg load)")
                            Text("£${bagsAmount.toInt()}")
                        }
                    }
                    if (insuranceAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Travel Insurance")
                            Text("£${insuranceAmount.toInt()}")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL COST", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("£${totalAmount.toInt()}", fontWeight = FontWeight.Black, color = TKRed, fontSize = 22.sp)
                    }
                }
            }

            Text("SELECT METHOD", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            // Method Picker Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TKLightGrey)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("CREDIT CARD", "TK WALLET", "MILES").forEach { m ->
                    val isSelected = method == m
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) TKRed else Color.Transparent)
                            .clickable { onMethodChange(m) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = m,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error display
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TKRed.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, TKRed)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = TKRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage, color = TKRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            when (method) {
                "CREDIT CARD" -> {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = onCardNumberChange,
                        label = { Text("Card Number") },
                        placeholder = { Text("4111 2222 3333 4444") },
                        modifier = Modifier.fillMaxWidth().testTag("credit_card_input")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = onExpiryChange,
                            label = { Text("Expiry Date") },
                            placeholder = { Text("MM/YY") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = onCvvChange,
                            label = { Text("CVV") },
                            placeholder = { Text("123") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                "TK WALLET" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("TK WALLET SECURE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Deduct directly from your saved Miles&Smiles linked secure pocket-book. Current Balance: £245.00", fontSize = 12.sp)
                        }
                    }
                }
                "MILES" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TKGold.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, TKGold)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("MILES REDEMPTION", fontWeight = FontWeight.Bold, color = TKGold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Redeem 25,000 Miles to cover full flight charges. Total Miles Balance: 84,250 Miles.", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmitPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_payment_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = TKRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CONFIRM AND PAY £${totalAmount.toInt()}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- SUB-SCREEN 6: CONFIRMED BOOKING SPLASH SCREEN ---
@Composable
fun BookingConfirmedScreen(
    trip: Trip,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon takeoff checkmark
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(TKRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "BOOKING CONFIRMED",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                "Thank you for choosing Turkish Airlines!",
                color = TKGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Flight details panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FROM", fontSize = 10.sp, color = Color.Gray)
                            Text(trip.originCode, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TKBlack)
                            Text(trip.originCity, fontSize = 12.sp, color = Color.Gray)
                        }

                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = TKRed,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.CenterVertically)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text("TO", fontSize = 10.sp, color = Color.Gray)
                            Text(trip.destCode, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TKRed)
                            Text(trip.destCity, fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FLIGHT", fontSize = 10.sp, color = Color.Gray)
                            Text(trip.flightNumber, fontWeight = FontWeight.Bold, color = TKBlack)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DATE", fontSize = 10.sp, color = Color.Gray)
                            Text(trip.departureDate, fontWeight = FontWeight.Bold, color = TKBlack)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("DEPARTURE", fontSize = 10.sp, color = Color.Gray)
                            Text(trip.departureTime, fontWeight = FontWeight.Bold, color = TKBlack)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TKLightGrey)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row {
                            Text("BOOKING CODE: ", fontSize = 12.sp, color = Color.Gray)
                            Text(trip.bookingCode, fontSize = 12.sp, fontWeight = FontWeight.Black, color = TKBlack)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirmed_view_trips_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = TKRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("VIEW MY TRIPS", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
