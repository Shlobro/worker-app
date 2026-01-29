package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.ShiftWorker
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
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

    @Query("""
        SELECT SUM(
            CASE 
                WHEN isHourlyRate = 1 THEN payRate * (SELECT hours FROM shifts WHERE id = :shiftId) 
                ELSE payRate 
            END + 
            COALESCE((SELECT hours FROM shifts WHERE id = :shiftId) * referencePayRate, 0.0)
        ) 
        FROM shift_workers WHERE shiftId = :shiftId
    """)
    suspend fun getTotalCostForShift(shiftId: Long): Double?

    @Query("UPDATE shift_workers SET isPaid = :isPaid WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, isPaid: Boolean)

    @Query("UPDATE shift_workers SET isPaid = :isPaid, amountPaid = :amountPaid, tipAmount = :tipAmount WHERE id = :id")
    suspend fun updatePaymentDetails(id: Long, isPaid: Boolean, amountPaid: Double, tipAmount: Double)

    @Query("UPDATE shift_workers SET isReferencePaid = :isReferencePaid, referenceAmountPaid = :referenceAmountPaid, referenceTipAmount = :referenceTipAmount WHERE id = :id")
    suspend fun updateReferencePaymentDetails(id: Long, isReferencePaid: Boolean, referenceAmountPaid: Double, referenceTipAmount: Double)

    @Query("""
        SELECT sw.*, w.name as workerName, s.date as shiftDate, p.name as projectName, s.hours as shiftHours, s.startTime, s.endTime
        FROM shift_workers sw
        INNER JOIN workers w ON sw.workerId = w.id
        INNER JOIN shifts s ON sw.shiftId = s.id
        INNER JOIN projects p ON s.projectId = p.id
        WHERE sw.isPaid = 0
        ORDER BY s.date DESC
    """)
    suspend fun getUnpaidShiftWorkers(): List<UnpaidShiftWorkerInfo>

    @Query("""
        SELECT sw.*, w.name as workerName, s.date as shiftDate, p.name as projectName, s.hours as shiftHours, s.startTime, s.endTime
        FROM shift_workers sw
        INNER JOIN workers w ON sw.workerId = w.id
        INNER JOIN shifts s ON sw.shiftId = s.id
        INNER JOIN projects p ON s.projectId = p.id
        WHERE sw.isPaid = 0
        ORDER BY s.date DESC
    """)
    fun getUnpaidShiftWorkersFlow(): Flow<List<UnpaidShiftWorkerInfo>>

    @Query("""
        SELECT sw.*, w.name as workerName, s.date as shiftDate, p.name as projectName, s.hours as shiftHours, s.startTime, s.endTime 
        FROM shift_workers sw
        INNER JOIN workers w ON sw.workerId = w.id
        INNER JOIN shifts s ON sw.shiftId = s.id
        INNER JOIN projects p ON s.projectId = p.id
        WHERE sw.workerId = :workerId AND sw.isPaid = 0
        ORDER BY s.date DESC
    """)
    suspend fun getUnpaidShiftWorkersForWorker(workerId: Long): List<UnpaidShiftWorkerInfo>

    @Query("""
        SELECT sw.*, w.name as workerName, s.date as shiftDate, p.name as projectName, s.hours as shiftHours, s.startTime, s.endTime 
        FROM shift_workers sw
        INNER JOIN workers w ON sw.workerId = w.id
        INNER JOIN shifts s ON sw.shiftId = s.id
        INNER JOIN projects p ON s.projectId = p.id
        WHERE sw.workerId = :workerId
        ORDER BY s.date DESC
    """)
    suspend fun getAllShiftWorkersForWorker(workerId: Long): List<UnpaidShiftWorkerInfo>

    @Query("""
        SELECT sw.*, w.name as workerName, s.date as shiftDate, p.name as projectName, s.hours as shiftHours, s.startTime, s.endTime 
        FROM shift_workers sw
        INNER JOIN workers w ON sw.workerId = w.id
        INNER JOIN shifts s ON sw.shiftId = s.id
        INNER JOIN projects p ON s.projectId = p.id
        WHERE sw.workerId = :workerId AND sw.isPaid = 1
        ORDER BY s.date DESC
    """)
    suspend fun getPaidShiftWorkersForWorker(workerId: Long): List<UnpaidShiftWorkerInfo>

    @Query("""
        SELECT sw.*, w.name as workerName, s.date as shiftDate, p.name as projectName, s.hours as shiftHours, s.startTime, s.endTime
        FROM shift_workers sw
        INNER JOIN workers w ON sw.workerId = w.id
        INNER JOIN shifts s ON sw.shiftId = s.id
        INNER JOIN projects p ON s.projectId = p.id
        WHERE sw.isPaid = 1
        ORDER BY s.date DESC
    """)
    suspend fun getAllPaidShiftWorkers(): List<UnpaidShiftWorkerInfo>

    @Query("""
        SELECT sw.*, w.name as workerName, s.date as shiftDate, p.name as projectName, s.hours as shiftHours, s.startTime, s.endTime
        FROM shift_workers sw
        INNER JOIN workers w ON sw.workerId = w.id
        INNER JOIN shifts s ON sw.shiftId = s.id
        INNER JOIN projects p ON s.projectId = p.id
        WHERE sw.isPaid = 1
        ORDER BY s.date DESC
    """)
    fun getAllPaidShiftWorkersFlow(): Flow<List<UnpaidShiftWorkerInfo>>
}