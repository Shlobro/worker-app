package com.example.workertracking.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_workers",
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["eventId", "workerId"], unique = true),
        Index(value = ["workerId"])
    ]
)
data class EventWorker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val workerId: Long?,
    val hours: Double,
    val isHourlyRate: Boolean, // true for hourly, false for fixed amount
    val payRate: Double, // hourly rate or fixed amount
    val referencePayRate: Double? = null, // reference worker hourly rate when applicable
    val isPaid: Boolean = false, // payment status
    val amountPaid: Double = 0.0,
    val tipAmount: Double = 0.0,
    val isReferencePaid: Boolean = false,
    val referenceAmountPaid: Double = 0.0,
    val referenceTipAmount: Double = 0.0
)