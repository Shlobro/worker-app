package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.ShiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _shifts = MutableStateFlow<List<Shift>>(emptyList())
    val shifts: StateFlow<List<Shift>> = _shifts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _project.value = projectRepository.getProjectById(projectId)
                loadProjectShifts(projectId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadProjectShifts(projectId: Long) {
        viewModelScope.launch {
            shiftRepository.getShiftsByProject(projectId).collect { shifts ->
                _shifts.value = shifts
            }
        }
    }
    
    fun deleteShift(shift: Shift) {
        viewModelScope.launch {
            try {
                shiftRepository.deleteShift(shift)
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }
}