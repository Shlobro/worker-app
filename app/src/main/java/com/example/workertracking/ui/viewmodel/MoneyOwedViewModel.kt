package com.example.workertracking.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import com.example.workertracking.repository.WorkerRepository
import com.example.workertracking.util.PaymentCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.max

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
    private val workerRepository: WorkerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoneyOwedUiState())
    val uiState: StateFlow<MoneyOwedUiState> = _uiState.asStateFlow()

    private val _showPaidItems = MutableStateFlow(false)

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                workerRepository.getShiftWorkersWithOutstandingPaymentsFlow(),
                workerRepository.getEventWorkersWithOutstandingPaymentsFlow(),
                _showPaidItems
            ) { unpaidShifts, unpaidEvents, showPaid ->
                Triple(unpaidShifts, unpaidEvents, showPaid)
            }.collect { (unpaidShifts, unpaidEvents, showPaid) ->
                _uiState.value = _uiState.value.copy(isLoading = true)

                try {
                    // Load paid items if needed
                    val paidShifts = if (showPaid) {
                        workerRepository.getAllPaidShiftWorkers()
                    } else {
                        emptyList()
                    }

                    val paidEvents = if (showPaid) {
                        workerRepository.getAllPaidEventWorkers()
                    } else {
                        emptyList()
                    }

                    val shiftTotal = unpaidShifts.sumOf { unpaidShift ->
                        val workerPayment = if (!unpaidShift.shiftWorker.isPaid) {
                            max(0.0, PaymentCalculator.calculateNetPayment(
                                totalPayment = PaymentCalculator.calculateWorkerPayment(
                                    payRate = unpaidShift.shiftWorker.payRate,
                                    hours = unpaidShift.shiftHours,
                                    isHourlyRate = unpaidShift.shiftWorker.isHourlyRate
                                ),
                                amountPaid = unpaidShift.shiftWorker.amountPaid,
                                tipAmount = unpaidShift.shiftWorker.tipAmount
                            ))
                        } else 0.0

                        val referencePayment = if (!unpaidShift.shiftWorker.isReferencePaid) {
                            max(0.0, PaymentCalculator.calculateNetReferencePayment(
                                totalReferencePayment = PaymentCalculator.calculateReferencePayment(
                                    referencePayRate = unpaidShift.shiftWorker.referencePayRate,
                                    hours = unpaidShift.shiftHours,
                                    isReferenceHourlyRate = unpaidShift.shiftWorker.isReferenceHourlyRate
                                ),
                                referenceAmountPaid = unpaidShift.shiftWorker.referenceAmountPaid,
                                referenceTipAmount = unpaidShift.shiftWorker.referenceTipAmount
                            ))
                        } else 0.0

                        workerPayment + referencePayment
                    }

                    val eventTotal = unpaidEvents.sumOf { unpaidEvent ->
                        val workerPayment = if (!unpaidEvent.eventWorker.isPaid) {
                            max(0.0, PaymentCalculator.calculateNetPayment(
                                totalPayment = PaymentCalculator.calculateWorkerPayment(
                                    payRate = unpaidEvent.eventWorker.payRate,
                                    hours = unpaidEvent.eventWorker.hours,
                                    isHourlyRate = unpaidEvent.eventWorker.isHourlyRate
                                ),
                                amountPaid = unpaidEvent.eventWorker.amountPaid,
                                tipAmount = unpaidEvent.eventWorker.tipAmount
                            ))
                        } else 0.0

                        val referencePayment = if (!unpaidEvent.eventWorker.isReferencePaid) {
                            max(0.0, PaymentCalculator.calculateNetReferencePayment(
                                totalReferencePayment = PaymentCalculator.calculateReferencePayment(
                                    referencePayRate = unpaidEvent.eventWorker.referencePayRate,
                                    hours = unpaidEvent.eventWorker.hours,
                                    isReferenceHourlyRate = unpaidEvent.eventWorker.isReferenceHourlyRate
                                ),
                                referenceAmountPaid = unpaidEvent.eventWorker.referenceAmountPaid,
                                referenceTipAmount = unpaidEvent.eventWorker.referenceTipAmount
                            ))
                        } else 0.0

                        workerPayment + referencePayment
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        unpaidShifts = unpaidShifts,
                        unpaidEvents = unpaidEvents,
                        paidShifts = paidShifts,
                        paidEvents = paidEvents,
                        totalDebt = shiftTotal + eventTotal,
                        showPaidItems = showPaid
                    )
                } catch (_: Exception) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun markShiftAsPaid(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markShiftWorkerAsPaid(shiftWorkerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark shift as paid: $shiftWorkerId", e)
            }
        }
    }

    fun markEventAsPaid(eventWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markEventWorkerAsPaid(eventWorkerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark event as paid: $eventWorkerId", e)
            }
        }
    }

    fun markShiftReferenceAsPaid(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markShiftReferenceAsPaid(shiftWorkerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark shift reference as paid: $shiftWorkerId", e)
            }
        }
    }

    fun markEventReferenceAsPaid(eventWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.markEventReferenceAsPaid(eventWorkerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark event reference as paid: $eventWorkerId", e)
            }
        }
    }

    fun revokeShiftPayment(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.revokeShiftWorkerPayment(shiftWorkerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to revoke shift payment: $shiftWorkerId", e)
            }
        }
    }

    fun revokeEventPayment(eventWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.revokeEventWorkerPayment(eventWorkerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to revoke event payment: $eventWorkerId", e)
            }
        }
    }

    fun revokeShiftReferencePayment(shiftWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.revokeShiftReferencePayment(shiftWorkerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to revoke shift reference payment: $shiftWorkerId", e)
            }
        }
    }

    fun revokeEventReferencePayment(eventWorkerId: Long) {
        viewModelScope.launch {
            try {
                workerRepository.revokeEventReferencePayment(eventWorkerId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to revoke event reference payment: $eventWorkerId", e)
            }
        }
    }

    companion object {
        private const val TAG = "MoneyOwedViewModel"
    }

    fun toggleShowPaidItems() {
        _showPaidItems.value = !_showPaidItems.value
    }

    class Factory(
        private val workerRepository: WorkerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoneyOwedViewModel::class.java)) {
                return MoneyOwedViewModel(workerRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
