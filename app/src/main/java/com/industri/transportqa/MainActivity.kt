package com.industri.transportqa

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.industri.transportqa.data.TestAppDatabase
import com.industri.transportqa.data.TicketEntity
import com.industri.transportqa.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSubmitBooking.setOnClickListener {
            handleBookingSubmission()
        }

        binding.btnResetBooking.setOnClickListener {
            resetBookingForm()
        }
    }

    private fun handleBookingSubmission() {
        val nikInput = binding.etNik.text?.toString()?.trim().orEmpty()
        val nameInput = binding.etName.text?.toString()?.trim().orEmpty()
        val ageText = binding.etAge.text?.toString()?.trim().orEmpty()
        val baggageText = binding.etBaggage.text?.toString()?.trim().orEmpty()

        // Validasi Alur Negatif 1: NIK Kosong
        if (nikInput.isEmpty()) {
            val emptyMsg = getString(R.string.error_nik_empty)
            binding.etNik.error = emptyMsg
            showErrorStatus(emptyMsg, "Transaksi dibatalkan: Kolom NIK tidak boleh kosong.")
            return
        }

        // Validasi Alur Negatif 2: NIK Tidak Tepat 16 Digit Angka
        if (!FareCalculator.isValidNik(nikInput)) {
            val invalidMsg = getString(R.string.error_nik_invalid)
            binding.etNik.error = invalidMsg
            showErrorStatus(invalidMsg, "Transaksi dibatalkan: NIK wajib terdiri dari 16 digit angka.")
            return
        }

        // Validasi Nama
        if (nameInput.isEmpty()) {
            val nameMsg = getString(R.string.error_name_empty)
            binding.etName.error = nameMsg
            showErrorStatus("Validasi Gagal", nameMsg)
            return
        }

        // Parsing Usia & Bagasi
        val age = ageText.toIntOrNull() ?: 25
        val baggage = baggageText.toIntOrNull() ?: 0

        val baseFare = FareCalculator.DEFAULT_BASE_FARE

        try {
            val totalFare = FareCalculator.calculateTotalFare(baseFare, age, baggage)
            val category = FareCalculator.getPassengerCategory(age)

            showSuccessStatus(
                nik = nikInput,
                name = nameInput,
                age = age,
                category = category,
                baggage = baggage,
                totalFare = totalFare
            )

            // Simpan ke Room Database
            saveTicketToDatabase(
                nik = nikInput,
                name = nameInput,
                age = age,
                baggage = baggage,
                totalFare = totalFare
            )

        } catch (e: IllegalArgumentException) {
            showErrorStatus("Input Tidak Valid", e.message ?: "Terjadi kesalahan perhitungan tarif.")
        }
    }

    private fun showSuccessStatus(
        nik: String,
        name: String,
        age: Int,
        category: String,
        baggage: Int,
        totalFare: Double
    ) {
        binding.cardBookingResult.visibility = View.VISIBLE
        binding.tvBookingStatus.visibility = View.VISIBLE
        binding.tvBookingStatus.text = getString(R.string.booking_success)
        binding.tvBookingStatus.setBackgroundResource(R.drawable.bg_status_badge_success)
        binding.tvBookingStatus.setTextColor(ContextCompat.getColor(this, R.color.status_success_text))

        val formattedFare = formatRupiah(totalFare)
        val details = buildString {
            append("Nama Penumpang: ").append(name).append("\n")
            append("NIK: ").append(nik).append("\n")
            append("Usia: ").append(age).append(" tahun (").append(category).append(")\n")
            append("Berat Bagasi: ").append(baggage).append(" kg")
            if (baggage > FareCalculator.FREE_BAGGAGE_LIMIT_KG) {
                val excess = baggage - FareCalculator.FREE_BAGGAGE_LIMIT_KG
                append(" (Kelebihan: ").append(excess).append(" kg)")
            }
            append("\n")
            append("Total Pembayaran: Rp ").append(formattedFare)
        }
        binding.tvBookingDetails.text = details
    }

    private fun showErrorStatus(status: String, message: String) {
        binding.cardBookingResult.visibility = View.VISIBLE
        binding.tvBookingStatus.visibility = View.VISIBLE
        binding.tvBookingStatus.text = status
        binding.tvBookingStatus.setBackgroundResource(R.drawable.bg_status_badge_error)
        binding.tvBookingStatus.setTextColor(ContextCompat.getColor(this, R.color.status_error_text))
        binding.tvBookingDetails.text = message
    }

    private fun resetBookingForm() {
        binding.etNik.text?.clear()
        binding.etName.text?.clear()
        binding.etAge.text?.clear()
        binding.etBaggage.text?.clear()
        binding.etNik.error = null
        binding.etName.error = null
        binding.cardBookingResult.visibility = View.GONE
    }

    private fun saveTicketToDatabase(
        nik: String,
        name: String,
        age: Int,
        baggage: Int,
        totalFare: Double
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = TestAppDatabase.getDatabase(applicationContext).ticketDao()
            dao.insertTicket(
                TicketEntity(
                    nik = nik,
                    passengerName = name,
                    passengerAge = age,
                    baggageWeightKg = baggage,
                    totalFare = totalFare
                )
            )
        }
    }

    private fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }
}
