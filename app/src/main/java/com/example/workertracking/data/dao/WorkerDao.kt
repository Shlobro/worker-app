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
    
    @Query("SELECT * FROM workers WHERE id = :id")
    fun getWorkerByIdFlow(id: Long): Flow<Worker?>

    @Insert
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN sw.isHourlyRate = 1 THEN sw.payRate * s.hours ELSE sw.payRate END), 0) +
            COALESCE(SUM(CASE WHEN ew.isHourlyRate = 1 THEN ew.hours * ew.payRate ELSE ew.payRate END), 0)
        FROM workers w
        LEFT JOIN shift_workers sw ON w.id = sw.workerId
        LEFT JOIN shifts s ON sw.shiftId = s.id
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