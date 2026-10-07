package com.industri.transportqa.api

import com.google.gson.annotations.SerializedName

/**
 * Model data respons ketersediaan jadwal penerbangan.
 */
data class FlightSchedule(
    @SerializedName("flight_number")
    val flightNumber: String,

    @SerializedName("airline")
    val airline: String,

    @SerializedName("origin")
    val origin: String,

    @SerializedName("destination")
    val destination: String,

    @SerializedName("departure_time")
    val departureTime: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("available_seats")
    val availableSeats: Int
)
