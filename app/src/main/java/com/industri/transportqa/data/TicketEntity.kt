package com.industri.transportqa.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Room Database untuk menyimpan data tiket reservasi transportasi.
 */
@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "nik")
    val nik: String,

    @ColumnInfo(name = "passenger_name")
    val passengerName: String,

    @ColumnInfo(name = "passenger_age")
    val passengerAge: Int,

    @ColumnInfo(name = "baggage_weight_kg")
    val baggageWeightKg: Int,

    @ColumnInfo(name = "total_fare")
    val totalFare: Double,

    @ColumnInfo(name = "booking_timestamp")
    val bookingTimestamp: Long = System.currentTimeMillis()
)
