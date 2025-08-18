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
            (COALESCE(SUM(pi.amount * pi.units), 0) + COALESCE(SUM(e.income), 0)) - 
            (COALESCE(SUM(CASE WHEN sw.isHourlyRate = 1 THEN sw.payRate * s.hours ELSE sw.payRate END + COALESCE(s.hours * sw.referencePayRate, 0)), 0) +
             COALESCE(SUM(CASE WHEN ew.isHourlyRate = 1 THEN ew.hours * ew.payRate ELSE ew.payRate END + COALESCE(ew.hours * ew.referencePayRate, 0)), 0))
        FROM employers emp
        LEFT JOIN projects p ON emp.id = p.employerId
        LEFT JOIN events e ON emp.id = e.employerId
        LEFT JOIN project_income pi ON p.id = pi.projectId
        LEFT JOIN shifts s ON p.id = s.projectId
        LEFT JOIN shift_workers sw ON s.id = sw.shiftId
        LEFT JOIN event_workers ew ON e.id = ew.eventId
        WHERE emp.id = :employerId
    """)
    suspend fun getTotalProfitFromEmployer(employerId: Long): Double?

    @Query("""
        SELECT 
            COALESCE(SUM(pi.amount * pi.units), 0) + COALESCE(SUM(e.income), 0)
        FROM employers emp
        LEFT JOIN projects p ON emp.id = p.employerId
        LEFT JOIN events e ON emp.id = e.employerId
        LEFT JOIN project_income pi ON p.id = pi.projectId
        WHERE emp.id = :employerId
    """)
    suspend fun getTotalIncomeFromEmployer(employerId: Long): Double?

    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN sw.isHourlyRate = 1 THEN sw.payRate * s.hours ELSE sw.payRate END + COALESCE(s.hours * sw.referencePayRate, 0)), 0) +
            COALESCE(SUM(CASE WHEN ew.isHourlyRate = 1 THEN ew.hours * ew.payRate ELSE ew.payRate END + COALESCE(ew.hours * ew.referencePayRate, 0)), 0)
        FROM employers emp
        LEFT JOIN projects p ON emp.id = p.employerId
        LEFT JOIN events e ON emp.id = e.employerId
        LEFT JOIN shifts s ON p.id = s.projectId
        LEFT JOIN shift_workers sw ON s.id = sw.shiftId
        LEFT JOIN event_workers ew ON e.id = ew.eventId
        WHERE emp.id = :employerId
    """)
    suspend fun getTotalExpensesFromEmployer(employerId: Long): Double?
}