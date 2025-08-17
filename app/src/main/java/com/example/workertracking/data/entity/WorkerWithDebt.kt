package com.example.workertracking.data.entity

data class WorkerWithDebt(
    val worker: Worker,
    val totalOwed: Double = 0.0,
    val unpaidShiftsCount: Int = 0,
    val unpaidEventsCount: Int = 0
)