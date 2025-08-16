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

class EventDetailViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _event.value = eventRepository.getEventById(eventId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEvent(name: String, date: Date, startTime: String, endTime: String, hours: String) {
        viewModelScope.launch {
            try {
                _event.value?.let { currentEvent ->
                    val updatedEvent = currentEvent.copy(
                        name = name,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        hours = hours
                    )
                    eventRepository.updateEvent(updatedEvent)
                    _event.value = updatedEvent
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