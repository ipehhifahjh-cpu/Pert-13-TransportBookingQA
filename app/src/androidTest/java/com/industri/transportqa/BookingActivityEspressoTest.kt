package com.industri.transportqa

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pengujian Antarmuka Pengguna (UI Test) Menggunakan Espresso.
 * Memvalidasi Alur Positif (Happy Path) dan Alur Negatif (Negative Flow / Tugas Mandiri 2).
 */
@RunWith(AndroidJUnit4::class)
class BookingActivityEspressoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Test Positif (Happy Path):
     * Mengisi NIK, Nama, Usia, dan Bagasi, lalu menekan tombol pesan
     * dan memverifikasi tvBookingStatus muncul dengan status "Pemesanan Berhasil".
     */
    @Test
    fun testBookingTicket_positiveFlow_success() {
        // 1. Mengisi NIK 16 digit yang valid
        onView(withId(R.id.etNik))
            .perform(replaceText("3201012304950001"), closeSoftKeyboard())

        // 2. Mengisi Nama Penumpang
        onView(withId(R.id.etName))
            .perform(replaceText("Budi Santoso"), closeSoftKeyboard())

        // 3. Mengisi Usia Penumpang
        onView(withId(R.id.etAge))
            .perform(replaceText("28"), closeSoftKeyboard())

        // 4. Mengisi Berat Bagasi (15 kg)
        onView(withId(R.id.etBaggage))
            .perform(replaceText("15"), closeSoftKeyboard())

        // 5. Menekan tombol proses pemesanan tiket
        onView(withId(R.id.btnSubmitBooking))
            .perform(click())

        // 6. Memverifikasi tvBookingStatus muncul di layar dengan pesan sukses
        onView(withId(R.id.tvBookingStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Pemesanan Berhasil"))))

        // 7. Memverifikasi rincian tiket menampilkan nama penumpang yang diinput
        onView(withId(R.id.tvBookingDetails))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Budi Santoso"))))
    }

    /**
     * [TUGAS MANDIRI 2] - Test Negatif (Negative Flow Scenario 1):
     * Mengosongkan kolom NIK, menekan tombol submit,
     * lalu memverifikasi bahwa pesan error "NIK tidak boleh kosong" muncul di layar
     * dan transaksi tidak diproses.
     */
    @Test
    fun testBookingTicket_negativeFlow_emptyNik_showsErrorAndBlocksTransaction() {
        // 1. Kosongkan kolom NIK
        onView(withId(R.id.etNik))
            .perform(clearText(), closeSoftKeyboard())

        // 2. Mengisi nama penumpang
        onView(withId(R.id.etName))
            .perform(replaceText("Ahmad Fauzi"), closeSoftKeyboard())

        // 3. Tekan tombol submit
        onView(withId(R.id.btnSubmitBooking))
            .perform(click())

        // 4. Verifikasi bahwa pesan error "NIK tidak boleh kosong" / "NIK Wajib 16 Digit" muncul di layar
        onView(withId(R.id.tvBookingStatus))
            .check(matches(isDisplayed()))
            .check(matches(anyOf(
                withText(containsString("NIK tidak boleh kosong")),
                withText(containsString("NIK Wajib 16 Digit"))
            )))

        // 5. Pastikan transaksi TIDAK berstatus "Pemesanan Berhasil"
        onView(withId(R.id.tvBookingStatus))
            .check(matches(not(withText("Pemesanan Berhasil"))))
    }

    /**
     * [TUGAS MANDIRI 2] - Test Negatif (Negative Flow Scenario 2):
     * Mengisi NIK yang kurang dari 16 digit, menekan tombol submit,
     * lalu memverifikasi bahwa pesan error "NIK Wajib 16 Digit" muncul di layar.
     */
    @Test
    fun testBookingTicket_negativeFlow_invalidNikLength_showsError() {
        // 1. Isi NIK tidak valid (< 16 digit)
        onView(withId(R.id.etNik))
            .perform(replaceText("12345"), closeSoftKeyboard())

        onView(withId(R.id.etName))
            .perform(replaceText("Dewi Sartika"), closeSoftKeyboard())

        // 2. Tekan tombol submit
        onView(withId(R.id.btnSubmitBooking))
            .perform(click())

        // 3. Verifikasi pesan error "NIK Wajib 16 Digit" muncul
        onView(withId(R.id.tvBookingStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("NIK Wajib 16 Digit"))))
    }

    /**
     * Pengujian Fitur Reset Form.
     */
    @Test
    fun testResetButton_clearsInputFields() {
        onView(withId(R.id.etNik))
            .perform(replaceText("3201012304950001"), closeSoftKeyboard())
        onView(withId(R.id.etName))
            .perform(replaceText("Citra Kirana"), closeSoftKeyboard())

        // Klik tombol reset
        onView(withId(R.id.btnResetBooking))
            .perform(click())

        // Verifikasi input kembali kosong
        onView(withId(R.id.etNik))
            .check(matches(withText("")))
        onView(withId(R.id.etName))
            .check(matches(withText("")))
    }
}
