package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Employer
import com.example.workertracking.repository.EmployerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmployersViewModel(
    private val employerRepository: EmployerRepository
) : ViewModel() {
    
    private val _employers = MutableStateFlow<List<Employer>>(emptyList())
    val employers: StateFlow<List<Employer>> = _employers.asStateFlow()
    
    private val _employerProfits = MutableStateFlow<Map<Long, Double>>(emptyMap())
    val employerProfits: StateFlow<Map<Long, Double>> = _employerProfits.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadEmployers()
    }
    
    private fun loadEmployers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                employerRepository.getAllEmployers().collect { employerList ->
                    _employers.value = employerList
                    loadEmployerProfits(employerList)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    private fun loadEmployerProfits(employers: List<Employer>) {
        viewModelScope.launch {
            val profits = mutableMapOf<Long, Double>()
            employers.forEach { employer ->
                try {
                    profits[employer.id] = employerRepository.getTotalProfitFromEmployer(employer.id)
                } catch (e: Exception) {
                    profits[employer.id] = 0.0
                }
            }
            _employerProfits.value = profits
        }
    }
    
    fun deleteEmployer(employer: Employer) {
        viewModelScope.launch {
            try {
                employerRepository.deleteEmployer(employer)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}