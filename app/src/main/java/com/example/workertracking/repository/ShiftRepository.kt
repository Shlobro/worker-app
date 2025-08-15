package com.example.workertracking.repository

import com.example.workertracking.data.dao.ShiftDao
import com.example.workertracking.data.entity.Shift
import kotlinx.coroutines.flow.Flow

class ShiftRepository(private val shiftDao: ShiftDao) {
    
    fun getAllShifts(): Flow<List<Shift>> = shiftDao.getAllShifts()
    
    fun getShiftsByProject(projectId: Long): Flow<List<Shift>> = 
        shiftDao.getShiftsByProject(projectId)
    
    fun getShiftsByWorker(workerId: Long): Flow<List<Shift>> = 
        shiftDao.getShiftsByWorker(workerId)
    
    suspend fun getShiftById(id: Long): Shift? = shiftDao.getShiftById(id)
    
    suspend fun insertShift(shift: Shift): Long = shiftDao.insertShift(shift)
    
    suspend fun updateShift(shift: Shift) = shiftDao.updateShift(shift)
    
    suspend fun deleteShift(shift: Shift) = shiftDao.deleteShift(shift)
}