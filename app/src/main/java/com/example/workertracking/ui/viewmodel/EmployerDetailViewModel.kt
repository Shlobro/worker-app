package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Employer
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Event
import com.example.workertracking.repository.EmployerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmployerDetailViewModel(
    private val employerRepository: EmployerRepository
) : ViewModel() {
    
    private val _employer = MutableStateFlow<Employer?>(null)
    val employer: StateFlow<Employer?> = _employer.asStateFlow()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()
    
    private val _totalProfit = MutableStateFlow(0.0)
    val totalProfit: StateFlow<Double> = _totalProfit.asStateFlow()
    
    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()
    
    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()
    
    fun loadEmployer(employerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load employer
                employerRepository.getEmployerByIdFlow(employerId).collect { employer ->
                    _employer.value = employer
                    if (employer != null) {
                        loadProjects(employerId)
                        loadEvents(employerId)
                        loadFinancials(employerId)
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    private fun loadProjects(employerId: Long) {
        viewModelScope.launch {
            try {
                val projects = employerRepository.getProjectsForEmployer(employerId)
                _projects.value = projects
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    private fun loadEvents(employerId: Long) {
        viewModelScope.launch {
            try {
                val events = employerRepository.getEventsForEmployer(employerId)
                _events.value = events
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    private fun loadFinancials(employerId: Long) {
        viewModelScope.launch {
            try {
                val profit = employerRepository.getTotalProfitFromEmployer(employerId)
                val income = employerRepository.getTotalIncomeFromEmployer(employerId)
                val expenses = employerRepository.getTotalExpensesFromEmployer(employerId)
                _totalProfit.value = profit
                _totalIncome.value = income
                _totalExpenses.value = expenses
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun deleteEmployer() {
        val currentEmployer = _employer.value ?: return
        
        viewModelScope.launch {
            try {
                employerRepository.deleteEmployer(currentEmployer)
                _deleteSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearDeleteSuccess() {
        _deleteSuccess.value = false
    }
}