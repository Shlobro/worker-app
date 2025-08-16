package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.data.entity.IncomeType
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.ShiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _shifts = MutableStateFlow<List<Shift>>(emptyList())
    val shifts: StateFlow<List<Shift>> = _shifts.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalPayments = MutableStateFlow(0.0)
    val totalPayments: StateFlow<Double> = _totalPayments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _project.value = projectRepository.getProjectById(projectId)
                loadProjectShifts(projectId)
                loadFinancialData(projectId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadProjectShifts(projectId: Long) {
        viewModelScope.launch {
            shiftRepository.getShiftsByProject(projectId).collect { shifts ->
                _shifts.value = shifts
                // Refresh financial data when shifts change
                loadFinancialData(projectId)
            }
        }
    }
    
    private fun loadFinancialData(projectId: Long) {
        viewModelScope.launch {
            // Load total income for project
            val income = projectRepository.getTotalIncomeForProject(projectId)
            _totalIncome.value = income
            
            // Load total payments owed to workers for this project
            val payments = shiftRepository.getTotalCostForProject(projectId) ?: 0.0
            _totalPayments.value = payments
        }
    }
    
    fun deleteShift(shift: Shift) {
        viewModelScope.launch {
            try {
                shiftRepository.deleteShift(shift)
                // Refresh financial data after deleting shift
                _project.value?.let { loadFinancialData(it.id) }
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }
    
    fun closeProject() {
        viewModelScope.launch {
            try {
                _project.value?.let { project ->
                    projectRepository.closeProject(project.id)
                    // Reload the project to get updated status
                    loadProject(project.id)
                }
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }
    
    fun updateProject(name: String, location: String, startDate: Date, incomeType: IncomeType, incomeAmount: Double) {
        viewModelScope.launch {
            try {
                _project.value?.let { currentProject ->
                    val updatedProject = currentProject.copy(
                        name = name,
                        location = location,
                        startDate = startDate,
                        incomeType = incomeType,
                        incomeAmount = incomeAmount
                    )
                    projectRepository.updateProject(updatedProject)
                    _project.value = updatedProject
                    _updateSuccess.value = true
                }
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }
    
    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}