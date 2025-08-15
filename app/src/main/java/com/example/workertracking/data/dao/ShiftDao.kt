package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.Shift
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts ORDER BY date DESC")
    fun getAllShifts(): Flow<List<Shift>>
    
    @Query("SELECT * FROM shifts WHERE projectId = :projectId ORDER BY date DESC")
    fun getShiftsByProject(projectId: Long): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE workerId = :workerId ORDER BY date DESC")
    fun getShiftsByWorker(workerId: Long): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getShiftById(id: Long): Shift?

    @Insert
    suspend fun insertShift(shift: Shift): Long

    @Update
    suspend fun updateShift(shift: Shift)

    @Delete
    suspend fun deleteShift(shift: Shift)

    @Query("SELECT SUM(hours * payRate) FROM shifts WHERE projectId = :projectId")
    suspend fun getTotalCostForProject(projectId: Long): Double?

    @Query("SELECT SUM(hours * payRate) FROM shifts WHERE workerId = :workerId")
    suspend fun getTotalEarnedByWorker(workerId: Long): Double?
}