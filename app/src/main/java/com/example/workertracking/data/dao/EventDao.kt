package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): Event?

    @Insert
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)
    
    @Query("""
        SELECT DISTINCT e.* FROM events e 
        INNER JOIN event_workers ew ON e.id = ew.eventId 
        WHERE ew.workerId = :workerId 
        ORDER BY e.date DESC
    """)
    fun getEventsForWorker(workerId: Long): Flow<List<Event>>
}