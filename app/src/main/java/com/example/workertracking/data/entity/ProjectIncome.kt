package com.example.workertracking.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "project_income",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProjectIncome(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val date: Date,
    val description: String, // e.g., "יום עבודה", "שבוע 1", etc.
    val amount: Double, // Amount earned for this period/unit
    val units: Double = 1.0 // e.g., 1 day, 2 weeks, 8 hours, etc.
)