package com.example.workertracking.repository

import com.example.workertracking.data.dao.WorkerDao
import com.example.workertracking.data.dao.PaymentDao
import com.example.workertracking.data.dao.ShiftWorkerDao
import com.example.workertracking.data.dao.EventWorkerDao
import com.example.workertracking.data.entity.Worker
import com.example.workertracking.data.entity.UnpaidShiftWorkerInfo
import com.example.workertracking.data.entity.UnpaidEventWorkerInfo
import kotlinx.coroutines.flow.Flow

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
        val totalEarned = workerDao.getTotalOwedToWorker(workerId) ?: 0.0
        val totalPaid = paymentDao.getTotalPaymentsToWorker(workerId) ?: 0.0
        return totalEarned - totalPaid
    }
    
    suspend fun getTotalPaymentsOwed(): Double {
        val unpaidShifts = shiftWorkerDao.getUnpaidShiftWorkers()
        val unpaidEvents = eventWorkerDao.getUnpaidEventWorkers()
        
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

    suspend fun getAllShiftWorkersForWorker(workerId: Long): List<UnpaidShiftWorkerInfo> {
        return shiftWorkerDao.getAllShiftWorkersForWorker(workerId)
    }

    suspend fun getAllEventWorkersForWorker(workerId: Long): List<UnpaidEventWorkerInfo> {
        return eventWorkerDao.getAllEventWorkersForWorker(workerId)
    }
}