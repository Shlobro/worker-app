package com.example.workertracking.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentCalculatorTest {

    private val delta = 0.001

    // --- calculateWorkerPayment ---

    @Test
    fun workerPayment_hourlyRate() {
        val result = PaymentCalculator.calculateWorkerPayment(
            payRate = 50.0, hours = 8.0, isHourlyRate = true
        )
        assertEquals(400.0, result, delta)
    }

    @Test
    fun workerPayment_fixedRate_ignoresHours() {
        val result = PaymentCalculator.calculateWorkerPayment(
            payRate = 300.0, hours = 8.0, isHourlyRate = false
        )
        assertEquals(300.0, result, delta)
    }

    @Test
    fun workerPayment_zeroHours() {
        val result = PaymentCalculator.calculateWorkerPayment(
            payRate = 50.0, hours = 0.0, isHourlyRate = true
        )
        assertEquals(0.0, result, delta)
    }

    // --- calculateReferencePayment ---

    @Test
    fun referencePayment_nullRate_returnsZero() {
        val result = PaymentCalculator.calculateReferencePayment(
            referencePayRate = null, hours = 8.0
        )
        assertEquals(0.0, result, delta)
    }

    @Test
    fun referencePayment_hourlyRate() {
        val result = PaymentCalculator.calculateReferencePayment(
            referencePayRate = 10.0, hours = 8.0, isReferenceHourlyRate = true
        )
        assertEquals(80.0, result, delta)
    }

    @Test
    fun referencePayment_fixedRate_ignoresHours() {
        val result = PaymentCalculator.calculateReferencePayment(
            referencePayRate = 50.0, hours = 8.0, isReferenceHourlyRate = false
        )
        assertEquals(50.0, result, delta)
    }

    // --- calculateNetPayment ---

    @Test
    fun netPayment_subtractsAmountPaidAndTip() {
        val result = PaymentCalculator.calculateNetPayment(
            totalPayment = 400.0, amountPaid = 100.0, tipAmount = 50.0
        )
        assertEquals(250.0, result, delta)
    }

    @Test
    fun netPayment_noDeductions() {
        val result = PaymentCalculator.calculateNetPayment(totalPayment = 400.0)
        assertEquals(400.0, result, delta)
    }

    // --- calculateNetReferencePayment ---

    @Test
    fun netReferencePayment_subtractsDeductions() {
        val result = PaymentCalculator.calculateNetReferencePayment(
            totalReferencePayment = 80.0, referenceAmountPaid = 30.0, referenceTipAmount = 10.0
        )
        assertEquals(40.0, result, delta)
    }

    // --- calculateTotalPayment ---

    @Test
    fun totalPayment_combinesWorkerAndReference() {
        val result = PaymentCalculator.calculateTotalPayment(
            payRate = 50.0, hours = 8.0, isHourlyRate = true,
            referencePayRate = 10.0, isReferenceHourlyRate = true
        )
        assertEquals(480.0, result, delta) // 400 worker + 80 reference
    }

    @Test
    fun totalPayment_noReference() {
        val result = PaymentCalculator.calculateTotalPayment(
            payRate = 50.0, hours = 8.0, isHourlyRate = true,
            referencePayRate = null
        )
        assertEquals(400.0, result, delta)
    }

    // --- calculateTotalNetPayment ---

    @Test
    fun totalNetPayment_includesBothWorkerAndReferenceMinusDeductions() {
        val result = PaymentCalculator.calculateTotalNetPayment(
            payRate = 50.0, hours = 8.0, isHourlyRate = true,
            referencePayRate = 10.0, isReferenceHourlyRate = true,
            amountPaid = 100.0, tipAmount = 50.0,
            referenceAmountPaid = 30.0, referenceTipAmount = 10.0
        )
        // 400 worker + 80 reference - 100 paid - 50 tip - 30 refPaid - 10 refTip = 290
        assertEquals(290.0, result, delta)
    }

    @Test
    fun totalNetPayment_noReference_matchesWorkerNetOnly() {
        val result = PaymentCalculator.calculateTotalNetPayment(
            payRate = 50.0, hours = 8.0, isHourlyRate = true,
            amountPaid = 100.0, tipAmount = 50.0
        )
        assertEquals(250.0, result, delta) // 400 - 100 - 50
    }

    // --- Key scenario: worker-only vs total demonstrates the bug this test suite guards ---

    @Test
    fun workerOwedAmount_excludesReferencePayment() {
        val payRate = 50.0
        val hours = 8.0
        val referencePayRate = 10.0
        val amountPaid = 100.0
        val tipAmount = 20.0

        // What we show on the worker detail screen (direct debt only)
        val workerGross = PaymentCalculator.calculateWorkerPayment(payRate, hours, true)
        val workerOwed = PaymentCalculator.calculateNetPayment(workerGross, amountPaid, tipAmount)

        // What calculateTotalNetPayment would give (includes reference - the old bug)
        val totalOwed = PaymentCalculator.calculateTotalNetPayment(
            payRate = payRate, hours = hours, isHourlyRate = true,
            referencePayRate = referencePayRate, isReferenceHourlyRate = true,
            amountPaid = amountPaid, tipAmount = tipAmount
        )

        assertEquals(280.0, workerOwed, delta)  // 400 - 100 - 20 = 280
        assertEquals(360.0, totalOwed, delta)    // 400 + 80 - 100 - 20 = 360
        // totalOwed is 80 higher because it includes the reference commission
        assertEquals(
            PaymentCalculator.calculateReferencePayment(referencePayRate, hours),
            totalOwed - workerOwed,
            delta
        )
    }
}
