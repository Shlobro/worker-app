package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import com.example.workertracking.repository.WorkerRepository
import com.example.workertracking.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MoneyOwedUiState(
    val isLoading: Boolean = true,
    val unpaidShifts: List<UnpaidShiftWorkerInfo> = emptyList(),
    val unpaidEvents: List<UnpaidEventWorkerInfo> = emptyList(),
    val paidShifts: List<UnpaidShiftWorkerInfo> = emptyList(),
    val paidEvents: List<UnpaidEventWorkerInfo> = emptyList(),
    val totalDebt: Double = 0.0,
    val showPaidItems: Boolean = false
)

class MoneyOwedViewModel(
    private val workerRepository: WorkerRepository,
    private val appContainer: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoneyOwedUiState())
    val uiState: StateFlow<MoneyOwedUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val unpaidShifts = workerRepository.getUnpaidShiftWorkers()
                val unpaidEvents = workerRepository.getUnpaidEventWorkers()
                
                // Load paid items if needed
                val paidShifts = if (_uiState.value.showPaidItems) {
                    workerRepository.getAllPaidShiftWorkers()
                } else {
                    emptyList()
                }
                
                val paidEvents = if (_uiState.value.showPaidItems) {
                    workerRepository.getAllPaidEventWorkers()
                } else {
                    emptyList()
                }
                
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
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    unpaidShifts = unpaidShifts,
                    unpaidEvents = unpaidEvents,
                    paidShifts = paidShifts,
                    paidEvents = paidEvents,
                    totalDebt = shiftTotal + eventTotal
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun markShiftAsPaid(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markShiftWorkerAsPaid(shiftWorkerId)
                loadData() // Refresh the data
                appContainer.triggerDashboardRefresh() // Trigger dashboard refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun markEventAsPaid(eventWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markEventWorkerAsPaid(eventWorkerId)
                loadData() // Refresh the data
                appContainer.triggerDashboardRefresh() // Trigger dashboard refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun revokeShiftPayment(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.revokeShiftWorkerPayment(shiftWorkerId)
                loadData() // Refresh the data
                appContainer.triggerDashboardRefresh() // Trigger dashboard refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun revokeEventPayment(eventWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.revokeEventWorkerPayment(eventWorkerId)
                loadData() // Refresh the data
                appContainer.triggerDashboardRefresh() // Trigger dashboard refresh
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleShowPaidItems() {
        _uiState.value = _uiState.value.copy(
            showPaidItems = !_uiState.value.showPaidItems
        )
        loadData() // Reload data to include/exclude paid items
    }

    class Factory(
        private val workerRepository: WorkerRepository,
        private val appContainer: AppContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoneyOwedViewModel::class.java)) {
                return MoneyOwedViewModel(workerRepository, appContainer) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}