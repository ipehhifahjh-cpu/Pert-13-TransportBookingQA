package com.industri.transportqa

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.truth.Truth.assertThat
import com.industri.transportqa.data.TestAppDatabase
import com.industri.transportqa.data.TicketDao
import com.industri.transportqa.data.TicketEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pengujian DAO Room (TicketEntity, TicketDao, TestAppDatabase)
 * Menggunakan In-Memory Database di memori RAM sementara (inMemoryDatabaseBuilder).
 */
@RunWith(AndroidJUnit4::class)
class TicketDaoTest {

    private lateinit var db: TestAppDatabase
    private lateinit var ticketDao: TicketDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Inisialisasi Room Database di RAM (in-memory) tanpa menyentuh disk fisik
        db = Room.inMemoryDatabaseBuilder(context, TestAppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        ticketDao = db.ticketDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertTicketAndGetById() = runBlocking {
        // Arrange
        val ticket = TicketEntity(
            nik = "3201012304950001",
            passengerName = "Budi Santoso",
            passengerAge = 28,
            baggageWeightKg = 15,
            totalFare = 1_000_000.0
        )

        // Act: Simpan tiket ke dalam in-memory database
        val insertedId = ticketDao.insertTicket(ticket)

        // Assert: Ambil kembali tiket berdasarkan ID yang digenerate
        val retrieved = ticketDao.getTicketById(insertedId)
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.id).isEqualTo(insertedId)
        assertThat(retrieved?.nik).isEqualTo("3201012304950001")
        assertThat(retrieved?.passengerName).isEqualTo("Budi Santoso")
        assertThat(retrieved?.passengerAge).isEqualTo(28)
        assertThat(retrieved?.baggageWeightKg).isEqualTo(15)
        assertThat(retrieved?.totalFare).isEqualTo(1_000_000.0)
    }

    @Test
    fun getTicketsByNik_returnsMatchingRecords() = runBlocking {
        // Arrange: Masukkan 3 tiket dengan NIK berbeda
        val ticket1 = TicketEntity(
            nik = "3201012304950001",
            passengerName = "Budi Santoso",
            passengerAge = 35,
            baggageWeightKg = 10,
            totalFare = 1_000_000.0
        )
        val ticket2 = TicketEntity(
            nik = "3201012304950001",
            passengerName = "Budi Santoso (Return)",
            passengerAge = 35,
            baggageWeightKg = 12,
            totalFare = 1_000_000.0
        )
        val ticket3 = TicketEntity(
            nik = "3171021405880002",
            passengerName = "Siti Aminah",
            passengerAge = 62,
            baggageWeightKg = 18,
            totalFare = 800_000.0
        )

        ticketDao.insertTicket(ticket1)
        ticketDao.insertTicket(ticket2)
        ticketDao.insertTicket(ticket3)

        // Act: Cari berdasarkan NIK Budi
        val budiTickets = ticketDao.getTicketsByNik("3201012304950001")

        // Assert: Harus ditemukan tepat 2 tiket untuk Budi
        assertThat(budiTickets).hasSize(2)
        assertThat(budiTickets.all { it.nik == "3201012304950001" }).isTrue()
    }

    @Test
    fun getAllTickets_returnsAllSavedTickets() = runBlocking {
        val ticketA = TicketEntity(nik = "1111222233334444", passengerName = "Andi", passengerAge = 20, baggageWeightKg = 5, totalFare = 1_000_000.0)
        val ticketB = TicketEntity(nik = "5555666677778888", passengerName = "Dewi", passengerAge = 65, baggageWeightKg = 25, totalFare = 925_000.0)

        ticketDao.insertTicket(ticketA)
        ticketDao.insertTicket(ticketB)

        val allTickets = ticketDao.getAllTickets()
        assertThat(allTickets).hasSize(2)
    }

    @Test
    fun deleteTicket_removesItemSuccessfully() = runBlocking {
        val ticket = TicketEntity(nik = "9999888877776666", passengerName = "Rian", passengerAge = 30, baggageWeightKg = 10, totalFare = 1_000_000.0)
        val id = ticketDao.insertTicket(ticket)

        val savedTicket = ticketDao.getTicketById(id)
        assertThat(savedTicket).isNotNull()

        // Act: Hapus tiket
        ticketDao.deleteTicket(savedTicket!!)

        // Assert: Tiket sudah tidak ada lagi
        val afterDelete = ticketDao.getTicketById(id)
        assertThat(afterDelete).isNull()
    }
}
