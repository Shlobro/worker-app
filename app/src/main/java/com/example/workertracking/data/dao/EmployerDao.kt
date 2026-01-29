package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.Employer
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployerDao {
    @Query("SELECT * FROM employers ORDER BY name")
    fun getAllEmployers(): Flow<List<Employer>>

    @Query("SELECT * FROM employers WHERE id = :id")
    suspend fun getEmployerById(id: Long): Employer?
    
    @Query("SELECT * FROM employers WHERE id = :id")
    fun getEmployerByIdFlow(id: Long): Flow<Employer?>

    @Insert
    suspend fun insertEmployer(employer: Employer): Long

    @Update
    suspend fun updateEmployer(employer: Employer)

    @Delete
    suspend fun deleteEmployer(employer: Employer)

    @Query("""
        SELECT
            (COALESCE(income.total, 0)) - (COALESCE(expenses.total, 0))
        FROM (
            SELECT
                COALESCE(
                    (SELECT SUM(pi.amount * pi.units)
                     FROM projects p
                     LEFT JOIN project_income pi ON p.id = pi.projectId
                     WHERE p.employerId = :employerId), 0
                ) +
                COALESCE(
                    (SELECT SUM(e.income)
                     FROM events e
                     WHERE e.employerId = :employerId), 0
                ) as total
        ) as income,
        (
            SELECT
                COALESCE(
                    (SELECT SUM(
                        CASE WHEN sw.isHourlyRate = 1
                             THEN sw.payRate * s.hours
                             ELSE sw.payRate
                        END +
                        CASE
                            WHEN sw.referencePayRate IS NULL THEN 0
                            WHEN sw.isReferenceHourlyRate = 1 THEN s.hours * sw.referencePayRate
                            ELSE sw.referencePayRate
                        END
                    )
                    FROM projects p
                    LEFT JOIN shifts s ON p.id = s.projectId
                    LEFT JOIN shift_workers sw ON s.id = sw.shiftId
                    WHERE p.employerId = :employerId), 0
                ) +
                COALESCE(
                    (SELECT SUM(
                        CASE WHEN ew.isHourlyRate = 1
                             THEN ew.hours * ew.payRate
                             ELSE ew.payRate
                        END +
                        CASE
                            WHEN ew.referencePayRate IS NULL THEN 0
                            WHEN ew.isReferenceHourlyRate = 1 THEN ew.hours * ew.referencePayRate
                            ELSE ew.referencePayRate
                        END
                    )
                    FROM events e
                    LEFT JOIN event_workers ew ON e.id = ew.eventId
                    WHERE e.employerId = :employerId), 0
                ) as total
        ) as expenses
    """)
    suspend fun getTotalProfitFromEmployer(employerId: Long): Double?

    @Query("""
        SELECT
            COALESCE(
                (SELECT SUM(pi.amount * pi.units)
                 FROM projects p
                 LEFT JOIN project_income pi ON p.id = pi.projectId
                 WHERE p.employerId = :employerId), 0
            ) +
            COALESCE(
                (SELECT SUM(e.income)
                 FROM events e
                 WHERE e.employerId = :employerId), 0
            )
    """)
    suspend fun getTotalIncomeFromEmployer(employerId: Long): Double?

    @Query("""
        SELECT
            COALESCE(
                (SELECT SUM(
                    CASE WHEN sw.isHourlyRate = 1
                         THEN sw.payRate * s.hours
                         ELSE sw.payRate
                    END +
                    CASE
                        WHEN sw.referencePayRate IS NULL THEN 0
                        WHEN sw.isReferenceHourlyRate = 1 THEN s.hours * sw.referencePayRate
                        ELSE sw.referencePayRate
                    END
                )
                FROM projects p
                LEFT JOIN shifts s ON p.id = s.projectId
                LEFT JOIN shift_workers sw ON s.id = sw.shiftId
                WHERE p.employerId = :employerId), 0
            ) +
            COALESCE(
                (SELECT SUM(
                    CASE WHEN ew.isHourlyRate = 1
                         THEN ew.hours * ew.payRate
                         ELSE ew.payRate
                    END +
                    CASE
                        WHEN ew.referencePayRate IS NULL THEN 0
                        WHEN ew.isReferenceHourlyRate = 1 THEN ew.hours * ew.referencePayRate
                        ELSE ew.referencePayRate
                    END
                )
                FROM events e
                LEFT JOIN event_workers ew ON e.id = ew.eventId
                WHERE e.employerId = :employerId), 0
            )
    """)
    suspend fun getTotalExpensesFromEmployer(employerId: Long): Double?
}