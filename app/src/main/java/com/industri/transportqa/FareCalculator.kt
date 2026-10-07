package com.industri.transportqa

/**
 * Logika Bisnis Perhitungan Tarif Transportasi dan Validasi Identitas (QA Modul TransportBookingQA).
 *
 * Aturan Bisnis:
 * 1. NIK harus tepat 16 digit angka.
 * 2. Balita (< 3 tahun) mendapatkan diskon 100% (tarif Rp 0).
 * 3. Lansia (>= 60 tahun) mendapatkan diskon tiket sebesar 20%.
 * 4. Penumpang reguler (3 - 59 tahun) membayar tarif dasar penuh.
 * 5. Bagasi gratis maksimal 20 kg. Kelebihan bagasi (> 20 kg) dikenakan denda Rp 25.000 / kg.
 * 6. Jika tarif dasar < 0 atau usia < 0, lemparkan IllegalArgumentException.
 */
object FareCalculator {

    const val FREE_BAGGAGE_LIMIT_KG = 20
    const val EXCESS_BAGGAGE_FEE_PER_KG = 25000.0
    const val DEFAULT_BASE_FARE = 1000000.0

    /**
     * Memvalidasi NIK (Nomor Induk Kependudukan).
     * Syarat: Tepat 16 digit angka (0-9).
     */
    fun isValidNik(nik: String?): Boolean {
        if (nik == null || nik.length != 16) {
            return false
        }
        return nik.all { it.isDigit() }
    }

    /**
     * Menghitung total tarif tiket berdasarkan usia dan berat bagasi.
     *
     * @param baseFare Tarif dasar tiket (tidak boleh negatif)
     * @param passengerAge Usia penumpang dalam tahun (tidak boleh negatif)
     * @param baggageWeightKg Berat bagasi penumpang dalam kilogram (tidak boleh negatif)
     * @throws IllegalArgumentException jika baseFare < 0, passengerAge < 0, atau baggageWeightKg < 0
     * @return Total biaya tiket akhir
     */
    fun calculateTotalFare(
        baseFare: Double,
        passengerAge: Int,
        baggageWeightKg: Int
    ): Double {
        if (baseFare < 0.0) {
            throw IllegalArgumentException("Tarif dasar tidak boleh negatif: $baseFare")
        }
        if (passengerAge < 0) {
            throw IllegalArgumentException("Usia penumpang tidak boleh negatif: $passengerAge")
        }
        if (baggageWeightKg < 0) {
            throw IllegalArgumentException("Berat bagasi tidak boleh negatif: $baggageWeightKg")
        }

        // Ketentuan Tugas Mandiri 1: Balita (< 3 tahun) selalu bernilai Rp 0
        if (passengerAge < 3) {
            return 0.0
        }

        // Diskon Lansia (>= 60 tahun) diskon 20%, selain itu tarif reguler
        val ticketFare = if (passengerAge >= 60) {
            baseFare * 0.8
        } else {
            baseFare
        }

        // Biaya kelebihan bagasi (> 20 kg: Rp 25.000 / kg)
        val excessWeight = if (baggageWeightKg > FREE_BAGGAGE_LIMIT_KG) {
            baggageWeightKg - FREE_BAGGAGE_LIMIT_KG
        } else {
            0
        }
        val baggageFee = excessWeight * EXCESS_BAGGAGE_FEE_PER_KG

        return ticketFare + baggageFee
    }

    /**
     * Mengembalikan nama kategori penumpang untuk tampilan UI deskriptif.
     */
    fun getPassengerCategory(passengerAge: Int): String {
        return when {
            passengerAge < 3 -> "Balita (Diskon 100%)"
            passengerAge >= 60 -> "Lansia (Diskon 20%)"
            else -> "Reguler / Dewasa"
        }
    }
}
