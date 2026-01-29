package com.example.workertracking.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.workertracking.data.Converters

@Entity(
    tableName = "workers",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["referenceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("referenceId")]
)
@TypeConverters(Converters::class)
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val referenceId: Long? = null,
    val photoUris: List<String> = emptyList()
)