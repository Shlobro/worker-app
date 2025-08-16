package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.repository.ShiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class AddShiftViewModel(
    private val shiftRepository: ShiftRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    fun saveShift(
        projectId: Long,
        name: String,
        date: Date,
        startTime: String,
        endTime: String,
        hours: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val shift = Shift(
                    projectId = projectId,
                    name = name,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    hours = hours
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
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }
}