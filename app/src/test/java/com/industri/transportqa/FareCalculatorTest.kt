package com.industri.transportqa

import com.google.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Unit Test JVM untuk pengujian logika bisnis kalkulasi tarif dan validasi NIK.
 * Menggunakan JUnit 4 dan Google Truth.
 */
class FareCalculatorTest {

    // =========================================================================
    // PENGUJIAN VALIDASI NIK
    // =========================================================================

    @Test
    fun isValidNik_withValid16Digits_returnsTrue() {
        // Arrange
        val validNik = "3201012304950001"

        // Act
        val result = FareCalculator.isValidNik(validNik)

        // Assert
        assertThat(result).isTrue()
    }

    @Test
    fun isValidNik_withLessThan16Digits_returnsFalse() {
        val shortNik = "320101230495"
        val result = FareCalculator.isValidNik(shortNik)
        assertThat(result).isFalse()
    }

    @Test
    fun isValidNik_withMoreThan16Digits_returnsFalse() {
        val longNik = "320101230495000199"
        val result = FareCalculator.isValidNik(longNik)
        assertThat(result).isFalse()
    }

    @Test
    fun isValidNik_withAlphanumericOrSpecialCharacters_returnsFalse() {
        val alphaNik = "320101230495ABCD"
        val symbolNik = "32010123-4950001"

        assertThat(FareCalculator.isValidNik(alphaNik)).isFalse()
        assertThat(FareCalculator.isValidNik(symbolNik)).isFalse()
    }

    @Test
    fun isValidNik_withEmptyOrNullString_returnsFalse() {
        assertThat(FareCalculator.isValidNik("")).isFalse()
        assertThat(FareCalculator.isValidNik("   ")).isFalse()
        assertThat(FareCalculator.isValidNik(null)).isFalse()
    }

    // =========================================================================
    // PENGUJIAN KALKULASI DISKON LANSIA & DENDA BAGASI BERLEBIH
    // =========================================================================

    @Test
    fun calculateTotalFare_regularPassenger_withinFreeBaggageLimit() {
        // Penumpang usia 25 thn (reguler), tarif dasar Rp 1.000.000, bagasi 15 kg (<= 20 kg free)
        val baseFare = 1_000_000.0
        val age = 25
        val baggage = 15

        val totalFare = FareCalculator.calculateTotalFare(baseFare, age, baggage)

        // Tidak ada diskon, tidak ada denda bagasi
        assertThat(totalFare).isEqualTo(1_000_000.0)
    }

    @Test
    fun calculateTotalFare_seniorCitizenAge60_receives20PercentDiscount() {
        // Lansia usia 60 tahun: diskon 20% (1.000.000 * 0.8 = 800.000)
        val baseFare = 1_000_000.0
        val age = 60
        val baggage = 10

        val totalFare = FareCalculator.calculateTotalFare(baseFare, age, baggage)

        assertThat(totalFare).isEqualTo(800_000.0)
    }

    @Test
    fun calculateTotalFare_seniorCitizenAge70_receives20PercentDiscount() {
        // Lansia usia 70 tahun: diskon 20%
        val baseFare = 500_000.0
        val age = 70
        val baggage = 20

        val totalFare = FareCalculator.calculateTotalFare(baseFare, age, baggage)

        assertThat(totalFare).isEqualTo(400_000.0)
    }

    @Test
    fun calculateTotalFare_excessBaggageOver20Kg_charged25kPerKg() {
        // Penumpang dewasa reguler (30 thn), bagasi 25 kg (kelebihan 5 kg x 25.000 = 125.000)
        val baseFare = 1_000_000.0
        val age = 30
        val baggage = 25

        val totalFare = FareCalculator.calculateTotalFare(baseFare, age, baggage)

        // Tarif tiket Rp 1.000.000 + Denda Rp 125.000 = Rp 1.125.000
        assertThat(totalFare).isEqualTo(1_125_000.0)
    }

    @Test
    fun calculateTotalFare_seniorCitizenWithExcessBaggage_combinedCalculation() {
        // Lansia usia 65 thn, tarif dasar Rp 1.000.000 (diskon 20% -> 800.000) + bagasi 22 kg (2 kg x 25.000 -> 50.000)
        val baseFare = 1_000_000.0
        val age = 65
        val baggage = 22

        val totalFare = FareCalculator.calculateTotalFare(baseFare, age, baggage)

        // 800.000 + 50.000 = 850.000
        assertThat(totalFare).isEqualTo(850_000.0)
    }

    // =========================================================================
    // [TUGAS MANDIRI 1] - SCENARIO EDGE-CASE TEST
    // =========================================================================

    /**
     * [Tugas Mandiri 1 - Scenario 1]:
     * Memastikan tarif dasar bernilai negatif melempar IllegalArgumentException.
     */
    @Test
    fun calculateTotalFare_negativeBaseFare_throwsIllegalArgumentException() {
        val negativeBaseFare = -100_000.0
        val passengerAge = 25
        val baggageWeight = 10

        val exception = assertThrows(IllegalArgumentException::class.java) {
            FareCalculator.calculateTotalFare(negativeBaseFare, passengerAge, baggageWeight)
        }

        assertThat(exception).hasMessageThat().contains("Tarif dasar tidak boleh negatif")
    }

    /**
     * [Tugas Mandiri 1 - Scenario 2]:
     * Memastikan tarif balita (usia 2 tahun) selalu bernilai Rp 0 (Diskon 100%).
     */
    @Test
    fun calculateTotalFare_toddlerAge2Years_alwaysReturnsZero() {
        val baseFare = 1_000_000.0
        val toddlerAge = 2
        val baggageWeight = 0

        val totalFare = FareCalculator.calculateTotalFare(baseFare, toddlerAge, baggageWeight)

        // Sesuai modul: Balita (< 3 thn) diskon 100% dan selalu bernilai Rp 0
        assertThat(totalFare).isEqualTo(0.0)
    }

    @Test
    fun calculateTotalFare_toddlerInfantAge0And1_alwaysReturnsZero() {
        val baseFare = 1_500_000.0

        val fareAge0 = FareCalculator.calculateTotalFare(baseFare, 0, 0)
        val fareAge1 = FareCalculator.calculateTotalFare(baseFare, 1, 0)

        assertThat(fareAge0).isEqualTo(0.0)
        assertThat(fareAge1).isEqualTo(0.0)
    }

    @Test
    fun calculateTotalFare_negativeAge_throwsIllegalArgumentException() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            FareCalculator.calculateTotalFare(1_000_000.0, -5, 10)
        }
        assertThat(exception).hasMessageThat().contains("Usia penumpang tidak boleh negatif")
    }

    @Test
    fun calculateTotalFare_negativeBaggage_throwsIllegalArgumentException() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            FareCalculator.calculateTotalFare(1_000_000.0, 30, -2)
        }
        assertThat(exception).hasMessageThat().contains("Berat bagasi tidak boleh negatif")
    }
}
