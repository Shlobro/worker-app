package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.WorkerWithDebt
import com.example.workertracking.repository.WorkerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkersViewModel(
    private val workerRepository: WorkerRepository
) : ViewModel() {
    
    private val _workers = MutableStateFlow<List<Worker>>(emptyList())
    val workers: StateFlow<List<Worker>> = _workers.asStateFlow()
    
    private val _workersWithDebt = MutableStateFlow<List<WorkerWithDebt>>(emptyList())
    val workersWithDebt: StateFlow<List<WorkerWithDebt>> = _workersWithDebt.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadWorkers()
        loadWorkersWithDebt()
    }
    
    private fun loadWorkers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                workerRepository.getAllWorkers().collect { workerList ->
                    _workers.value = workerList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            try {
                workerRepository.deleteWorker(worker)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private fun loadWorkersWithDebt() {
        viewModelScope.launch {
            try {
                workerRepository.getAllWorkers().collect { workerList ->
                    val workersWithDebt = workerList.map { worker ->
                        val unpaidShifts = workerRepository.getUnpaidShiftWorkersForWorker(worker.id)
                        val unpaidEvents = workerRepository.getUnpaidEventWorkersForWorker(worker.id)
                        
                        val shiftTotal = unpaidShifts.sumOf { unpaidShift ->
                            val workerPayment = if (unpaidShift.shiftWorker.isHourlyRate) {
                                unpaidShift.shiftWorker.payRate * unpaidShift.shiftHours
                            } else {
                                unpaidShift.shiftWorker.payRate
                            }
                            val referencePayment = (unpaidShift.shiftWorker.referencePayRate ?: 0.0) * unpaidShift.shiftHours
                            workerPayment + referencePayment
                        }
                        
                        val eventTotal = unpaidEvents.sumOf { unpaidEvent ->
                            val workerPayment = if (unpaidEvent.eventWorker.isHourlyRate) {
                                unpaidEvent.eventWorker.hours * unpaidEvent.eventWorker.payRate
                            } else {
                                unpaidEvent.eventWorker.payRate
                            }
                            val referencePayment = (unpaidEvent.eventWorker.referencePayRate ?: 0.0) * unpaidEvent.eventWorker.hours
                            workerPayment + referencePayment
                        }
                        
                        WorkerWithDebt(
                            worker = worker,
                            totalOwed = shiftTotal + eventTotal,
                            unpaidShiftsCount = unpaidShifts.size,
                            unpaidEventsCount = unpaidEvents.size
                        )
                    }
                    _workersWithDebt.value = workersWithDebt
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}