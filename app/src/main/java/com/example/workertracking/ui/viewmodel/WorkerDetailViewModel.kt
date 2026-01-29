package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.Project
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import com.example.workertracking.repository.WorkerRepository
import com.example.workertracking.repository.ProjectRepository
import com.example.workertracking.repository.EventRepository
import com.example.workertracking.repository.ShiftRepository
import com.example.workertracking.util.PaymentCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class WorkerDetailViewModel(
    private val workerRepository: WorkerRepository,
    private val projectRepository: ProjectRepository,
    private val eventRepository: EventRepository,
    @Suppress("unused") private val shiftRepository: ShiftRepository
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
    
    private val _dateFilter = MutableStateFlow<Pair<Date?, Date?>>(Pair(null, null))
    val dateFilter: StateFlow<Pair<Date?, Date?>> = _dateFilter.asStateFlow()
    
    private val _unpaidReferenceShifts = MutableStateFlow<List<UnpaidShiftWorkerInfo>>(emptyList())
    val unpaidReferenceShifts: StateFlow<List<UnpaidShiftWorkerInfo>> = _unpaidReferenceShifts.asStateFlow()

    private val _unpaidReferenceEvents = MutableStateFlow<List<UnpaidEventWorkerInfo>>(emptyList())
    val unpaidReferenceEvents: StateFlow<List<UnpaidEventWorkerInfo>> = _unpaidReferenceEvents.asStateFlow()

    private val _totalReferenceOwed = MutableStateFlow(0.0)
    val totalReferenceOwed: StateFlow<Double> = _totalReferenceOwed.asStateFlow()

    private val _paidShifts = MutableStateFlow<List<UnpaidShiftWorkerInfo>>(emptyList())
    val paidShifts: StateFlow<List<UnpaidShiftWorkerInfo>> = _paidShifts.asStateFlow()

    private val _paidEvents = MutableStateFlow<List<UnpaidEventWorkerInfo>>(emptyList())
    val paidEvents: StateFlow<List<UnpaidEventWorkerInfo>> = _paidEvents.asStateFlow()

    private val _showPaidItems = MutableStateFlow(false)
    val showPaidItems: StateFlow<Boolean> = _showPaidItems.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    private suspend fun loadAllWorkers() {
        _allWorkers.value = workerRepository.getAllWorkers().first()
    }

    private suspend fun loadWorkerProjectsAndEvents(workerId: Long) {
        // Load projects where worker has worked through shifts
        _projects.value = projectRepository.getProjectsForWorker(workerId)

        // Load only events where this worker is assigned
        _events.value = eventRepository.getEventsForWorker(workerId).first()
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
                _error.value = e.message
            }
        }
    }
    
    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }
    
    fun deleteWorker() {
        viewModelScope.launch {
            try {
                _worker.value?.let { worker ->
                    workerRepository.deleteWorker(worker)
                }
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }

    private fun loadUnpaidItems(workerId: Long) {
        viewModelScope.launch {
            try {
                val currentFilter = _dateFilter.value
                val unpaidShifts = workerRepository.getUnpaidShiftWorkersForWorkerWithDateFilter(
                    workerId, 
                    currentFilter.first, 
                    currentFilter.second
                )
                val unpaidEvents = workerRepository.getUnpaidEventWorkersForWorkerWithDateFilter(
                    workerId, 
                    currentFilter.first, 
                    currentFilter.second
                )
                
                _unpaidShifts.value = unpaidShifts
                _unpaidEvents.value = unpaidEvents
                
                // Load reference payments owed TO this worker (when they are the reference worker)
                val unpaidReferenceShifts = workerRepository.getUnpaidReferenceShiftsForWorker(workerId)
                val unpaidReferenceEvents = workerRepository.getUnpaidReferenceEventsForWorker(workerId)
                
                _unpaidReferenceShifts.value = unpaidReferenceShifts
                _unpaidReferenceEvents.value = unpaidReferenceEvents
                
                // Calculate total owed directly to this worker (exclude reference payments they make)
                val shiftTotal = unpaidShifts.sumOf { unpaidShift ->
                    PaymentCalculator.calculateWorkerPayment(
                        payRate = unpaidShift.shiftWorker.payRate,
                        hours = unpaidShift.shiftHours,
                        isHourlyRate = unpaidShift.shiftWorker.isHourlyRate
                    )
                }
                
                val eventTotal = unpaidEvents.sumOf { unpaidEvent ->
                    PaymentCalculator.calculateTotalNetPayment(
                        payRate = unpaidEvent.eventWorker.payRate,
                        hours = unpaidEvent.eventWorker.hours,
                        isHourlyRate = unpaidEvent.eventWorker.isHourlyRate,
                        referencePayRate = unpaidEvent.eventWorker.referencePayRate,
                        amountPaid = unpaidEvent.eventWorker.amountPaid,
                        tipAmount = unpaidEvent.eventWorker.tipAmount,
                        referenceAmountPaid = unpaidEvent.eventWorker.referenceAmountPaid,
                        referenceTipAmount = unpaidEvent.eventWorker.referenceTipAmount
                    )
                }
                
                // Calculate reference payments owed TO this worker
                val referenceShiftTotal = unpaidReferenceShifts.sumOf { shift ->
                    PaymentCalculator.calculateReferencePayment(
                        referencePayRate = shift.shiftWorker.referencePayRate,
                        hours = shift.shiftHours
                    )
                }
                
                val referenceEventTotal = unpaidReferenceEvents.sumOf { event ->
                    val totalRefCost = PaymentCalculator.calculateReferencePayment(
                        referencePayRate = event.eventWorker.referencePayRate,
                        hours = event.eventWorker.hours
                    )
                    PaymentCalculator.calculateNetReferencePayment(
                        totalReferencePayment = totalRefCost,
                        referenceAmountPaid = event.eventWorker.referenceAmountPaid,
                        referenceTipAmount = event.eventWorker.referenceTipAmount
                    )
                }
                
                _totalOwed.value = shiftTotal + eventTotal
                _totalReferenceOwed.value = referenceShiftTotal + referenceEventTotal
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }

    private fun loadCompleteHistory(workerId: Long) {
        viewModelScope.launch {
            try {
                val currentFilter = _dateFilter.value
                val allShifts = workerRepository.getAllShiftWorkersForWorkerWithDateFilter(
                    workerId, 
                    currentFilter.first, 
                    currentFilter.second
                )
                val allEvents = workerRepository.getAllEventWorkersForWorkerWithDateFilter(
                    workerId, 
                    currentFilter.first, 
                    currentFilter.second
                )
                
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
                    if (_showPaidItems.value) {
                        loadPaidItems(worker.id) // Refresh paid items if showing
                    }
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
                    if (_showPaidItems.value) {
                        loadPaidItems(worker.id) // Refresh paid items if showing
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateEventWorkerPayment(eventWorkerId: Long, isPaid: Boolean, amountPaid: Double, tipAmount: Double) {
        viewModelScope.launch {
            try {
                workerRepository.updateEventWorkerPayment(eventWorkerId, isPaid, amountPaid, tipAmount)
                _worker.value?.let { worker ->
                    loadUnpaidItems(worker.id) // Refresh the data
                    loadCompleteHistory(worker.id) // Refresh complete history too
                    if (_showPaidItems.value) {
                        loadPaidItems(worker.id) // Refresh paid items if showing
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun setDateFilter(startDate: Date?, endDate: Date?) {
        _dateFilter.value = Pair(startDate, endDate)
        // Reload data with filter
        _worker.value?.let { worker ->
            loadUnpaidItems(worker.id)
            loadCompleteHistory(worker.id)
        }
    }
    
    fun clearDateFilter() {
        _dateFilter.value = Pair(null, null)
        // Reload data without filter
        _worker.value?.let { worker ->
            loadUnpaidItems(worker.id)
            loadCompleteHistory(worker.id)
        }
    }
    
    fun markAllAsPaid() {
        viewModelScope.launch {
            try {
                _worker.value?.let { worker ->
                    workerRepository.markAllAsPayedForWorker(worker.id)
                    loadUnpaidItems(worker.id) // Refresh the data
                    loadCompleteHistory(worker.id) // Refresh complete history too
                    if (_showPaidItems.value) {
                        loadPaidItems(worker.id) // Refresh paid items if showing
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun revokeShiftPayment(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.revokeShiftWorkerPayment(shiftWorkerId)
                _worker.value?.let { worker ->
                    loadUnpaidItems(worker.id) // Refresh unpaid items
                    loadCompleteHistory(worker.id) // Refresh complete history
                    if (_showPaidItems.value) {
                        loadPaidItems(worker.id) // Refresh paid items
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun revokeEventPayment(eventWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.revokeEventWorkerPayment(eventWorkerId)
                _worker.value?.let { worker ->
                    loadUnpaidItems(worker.id) // Refresh unpaid items
                    loadCompleteHistory(worker.id) // Refresh complete history
                    if (_showPaidItems.value) {
                        loadPaidItems(worker.id) // Refresh paid items
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleShowPaidItems() {
        _showPaidItems.value = !_showPaidItems.value
        if (_showPaidItems.value) {
            _worker.value?.let { worker ->
                loadPaidItems(worker.id)
            }
        } else {
            _paidShifts.value = emptyList()
            _paidEvents.value = emptyList()
        }
    }

    private fun loadPaidItems(workerId: Long) {
        viewModelScope.launch {
            try {
                val paidShifts = workerRepository.getPaidShiftWorkersForWorker(workerId)
                val paidEvents = workerRepository.getPaidEventWorkersForWorker(workerId)
                
                _paidShifts.value = paidShifts
                _paidEvents.value = paidEvents
            } catch (e: Exception) {
                // Handle error silently or add error state if needed
            }
        }
    }
}
