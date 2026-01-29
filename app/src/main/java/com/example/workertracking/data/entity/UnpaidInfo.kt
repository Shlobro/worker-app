package com.example.workertracking.data.entity

import androidx.room.Embedded

data class UnpaidShiftWorkerInfo(
    @Embedded val shiftWorker: ShiftWorker,
    val workerName: String?,
    val shiftDate: Long,
    val projectName: String,
    val shiftHours: Double,
    val startTime: String,
    val endTime: String
)

data class UnpaidEventWorkerInfo(
    @Embedded val eventWorker: EventWorker,
    val workerName: String?,
    val eventDate: Long,
    val eventName: String
)