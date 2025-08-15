package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.WorkerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers.asStateFlow()

    private val _shifts = MutableStateFlow<List<Shift>>(emptyList())
    val shifts: StateFlow<List<Shift>> = _shifts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _project.value = projectRepository.getProjectById(projectId)
                loadProjectWorkersAndShifts(projectId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadProjectWorkersAndShifts(projectId: Long) {
        viewModelScope.launch {
            // For now, load all workers and shifts
            // TODO: Filter by project when DAO methods are available
            workerRepository.getAllWorkers().collect { workers ->
                _workers.value = workers
            }
        }
        viewModelScope.launch {
            // TODO: Load shifts for this project when Shift entity and DAO are implemented
            _shifts.value = emptyList()
        }
    }
}