# Util Package Developer Guide

## Overview
The `util` package contains utility classes that provide common functionality used throughout the application. These utilities centralize reusable logic to maintain consistency and reduce code duplication.

## Files

### PaymentCalculator.kt
**Purpose:** Single source of truth for all payment calculations in the application.

**Type:** Object (singleton)

**Key Methods:**

1. **calculateWorkerPayment(payRate, hours, isHourlyRate)**
   - Calculates worker payment based on hourly or fixed rate
   - Returns: payRate * hours (if hourly) or payRate (if fixed)

2. **calculateReferencePayment(referencePayRate, hours)**
   - Calculates reference worker commission
   - Returns: referencePayRate * hours (or 0.0 if no reference)

3. **calculateTotalPayment(payRate, hours, isHourlyRate, referencePayRate)**
   - Calculates total payment including worker payment and reference commission
   - Returns: worker payment + reference payment

4. **calculateNetPayment(totalPayment, amountPaid, tipAmount)**
   - Calculates net payment after deductions
   - Returns: totalPayment - amountPaid - tipAmount

5. **calculateNetReferencePayment(totalReferencePayment, referenceAmountPaid, referenceTipAmount)**
   - Calculates net reference payment after deductions
   - Returns: totalReferencePayment - referenceAmountPaid - referenceTipAmount

6. **calculateTotalNetPayment(...)**
   - Calculates total net payment including both worker and reference after all deductions
   - Combines all payment calculations and deductions into single method
   - Returns: total payment - all deductions

**Usage:**
```kotlin
// Hourly payment
val payment = PaymentCalculator.calculateWorkerPayment(
    payRate = 15.0,
    hours = 8.0,
    isHourlyRate = true
) // Returns 120.0

// Fixed payment
val payment = PaymentCalculator.calculateWorkerPayment(
    payRate = 200.0,
    hours = 8.0,
    isHourlyRate = false
) // Returns 200.0

// Total payment with reference
val total = PaymentCalculator.calculateTotalPayment(
    payRate = 15.0,
    hours = 8.0,
    isHourlyRate = true,
    referencePayRate = 2.0
) // Returns 136.0 (120.0 worker + 16.0 reference)

// Net payment after deductions
val net = PaymentCalculator.calculateTotalNetPayment(
    payRate = 15.0,
    hours = 8.0,
    isHourlyRate = true,
    referencePayRate = 2.0,
    amountPaid = 50.0,
    tipAmount = 10.0
) // Returns 76.0 (136.0 - 50.0 - 10.0)
```

**Where to Use:**
- ViewModels: MoneyOwedViewModel, WorkerDetailViewModel, WorkersViewModel
- Any location that performs payment calculations or net/total payment summaries

**Benefits:**
- Ensures consistent payment calculations across the app
- Single place to fix calculation bugs
- Easy to test and verify payment logic
- Reduces code duplication
