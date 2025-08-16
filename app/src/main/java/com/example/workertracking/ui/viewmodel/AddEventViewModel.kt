package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Event
import com.example.workertracking.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class AddEventViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    fun saveEvent(
        name: String,
        date: Date,
        startTime: String,
        endTime: String,
        hours: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val event = Event(
                    name = name,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    hours = hours
                )
                eventRepository.insertEvent(event)
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