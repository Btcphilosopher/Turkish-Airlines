package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.db.AppDatabase
import com.example.data.provider.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class TurkishAirlinesAppState(
    val user: User? = User(),
    val trips: List<Trip> = emptyList(),
    val selectedTrip: Trip? = null,
    val search: FlightSearchState = FlightSearchState(),
    val flightResults: List<Flight> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

private data class FlowPart1(
    val user: User?,
    val trips: List<Trip>,
    val selectedTrip: Trip?,
    val search: FlightSearchState
)

private data class FlowPart2(
    val flightResults: List<Flight>,
    val notifications: List<Notification>,
    val isLoading: Boolean,
    val error: String?
)

class TurkishAirlinesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val tripDao = db.tripDao()
    private val boardingPassDao = db.boardingPassDao()
    private val milesDao = db.milesDao()
    private val walletDao = db.walletDao()
    private val notificationDao = db.notificationDao()

    // Providers
    val airportProvider: AirportProvider = MockAirportProvider()
    val searchProvider: FlightSearchProvider = MockFlightSearchProvider()
    val bookingProvider: BookingProvider = MockBookingProvider()
    val checkInProvider: CheckInProvider = MockCheckInProvider()
    val flightStatusProvider: FlightStatusProvider = MockFlightStatusProvider()
    val paymentProvider: PaymentProvider = MockPaymentProvider()
    val milesProvider: MilesProvider = MockMilesProvider()
    val walletProvider: WalletProvider = MockWalletProvider()

    // Base UI and Developer state
    val isOffline = MutableStateFlow(false)
    val networkFailure = MutableStateFlow(false)
    val paymentFailureSimulation = MutableStateFlow(false)
    val selectedLanguage = MutableStateFlow("English")

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _searchState = MutableStateFlow(FlightSearchState())
    private val _flightResults = MutableStateFlow<List<Flight>>(emptyList())
    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    private val _user = MutableStateFlow(User())

    // Active flight tracking
    private val _activeTracking = MutableStateFlow<FlightTrackingState?>(null)
    val activeTracking: StateFlow<FlightTrackingState?> = _activeTracking.asStateFlow()

    // Booking Flow Temporary State
    val bookingSelectedFlight = MutableStateFlow<Flight?>(null)
    val bookingSelectedFareType = MutableStateFlow("ECOFLY") // ECOFLY, FLEXIBLE, BUSINESS
    val bookingSelectedSeat = MutableStateFlow<String?>(null)
    val bookingBaggageExtraCount = MutableStateFlow(0) // extra bags
    val bookingPaymentProcessing = MutableStateFlow(false)
    val bookingConfirmedTrip = MutableStateFlow<Trip?>(null)

    // Main App State
    val appState: StateFlow<TurkishAirlinesAppState> = combine(
        combine(_user, tripDao.getAllTrips(), _selectedTrip, _searchState) { user, trips, selectedTrip, search ->
            FlowPart1(user, trips, selectedTrip, search)
        },
        combine(_flightResults, notificationDao.getAllNotifications(), _isLoading, _error) { flightResults, notifications, isLoading, error ->
            FlowPart2(flightResults, notifications, isLoading, error)
        }
    ) { p1, p2 ->
        TurkishAirlinesAppState(
            user = p1.user,
            trips = p1.trips,
            selectedTrip = p1.selectedTrip,
            search = p1.search,
            flightResults = p2.flightResults,
            notifications = p2.notifications,
            isLoading = p2.isLoading,
            error = p2.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TurkishAirlinesAppState()
    )

    init {
        // Seed default database states
        viewModelScope.launch {
            tripDao.getAllTrips().first().let { currentTrips ->
                if (currentTrips.isEmpty()) {
                    seedDefaultData()
                }
            }
        }
    }

    private suspend fun seedDefaultData() {
        // Seed next trip: London -> Istanbul TK1988 on 12 OCT
        val defaultTrip = Trip(
            id = "mock_trip_1988",
            bookingCode = "ABC123",
            flightNumber = "TK1988",
            originCode = "LHR",
            originCity = "London",
            destCode = "IST",
            destCity = "Istanbul",
            departureTime = "18:45",
            arrivalTime = "01:40",
            departureDate = "12 OCT",
            passengerFirstName = "Alex",
            passengerLastName = "Morgan",
            seatNumber = "14A",
            status = "BOOKED",
            checkedIn = false,
            baggageCount = 1,
            baggageWeight = 23.0,
            fareType = "ECOFLY",
            paymentAmount = 284.0,
            paymentMethod = "TK WALLET"
        )
        tripDao.insertTrip(defaultTrip)

        // Seed initial notifications
        notificationDao.insertNotification(
            Notification(
                id = "n_welcome",
                title = "Welcome to Turkish Airlines",
                message = "Welcome Alex Morgan! Explore the world with your Elite Plus status.",
                timestamp = System.currentTimeMillis() - 86400000
            )
        )
        notificationDao.insertNotification(
            Notification(
                id = "n_checkin_soon",
                title = "Check-In Reminder",
                message = "Your flight TK1988 from London Heathrow is available for check-in soon.",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )

        // Seed some loyalty transactions
        milesDao.insertTransaction(
            MilesTransaction(amount = 8420, description = "Flight TK1988 LHR-IST", type = "EARNED", date = "08 SEP")
        )
        milesDao.insertTransaction(
            MilesTransaction(amount = 4100, description = "Flight TK1991 IST-LHR", type = "EARNED", date = "12 AUG")
        )
        milesDao.insertTransaction(
            MilesTransaction(amount = -25000, description = "Award Ticket Redeemed", type = "REDEEMED", date = "01 JUL")
        )

        // Seed some wallet transactions
        walletDao.insertTransaction(
            WalletTransaction(amount = 245.0, currency = "GBP", description = "Balance Carried Forward", date = "08 SEP")
        )
    }

    // Flights search execution
    fun executeFlightSearch() {
        if (networkFailure.value) {
            _error.value = "Network failure! Please check your connection."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val searchState = _searchState.value
                val results = searchProvider.searchFlights(
                    from = searchState.fromAirport?.code ?: "LHR",
                    to = searchState.toAirport?.code ?: "IST",
                    date = searchState.departureDate,
                    cabin = searchState.cabinClass
                )
                _flightResults.value = results
            } catch (e: Exception) {
                _error.value = "Failed to search flights. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchAirport(isFrom: Boolean, airport: Airport) {
        _searchState.value = if (isFrom) {
            _searchState.value.copy(fromAirport = airport)
        } else {
            _searchState.value.copy(toAirport = airport)
        }
    }

    fun updateSearchCabinAndPassengers(cabin: String, passengers: Int, type: String) {
        _searchState.value = _searchState.value.copy(
            cabinClass = cabin,
            passengers = passengers,
            searchType = type
        )
    }

    fun updateSearchDates(departure: String, returnDate: String) {
        _searchState.value = _searchState.value.copy(
            departureDate = departure,
            returnDate = returnDate
        )
    }

    fun selectTrip(trip: Trip?) {
        _selectedTrip.value = trip
    }

    // Retrieve boarding pass
    fun getBoardingPassForTrip(tripId: String, callback: (BoardingPass?) -> Unit) {
        viewModelScope.launch {
            val bp = boardingPassDao.getBoardingPassByTripId(tripId)
            callback(bp)
        }
    }

    // Start booking flight
    fun selectFlightForBooking(flight: Flight) {
        bookingSelectedFlight.value = flight
        bookingSelectedFareType.value = "ECOFLY"
        bookingSelectedSeat.value = null
        bookingBaggageExtraCount.value = 0
        bookingConfirmedTrip.value = null
    }

    // Confirm booking payment
    fun processBookingPayment(
        cardNumber: String?,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            bookingPaymentProcessing.value = true
            val flight = bookingSelectedFlight.value ?: return@launch
            val fare = bookingSelectedFareType.value
            val seat = bookingSelectedSeat.value ?: "14A"
            val extraBagsPrice = bookingBaggageExtraCount.value * 40.0
            val seatPrice = if (bookingSelectedSeat.value != null) 24.0 else 0.0
            val flightPrice = if (fare == "BUSINESS") flight.price * 3.2 else if (fare == "FLEXIBLE") flight.price + 42.0 else flight.price
            val total = flightPrice + seatPrice + extraBagsPrice

            val paymentMethod = if (cardNumber != null) "CREDIT CARD" else "TK WALLET"

            // Handle simulation payment failure
            if (paymentFailureSimulation.value) {
                delay(1200)
                bookingPaymentProcessing.value = false
                _error.value = "Payment Rejected! Simulated credit card authentication failed."
                callback(false)
                return@launch
            }

            val result = paymentProvider.processPayment(
                amount = total,
                method = paymentMethod,
                cardNumber = cardNumber,
                tkWalletCurrency = "GBP"
            )

            if (result is PaymentResult.Success) {
                // Create Trip & Boarding Pass
                val trip = Trip(
                    id = "trip_" + UUID.randomUUID().toString().take(6),
                    bookingCode = UUID.randomUUID().toString().uppercase().take(6),
                    flightNumber = flight.flightNumber,
                    originCode = flight.originCode,
                    originCity = flight.originCity,
                    destCode = flight.destCode,
                    destCity = flight.destCity,
                    departureTime = flight.departureTime,
                    arrivalTime = flight.arrivalTime,
                    departureDate = _searchState.value.departureDate,
                    passengerFirstName = _user.value.firstName,
                    passengerLastName = _user.value.lastName,
                    seatNumber = seat,
                    status = "BOOKED",
                    checkedIn = false,
                    baggageCount = 1 + bookingBaggageExtraCount.value,
                    baggageWeight = 23.0 + (bookingBaggageExtraCount.value * 23.0),
                    fareType = fare,
                    paymentAmount = total,
                    paymentMethod = paymentMethod
                )

                // Save to database
                tripDao.insertTrip(trip)

                // Save wallet transaction if payment was from TK Wallet
                if (paymentMethod == "TK WALLET") {
                    walletDao.insertTransaction(
                        WalletTransaction(
                            amount = -total,
                            currency = "GBP",
                            description = "Flight booking ${trip.bookingCode}",
                            date = "08 SEP"
                        )
                    )
                }

                // Insert notifications
                notificationDao.insertNotification(
                    Notification(
                        id = "n_booking_${trip.bookingCode}",
                        title = "Booking Confirmed",
                        message = "Your trip from ${trip.originCity} to ${trip.destCity} is confirmed! Code: ${trip.bookingCode}",
                        timestamp = System.currentTimeMillis()
                    )
                )

                bookingConfirmedTrip.value = trip
                bookingPaymentProcessing.value = false
                callback(true)
            } else {
                bookingPaymentProcessing.value = false
                _error.value = "Payment Failed! Provider returned error."
                callback(false)
            }
        }
    }

    // Guided Check-in operation
    fun performCheckIn(trip: Trip, seat: String, bags: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedTrip = checkInProvider.performCheckIn(
                    trip = trip,
                    seat = seat,
                    baggageCount = bags,
                    baggageWeight = bags * 23.0
                )
                // Save updated trip to local db
                tripDao.updateTrip(updatedTrip)

                // Generate and save Boarding Pass
                val pass = BoardingPass(
                    id = "bp_${updatedTrip.id}",
                    tripId = updatedTrip.id,
                    passengerName = "${updatedTrip.passengerFirstName} ${updatedTrip.passengerLastName}",
                    flightNumber = updatedTrip.flightNumber,
                    originCode = updatedTrip.originCode,
                    destCode = updatedTrip.destCode,
                    departureTime = updatedTrip.departureTime,
                    departureDate = updatedTrip.departureDate,
                    gate = updatedTrip.gate,
                    seat = seat,
                    cabinClass = if (updatedTrip.fareType == "BUSINESS") "Business" else "Economy",
                    qrCodeData = "TK_${updatedTrip.bookingCode}_${updatedTrip.flightNumber}_${seat}"
                )
                boardingPassDao.insertBoardingPass(pass)

                // Update selected trip state
                if (_selectedTrip.value?.id == trip.id) {
                    _selectedTrip.value = updatedTrip
                }

                // Loyalty Miles update simulation for check-in
                val milesToAdd = if (updatedTrip.fareType == "BUSINESS") 8420 else 3420
                _user.value = _user.value.copy(
                    milesBalance = _user.value.milesBalance + milesToAdd,
                    statusMiles = _user.value.statusMiles + (milesToAdd / 3)
                )
                milesDao.insertTransaction(
                    MilesTransaction(
                        amount = milesToAdd,
                        description = "Check-in Flight ${updatedTrip.flightNumber}",
                        type = "EARNED",
                        date = "08 SEP"
                    )
                )

                // Insert check-in notification
                notificationDao.insertNotification(
                    Notification(
                        id = "n_checkin_${updatedTrip.bookingCode}",
                        title = "Check-In Completed",
                        message = "You have successfully checked in for flight ${updatedTrip.flightNumber}. Seat $seat.",
                        timestamp = System.currentTimeMillis()
                    )
                )

                callback(true)
            } catch (e: Exception) {
                _error.value = "Check-In Failed! " + e.localizedMessage
                callback(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Retrieve Booking via PNR & Surname
    fun retrieveBooking(pnr: String, surname: String, callback: (Trip?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val trip = bookingProvider.retrieveBooking(pnr, surname)
            if (trip != null) {
                tripDao.insertTrip(trip)
                _selectedTrip.value = trip
                callback(trip)
            } else {
                _error.value = "No bookings found for PNR $pnr and Surname $surname."
                callback(null)
            }
            _isLoading.value = false
        }
    }

    // Top up TK Wallet balance
    fun topUpWallet(amount: Double, currency: String) {
        viewModelScope.launch {
            _isLoading.value = true
            walletProvider.topUp(amount, currency)
            walletDao.insertTransaction(
                WalletTransaction(
                    amount = amount,
                    currency = currency,
                    description = "Top Up",
                    date = "08 SEP"
                )
            )
            // Trigger UI update by force updating user state or balance
            val currentGbp = walletProvider.getBalance("GBP")
            _user.value = _user.value.copy() // Notify Flow listeners
            _isLoading.value = false
        }
    }

    // Cancel / Refund booking simulation
    fun cancelTrip(trip: Trip) {
        viewModelScope.launch {
            _isLoading.value = true
            tripDao.deleteTripById(trip.id)
            if (_selectedTrip.value?.id == trip.id) {
                _selectedTrip.value = null
            }
            // Refund wallet if booked via wallet
            if (trip.paymentMethod == "TK WALLET") {
                walletProvider.topUp(trip.paymentAmount, "GBP")
                walletDao.insertTransaction(
                    WalletTransaction(
                        amount = trip.paymentAmount,
                        currency = "GBP",
                        description = "Refund ${trip.bookingCode}",
                        date = "08 SEP"
                    )
                )
            }
            _isLoading.value = false
        }
    }

    // Flight status search
    fun searchFlightStatus(flightNumber: String, callback: (Flight?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val flight = flightStatusProvider.getFlightStatus(flightNumber)
            callback(flight)
            _isLoading.value = false
        }
    }

    // Live flight tracking visualizer stream
    fun startFlightTracking(flightNumber: String) {
        viewModelScope.launch {
            flightStatusProvider.trackFlight(flightNumber).collect { state ->
                _activeTracking.value = state
            }
        }
    }

    fun stopFlightTracking() {
        _activeTracking.value = null
    }

    // Clear error
    fun clearError() {
        _error.value = null
    }

    // Developer / Simulation Menu Trigger updates
    fun triggerSimulation(type: String) {
        viewModelScope.launch {
            when (type) {
                "DELAYED" -> {
                    // Find TK1988 trip and delay it
                    tripDao.getAllTrips().first().firstOrNull { it.flightNumber == "TK1988" }?.let { trip ->
                        val updated = trip.copy(
                            isDisrupted = true,
                            originalTime = "18:45",
                            updatedTime = "20:10",
                            status = "DISRUPTED"
                        )
                        tripDao.updateTrip(updated)
                        if (_selectedTrip.value?.id == trip.id) _selectedTrip.value = updated

                        notificationDao.insertNotification(
                            Notification(
                                id = "sim_delay_${System.currentTimeMillis()}",
                                title = "FLIGHT DELAY",
                                message = "Flight TK1988 is delayed from 18:45. New departure time: 20:10.",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                "GATE_CHANGE" -> {
                    tripDao.getAllTrips().first().firstOrNull { it.flightNumber == "TK1988" }?.let { trip ->
                        val updated = trip.copy(gate = "B14")
                        tripDao.updateTrip(updated)
                        if (_selectedTrip.value?.id == trip.id) _selectedTrip.value = updated

                        notificationDao.insertNotification(
                            Notification(
                                id = "sim_gate_${System.currentTimeMillis()}",
                                title = "GATE CHANGE",
                                message = "TK1988 departure gate changed from B12 to B14.",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                "BOARDING" -> {
                    notificationDao.insertNotification(
                        Notification(
                            id = "sim_boarding_${System.currentTimeMillis()}",
                            title = "BOARDING",
                            message = "Boarding has started for flight TK1988. Gate B12.",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                "OFFLINE_TOGGLE" -> {
                    isOffline.value = !isOffline.value
                }
                "PAYMENT_FAILURE_TOGGLE" -> {
                    paymentFailureSimulation.value = !paymentFailureSimulation.value
                }
                "NETWORK_FAILURE_TOGGLE" -> {
                    networkFailure.value = !networkFailure.value
                }
            }
        }
    }
}
