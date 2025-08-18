package com.example.workertracking.repository

import com.example.workertracking.data.dao.EmployerDao
import com.example.workertracking.data.dao.ProjectDao
import com.example.workertracking.data.dao.EventDao
import com.example.workertracking.data.entity.Employer
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Event
import kotlinx.coroutines.flow.Flow

class EmployerRepository(
    private val employerDao: EmployerDao,
    private val projectDao: ProjectDao,
    private val eventDao: EventDao
) {
    
    fun getAllEmployers(): Flow<List<Employer>> = employerDao.getAllEmployers()
    
    suspend fun getEmployerById(id: Long): Employer? = employerDao.getEmployerById(id)
    
    fun getEmployerByIdFlow(id: Long): Flow<Employer?> = employerDao.getEmployerByIdFlow(id)
    
    suspend fun insertEmployer(employer: Employer): Long = employerDao.insertEmployer(employer)
    
    suspend fun updateEmployer(employer: Employer) = employerDao.updateEmployer(employer)
    
    suspend fun deleteEmployer(employer: Employer) = employerDao.deleteEmployer(employer)
    
    suspend fun getTotalProfitFromEmployer(employerId: Long): Double {
        return employerDao.getTotalProfitFromEmployer(employerId) ?: 0.0
    }
    
    suspend fun getTotalIncomeFromEmployer(employerId: Long): Double {
        return employerDao.getTotalIncomeFromEmployer(employerId) ?: 0.0
    }
    
    suspend fun getTotalExpensesFromEmployer(employerId: Long): Double {
        return employerDao.getTotalExpensesFromEmployer(employerId) ?: 0.0
    }
    
    suspend fun getProjectsForEmployer(employerId: Long): List<Project> {
        return projectDao.getProjectsByEmployer(employerId)
    }
    
    suspend fun getEventsForEmployer(employerId: Long): List<Event> {
        return eventDao.getEventsByEmployer(employerId)
    }
}