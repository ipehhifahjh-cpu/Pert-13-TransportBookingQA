package com.industri.transportqa

import com.google.truth.Truth.assertThat
import com.industri.transportqa.api.FlightApiService
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Pengujian integrasi API ketersediaan jadwal penerbangan menggunakan MockWebServer dan Retrofit.
 * Memvalidasi respons HTTP 200 dan parsing data JSON.
 */
class MockWebServerTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: FlightApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(FlightApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun getAvailableFlights_returnsHttp200AndValidFlightList() = runTest {
        // Arrange: Siapkan respons dummy JSON ketersediaan jadwal penerbangan (HTTP 200)
        val jsonResponse = """
            [
              {
                "flight_number": "GA-402",
                "airline": "Garuda Indonesia",
                "origin": "CGK",
                "destination": "DPS",
                "departure_time": "2026-10-15T08:30:00Z",
                "price": 1250000.0,
                "available_seats": 18
              },
              {
                "flight_number": "QZ-751",
                "airline": "AirAsia",
                "origin": "CGK",
                "destination": "DPS",
                "departure_time": "2026-10-15T11:45:00Z",
                "price": 850000.0,
                "available_seats": 5
              }
            ]
        """.trimIndent()

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(jsonResponse)

        mockWebServer.enqueue(mockResponse)

        // Act: Eksekusi request API via Retrofit
        val response = apiService.getAvailableFlights()

        // Assert: Validasi HTTP status code 200 & kelengkapan data
        assertThat(response.isSuccessful).isTrue()
        assertThat(response.code()).isEqualTo(200)

        val flightList = response.body()
        assertThat(flightList).isNotNull()
        assertThat(flightList).hasSize(2)

        val firstFlight = flightList!![0]
        assertThat(firstFlight.flightNumber).isEqualTo("GA-402")
        assertThat(firstFlight.airline).isEqualTo("Garuda Indonesia")
        assertThat(firstFlight.origin).isEqualTo("CGK")
        assertThat(firstFlight.destination).isEqualTo("DPS")
        assertThat(firstFlight.price).isEqualTo(1250000.0)
        assertThat(firstFlight.availableSeats).isEqualTo(18)

        // Validasi struktur URL dan HTTP Method yang diterima MockWebServer
        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.method).isEqualTo("GET")
        assertThat(recordedRequest.path).isEqualTo("/api/v1/flights/available")
    }

    @Test
    fun searchFlights_withParameters_returnsMatchingFlights() = runTest {
        // Arrange
        val searchJson = """
            [
              {
                "flight_number": "ID-6512",
                "airline": "Batik Air",
                "origin": "SUB",
                "destination": "UPG",
                "departure_time": "2026-10-16T14:00:00Z",
                "price": 950000.0,
                "available_seats": 12
              }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(searchJson)
        )

        // Act
        val response = apiService.searchFlights("SUB", "UPG")

        // Assert
        assertThat(response.isSuccessful).isTrue()
        val results = response.body()
        assertThat(results).isNotNull()
        assertThat(results).hasSize(1)
        assertThat(results!![0].airline).isEqualTo("Batik Air")

        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.path).contains("origin=SUB")
        assertThat(recordedRequest.path).contains("destination=UPG")
    }
}
