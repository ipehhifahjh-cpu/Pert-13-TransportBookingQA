package com.industri.transportqa.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit Service Interface untuk integrasi API jadwal transportasi.
 */
interface FlightApiService {

    @GET("api/v1/flights/available")
    suspend fun getAvailableFlights(): Response<List<FlightSchedule>>

    @GET("api/v1/flights/search")
    suspend fun searchFlights(
        @Query("origin") origin: String,
        @Query("destination") destination: String
    ): Response<List<FlightSchedule>>
}
