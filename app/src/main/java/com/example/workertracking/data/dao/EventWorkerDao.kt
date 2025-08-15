package com.example.workertracking.data.dao

import androidx.room.*
import com.example.workertracking.data.entity.EventWorker
import kotlinx.coroutines.flow.Flow

@Dao
interface EventWorkerDao {
    @Query("SELECT * FROM event_workers WHERE eventId = :eventId")
    fun getWorkersByEvent(eventId: Long): Flow<List<EventWorker>>

    @Query("SELECT * FROM event_workers WHERE workerId = :workerId")
    fun getEventsByWorker(workerId: Long): Flow<List<EventWorker>>

    @Insert
    suspend fun insertEventWorker(eventWorker: EventWorker): Long

    @Update
    suspend fun updateEventWorker(eventWorker: EventWorker)

    @Delete
    suspend fun deleteEventWorker(eventWorker: EventWorker)

    @Query("DELETE FROM event_workers WHERE eventId = :eventId")
    suspend fun deleteAllWorkersFromEvent(eventId: Long)

    @Query("SELECT SUM(hours * payRate) FROM event_workers WHERE eventId = :eventId")
    suspend fun getTotalCostForEvent(eventId: Long): Double?
}