package com.example.workertracking.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workerId: Long,
    val amount: Double,
    val datePaid: Date,
    val sourceType: PaymentSourceType,
    val sourceId: Long?
)

enum class PaymentSourceType {
    PROJECT,
    EVENT,
    OTHER
}