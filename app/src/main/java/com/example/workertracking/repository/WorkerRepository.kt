package com.example.workertracking.repository

import com.example.workertracking.data.dao.WorkerDao
import com.example.workertracking.data.dao.PaymentDao
import com.example.workertracking.data.dao.ShiftWorkerDao
import com.example.workertracking.data.dao.EventWorkerDao
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import kotlinx.coroutines.flow.Flow
import java.util.*

class WorkerRepository(
    private val workerDao: WorkerDao,
    private val paymentDao: PaymentDao,
    private val shiftWorkerDao: ShiftWorkerDao,
    private val eventWorkerDao: EventWorkerDao
) {
    
    fun getAllWorkers(): Flow<List<Worker>> = workerDao.getAllWorkers()
    
    suspend fun getWorkerById(id: Long): Worker? = workerDao.getWorkerById(id)
    
    fun getWorkerByIdFlow(id: Long): Flow<Worker?> = workerDao.getWorkerByIdFlow(id)
    
    suspend fun insertWorker(worker: Worker): Long = workerDao.insertWorker(worker)
    
    suspend fun updateWorker(worker: Worker) = workerDao.updateWorker(worker)
    
    suspend fun deleteWorker(worker: Worker) = workerDao.deleteWorker(worker)
    
    suspend fun getTotalOwedToWorker(workerId: Long): Double {
        val shifts = shiftWorkerDao.getAllShiftWorkersForWorker(workerId)
        val events = eventWorkerDao.getAllEventWorkersForWorker(workerId)
        
        val shiftsOwed = shifts.sumOf { shiftInfo ->
            if (shiftInfo.shiftWorker.isPaid) 0.0 else {
                val cost = if (shiftInfo.shiftWorker.isHourlyRate) {
                    shiftInfo.shiftWorker.payRate * shiftInfo.shiftHours
                } else {
                    shiftInfo.shiftWorker.payRate
                }
                val ref = (shiftInfo.shiftWorker.referencePayRate ?: 0.0) * shiftInfo.shiftHours
                cost + ref
            }
        }
        
        val eventsOwed = events.sumOf { eventInfo ->
            if (eventInfo.eventWorker.isPaid) 0.0 else {
                val cost = if (eventInfo.eventWorker.isHourlyRate) {
                    eventInfo.eventWorker.hours * eventInfo.eventWorker.payRate
                } else {
                    eventInfo.eventWorker.payRate
                }
                val ref = (eventInfo.eventWorker.referencePayRate ?: 0.0) * eventInfo.eventWorker.hours
                val totalCost = cost + ref
                totalCost - eventInfo.eventWorker.amountPaid - eventInfo.eventWorker.tipAmount
            }
        }
        
        val refShiftsOwed = getUnpaidReferenceShiftsForWorker(workerId).sumOf { shift ->
            (shift.shiftWorker.referencePayRate ?: 0.0) * shift.shiftHours
        }
        
        val refEventsOwed = getUnpaidReferenceEventsForWorker(workerId).sumOf { event ->
            val totalRefCost = (event.eventWorker.referencePayRate ?: 0.0) * event.eventWorker.hours
            if (event.eventWorker.isReferencePaid) 0.0 else totalRefCost - event.eventWorker.referenceAmountPaid - event.eventWorker.referenceTipAmount
        }
        
        return shiftsOwed + eventsOwed + refShiftsOwed + refEventsOwed
    }
    
    suspend fun getTotalPaymentsOwed(): Double {
        val unpaidShifts = shiftWorkerDao.getUnpaidShiftWorkers()
        val unpaidEvents = eventWorkerDao.getUnpaidEventWorkers()
        
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
            workerPayment + referencePayment - unpaidEvent.eventWorker.amountPaid
        }
        
        return shiftTotal + eventTotal
    }

    suspend fun getUnpaidShiftWorkers(): List<UnpaidShiftWorkerInfo> {
        return shiftWorkerDao.getUnpaidShiftWorkers()
    }

    suspend fun getUnpaidEventWorkers(): List<UnpaidEventWorkerInfo> {
        return eventWorkerDao.getUnpaidEventWorkers()
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

    suspend fun updateEventWorkerPayment(eventWorkerId: Long, isPaid: Boolean, amountPaid: Double, tipAmount: Double) {
        eventWorkerDao.updatePaymentDetails(eventWorkerId, isPaid, amountPaid, tipAmount)
    }

    suspend fun updateEventWorkerReferencePayment(eventWorkerId: Long, isReferencePaid: Boolean, referenceAmountPaid: Double, referenceTipAmount: Double) {
        eventWorkerDao.updateReferencePaymentDetails(eventWorkerId, isReferencePaid, referenceAmountPaid, referenceTipAmount)
    }

    suspend fun updateEventWorker(eventWorker: com.example.workertracking.data.entity.EventWorker) {
        eventWorkerDao.updateEventWorker(eventWorker)
    }

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

    suspend fun getAllShiftWorkersForWorker(workerId: Long): List<UnpaidShiftWorkerInfo> {
        return shiftWorkerDao.getAllShiftWorkersForWorker(workerId)
    }

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
        
        return shifts.filter { shift ->
            val shiftDate = Date(shift.shiftDate)
            when {
                startDate != null && endDate != null -> 
                    shiftDate >= startDate && shiftDate <= endDate
                startDate != null -> shiftDate >= startDate
                endDate != null -> shiftDate <= endDate
                else -> true
            }
        }
    }
    
    private fun filterEventsByDate(
        events: List<UnpaidEventWorkerInfo>, 
        startDate: Date?, 
        endDate: Date?
    ): List<UnpaidEventWorkerInfo> {
        if (startDate == null && endDate == null) return events
        
        return events.filter { event ->
            val eventDate = Date(event.eventDate)
            when {
                startDate != null && endDate != null -> 
                    eventDate >= startDate && eventDate <= endDate
                startDate != null -> eventDate >= startDate
                endDate != null -> eventDate <= endDate
                else -> true
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
            val workerPayment = if (shift.shiftWorker.isHourlyRate) {
                shift.shiftWorker.payRate * shift.shiftHours
            } else {
                shift.shiftWorker.payRate
            }
            val referencePayment = (shift.shiftWorker.referencePayRate ?: 0.0) * shift.shiftHours
            workerPayment + referencePayment
        }
        
        val eventEarnings = events.sumOf { event ->
            val workerPayment = if (event.eventWorker.isHourlyRate) {
                event.eventWorker.hours * event.eventWorker.payRate
            } else {
                event.eventWorker.payRate
            }
            val referencePayment = (event.eventWorker.referencePayRate ?: 0.0) * event.eventWorker.hours
            workerPayment + referencePayment
        }
        
        return shiftEarnings + eventEarnings
    }
    
    // Methods to calculate reference payments owed TO a worker (when they are the reference worker)
    suspend fun getUnpaidReferenceShiftsForWorker(workerId: Long): List<UnpaidShiftWorkerInfo> {
        val allUnpaidShifts = shiftWorkerDao.getUnpaidShiftWorkers()
        return allUnpaidShifts.filter { shift ->
            // Find workers who have this worker as their reference
            val worker = workerDao.getWorkerById(shift.shiftWorker.workerId)
            worker?.referenceId == workerId && shift.shiftWorker.referencePayRate != null
        }
    }
    
    suspend fun getUnpaidReferenceEventsForWorker(workerId: Long): List<UnpaidEventWorkerInfo> {
        val allUnpaidEvents = eventWorkerDao.getUnpaidEventWorkers()
        return allUnpaidEvents.filter { event ->
            // Find workers who have this worker as their reference
            val worker = workerDao.getWorkerById(event.eventWorker.workerId)
            worker?.referenceId == workerId && event.eventWorker.referencePayRate != null
        }
    }
    
    suspend fun getTotalReferencePaymentsOwedToWorker(workerId: Long): Double {
        val referenceShifts = getUnpaidReferenceShiftsForWorker(workerId)
        val referenceEvents = getUnpaidReferenceEventsForWorker(workerId)

        val shiftReferenceTotal = referenceShifts.sumOf { shift ->
            (shift.shiftWorker.referencePayRate ?: 0.0) * shift.shiftHours
        }

        val eventReferenceTotal = referenceEvents.sumOf { event ->
            val totalRefCost = (event.eventWorker.referencePayRate ?: 0.0) * event.eventWorker.hours
            totalRefCost - event.eventWorker.referenceAmountPaid - event.eventWorker.referenceTipAmount
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