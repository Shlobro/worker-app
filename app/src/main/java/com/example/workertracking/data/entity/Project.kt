package com.example.workertracking.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = Employer::class,
            parentColumns = ["id"],
            childColumns = ["employerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val location: String,
    val startDate: Date,
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val endDate: Date? = null,
    val employerId: Long? = null
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