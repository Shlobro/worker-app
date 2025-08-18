package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY startDate DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): Project?

    @Insert
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)


    @Query("UPDATE projects SET status = 'CLOSED', endDate = :endDate WHERE id = :projectId")
    suspend fun closeProject(projectId: Long, endDate: Long)

    @Query("SELECT * FROM projects WHERE employerId = :employerId ORDER BY startDate DESC")
    suspend fun getProjectsByEmployer(employerId: Long): List<Project>
}