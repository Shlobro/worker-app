package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.WorkerWithDebtData
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

    @Query("""
        SELECT
            w.id,
            w.name,
            w.phoneNumber,
            w.referenceId,
            w.photoUris,
            COALESCE(unpaid_shifts.total, 0.0) as unpaidShiftsTotal,
            COALESCE(unpaid_shifts.count, 0) as unpaidShiftsCount,
            COALESCE(unpaid_events.total, 0.0) as unpaidEventsTotal,
            COALESCE(unpaid_events.count, 0) as unpaidEventsCount,
            COALESCE(reference_shifts.total, 0.0) as referenceShiftsTotal,
            COALESCE(reference_shifts.count, 0) as referenceShiftsCount,
            COALESCE(reference_events.total, 0.0) as referenceEventsTotal,
            COALESCE(reference_events.count, 0) as referenceEventsCount,
            COALESCE(total_earnings.earnings, 0.0) as totalEarnings,
            ref_worker.name as referenceWorkerName
        FROM workers w
        LEFT JOIN (
            SELECT
                sw.workerId,
                SUM(CASE WHEN sw.isHourlyRate = 1 THEN sw.payRate * s.hours ELSE sw.payRate END) as total,
                COUNT(*) as count
            FROM shift_workers sw
            INNER JOIN shifts s ON sw.shiftId = s.id
            WHERE sw.isPaid = 0
            GROUP BY sw.workerId
        ) unpaid_shifts ON w.id = unpaid_shifts.workerId
        LEFT JOIN (
            SELECT
                ew.workerId,
                SUM(CASE WHEN ew.isHourlyRate = 1 THEN ew.hours * ew.payRate ELSE ew.payRate END
                    - ew.amountPaid - ew.tipAmount) as total,
                COUNT(*) as count
            FROM event_workers ew
            WHERE ew.isPaid = 0
            GROUP BY ew.workerId
        ) unpaid_events ON w.id = unpaid_events.workerId
        LEFT JOIN (
            SELECT
                w2.referenceId as refWorkerId,
                SUM(sw2.referencePayRate * s2.hours) as total,
                COUNT(*) as count
            FROM workers w2
            INNER JOIN shift_workers sw2 ON w2.id = sw2.workerId
            INNER JOIN shifts s2 ON sw2.shiftId = s2.id
            WHERE sw2.isPaid = 0 AND sw2.referencePayRate IS NOT NULL AND w2.referenceId IS NOT NULL
            GROUP BY w2.referenceId
        ) reference_shifts ON w.id = reference_shifts.refWorkerId
        LEFT JOIN (
            SELECT
                w3.referenceId as refWorkerId,
                SUM((ew2.referencePayRate * ew2.hours) - ew2.referenceAmountPaid - ew2.referenceTipAmount) as total,
                COUNT(*) as count
            FROM workers w3
            INNER JOIN event_workers ew2 ON w3.id = ew2.workerId
            WHERE ew2.isReferencePaid = 0 AND ew2.referencePayRate IS NOT NULL AND w3.referenceId IS NOT NULL
            GROUP BY w3.referenceId
        ) reference_events ON w.id = reference_events.refWorkerId
        LEFT JOIN (
            SELECT
                sw3.workerId,
                SUM(CASE WHEN sw3.isHourlyRate = 1 THEN sw3.payRate * s3.hours ELSE sw3.payRate END) as shift_earnings
            FROM shift_workers sw3
            INNER JOIN shifts s3 ON sw3.shiftId = s3.id
            GROUP BY sw3.workerId
        ) shift_earnings ON w.id = shift_earnings.workerId
        LEFT JOIN (
            SELECT
                ew3.workerId,
                SUM(CASE WHEN ew3.isHourlyRate = 1 THEN ew3.hours * ew3.payRate ELSE ew3.payRate END) as event_earnings
            FROM event_workers ew3
            GROUP BY ew3.workerId
        ) event_earnings ON w.id = event_earnings.workerId
        LEFT JOIN (
            SELECT
                workerId,
                COALESCE(shift_earnings.shift_earnings, 0.0) + COALESCE(event_earnings.event_earnings, 0.0) as earnings
            FROM (SELECT DISTINCT workerId FROM shift_workers UNION SELECT DISTINCT workerId FROM event_workers) all_workers
            LEFT JOIN (
                SELECT
                    sw3.workerId,
                    SUM(CASE WHEN sw3.isHourlyRate = 1 THEN sw3.payRate * s3.hours ELSE sw3.payRate END) as shift_earnings
                FROM shift_workers sw3
                INNER JOIN shifts s3 ON sw3.shiftId = s3.id
                GROUP BY sw3.workerId
            ) shift_earnings ON all_workers.workerId = shift_earnings.workerId
            LEFT JOIN (
                SELECT
                    ew3.workerId,
                    SUM(CASE WHEN ew3.isHourlyRate = 1 THEN ew3.hours * ew3.payRate ELSE ew3.payRate END) as event_earnings
                FROM event_workers ew3
                GROUP BY ew3.workerId
            ) event_earnings ON all_workers.workerId = event_earnings.workerId
        ) total_earnings ON w.id = total_earnings.workerId
        LEFT JOIN workers ref_worker ON w.referenceId = ref_worker.id
        ORDER BY w.name
    """)
    fun getAllWorkersWithDebtData(): Flow<List<WorkerWithDebtData>>
}