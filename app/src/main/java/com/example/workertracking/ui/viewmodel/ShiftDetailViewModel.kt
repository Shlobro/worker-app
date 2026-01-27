package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.data.entity.ShiftWorker
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.repository.ShiftRepository
import com.example.workertracking.repository.WorkerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class ShiftDetailViewModel(
    private val shiftRepository: ShiftRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _shift = MutableStateFlow<Shift?>(null)
    val shift: StateFlow<Shift?> = _shift.asStateFlow()
    
    private val _shiftWorkers = MutableStateFlow<List<Pair<ShiftWorker, Worker>>>(emptyList())
    val shiftWorkers: StateFlow<List<Pair<ShiftWorker, Worker>>> = _shiftWorkers.asStateFlow()
    
    private val _allWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val allWorkers: StateFlow<List<Worker>> = _allWorkers.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()
    
    fun loadShiftDetails(shiftId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load shift
                val shift = shiftRepository.getShiftById(shiftId)
                _shift.value = shift
                
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
        
        // Load all workers as a separate flow
        viewModelScope.launch {
            workerRepository.getAllWorkers().collect { workers ->
                _allWorkers.value = workers
                // Refresh shift workers when workers change
                if (_shift.value != null) {
                    loadShiftWorkers(_shift.value!!.id)
                }
            }
        }
        
        // Load shift workers as a separate flow
        loadShiftWorkers(shiftId)
    }
    
    fun addWorkerToShift(shiftId: Long, workerId: Long, isHourlyRate: Boolean, payRate: Double, referencePayRate: Double? = null) {
        viewModelScope.launch {
            try {
                val shiftWorker = ShiftWorker(
                    shiftId = shiftId,
                    workerId = workerId,
                    isHourlyRate = isHourlyRate,
                    payRate = payRate,
                    referencePayRate = referencePayRate
                )

                shiftRepository.addWorkerToShift(shiftWorker)

                // Refresh shift workers
                loadShiftWorkers(shiftId)
            } catch (e: Exception) {
                if (e.message?.contains("UNIQUE constraint failed") == true ||
                    e.message?.contains("index_shift_workers_shiftId_workerId") == true) {
                    _error.value = "This worker is already assigned to this shift"
                } else {
                    _error.value = e.message ?: "Failed to add worker to shift"
                }
            }
        }
    }
    
    fun removeWorkerFromShift(shiftId: Long, workerId: Long) {
        viewModelScope.launch {
            try {
                shiftRepository.removeWorkerFromShift(shiftId, workerId)
                
                // Refresh shift workers
                loadShiftWorkers(shiftId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateWorkerPayment(shiftWorker: ShiftWorker) {
        viewModelScope.launch {
            try {
                shiftRepository.updateShiftWorker(shiftWorker)
                
                // Refresh shift workers
                loadShiftWorkers(shiftWorker.shiftId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    private fun loadShiftWorkers(shiftId: Long) {
        viewModelScope.launch {
            shiftRepository.getWorkersByShift(shiftId).collect { shiftWorkers ->
                val shiftWorkersWithWorkerInfo = shiftWorkers.mapNotNull { shiftWorker ->
                    val worker = _allWorkers.value.find { it.id == shiftWorker.workerId }
                    worker?.let { shiftWorker to it }
                }
                _shiftWorkers.value = shiftWorkersWithWorkerInfo
            }
        }
    }
    
    fun updateShift(name: String, date: Date, startTime: String, endTime: String, hours: Double) {
        viewModelScope.launch {
            try {
                _shift.value?.let { currentShift ->
                    val updatedShift = currentShift.copy(
                        name = name,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        hours = hours
                    )
                    shiftRepository.updateShift(updatedShift)
                    _shift.value = updatedShift
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
    
    fun deleteShift(shift: Shift) {
        viewModelScope.launch {
            try {
                shiftRepository.deleteShift(shift)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun markShiftWorkerAsPaid(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markShiftWorkerAsPaid(shiftWorkerId)
                // Refresh data to reflect payment status change
                _shift.value?.let { shift ->
                    loadShiftWorkers(shift.id)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}