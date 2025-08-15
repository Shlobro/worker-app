package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.IncomeType
import com.example.workertracking.data.entity.Project
import com.example.workertracking.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class AddProjectViewModel(
    private val projectRepository: ProjectRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    fun saveProject(
        name: String,
        location: String,
        startDate: Date,
        incomeType: IncomeType,
        incomeAmount: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val project = Project(
                    name = name,
                    location = location,
                    startDate = startDate,
                    incomeType = incomeType,
                    incomeAmount = incomeAmount
                )
                projectRepository.insertProject(project)
                _saveSuccess.value = true
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }
}