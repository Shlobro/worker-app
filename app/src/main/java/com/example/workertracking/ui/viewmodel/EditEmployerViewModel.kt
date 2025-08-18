package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Employer
import com.example.workertracking.repository.EmployerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditEmployerViewModel(
    private val employerRepository: EmployerRepository
) : ViewModel() {
    
    private val _employer = MutableStateFlow<Employer?>(null)
    val employer: StateFlow<Employer?> = _employer.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()
    
    fun loadEmployer(employerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                employerRepository.getEmployerByIdFlow(employerId).collect { employer ->
                    _employer.value = employer
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun updateEmployer(name: String, phoneNumber: String) {
        val currentEmployer = _employer.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedEmployer = currentEmployer.copy(
                    name = name,
                    phoneNumber = phoneNumber
                )
                employerRepository.updateEmployer(updatedEmployer)
                _updateSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}