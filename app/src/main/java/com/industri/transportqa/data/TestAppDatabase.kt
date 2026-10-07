package com.industri.transportqa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database untuk Modul TransportBookingQA.
 * Mendukung inMemoryDatabaseBuilder untuk pengujian unit dan DAO.
 */
@Database(entities = [TicketEntity::class], version = 1, exportSchema = false)
abstract class TestAppDatabase : RoomDatabase() {

    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: TestAppDatabase? = null

        fun getDatabase(context: Context): TestAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TestAppDatabase::class.java,
                    "transport_booking_db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
