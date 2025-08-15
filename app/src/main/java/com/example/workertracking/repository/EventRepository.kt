package com.example.workertracking.repository

import com.example.workertracking.data.dao.EventDao
import com.example.workertracking.data.dao.EventWorkerDao
import com.example.workertracking.data.entity.Event
import com.example.workertracking.data.entity.EventWorker
import com.example.workertracking.data.entity.Worker
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao,
    private val eventWorkerDao: EventWorkerDao
) {
    
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()
    
    suspend fun getEventById(id: Long): Event? = eventDao.getEventById(id)
    
    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)
    
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
    
    // EventWorker methods
    fun getWorkersForEvent(eventId: Long): Flow<List<EventWorker>> = 
        eventWorkerDao.getWorkersByEvent(eventId)
    
    suspend fun insertEventWorker(eventWorker: EventWorker) = 
        eventWorkerDao.insertEventWorker(eventWorker)
    
    suspend fun deleteEventWorker(eventWorker: EventWorker) = 
        eventWorkerDao.deleteEventWorker(eventWorker)
    
    suspend fun getTotalEventCost(eventId: Long): Double? = 
        eventWorkerDao.getTotalCostForEvent(eventId)
}