package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.EventWorker
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface EventWorkerDao {
    @Query("SELECT * FROM event_workers WHERE eventId = :eventId")
    fun getWorkersByEvent(eventId: Long): Flow<List<EventWorker>>

    @Query("SELECT * FROM event_workers WHERE workerId = :workerId")
    fun getEventsByWorker(workerId: Long): Flow<List<EventWorker>>

    @Insert
    suspend fun insertEventWorker(eventWorker: EventWorker): Long

    @Update
    suspend fun updateEventWorker(eventWorker: EventWorker)

    @Delete
    suspend fun deleteEventWorker(eventWorker: EventWorker)

    @Query("DELETE FROM event_workers WHERE eventId = :eventId")
    suspend fun deleteAllWorkersFromEvent(eventId: Long)

    @Query("UPDATE event_workers SET isPaid = :isPaid WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, isPaid: Boolean)

    @Query("UPDATE event_workers SET isPaid = :isPaid, amountPaid = :amountPaid, tipAmount = :tipAmount WHERE id = :id")
    suspend fun updatePaymentDetails(id: Long, isPaid: Boolean, amountPaid: Double, tipAmount: Double)

    @Query("UPDATE event_workers SET isReferencePaid = :isReferencePaid, referenceAmountPaid = :referenceAmountPaid, referenceTipAmount = :referenceTipAmount WHERE id = :id")
    suspend fun updateReferencePaymentDetails(id: Long, isReferencePaid: Boolean, referenceAmountPaid: Double, referenceTipAmount: Double)

    @Query("""
        SELECT SUM(
            CASE 
                WHEN isHourlyRate = 1 THEN hours * payRate 
                ELSE payRate 
            END + 
            tipAmount +
            COALESCE(hours * referencePayRate, 0.0) +
            referenceTipAmount
        ) 
        FROM event_workers WHERE eventId = :eventId
    """)
    suspend fun getTotalCostForEvent(eventId: Long): Double?

    @Query("""
        SELECT ew.*, w.name as workerName, e.date as eventDate, e.name as eventName 
        FROM event_workers ew
        INNER JOIN workers w ON ew.workerId = w.id
        INNER JOIN events e ON ew.eventId = e.id
        WHERE ew.isPaid = 0
        ORDER BY e.date DESC
    """)
    suspend fun getUnpaidEventWorkers(): List<UnpaidEventWorkerInfo>

    @Query("""
        SELECT ew.*, w.name as workerName, e.date as eventDate, e.name as eventName 
        FROM event_workers ew
        INNER JOIN workers w ON ew.workerId = w.id
        INNER JOIN events e ON ew.eventId = e.id
        WHERE ew.workerId = :workerId AND ew.isPaid = 0
        ORDER BY e.date DESC
    """)
    suspend fun getUnpaidEventWorkersForWorker(workerId: Long): List<UnpaidEventWorkerInfo>

    @Query("""
        SELECT ew.*, w.name as workerName, e.date as eventDate, e.name as eventName 
        FROM event_workers ew
        INNER JOIN workers w ON ew.workerId = w.id
        INNER JOIN events e ON ew.eventId = e.id
        WHERE ew.workerId = :workerId
        ORDER BY e.date DESC
    """)
    suspend fun getAllEventWorkersForWorker(workerId: Long): List<UnpaidEventWorkerInfo>

    @Query("""
        SELECT ew.*, w.name as workerName, e.date as eventDate, e.name as eventName 
        FROM event_workers ew
        INNER JOIN workers w ON ew.workerId = w.id
        INNER JOIN events e ON ew.eventId = e.id
        WHERE ew.workerId = :workerId AND ew.isPaid = 1
        ORDER BY e.date DESC
    """)
    suspend fun getPaidEventWorkersForWorker(workerId: Long): List<UnpaidEventWorkerInfo>

    @Query("""
        SELECT ew.*, w.name as workerName, e.date as eventDate, e.name as eventName
        FROM event_workers ew
        INNER JOIN workers w ON ew.workerId = w.id
        INNER JOIN events e ON ew.eventId = e.id
        WHERE ew.isPaid = 1
        ORDER BY e.date DESC
    """)
    suspend fun getAllPaidEventWorkers(): List<UnpaidEventWorkerInfo>

    @Query("""
        SELECT ew.*, w.name as workerName, e.date as eventDate, e.name as eventName
        FROM event_workers ew
        INNER JOIN workers w ON ew.workerId = w.id
        INNER JOIN events e ON ew.eventId = e.id
        WHERE ew.isReferencePaid = 0 AND ew.referencePayRate IS NOT NULL
        ORDER BY e.date DESC
    """)
    suspend fun getUnpaidReferencePayments(): List<UnpaidEventWorkerInfo>
}