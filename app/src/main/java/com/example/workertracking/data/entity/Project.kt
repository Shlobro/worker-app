package com.example.workertracking.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val location: String,
    val startDate: Date,
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val endDate: Date? = null
)

enum class IncomeType {
    DAILY,
    WEEKLY,
    HOURLY,
    MONTHLY,
    FIXED
}

enum class ProjectStatus {
    ACTIVE,
    CLOSED
}