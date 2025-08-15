package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.ProjectIncome
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectIncomeDao {
    @Query("SELECT * FROM project_income WHERE projectId = :projectId ORDER BY date DESC")
    fun getIncomesByProject(projectId: Long): Flow<List<ProjectIncome>>

    @Query("SELECT * FROM project_income WHERE id = :id")
    suspend fun getIncomeById(id: Long): ProjectIncome?

    @Query("SELECT SUM(amount * units) FROM project_income WHERE projectId = :projectId")
    suspend fun getTotalIncomeForProject(projectId: Long): Double?

    @Insert
    suspend fun insertIncome(income: ProjectIncome): Long

    @Update
    suspend fun updateIncome(income: ProjectIncome)

    @Delete
    suspend fun deleteIncome(income: ProjectIncome)
}