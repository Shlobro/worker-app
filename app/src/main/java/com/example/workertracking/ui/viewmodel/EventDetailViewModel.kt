package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.EventWorker
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.repository.EventRepository
import com.example.workertracking.repository.WorkerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

data class EventWorkerWithName(
    val eventWorker: EventWorker,
    val workerName: String
)

class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _eventWorkers = MutableStateFlow<List<EventWorkerWithName>>(emptyList())
    val eventWorkers: StateFlow<List<EventWorkerWithName>> = _eventWorkers.asStateFlow()

    private val _totalCost = MutableStateFlow(0.0)
    val totalCost: StateFlow<Double> = _totalCost.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    private val _allWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val allWorkers: StateFlow<List<Worker>> = _allWorkers.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _event.value = eventRepository.getEventById(eventId)
                loadEventWorkers(eventId)
                loadTotalCost(eventId)
                loadAllWorkers()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadAllWorkers() {
        _allWorkers.value = workerRepository.getAllWorkers().first()
    }

    private suspend fun loadEventWorkers(eventId: Long) {
        val eventWorkers = eventRepository.getWorkersForEvent(eventId).first()
        val eventWorkersWithNames = eventWorkers.map { eventWorker ->
            val workerId = eventWorker.workerId
            val worker = workerId?.let { workerRepository.getWorkerById(it) }
            EventWorkerWithName(
                eventWorker = eventWorker,
                workerName = worker?.name ?: "(Deleted Worker)"
            )
        }
        _eventWorkers.value = eventWorkersWithNames
    }

    private suspend fun loadTotalCost(eventId: Long) {
        val cost = eventRepository.getTotalEventCost(eventId) ?: 0.0
        _totalCost.value = cost
    }

    fun updateEvent(name: String, date: Date, startTime: String, endTime: String, hours: String, income: Double) {
        viewModelScope.launch {
            try {
                _event.value?.let { currentEvent ->
                    val updatedEvent = currentEvent.copy(
                        name = name,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        hours = hours,
                        income = income
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

    fun deleteEvent() {
        viewModelScope.launch {
            try {
                _event.value?.let { currentEvent ->
                    eventRepository.deleteEvent(currentEvent)
                    _deleteSuccess.value = true
                }
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun clearDeleteSuccess() {
        _deleteSuccess.value = false
    }

    fun addWorkerToEvent(eventId: Long, workerId: Long, hours: Double, isHourlyRate: Boolean, payRate: Double, referencePayRate: Double? = null) {
        viewModelScope.launch {
            try {
                val eventWorker = EventWorker(
                    eventId = eventId,
                    workerId = workerId,
                    hours = hours,
                    isHourlyRate = isHourlyRate,
                    payRate = payRate,
                    referencePayRate = referencePayRate
                )
                eventRepository.insertEventWorker(eventWorker)
                loadEventWorkers(eventId)
                loadTotalCost(eventId)
            } catch (e: Exception) {
                if (e.message?.contains("UNIQUE constraint failed") == true ||
                    e.message?.contains("index_event_workers_eventId_workerId") == true) {
                    _error.value = "This worker is already assigned to this event"
                } else {
                    _error.value = e.message ?: "Failed to add worker to event"
                }
            }
        }
    }

    fun removeWorkerFromEvent(eventWorker: EventWorker) {
        viewModelScope.launch {
            try {
                eventRepository.deleteEventWorker(eventWorker)
                _event.value?.let { event ->
                    loadEventWorkers(event.id)
                    loadTotalCost(event.id)
                }
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }

    fun updateWorkerInEvent(eventWorker: EventWorker) {
        viewModelScope.launch {
            try {
                workerRepository.updateEventWorker(eventWorker)
                _event.value?.let { event ->
                    loadEventWorkers(event.id)
                    loadTotalCost(event.id)
                }
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }

    fun updateEventWorkerPayment(eventWorkerId: Long, isPaid: Boolean, amountPaid: Double, tipAmount: Double) {
        viewModelScope.launch {
            try {
                workerRepository.updateEventWorkerPayment(eventWorkerId, isPaid, amountPaid, tipAmount)
                _event.value?.let { event ->
                    loadEventWorkers(event.id)
                    loadTotalCost(event.id)
                }
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }

    fun updateEventWorkerReferencePayment(eventWorkerId: Long, isReferencePaid: Boolean, referenceAmountPaid: Double, referenceTipAmount: Double) {
        viewModelScope.launch {
            try {
                workerRepository.updateEventWorkerReferencePayment(eventWorkerId, isReferencePaid, referenceAmountPaid, referenceTipAmount)
                _event.value?.let { event ->
                    loadEventWorkers(event.id)
                    loadTotalCost(event.id)
                }
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}