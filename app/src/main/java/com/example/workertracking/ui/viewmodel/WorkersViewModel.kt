package com.example.workertracking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.WorkerWithDebt
import com.example.workertracking.repository.WorkerRepository
import com.example.workertracking.util.PaymentCalculator
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
    
    private val _workerEarnings = MutableStateFlow<Map<Long, Double>>(emptyMap())
    val workerEarnings: StateFlow<Map<Long, Double>> = _workerEarnings.asStateFlow()
    
    private val _referenceWorkerNames = MutableStateFlow<Map<Long, String>>(emptyMap())
    val referenceWorkerNames: StateFlow<Map<Long, String>> = _referenceWorkerNames.asStateFlow()
    
    
    init {
        loadWorkers()
        loadWorkersWithDebtOptimized()
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

    private fun loadWorkersWithDebtOptimized() {
        viewModelScope.launch {
            try {
                workerRepository.getAllWorkersWithDebtData().collect { dataList ->
                    _workersWithDebt.value = dataList.map { it.toWorkerWithDebt() }
                    _workerEarnings.value = dataList.associate { it.id to it.totalEarnings }
                    _referenceWorkerNames.value = dataList.mapNotNull { data ->
                        data.referenceWorkerName?.let { data.id to it }
                    }.toMap()
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    @Deprecated("Use loadWorkersWithDebtOptimized instead - this causes N+1 query problem")
    private fun loadWorkersWithDebt() {
        viewModelScope.launch {
            try {
                workerRepository.getAllWorkers().collect { workerList ->
                    val workersWithDebt = workerList.map { worker ->
                        val unpaidShifts = workerRepository.getUnpaidShiftWorkersForWorker(worker.id)
                        val unpaidEvents = workerRepository.getUnpaidEventWorkersForWorker(worker.id)

                        // Calculate direct payments owed TO this worker (exclude reference payments they make)
                        val shiftTotal = unpaidShifts.sumOf { unpaidShift ->
                            PaymentCalculator.calculateWorkerPayment(
                                payRate = unpaidShift.shiftWorker.payRate,
                                hours = unpaidShift.shiftHours,
                                isHourlyRate = unpaidShift.shiftWorker.isHourlyRate
                            )
                        }

                        val eventTotal = unpaidEvents.sumOf { unpaidEvent ->
                            val basePayment = PaymentCalculator.calculateWorkerPayment(
                                payRate = unpaidEvent.eventWorker.payRate,
                                hours = unpaidEvent.eventWorker.hours,
                                isHourlyRate = unpaidEvent.eventWorker.isHourlyRate
                            )
                            PaymentCalculator.calculateNetPayment(
                                totalPayment = basePayment,
                                amountPaid = unpaidEvent.eventWorker.amountPaid,
                                tipAmount = unpaidEvent.eventWorker.tipAmount
                            )
                        }

                        // Calculate reference payments owed TO this worker (when they are the reference worker)
                        val unpaidReferenceShifts = workerRepository.getUnpaidReferenceShiftsForWorker(worker.id)
                        val unpaidReferenceEvents = workerRepository.getUnpaidReferenceEventsForWorker(worker.id)

                        val referenceShiftTotal = unpaidReferenceShifts.sumOf { shift ->
                            PaymentCalculator.calculateReferencePayment(
                                referencePayRate = shift.shiftWorker.referencePayRate,
                                hours = shift.shiftHours
                            )
                        }

                        val referenceEventTotal = unpaidReferenceEvents.sumOf { event ->
                            val baseReferencePayment = PaymentCalculator.calculateReferencePayment(
                                referencePayRate = event.eventWorker.referencePayRate,
                                hours = event.eventWorker.hours
                            )
                            PaymentCalculator.calculateNetReferencePayment(
                                totalReferencePayment = baseReferencePayment,
                                referenceAmountPaid = event.eventWorker.referenceAmountPaid,
                                referenceTipAmount = event.eventWorker.referenceTipAmount
                            )
                        }

                        WorkerWithDebt(
                            worker = worker,
                            totalOwed = shiftTotal + eventTotal,
                            unpaidShiftsCount = unpaidShifts.size,
                            unpaidEventsCount = unpaidEvents.size,
                            totalReferenceOwed = referenceShiftTotal + referenceEventTotal,
                            unpaidReferenceShiftsCount = unpaidReferenceShifts.size,
                            unpaidReferenceEventsCount = unpaidReferenceEvents.size
                        )
                    }
                    _workersWithDebt.value = workersWithDebt
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    @Deprecated("Use loadWorkersWithDebtOptimized instead - this causes N+1 query problem")
    private fun loadWorkerEarnings() {
        viewModelScope.launch {
            try {
                workerRepository.getAllWorkers().collect { workerList ->
                    val earningsMap = mutableMapOf<Long, Double>()

                    workerList.forEach { worker ->
                        val totalEarnings = workerRepository.getTotalEarningsForWorker(worker.id)
                        earningsMap[worker.id] = totalEarnings
                    }

                    _workerEarnings.value = earningsMap
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }


    @Deprecated("Use loadWorkersWithDebtOptimized instead - this causes N+1 query problem")
    private fun loadReferenceWorkerNames() {
        viewModelScope.launch {
            try {
                workerRepository.getAllWorkers().collect { workerList ->
                    val referenceNamesMap = mutableMapOf<Long, String>()

                    workerList.forEach { worker ->
                        if (worker.referenceId != null) {
                            val referenceWorker = workerRepository.getWorkerById(worker.referenceId)
                            referenceWorker?.let {
                                referenceNamesMap[worker.id] = it.name
                            }
                        }
                    }

                    _referenceWorkerNames.value = referenceNamesMap
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
