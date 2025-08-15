package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE workerId = :workerId ORDER BY datePaid DESC")
    fun getPaymentsByWorker(workerId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY datePaid DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Insert
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT SUM(amount) FROM payments")
    suspend fun getTotalPayments(): Double?

    @Query("SELECT SUM(amount) FROM payments WHERE workerId = :workerId")
    suspend fun getTotalPaymentsToWorker(workerId: Long): Double?
}