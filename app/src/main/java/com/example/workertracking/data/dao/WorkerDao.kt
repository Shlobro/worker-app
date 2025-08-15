package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.Worker
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY name")
    fun getAllWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerById(id: Long): Worker?

    @Query("SELECT * FROM workers WHERE referenceId = :referenceId")
    suspend fun getWorkersByReference(referenceId: Long): List<Worker>

    @Insert
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    @Query("""
        SELECT SUM(s.hours * s.payRate) + SUM(ew.hours * ew.payRate) 
        FROM workers w
        LEFT JOIN shifts s ON w.id = s.workerId
        LEFT JOIN event_workers ew ON w.id = ew.workerId
        WHERE w.id = :workerId
    """)
    suspend fun getTotalOwedToWorker(workerId: Long): Double?

    @Query("""
        SELECT SUM(p.amount) 
        FROM payments p 
        WHERE p.workerId = :workerId
    """)
    suspend fun getTotalPaidToWorker(workerId: Long): Double?
}