package com.example.workertracking.repository

import com.example.workertracking.data.dao.ShiftDao
import com.example.workertracking.data.dao.ShiftWorkerDao
import com.example.workertracking.data.entity.Shift
import com.example.workertracking.data.entity.ShiftWorker
import com.example.workertracking.data.entity.Worker
import kotlinx.coroutines.flow.Flow

class ShiftRepository(
    private val shiftDao: ShiftDao,
    private val shiftWorkerDao: ShiftWorkerDao
) {
    
    fun getAllShifts(): Flow<List<Shift>> = shiftDao.getAllShifts()
    
    fun getShiftsByProject(projectId: Long): Flow<List<Shift>> = 
        shiftDao.getShiftsByProject(projectId)
    
    suspend fun getShiftById(id: Long): Shift? = shiftDao.getShiftById(id)
    
    suspend fun insertShift(shift: Shift): Long = shiftDao.insertShift(shift)
    
    suspend fun updateShift(shift: Shift) = shiftDao.updateShift(shift)
    
    suspend fun deleteShift(shift: Shift) = shiftDao.deleteShift(shift)
    
    // ShiftWorker methods
    fun getWorkersByShift(shiftId: Long): Flow<List<ShiftWorker>> = 
        shiftWorkerDao.getWorkersByShift(shiftId)
    
    fun getWorkersForShift(shiftId: Long): Flow<List<Worker>> = 
        shiftWorkerDao.getWorkersForShift(shiftId)
    
    suspend fun addWorkerToShift(shiftWorker: ShiftWorker): Long = 
        shiftWorkerDao.insertShiftWorker(shiftWorker)
    
    suspend fun updateShiftWorker(shiftWorker: ShiftWorker) = 
        shiftWorkerDao.updateShiftWorker(shiftWorker)
    
    suspend fun removeWorkerFromShift(shiftId: Long, workerId: Long) = 
        shiftWorkerDao.removeWorkerFromShift(shiftId, workerId)
    
    suspend fun getTotalCostForShift(shiftId: Long): Double? = 
        shiftWorkerDao.getTotalCostForShift(shiftId)
    
    suspend fun getTotalCostForProject(projectId: Long): Double? = 
        shiftDao.getTotalCostForProject(projectId)
}