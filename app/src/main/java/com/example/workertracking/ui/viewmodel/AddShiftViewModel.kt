package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.repository.ShiftRepository
import com.example.workertracking.repository.WorkerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class AddShiftViewModel(
    private val shiftRepository: ShiftRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    private val _allWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val allWorkers: StateFlow<List<Worker>> = _allWorkers.asStateFlow()
    
    fun loadAllWorkers() {
        viewModelScope.launch {
            workerRepository.getAllWorkers().collect { workers ->
                _allWorkers.value = workers
            }
        }
    }
    
    fun saveShift(
        projectId: Long,
        workerId: Long,
        date: Date,
        startTime: String,
        hours: Double,
        payRate: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Calculate end time - for now just store as string
                val endTime = calculateEndTime(startTime, hours)
                
                val shift = Shift(
                    projectId = projectId,
                    workerId = workerId,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    hours = hours,
                    payRate = payRate
                )
                
                shiftRepository.insertShift(shift)
                
                _saveSuccess.value = true
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    private fun calculateEndTime(startTime: String, hours: Double): String {
        // Simple calculation - in real app you'd use proper time handling
        return "$startTime + ${hours}h"
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }
}