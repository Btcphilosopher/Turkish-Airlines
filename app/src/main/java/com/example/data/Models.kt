package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class User(
    val id: String = "1",
    val firstName: String = "Alex",
    val lastName: String = "Morgan",
    val email: String = "alex.morgan@milesandsmiles.com",
    val phone: String = "+44 7911 123456",
    val dob: String = "1990-05-15",
    val nationality: String = "British",
    val passportNumber: String = "GBR987654A",
    val milesAndSmilesNumber: String = "TK84250198",
    val tier: String = "Elite Plus", // Classic, Classic Plus, Elite, Elite Plus
    val milesBalance: Int = 84250,
    val statusMiles: Int = 23400
)

data class Airport(
    val code: String,
    val name: String,
    val city: String,
    val country: String
)

@Entity(tableName = "flights")
data class Flight(
    @PrimaryKey val id: String,
    val flightNumber: String,
    val originCode: String,
    val originCity: String,
    val originName: String,
    val destCode: String,
    val destCity: String,
    val destName: String,
    val departureTime: String, // e.g. "18:45"
    val arrivalTime: String, // e.g. "01:40"
    val duration: String, // e.g. "3h 55m"
    val stops: Int = 0,
    val aircraft: String = "Boeing 787-9 Dreamliner",
    val baggageAllowance: String = "30 kg",
    val cabinClass: String = "Economy", // Economy, Business
    val price: Double,
    val milesEarned: Int,
    val status: String = "SCHEDULED" // SCHEDULED, DELAYED, CANCELLED, BOARDING, DEPARTED, IN_FLIGHT, LANDED
)

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val id: String,
    val bookingCode: String,
    val flightNumber: String,
    val originCode: String,
    val originCity: String,
    val destCode: String,
    val destCity: String,
    val departureTime: String,
    val arrivalTime: String,
    val departureDate: String, // e.g. "12 OCT"
    val passengerFirstName: String,
    val passengerLastName: String,
    val seatNumber: String? = null,
    val status: String = "BOOKED", // BOOKED, CHECKED_IN, DISRUPTED
    val checkedIn: Boolean = false,
    val baggageCount: Int = 1,
    val baggageWeight: Double = 23.0,
    val fareType: String = "ECOFLY", // ECOFLY, FLEXIBLE, BUSINESS
    val paymentAmount: Double,
    val paymentMethod: String,
    val isDisrupted: Boolean = false,
    val originalTime: String? = null,
    val updatedTime: String? = null,
    val gate: String = "B12"
)

@Entity(tableName = "boarding_passes")
data class BoardingPass(
    @PrimaryKey val id: String,
    val tripId: String,
    val passengerName: String,
    val flightNumber: String,
    val originCode: String,
    val destCode: String,
    val departureTime: String,
    val departureDate: String,
    val gate: String,
    val seat: String,
    val cabinClass: String,
    val qrCodeData: String
)

@Entity(tableName = "miles_transactions")
data class MilesTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val description: String,
    val type: String, // EARNED, REDEEMED, EXPIRED, ADJUSTED
    val date: String
)

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String, // TRY, EUR, USD, GBP
    val description: String,
    val date: String
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val read: Boolean = false
)

data class FlightSearchState(
    val fromAirport: Airport? = Airport("LHR", "London Heathrow", "London", "United Kingdom"),
    val toAirport: Airport? = Airport("IST", "Istanbul Airport", "Istanbul", "Turkey"),
    val departureDate: String = "12 OCT",
    val returnDate: String = "19 OCT",
    val passengers: Int = 1,
    val cabinClass: String = "Economy", // Economy, Business
    val searchType: String = "ROUND TRIP" // ROUND TRIP, ONE WAY, MULTI CITY
)

data class Destination(
    val name: String,
    val tagline: String,
    val description: String,
    val imagePrompt: String,
    val bestTime: String,
    val popularRoutes: String
)

data class Offer(
    val id: String,
    val title: String,
    val description: String,
    val priceLabel: String,
    val type: String // FLIGHT, MILES
)
