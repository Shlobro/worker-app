package com.example.workertracking.util

/**
 * Single source of truth for all payment calculations in the app.
 * Centralizes payment logic to ensure consistency across ViewModels and Repositories.
 */
object PaymentCalculator {

    /**
     * Calculate worker payment based on hourly or fixed rate.
     *
     * @param payRate The payment rate (hourly rate or fixed amount)
     * @param hours Number of hours worked (only used if isHourlyRate is true)
     * @param isHourlyRate True if payment is hourly, false if fixed amount
     * @return Total worker payment amount
     */
    fun calculateWorkerPayment(
        payRate: Double,
        hours: Double,
        isHourlyRate: Boolean
    ): Double {
        return if (isHourlyRate) {
            payRate * hours
        } else {
            payRate
        }
    }

    /**
     * Calculate reference worker commission payment.
     *
     * @param referencePayRate The commission rate (hourly rate or fixed amount, null if no reference)
     * @param hours Number of hours worked (only used if isReferenceHourlyRate is true)
     * @param isReferenceHourlyRate True if commission is hourly, false if fixed amount
     * @return Total reference commission amount
     */
    fun calculateReferencePayment(
        referencePayRate: Double?,
        hours: Double,
        isReferenceHourlyRate: Boolean = true
    ): Double {
        if (referencePayRate == null) return 0.0
        return if (isReferenceHourlyRate) {
            referencePayRate * hours
        } else {
            referencePayRate
        }
    }

    /**
     * Calculate total payment including worker payment and reference commission.
     *
     * @param payRate The payment rate (hourly rate or fixed amount)
     * @param hours Number of hours worked
     * @param isHourlyRate True if payment is hourly, false if fixed amount
     * @param referencePayRate The commission rate (hourly rate or fixed amount, null if no reference)
     * @param isReferenceHourlyRate True if commission is hourly, false if fixed amount
     * @return Total payment including worker payment and reference commission
     */
    fun calculateTotalPayment(
        payRate: Double,
        hours: Double,
        isHourlyRate: Boolean,
        referencePayRate: Double? = null,
        isReferenceHourlyRate: Boolean = true
    ): Double {
        val workerPayment = calculateWorkerPayment(payRate, hours, isHourlyRate)
        val referencePayment = calculateReferencePayment(referencePayRate, hours, isReferenceHourlyRate)
        return workerPayment + referencePayment
    }

    /**
     * Calculate net payment after deductions.
     *
     * @param totalPayment Total payment amount before deductions
     * @param amountPaid Amount already paid
     * @param tipAmount Tip amount already paid
     * @return Net payment owed after deductions
     */
    fun calculateNetPayment(
        totalPayment: Double,
        amountPaid: Double = 0.0,
        tipAmount: Double = 0.0
    ): Double {
        return totalPayment - amountPaid - tipAmount
    }

    /**
     * Calculate net reference payment after deductions.
     *
     * @param totalReferencePayment Total reference payment before deductions
     * @param referenceAmountPaid Amount already paid to reference
     * @param referenceTipAmount Tip amount already paid to reference
     * @return Net reference payment owed after deductions
     */
    fun calculateNetReferencePayment(
        totalReferencePayment: Double,
        referenceAmountPaid: Double = 0.0,
        referenceTipAmount: Double = 0.0
    ): Double {
        return totalReferencePayment - referenceAmountPaid - referenceTipAmount
    }

    /**
     * Calculate total net payment including both worker and reference payments after all deductions.
     *
     * @param payRate The payment rate (hourly rate or fixed amount)
     * @param hours Number of hours worked
     * @param isHourlyRate True if payment is hourly, false if fixed amount
     * @param referencePayRate The commission rate (hourly rate or fixed amount, null if no reference)
     * @param isReferenceHourlyRate True if commission is hourly, false if fixed amount
     * @param amountPaid Amount already paid to worker
     * @param tipAmount Tip amount already paid to worker
     * @param referenceAmountPaid Amount already paid to reference
     * @param referenceTipAmount Tip amount already paid to reference
     * @return Total net payment owed after all deductions
     */
    fun calculateTotalNetPayment(
        payRate: Double,
        hours: Double,
        isHourlyRate: Boolean,
        referencePayRate: Double? = null,
        isReferenceHourlyRate: Boolean = true,
        amountPaid: Double = 0.0,
        tipAmount: Double = 0.0,
        referenceAmountPaid: Double = 0.0,
        referenceTipAmount: Double = 0.0
    ): Double {
        val workerPayment = calculateWorkerPayment(payRate, hours, isHourlyRate)
        val referencePayment = calculateReferencePayment(referencePayRate, hours, isReferenceHourlyRate)
        val totalPayment = workerPayment + referencePayment

        return totalPayment - amountPaid - tipAmount - referenceAmountPaid - referenceTipAmount
    }

}
