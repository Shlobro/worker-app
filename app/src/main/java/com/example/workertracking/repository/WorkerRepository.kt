package com.example.workertracking.repository

import com.example.workertracking.data.dao.WorkerDao
import com.example.workertracking.data.dao.PaymentDao
import com.example.workertracking.data.entity.Worker
import kotlinx.coroutines.flow.Flow

class WorkerRepository(
    private val workerDao: WorkerDao,
    private val paymentDao: PaymentDao
) {
    
    fun getAllWorkers(): Flow<List<Worker>> = workerDao.getAllWorkers()
    
    suspend fun getWorkerById(id: Long): Worker? = workerDao.getWorkerById(id)
    
    suspend fun insertWorker(worker: Worker): Long = workerDao.insertWorker(worker)
    
    suspend fun updateWorker(worker: Worker) = workerDao.updateWorker(worker)
    
    suspend fun deleteWorker(worker: Worker) = workerDao.deleteWorker(worker)
    
    suspend fun getTotalOwedToWorker(workerId: Long): Double {
        val totalEarned = workerDao.getTotalOwedToWorker(workerId) ?: 0.0
        val totalPaid = paymentDao.getTotalPaymentsToWorker(workerId) ?: 0.0
        return totalEarned - totalPaid
    }
    
    suspend fun getTotalPaymentsOwed(): Double {
        val allWorkers = workerDao.getAllWorkers()
        // This is simplified - in real implementation we'd calculate properly
        return 0.0
    }
}