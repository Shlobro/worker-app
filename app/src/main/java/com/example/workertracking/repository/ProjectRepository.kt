package com.example.workertracking.repository

import com.example.workertracking.data.dao.ProjectDao
import com.example.workertracking.data.dao.ProjectIncomeDao
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.ProjectIncome
import kotlinx.coroutines.flow.Flow
import java.util.Date

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val projectIncomeDao: ProjectIncomeDao
) {
    
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()
    
    suspend fun getProjectById(id: Long): Project? = projectDao.getProjectById(id)
    
    suspend fun insertProject(project: Project): Long = projectDao.insertProject(project)
    
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    
    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)
    
    suspend fun closeProject(projectId: Long) = projectDao.closeProject(projectId, Date().time)
    
    suspend fun getTotalProjectIncome(): Double {
        val fixedIncome = projectDao.getTotalFixedProjectIncome() ?: 0.0
        val variableIncome = projectDao.getTotalProjectIncomeExcludingFixed() ?: 0.0
        return fixedIncome + variableIncome
    }
    
    // Project Income methods
    fun getIncomesByProject(projectId: Long): Flow<List<ProjectIncome>> = 
        projectIncomeDao.getIncomesByProject(projectId)
    
    suspend fun getTotalIncomeForProject(projectId: Long): Double =
        projectIncomeDao.getTotalIncomeForProject(projectId) ?: 0.0
    
    suspend fun insertIncome(income: ProjectIncome): Long = 
        projectIncomeDao.insertIncome(income)
    
    suspend fun updateIncome(income: ProjectIncome) = 
        projectIncomeDao.updateIncome(income)
    
    suspend fun deleteIncome(income: ProjectIncome) = 
        projectIncomeDao.deleteIncome(income)
}