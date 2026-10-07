package com.industri.transportqa.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object (DAO) untuk manipulasi data tiket di Room Database.
 */
@Dao
interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: Long): TicketEntity?

    @Query("SELECT * FROM tickets WHERE nik = :nik")
    suspend fun getTicketsByNik(nik: String): List<TicketEntity>

    @Query("SELECT * FROM tickets ORDER BY id DESC")
    suspend fun getAllTickets(): List<TicketEntity>

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Delete
    suspend fun deleteTicket(ticket: TicketEntity)

    @Query("DELETE FROM tickets")
    suspend fun clearAllTickets()
}
