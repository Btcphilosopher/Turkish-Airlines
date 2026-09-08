package com.example.data.provider

import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

interface AirportProvider {
    fun getAirports(): List<Airport>
    fun searchAirports(query: String): List<Airport>
    fun getRecentSearches(): List<Airport>
}

interface FlightSearchProvider {
    suspend fun searchFlights(
        from: String,
        to: String,
        date: String,
        cabin: String
    ): List<Flight>
}

interface BookingProvider {
    suspend fun retrieveBooking(pnr: String, surname: String): Trip?
    suspend fun createTripFromBooking(flight: Flight, fareType: String, seat: String?, passenger: User): Trip
}

interface CheckInProvider {
    fun isEligibleForCheckIn(trip: Trip): Boolean
    suspend fun performCheckIn(trip: Trip, seat: String, baggageCount: Int, baggageWeight: Double): Trip
}

interface FlightStatusProvider {
    suspend fun getFlightStatus(flightNumber: String): Flight?
    fun trackFlight(flightNumber: String): Flow<FlightTrackingState>
}

data class FlightTrackingState(
    val flightNumber: String,
    val origin: String,
    val destination: String,
    val progress: Float, // 0.0 to 1.0
    val durationRemaining: String,
    val status: String, // DEPARTED, IN FLIGHT, LANDED, DELAYED
    val altitude: Int = 36000,
    val speed: Int = 540
)

interface PaymentProvider {
    suspend fun processPayment(
        amount: Double,
        method: String,
        cardNumber: String?,
        tkWalletCurrency: String?
    ): PaymentResult
}

sealed class PaymentResult {
    object Success : PaymentResult()
    data class Failure(val message: String) : PaymentResult()
}

interface MilesProvider {
    fun getLoyaltySnapshot(): User
    fun getFictionalOffers(): List<Offer>
}

interface WalletProvider {
    fun getBalance(currency: String): Double
    suspend fun topUp(amount: Double, currency: String): Double
    suspend fun payWithWallet(amount: Double, currency: String): Boolean
}

// Concrete Mock Implementations
class MockAirportProvider : AirportProvider {
    private val airports = listOf(
        Airport("LHR", "London Heathrow", "London", "United Kingdom"),
        Airport("LGW", "London Gatwick", "London", "United Kingdom"),
        Airport("IST", "Istanbul Airport", "Istanbul", "Turkey"),
        Airport("SAW", "Sabiha Gokcen", "Istanbul", "Turkey"),
        Airport("JFK", "John F. Kennedy", "New York", "United States"),
        Airport("HND", "Haneda Airport", "Tokyo", "Japan"),
        Airport("DXB", "Dubai International", "Dubai", "UAE"),
        Airport("SIN", "Changi Airport", "Singapore", "Singapore"),
        Airport("FCO", "Fiumicino", "Rome", "Italy"),
        Airport("CDG", "Charles de Gaulle", "Paris", "France"),
        Airport("CPT", "Cape Town International", "Cape Town", "South Africa"),
        Airport("BKK", "Suvarnabhumi", "Bangkok", "Thailand")
    )

    override fun getAirports() = airports

    override fun searchAirports(query: String): List<Airport> {
        if (query.isBlank()) return airports.take(4)
        val lower = query.lowercase().trim()
        return airports.filter {
            it.code.lowercase().contains(lower) ||
            it.city.lowercase().contains(lower) ||
            it.name.lowercase().contains(lower) ||
            it.country.lowercase().contains(lower)
        }
    }

    override fun getRecentSearches(): List<Airport> {
        return listOf(
            Airport("LHR", "London Heathrow", "London", "United Kingdom"),
            Airport("IST", "Istanbul Airport", "Istanbul", "Turkey")
        )
    }
}

class MockFlightSearchProvider : FlightSearchProvider {
    override suspend fun searchFlights(from: String, to: String, date: String, cabin: String): List<Flight> {
        delay(600) // Simulate network delay
        val basePrice = when {
            from == "LHR" && to == "IST" -> 284.0
            from == "IST" && to == "LHR" -> 310.0
            else -> 450.0
        }
        val isBusiness = cabin.equals("Business", ignoreCase = true)
        val multiplier = if (isBusiness) 3.2 else 1.0
        val finalPrice = basePrice * multiplier

        return listOf(
            Flight(
                id = "${from}_${to}_TK1988",
                flightNumber = "TK1988",
                originCode = from,
                originCity = if (from == "LHR") "London" else "Istanbul",
                originName = if (from == "LHR") "London Heathrow" else "Istanbul Airport",
                destCode = to,
                destCity = if (to == "IST") "Istanbul" else "London",
                destName = if (to == "IST") "Istanbul Airport" else "London Heathrow",
                departureTime = "18:45",
                arrivalTime = "01:40",
                duration = "3h 55m",
                stops = 0,
                aircraft = "Boeing 787-9 Dreamliner",
                baggageAllowance = if (isBusiness) "40 kg" else "30 kg",
                cabinClass = cabin,
                price = finalPrice,
                milesEarned = if (isBusiness) 8420 else 3420
            ),
            Flight(
                id = "${from}_${to}_TK1992",
                flightNumber = "TK1992",
                originCode = from,
                originCity = if (from == "LHR") "London" else "Istanbul",
                originName = if (from == "LHR") "London Heathrow" else "Istanbul Airport",
                destCode = to,
                destCity = if (to == "IST") "Istanbul" else "London",
                destName = if (to == "IST") "Istanbul Airport" else "London Heathrow",
                departureTime = "07:15",
                arrivalTime = "14:10",
                duration = "3h 55m",
                stops = 0,
                aircraft = "Airbus A350-900",
                baggageAllowance = if (isBusiness) "40 kg" else "30 kg",
                cabinClass = cabin,
                price = finalPrice - 20.0,
                milesEarned = if (isBusiness) 8200 else 3200
            ),
            Flight(
                id = "${from}_${to}_TK1996",
                flightNumber = "TK1996",
                originCode = from,
                originCity = if (from == "LHR") "London" else "Istanbul",
                originName = if (from == "LHR") "London Heathrow" else "Istanbul Airport",
                destCode = to,
                destCity = if (to == "IST") "Istanbul" else "London",
                destName = if (to == "IST") "Istanbul Airport" else "London Heathrow",
                departureTime = "12:30",
                arrivalTime = "19:25",
                duration = "3h 55m",
                stops = 1,
                aircraft = "Boeing 737MAX-9",
                baggageAllowance = if (isBusiness) "40 kg" else "30 kg",
                cabinClass = cabin,
                price = finalPrice * 0.85, // cheaper but has stop
                milesEarned = if (isBusiness) 7100 else 2800
            )
        )
    }
}

class MockBookingProvider : BookingProvider {
    override suspend fun retrieveBooking(pnr: String, surname: String): Trip? {
        delay(800)
        if (pnr.uppercase() == "ABC123" && surname.lowercase() == "morgan") {
            return Trip(
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
        }
        return null
    }

    override suspend fun createTripFromBooking(flight: Flight, fareType: String, seat: String?, passenger: User): Trip {
        return Trip(
            id = "trip_" + UUID.randomUUID().toString().take(6),
            bookingCode = UUID.randomUUID().toString().uppercase().take(6),
            flightNumber = flight.flightNumber,
            originCode = flight.originCode,
            originCity = flight.originCity,
            destCode = flight.destCode,
            destCity = flight.destCity,
            departureTime = flight.departureTime,
            arrivalTime = flight.arrivalTime,
            departureDate = "12 OCT",
            passengerFirstName = passenger.firstName,
            passengerLastName = passenger.lastName,
            seatNumber = seat,
            status = "BOOKED",
            checkedIn = false,
            baggageCount = 1,
            baggageWeight = 23.0,
            fareType = fareType,
            paymentAmount = flight.price + (if (seat != null) 24.0 else 0.0),
            paymentMethod = "CREDIT CARD"
        )
    }
}

class MockCheckInProvider : CheckInProvider {
    override fun isEligibleForCheckIn(trip: Trip): Boolean {
        // online check-in window: 24h before until 90 mins before departure
        return true // Enabled by default for all mock trips
    }

    override suspend fun performCheckIn(trip: Trip, seat: String, baggageCount: Int, baggageWeight: Double): Trip {
        delay(1000)
        return trip.copy(
            checkedIn = true,
            status = "CHECKED_IN",
            seatNumber = seat,
            baggageCount = baggageCount,
            baggageWeight = baggageWeight
        )
    }
}

class MockFlightStatusProvider : FlightStatusProvider {
    override suspend fun getFlightStatus(flightNumber: String): Flight? {
        delay(400)
        val upper = flightNumber.uppercase()
        if (upper.contains("1988") || upper.contains("TK1988")) {
            return Flight(
                id = "TK1988",
                flightNumber = "TK1988",
                originCode = "LHR",
                originCity = "London",
                originName = "London Heathrow",
                destCode = "IST",
                destCity = "Istanbul",
                destName = "Istanbul Airport",
                departureTime = "18:45",
                arrivalTime = "01:40",
                duration = "3h 55m",
                stops = 0,
                aircraft = "Boeing 787-9",
                baggageAllowance = "30 kg",
                cabinClass = "Economy",
                price = 284.0,
                milesEarned = 3420,
                status = "BOARDING"
            )
        }
        return null
    }

    override fun trackFlight(flightNumber: String): Flow<FlightTrackingState> = flow {
        var progress = 0.45f
        while (progress <= 1.0f) {
            val remainingMins = ((1.0f - progress) * 235).toInt()
            val remainingStr = "${remainingMins / 60}h ${remainingMins % 60}m"
            emit(
                FlightTrackingState(
                    flightNumber = flightNumber,
                    origin = "LHR",
                    destination = "IST",
                    progress = progress,
                    durationRemaining = remainingStr,
                    status = if (progress >= 1.0f) "LANDED" else "IN FLIGHT",
                    altitude = if (progress >= 0.95f) 4000 else 36000,
                    speed = if (progress >= 0.95f) 180 else 540
                )
            )
            progress += 0.05f
            delay(1500)
        }
    }
}

class MockPaymentProvider : PaymentProvider {
    override suspend fun processPayment(
        amount: Double,
        method: String,
        cardNumber: String?,
        tkWalletCurrency: String?
    ): PaymentResult {
        delay(1500) // processing animation delay
        if (cardNumber?.startsWith("4444") == true) {
            return PaymentResult.Failure("DECLINED: Insufficient Funds")
        }
        return PaymentResult.Success
    }
}

class MockMilesProvider : MilesProvider {
    override fun getLoyaltySnapshot() = User()

    override fun getFictionalOffers(): List<Offer> {
        return listOf(
            Offer("o1", "FLY TO ISTANBUL", "Experience the vibrant streets of Istanbul from only £199. Double Miles & Smiles earned this month.", "£199", "FLIGHT"),
            Offer("o2", "MILES & SMILES PLATINUM OFFER", "Earn up to 25,000 bonus Miles by booking Business Class flights through October.", "25,000 MILES", "MILES"),
            Offer("o3", "DISCOVER THE BALKANS", "Special rates on flights to Sarajevo, Skopje, and Belgrade.", "£149", "FLIGHT"),
            Offer("o4", "TK WALLET UPGRADE BONUS", "Load £200 or more in your TK Wallet and receive a 10% cash bonus instantly.", "+10% BONUS", "MILES")
        )
    }
}

class MockWalletProvider : WalletProvider {
    private var balanceMap = mutableMapOf(
        "GBP" to 245.00,
        "TRY" to 10800.0,
        "EUR" to 280.0,
        "USD" to 320.0
    )

    override fun getBalance(currency: String): Double {
        return balanceMap[currency] ?: 0.0
    }

    override suspend fun topUp(amount: Double, currency: String): Double {
        delay(500)
        val current = balanceMap[currency] ?: 0.0
        val newBalance = current + amount
        balanceMap[currency] = newBalance
        return newBalance
    }

    override suspend fun payWithWallet(amount: Double, currency: String): Boolean {
        val current = balanceMap[currency] ?: 0.0
        if (current >= amount) {
            balanceMap[currency] = current - amount
            return true
        }
        return false
    }
}
