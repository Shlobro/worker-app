package com.example.workertracking.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employers")
data class Employer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String
)