package com.example.workertracking.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = Employer::class,
            parentColumns = ["id"],
            childColumns = ["employerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("employerId")]
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val date: Date,
    val startTime: String,
    val endTime: String,
    val hours: String,
    val income: Double = 0.0,
    val employerId: Long? = null
)