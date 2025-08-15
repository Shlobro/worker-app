package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.repository.WorkerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddWorkerViewModel(
    private val workerRepository: WorkerRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    private val _availableWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val availableWorkers: StateFlow<List<Worker>> = _availableWorkers.asStateFlow()
    
    init {
        loadAvailableWorkers()
    }
    
    private fun loadAvailableWorkers() {
        viewModelScope.launch {
            workerRepository.getAllWorkers().collect { workers ->
                _availableWorkers.value = workers
            }
        }
    }
    
    fun saveWorker(
        name: String,
        phoneNumber: String,
        referenceWorkerId: Long?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val worker = Worker(
                    name = name,
                    phoneNumber = phoneNumber,
                    referenceId = referenceWorkerId
                )
                workerRepository.insertWorker(worker)
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