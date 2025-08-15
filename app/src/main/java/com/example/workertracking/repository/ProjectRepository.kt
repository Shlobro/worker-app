package com.example.workertracking.repository

import com.example.workertracking.data.dao.ProjectDao
import com.example.workertracking.data.entity.Project
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()
    
    suspend fun getProjectById(id: Long): Project? = projectDao.getProjectById(id)
    
    suspend fun insertProject(project: Project): Long = projectDao.insertProject(project)
    
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    
    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)
    
    suspend fun getTotalProjectIncome(): Double {
        val fixedIncome = projectDao.getTotalFixedProjectIncome() ?: 0.0
        val variableIncome = projectDao.getTotalProjectIncomeExcludingFixed() ?: 0.0
        return fixedIncome + variableIncome
    }
}