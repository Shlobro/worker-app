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
    val incomeType: IncomeType,
    val incomeAmount: Double
)

enum class IncomeType {
    DAILY,
    WEEKLY,
    HOURLY,
    MONTHLY,
    FIXED
}