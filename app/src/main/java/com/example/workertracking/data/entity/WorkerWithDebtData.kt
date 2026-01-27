package com.example.workertracking.data.entity

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class WorkerWithDebtData(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val referenceId: Long?,
    val photoUris: String,
    val unpaidShiftsTotal: Double,
    val unpaidShiftsCount: Int,
    val unpaidEventsTotal: Double,
    val unpaidEventsCount: Int,
    val referenceShiftsTotal: Double,
    val referenceShiftsCount: Int,
    val referenceEventsTotal: Double,
    val referenceEventsCount: Int,
    val totalEarnings: Double,
    val referenceWorkerName: String?
) {
    fun toWorker(): Worker {
        val listType = object : TypeToken<List<String>>() {}.type
        val photoUrisList: List<String> = try {
            Gson().fromJson(photoUris, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return Worker(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
            referenceId = referenceId,
            photoUris = photoUrisList
        )
    }

    fun toWorkerWithDebt(): WorkerWithDebt {
        return WorkerWithDebt(
            worker = toWorker(),
            totalOwed = unpaidShiftsTotal + unpaidEventsTotal,
            unpaidShiftsCount = unpaidShiftsCount,
            unpaidEventsCount = unpaidEventsCount,
            totalReferenceOwed = referenceShiftsTotal + referenceEventsTotal,
            unpaidReferenceShiftsCount = referenceShiftsCount,
            unpaidReferenceEventsCount = referenceEventsCount
        )
    }
}
