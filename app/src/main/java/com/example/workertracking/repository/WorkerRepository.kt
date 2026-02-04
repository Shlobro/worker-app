package com.example.workertracking.repository

import com.example.workertracking.data.dao.WorkerDao
import com.example.workertracking.data.dao.PaymentDao
import com.example.workertracking.data.dao.ShiftWorkerDao
import com.example.workertracking.data.dao.EventWorkerDao
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import com.example.workertracking.data.entity.WorkerWithDebt
import com.example.workertracking.data.entity.WorkerWithDebtData
import com.example.workertracking.util.PaymentCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import kotlin.math.max

class WorkerRepository(
    private val workerDao: WorkerDao,
    @Suppress("unused") private val paymentDao: PaymentDao,
    private val shiftWorkerDao: ShiftWorkerDao,
    private val eventWorkerDao: EventWorkerDao
) {
    
    fun getAllWorkers(): Flow<List<Worker>> = workerDao.getAllWorkers()

    @Suppress("unused")
    fun getAllWorkersWithDebt(): Flow<List<WorkerWithDebt>> {
        return workerDao.getAllWorkersWithDebtData().map { dataList ->
            dataList.map { it.toWorkerWithDebt() }
        }
    }

    fun getAllWorkersWithDebtData(): Flow<List<WorkerWithDebtData>> {
        return workerDao.getAllWorkersWithDebtData()
    }

    suspend fun getWorkerById(id: Long): Worker? = workerDao.getWorkerById(id)

    fun getWorkerByIdFlow(id: Long): Flow<Worker?> = workerDao.getWorkerByIdFlow(id)

    suspend fun insertWorker(worker: Worker): Long {
        validateNoCircularReference(worker.referenceId, worker.id)
        return workerDao.insertWorker(worker)
    }

    suspend fun updateWorker(worker: Worker) {
        validateNoCircularReference(worker.referenceId, worker.id)
        workerDao.updateWorker(worker)
    }

    /**
     * Validates that setting a reference worker doesn't create a circular reference chain.
     * A circular reference occurs when: A -> B -> C -> A
     *
     * @param referenceId The ID of the worker being set as a reference
     * @param workerId The ID of the worker being created/updated
     * @throws IllegalArgumentException if a circular reference is detected
     */
    private suspend fun validateNoCircularReference(referenceId: Long?, workerId: Long) {
        if (referenceId == null) return

        // Check if trying to reference self
        if (referenceId == workerId) {
            throw IllegalArgumentException("A worker cannot reference themselves")
        }

        // Check if the reference worker exists
        val referenceWorker = workerDao.getWorkerById(referenceId)
            ?: throw IllegalArgumentException("Reference worker with ID $referenceId does not exist")

        // Traverse the reference chain to detect cycles
        val visited = mutableSetOf<Long>()
        visited.add(workerId) // Add the current worker to visited set

        var currentRefId: Long? = referenceWorker.referenceId
        while (currentRefId != null) {
            if (visited.contains(currentRefId)) {
                throw IllegalArgumentException("Circular reference detected: setting this reference would create a cycle in the worker reference chain")
            }
            visited.add(currentRefId)

            val nextWorker = workerDao.getWorkerById(currentRefId)
            currentRefId = nextWorker?.referenceId
        }
    }
    
    suspend fun deleteWorker(worker: Worker) = workerDao.deleteWorker(worker)
    
    @Suppress("unused")
    suspend fun getTotalOwedToWorker(workerId: Long): Double {
        val shifts = shiftWorkerDao.getAllShiftWorkersForWorker(workerId)
        val events = eventWorkerDao.getAllEventWorkersForWorker(workerId)

        val shiftsOwed = shifts.sumOf { shiftInfo ->
            var total = 0.0
            if (!shiftInfo.shiftWorker.isPaid) {
                val workerPayment = PaymentCalculator.calculateWorkerPayment(
                    payRate = shiftInfo.shiftWorker.payRate,
                    hours = shiftInfo.shiftHours,
                    isHourlyRate = shiftInfo.shiftWorker.isHourlyRate
                )
                val netWorkerPayment = PaymentCalculator.calculateNetPayment(
                    totalPayment = workerPayment,
                    amountPaid = shiftInfo.shiftWorker.amountPaid,
                    tipAmount = shiftInfo.shiftWorker.tipAmount
                )
                total += max(0.0, netWorkerPayment)
            }
            if (!shiftInfo.shiftWorker.isReferencePaid && shiftInfo.shiftWorker.referencePayRate != null) {
                val refPayment = PaymentCalculator.calculateReferencePayment(
                    referencePayRate = shiftInfo.shiftWorker.referencePayRate,
                    hours = shiftInfo.shiftHours,
                    isReferenceHourlyRate = shiftInfo.shiftWorker.isReferenceHourlyRate
                )
                val netRefPayment = PaymentCalculator.calculateNetReferencePayment(
                    totalReferencePayment = refPayment,
                    referenceAmountPaid = shiftInfo.shiftWorker.referenceAmountPaid,
                    referenceTipAmount = shiftInfo.shiftWorker.referenceTipAmount
                )
                total += max(0.0, netRefPayment)
            }
            total
        }

        val eventsOwed = events.sumOf { eventInfo ->
            var total = 0.0
            if (!eventInfo.eventWorker.isPaid) {
                val workerPayment = PaymentCalculator.calculateWorkerPayment(
                    payRate = eventInfo.eventWorker.payRate,
                    hours = eventInfo.eventWorker.hours,
                    isHourlyRate = eventInfo.eventWorker.isHourlyRate
                )
                val netWorkerPayment = PaymentCalculator.calculateNetPayment(
                    totalPayment = workerPayment,
                    amountPaid = eventInfo.eventWorker.amountPaid,
                    tipAmount = eventInfo.eventWorker.tipAmount
                )
                total += max(0.0, netWorkerPayment)
            }
            if (!eventInfo.eventWorker.isReferencePaid && eventInfo.eventWorker.referencePayRate != null) {
                val refPayment = PaymentCalculator.calculateReferencePayment(
                    referencePayRate = eventInfo.eventWorker.referencePayRate,
                    hours = eventInfo.eventWorker.hours,
                    isReferenceHourlyRate = eventInfo.eventWorker.isReferenceHourlyRate
                )
                val netRefPayment = PaymentCalculator.calculateNetReferencePayment(
                    totalReferencePayment = refPayment,
                    referenceAmountPaid = eventInfo.eventWorker.referenceAmountPaid,
                    referenceTipAmount = eventInfo.eventWorker.referenceTipAmount
                )
                total += max(0.0, netRefPayment)
            }
            total
        }

        val refShiftsOwed = getUnpaidReferenceShiftsForWorker(workerId).sumOf { shift ->
            if (shift.shiftWorker.isReferencePaid) 0.0 else {
                val refPayment = PaymentCalculator.calculateReferencePayment(
                    referencePayRate = shift.shiftWorker.referencePayRate,
                    hours = shift.shiftHours,
                    isReferenceHourlyRate = shift.shiftWorker.isReferenceHourlyRate
                )
                val netRefPayment = PaymentCalculator.calculateNetReferencePayment(
                    totalReferencePayment = refPayment,
                    referenceAmountPaid = shift.shiftWorker.referenceAmountPaid,
                    referenceTipAmount = shift.shiftWorker.referenceTipAmount
                )
                max(0.0, netRefPayment)
            }
        }

        val refEventsOwed = getUnpaidReferenceEventsForWorker(workerId).sumOf { event ->
            if (event.eventWorker.isReferencePaid) 0.0 else {
                val refPayment = PaymentCalculator.calculateReferencePayment(
                    referencePayRate = event.eventWorker.referencePayRate,
                    hours = event.eventWorker.hours,
                    isReferenceHourlyRate = event.eventWorker.isReferenceHourlyRate
                )
                val netRefPayment = PaymentCalculator.calculateNetReferencePayment(
                    totalReferencePayment = refPayment,
                    referenceAmountPaid = event.eventWorker.referenceAmountPaid,
                    referenceTipAmount = event.eventWorker.referenceTipAmount
                )
                max(0.0, netRefPayment)
            }
        }

        return shiftsOwed + eventsOwed + refShiftsOwed + refEventsOwed
    }
    
    suspend fun getTotalPaymentsOwed(): Double {
        val shifts = shiftWorkerDao.getShiftWorkersWithOutstandingPayments()
        val events = eventWorkerDao.getEventWorkersWithOutstandingPayments()

        val shiftTotal = shifts.sumOf { shift ->
            var total = 0.0
            if (!shift.shiftWorker.isPaid) {
                val workerPayment = PaymentCalculator.calculateWorkerPayment(
                    payRate = shift.shiftWorker.payRate,
                    hours = shift.shiftHours,
                    isHourlyRate = shift.shiftWorker.isHourlyRate
                )
                val netWorkerPayment = PaymentCalculator.calculateNetPayment(
                    totalPayment = workerPayment,
                    amountPaid = shift.shiftWorker.amountPaid,
                    tipAmount = shift.shiftWorker.tipAmount
                )
                total += max(0.0, netWorkerPayment)
            }
            if (!shift.shiftWorker.isReferencePaid && shift.shiftWorker.referencePayRate != null) {
                val refPayment = PaymentCalculator.calculateReferencePayment(
                    referencePayRate = shift.shiftWorker.referencePayRate,
                    hours = shift.shiftHours,
                    isReferenceHourlyRate = shift.shiftWorker.isReferenceHourlyRate
                )
                val netRefPayment = PaymentCalculator.calculateNetReferencePayment(
                    totalReferencePayment = refPayment,
                    referenceAmountPaid = shift.shiftWorker.referenceAmountPaid,
                    referenceTipAmount = shift.shiftWorker.referenceTipAmount
                )
                total += max(0.0, netRefPayment)
            }
            total
        }

        val eventTotal = events.sumOf { event ->
            var total = 0.0
            if (!event.eventWorker.isPaid) {
                val workerPayment = PaymentCalculator.calculateWorkerPayment(
                    payRate = event.eventWorker.payRate,
                    hours = event.eventWorker.hours,
                    isHourlyRate = event.eventWorker.isHourlyRate
                )
                val netWorkerPayment = PaymentCalculator.calculateNetPayment(
                    totalPayment = workerPayment,
                    amountPaid = event.eventWorker.amountPaid,
                    tipAmount = event.eventWorker.tipAmount
                )
                total += max(0.0, netWorkerPayment)
            }
            if (!event.eventWorker.isReferencePaid && event.eventWorker.referencePayRate != null) {
                val refPayment = PaymentCalculator.calculateReferencePayment(
                    referencePayRate = event.eventWorker.referencePayRate,
                    hours = event.eventWorker.hours,
                    isReferenceHourlyRate = event.eventWorker.isReferenceHourlyRate
                )
                val netRefPayment = PaymentCalculator.calculateNetReferencePayment(
                    totalReferencePayment = refPayment,
                    referenceAmountPaid = event.eventWorker.referenceAmountPaid,
                    referenceTipAmount = event.eventWorker.referenceTipAmount
                )
                total += max(0.0, netRefPayment)
            }
            total
        }

        return shiftTotal + eventTotal
    }

    fun getShiftWorkersWithOutstandingPaymentsFlow(): Flow<List<UnpaidShiftWorkerInfo>> {
        return shiftWorkerDao.getShiftWorkersWithOutstandingPaymentsFlow()
    }

    fun getEventWorkersWithOutstandingPaymentsFlow(): Flow<List<UnpaidEventWorkerInfo>> {
        return eventWorkerDao.getEventWorkersWithOutstandingPaymentsFlow()
    }

    suspend fun getUnpaidShiftWorkersForWorker(workerId: Long): List<UnpaidShiftWorkerInfo> {
        return shiftWorkerDao.getUnpaidShiftWorkersForWorker(workerId)
    }

    suspend fun getUnpaidEventWorkersForWorker(workerId: Long): List<UnpaidEventWorkerInfo> {
        return eventWorkerDao.getUnpaidEventWorkersForWorker(workerId)
    }

    suspend fun markShiftWorkerAsPaid(shiftWorkerId: Long) {
        shiftWorkerDao.updatePaymentStatus(shiftWorkerId, true)
    }

    suspend fun markEventWorkerAsPaid(eventWorkerId: Long) {
        eventWorkerDao.updatePaymentStatus(eventWorkerId, true)
    }

    suspend fun markShiftReferenceAsPaid(shiftWorkerId: Long) {
        shiftWorkerDao.updateReferencePaymentStatus(shiftWorkerId, true)
    }

    suspend fun markEventReferenceAsPaid(eventWorkerId: Long) {
        eventWorkerDao.updateReferencePaymentStatus(eventWorkerId, true)
    }

    suspend fun revokeShiftReferencePayment(shiftWorkerId: Long) {
        shiftWorkerDao.updateReferencePaymentStatus(shiftWorkerId, false)
    }

    suspend fun revokeEventReferencePayment(eventWorkerId: Long) {
        eventWorkerDao.updateReferencePaymentStatus(eventWorkerId, false)
    }

    suspend fun updateEventWorkerPayment(eventWorkerId: Long, isPaid: Boolean, amountPaid: Double, tipAmount: Double) {
        eventWorkerDao.updatePaymentDetails(eventWorkerId, isPaid, amountPaid, tipAmount)
    }

    suspend fun updateEventWorkerReferencePayment(eventWorkerId: Long, isReferencePaid: Boolean, referenceAmountPaid: Double, referenceTipAmount: Double) {
        eventWorkerDao.updateReferencePaymentDetails(eventWorkerId, isReferencePaid, referenceAmountPaid, referenceTipAmount)
    }

    suspend fun updateShiftWorkerPayment(shiftWorkerId: Long, isPaid: Boolean, amountPaid: Double, tipAmount: Double) {
        shiftWorkerDao.updatePaymentDetails(shiftWorkerId, isPaid, amountPaid, tipAmount)
    }

    suspend fun updateShiftWorkerReferencePayment(shiftWorkerId: Long, isReferencePaid: Boolean, referenceAmountPaid: Double, referenceTipAmount: Double) {
        shiftWorkerDao.updateReferencePaymentDetails(shiftWorkerId, isReferencePaid, referenceAmountPaid, referenceTipAmount)
    }

    suspend fun updateEventWorker(eventWorker: com.example.workertracking.data.entity.EventWorker) {
        eventWorkerDao.updateEventWorker(eventWorker)
    }

    @Suppress("unused")
    suspend fun deleteEventWorker(eventWorker: com.example.workertracking.data.entity.EventWorker) {
        eventWorkerDao.deleteEventWorker(eventWorker)
    }

    suspend fun revokeShiftWorkerPayment(shiftWorkerId: Long) {
        shiftWorkerDao.updatePaymentStatus(shiftWorkerId, false)
    }

    suspend fun revokeEventWorkerPayment(eventWorkerId: Long) {
        eventWorkerDao.updatePaymentStatus(eventWorkerId, false)
    }

    suspend fun getPaidShiftWorkersForWorker(workerId: Long): List<UnpaidShiftWorkerInfo> {
        return shiftWorkerDao.getPaidShiftWorkersForWorker(workerId)
    }

    suspend fun getPaidEventWorkersForWorker(workerId: Long): List<UnpaidEventWorkerInfo> {
        return eventWorkerDao.getPaidEventWorkersForWorker(workerId)
    }

    suspend fun getAllPaidShiftWorkers(): List<UnpaidShiftWorkerInfo> {
        return shiftWorkerDao.getAllPaidShiftWorkers()
    }

    suspend fun getAllPaidEventWorkers(): List<UnpaidEventWorkerInfo> {
        return eventWorkerDao.getAllPaidEventWorkers()
    }

    @Suppress("unused")
    suspend fun getAllShiftWorkersForWorker(workerId: Long): List<UnpaidShiftWorkerInfo> {
        return shiftWorkerDao.getAllShiftWorkersForWorker(workerId)
    }

    @Suppress("unused")
    suspend fun getAllEventWorkersForWorker(workerId: Long): List<UnpaidEventWorkerInfo> {
        return eventWorkerDao.getAllEventWorkersForWorker(workerId)
    }
    
    // Date filtering methods
    suspend fun getUnpaidShiftWorkersForWorkerWithDateFilter(
        workerId: Long, 
        startDate: Date?, 
        endDate: Date?
    ): List<UnpaidShiftWorkerInfo> {
        val allUnpaidShifts = shiftWorkerDao.getUnpaidShiftWorkersForWorker(workerId)
        return filterShiftsByDate(allUnpaidShifts, startDate, endDate)
    }

    suspend fun getUnpaidEventWorkersForWorkerWithDateFilter(
        workerId: Long, 
        startDate: Date?, 
        endDate: Date?
    ): List<UnpaidEventWorkerInfo> {
        val allUnpaidEvents = eventWorkerDao.getUnpaidEventWorkersForWorker(workerId)
        return filterEventsByDate(allUnpaidEvents, startDate, endDate)
    }

    suspend fun getAllShiftWorkersForWorkerWithDateFilter(
        workerId: Long, 
        startDate: Date?, 
        endDate: Date?
    ): List<UnpaidShiftWorkerInfo> {
        val allShifts = shiftWorkerDao.getAllShiftWorkersForWorker(workerId)
        return filterShiftsByDate(allShifts, startDate, endDate)
    }

    suspend fun getAllEventWorkersForWorkerWithDateFilter(
        workerId: Long, 
        startDate: Date?, 
        endDate: Date?
    ): List<UnpaidEventWorkerInfo> {
        val allEvents = eventWorkerDao.getAllEventWorkersForWorker(workerId)
        return filterEventsByDate(allEvents, startDate, endDate)
    }
    
    private fun filterShiftsByDate(
        shifts: List<UnpaidShiftWorkerInfo>,
        startDate: Date?,
        endDate: Date?
    ): List<UnpaidShiftWorkerInfo> {
        if (startDate == null && endDate == null) return shifts

        // Add 24 hours to endDate to include the entire day (endDate at 23:59:59.999)
        val adjustedEndDate = endDate?.let { Date(it.time + 24 * 60 * 60 * 1000 - 1) }

        return shifts.filter { shift ->
            val shiftDate = Date(shift.shiftDate)
            when {
                startDate != null && adjustedEndDate != null ->
                    shiftDate >= startDate && shiftDate <= adjustedEndDate
                startDate != null -> shiftDate >= startDate
                else -> shiftDate <= adjustedEndDate!!
            }
        }
    }
    
    private fun filterEventsByDate(
        events: List<UnpaidEventWorkerInfo>,
        startDate: Date?,
        endDate: Date?
    ): List<UnpaidEventWorkerInfo> {
        if (startDate == null && endDate == null) return events

        // Add 24 hours to endDate to include the entire day (endDate at 23:59:59.999)
        val adjustedEndDate = endDate?.let { Date(it.time + 24 * 60 * 60 * 1000 - 1) }

        return events.filter { event ->
            val eventDate = Date(event.eventDate)
            when {
                startDate != null && adjustedEndDate != null ->
                    eventDate >= startDate && eventDate <= adjustedEndDate
                startDate != null -> eventDate >= startDate
                else -> eventDate <= adjustedEndDate!!
            }
        }
    }
    
    suspend fun getTotalEarningsForWorker(workerId: Long): Double {
        return getTotalEarningsForWorkerWithDateFilter(workerId, null, null)
    }
    
    suspend fun getTotalEarningsForWorkerWithDateFilter(
        workerId: Long,
        startDate: Date?,
        endDate: Date?
    ): Double {
        val shifts = getAllShiftWorkersForWorkerWithDateFilter(workerId, startDate, endDate)
        val events = getAllEventWorkersForWorkerWithDateFilter(workerId, startDate, endDate)

        val shiftEarnings = shifts.sumOf { shift ->
            PaymentCalculator.calculateWorkerPayment(
                payRate = shift.shiftWorker.payRate,
                hours = shift.shiftHours,
                isHourlyRate = shift.shiftWorker.isHourlyRate
            )
        }

        val eventEarnings = events.sumOf { event ->
            PaymentCalculator.calculateWorkerPayment(
                payRate = event.eventWorker.payRate,
                hours = event.eventWorker.hours,
                isHourlyRate = event.eventWorker.isHourlyRate
            )
        }

        return shiftEarnings + eventEarnings
    }
    
    // Methods to calculate reference payments owed TO a worker (when they are the reference worker)
    suspend fun getUnpaidReferenceShiftsForWorker(workerId: Long): List<UnpaidShiftWorkerInfo> {
        val allUnpaidShifts = shiftWorkerDao.getUnpaidShiftWorkers()
        return allUnpaidShifts.filter { shift ->
            // Find workers who have this worker as their reference
            val shiftWorkerId = shift.shiftWorker.workerId ?: return@filter false
            val worker = workerDao.getWorkerById(shiftWorkerId)
            worker?.referenceId == workerId && shift.shiftWorker.referencePayRate != null
        }
    }

    suspend fun getUnpaidReferenceEventsForWorker(workerId: Long): List<UnpaidEventWorkerInfo> {
        val allUnpaidReferencePayments = eventWorkerDao.getUnpaidReferencePayments()
        return allUnpaidReferencePayments.filter { event ->
            // Find workers who have this worker as their reference
            val eventWorkerId = event.eventWorker.workerId ?: return@filter false
            val worker = workerDao.getWorkerById(eventWorkerId)
            worker?.referenceId == workerId
        }
    }
    
    @Suppress("unused")
    suspend fun getTotalReferencePaymentsOwedToWorker(workerId: Long): Double {
        val referenceShifts = getUnpaidReferenceShiftsForWorker(workerId)
        val referenceEvents = getUnpaidReferenceEventsForWorker(workerId)

        val shiftReferenceTotal = referenceShifts.sumOf { shift ->
            if (shift.shiftWorker.isReferencePaid) 0.0 else {
                val refPayment = PaymentCalculator.calculateReferencePayment(
                    referencePayRate = shift.shiftWorker.referencePayRate,
                    hours = shift.shiftHours,
                    isReferenceHourlyRate = shift.shiftWorker.isReferenceHourlyRate
                )
                val netRefPayment = PaymentCalculator.calculateNetReferencePayment(
                    totalReferencePayment = refPayment,
                    referenceAmountPaid = shift.shiftWorker.referenceAmountPaid,
                    referenceTipAmount = shift.shiftWorker.referenceTipAmount
                )
                max(0.0, netRefPayment)
            }
        }

        val eventReferenceTotal = referenceEvents.sumOf { event ->
            if (event.eventWorker.isReferencePaid) 0.0 else {
                val refPayment = PaymentCalculator.calculateReferencePayment(
                    referencePayRate = event.eventWorker.referencePayRate,
                    hours = event.eventWorker.hours,
                    isReferenceHourlyRate = event.eventWorker.isReferenceHourlyRate
                )
                val netRefPayment = PaymentCalculator.calculateNetReferencePayment(
                    totalReferencePayment = refPayment,
                    referenceAmountPaid = event.eventWorker.referenceAmountPaid,
                    referenceTipAmount = event.eventWorker.referenceTipAmount
                )
                max(0.0, netRefPayment)
            }
        }

        return shiftReferenceTotal + eventReferenceTotal
    }
    
    suspend fun markAllAsPayedForWorker(workerId: Long) {
        val unpaidShifts = getUnpaidShiftWorkersForWorker(workerId)
        val unpaidEvents = getUnpaidEventWorkersForWorker(workerId)
        
        // Mark all unpaid shifts as paid
        unpaidShifts.forEach { unpaidShift ->
            shiftWorkerDao.updatePaymentStatus(unpaidShift.shiftWorker.id, true)
        }
        
        // Mark all unpaid events as paid
        unpaidEvents.forEach { unpaidEvent ->
            eventWorkerDao.updatePaymentStatus(unpaidEvent.eventWorker.id, true)
        }
    }
}