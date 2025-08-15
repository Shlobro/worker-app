package com.example.workertracking.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "shift_workers",
    foreignKeys = [
        ForeignKey(
            entity = Shift::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShiftWorker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shiftId: Long,
    val workerId: Long,
    val isHourlyRate: Boolean, // true for hourly, false for fixed amount
    val payRate: Double // hourly rate or fixed amount
)