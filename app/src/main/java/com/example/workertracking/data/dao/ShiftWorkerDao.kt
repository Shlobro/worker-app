package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.ShiftWorker
import com.example.workertracking.data.entity.Worker
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftWorkerDao {
    @Query("SELECT * FROM shift_workers WHERE shiftId = :shiftId")
    fun getWorkersByShift(shiftId: Long): Flow<List<ShiftWorker>>

    @Query("SELECT * FROM shift_workers WHERE workerId = :workerId")
    fun getShiftsByWorker(workerId: Long): Flow<List<ShiftWorker>>

    @Query("""
        SELECT w.* FROM workers w 
        INNER JOIN shift_workers sw ON w.id = sw.workerId 
        WHERE sw.shiftId = :shiftId
    """)
    fun getWorkersForShift(shiftId: Long): Flow<List<Worker>>

    @Query("SELECT * FROM shift_workers WHERE shiftId = :shiftId AND workerId = :workerId")
    suspend fun getShiftWorker(shiftId: Long, workerId: Long): ShiftWorker?

    @Insert
    suspend fun insertShiftWorker(shiftWorker: ShiftWorker): Long

    @Update
    suspend fun updateShiftWorker(shiftWorker: ShiftWorker)

    @Delete
    suspend fun deleteShiftWorker(shiftWorker: ShiftWorker)

    @Query("DELETE FROM shift_workers WHERE shiftId = :shiftId AND workerId = :workerId")
    suspend fun removeWorkerFromShift(shiftId: Long, workerId: Long)

    @Query("SELECT SUM(CASE WHEN isHourlyRate = 1 THEN payRate * (SELECT hours FROM shifts WHERE id = :shiftId) ELSE payRate END) FROM shift_workers WHERE shiftId = :shiftId")
    suspend fun getTotalCostForShift(shiftId: Long): Double?
}