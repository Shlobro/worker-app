package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import com.example.workertracking.repository.WorkerRepository
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.EventRepository
import com.example.workertracking.repository.ShiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkerDetailViewModel(
    private val workerRepository: WorkerRepository,
    private val projectRepository: ProjectRepository,
    private val eventRepository: EventRepository,
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    private val _worker = MutableStateFlow<Worker?>(null)
    val worker: StateFlow<Worker?> = _worker.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val allWorkers: StateFlow<List<Worker>> = _allWorkers.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _referenceWorker = MutableStateFlow<Worker?>(null)
    val referenceWorker: StateFlow<Worker?> = _referenceWorker.asStateFlow()

    private val _unpaidShifts = MutableStateFlow<List<UnpaidShiftWorkerInfo>>(emptyList())
    val unpaidShifts: StateFlow<List<UnpaidShiftWorkerInfo>> = _unpaidShifts.asStateFlow()

    private val _unpaidEvents = MutableStateFlow<List<UnpaidEventWorkerInfo>>(emptyList())
    val unpaidEvents: StateFlow<List<UnpaidEventWorkerInfo>> = _unpaidEvents.asStateFlow()

    private val _totalOwed = MutableStateFlow(0.0)
    val totalOwed: StateFlow<Double> = _totalOwed.asStateFlow()

    private val _allShifts = MutableStateFlow<List<UnpaidShiftWorkerInfo>>(emptyList())
    val allShifts: StateFlow<List<UnpaidShiftWorkerInfo>> = _allShifts.asStateFlow()

    private val _allEvents = MutableStateFlow<List<UnpaidEventWorkerInfo>>(emptyList())
    val allEvents: StateFlow<List<UnpaidEventWorkerInfo>> = _allEvents.asStateFlow()

    fun loadWorker(workerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val worker = workerRepository.getWorkerById(workerId)
                _worker.value = worker
                
                // Load reference worker if exists
                worker?.referenceId?.let { referenceId ->
                    _referenceWorker.value = workerRepository.getWorkerById(referenceId)
                } ?: run {
                    _referenceWorker.value = null
                }
                
                loadWorkerProjectsAndEvents(workerId)
                loadAllWorkers()
                loadUnpaidItems(workerId)
                loadCompleteHistory(workerId)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadAllWorkers() {
        viewModelScope.launch {
            workerRepository.getAllWorkers().collect { workers ->
                _allWorkers.value = workers
            }
        }
    }

    private fun loadWorkerProjectsAndEvents(workerId: Long) {
        viewModelScope.launch {
            // For now, load empty projects list
            // TODO: Implement proper project loading for worker through ShiftWorker table
            _projects.value = emptyList()
        }
        viewModelScope.launch {
            // Load only events where this worker is assigned
            eventRepository.getEventsForWorker(workerId).collect { events ->
                _events.value = events
            }
        }
    }
    
    fun updateWorker(name: String, phoneNumber: String, referenceId: Long?) {
        viewModelScope.launch {
            try {
                _worker.value?.let { currentWorker ->
                    val updatedWorker = currentWorker.copy(
                        name = name,
                        phoneNumber = phoneNumber,
                        referenceId = referenceId
                    )
                    workerRepository.updateWorker(updatedWorker)
                    _worker.value = updatedWorker
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

    private fun loadUnpaidItems(workerId: Long) {
        viewModelScope.launch {
            try {
                val unpaidShifts = workerRepository.getUnpaidShiftWorkersForWorker(workerId)
                val unpaidEvents = workerRepository.getUnpaidEventWorkersForWorker(workerId)
                
                _unpaidShifts.value = unpaidShifts
                _unpaidEvents.value = unpaidEvents
                
                val shiftTotal = unpaidShifts.sumOf { unpaidShift ->
                    if (unpaidShift.shiftWorker.isHourlyRate) {
                        unpaidShift.shiftWorker.payRate * unpaidShift.shiftHours
                    } else {
                        unpaidShift.shiftWorker.payRate
                    }
                }
                
                val eventTotal = unpaidEvents.sumOf { unpaidEvent ->
                    unpaidEvent.eventWorker.hours * unpaidEvent.eventWorker.payRate
                }
                
                _totalOwed.value = shiftTotal + eventTotal
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }

    private fun loadCompleteHistory(workerId: Long) {
        viewModelScope.launch {
            try {
                val allShifts = workerRepository.getAllShiftWorkersForWorker(workerId)
                val allEvents = workerRepository.getAllEventWorkersForWorker(workerId)
                
                _allShifts.value = allShifts
                _allEvents.value = allEvents
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }

    fun markShiftAsPaid(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markShiftWorkerAsPaid(shiftWorkerId)
                _worker.value?.let { worker ->
                    loadUnpaidItems(worker.id) // Refresh the data
                    loadCompleteHistory(worker.id) // Refresh complete history too
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun markEventAsPaid(eventWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markEventWorkerAsPaid(eventWorkerId)
                _worker.value?.let { worker ->
                    loadUnpaidItems(worker.id) // Refresh the data
                    loadCompleteHistory(worker.id) // Refresh complete history too
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}