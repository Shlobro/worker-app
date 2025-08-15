package com.example.workertracking.repository

import com.example.workertracking.data.dao.EventDao
import com.example.workertracking.data.entity.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()
    
    suspend fun getEventById(id: Long): Event? = eventDao.getEventById(id)
    
    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)
    
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
}