package com.example.workertracking.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workertracking.WorkerTrackingApplication
import com.example.workertracking.data.entity.Worker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class WorkerPhotoGalleryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val workerRepository = (application as WorkerTrackingApplication).container.workerRepository
    
    private val _worker = MutableStateFlow<Worker?>(null)
    val worker: StateFlow<Worker?> = _worker.asStateFlow()
    
    var photoUri: Uri? = null
        private set
    
    fun loadWorker(workerId: Long) {
        viewModelScope.launch {
            workerRepository.getWorkerByIdFlow(workerId).collect { worker: Worker? ->
                _worker.value = worker
            }
        }
    }
    
    fun addPhoto(workerId: Long, photoUri: String) {
        viewModelScope.launch {
            val currentWorker = _worker.value ?: return@launch
            val updatedPhotos = currentWorker.photoUris + photoUri
            val updatedWorker = currentWorker.copy(photoUris = updatedPhotos)
            workerRepository.updateWorker(updatedWorker)
        }
    }
    
    fun removePhoto(workerId: Long, photoUri: String) {
        viewModelScope.launch {
            val currentWorker = _worker.value ?: return@launch
            val updatedPhotos = currentWorker.photoUris - photoUri
            val updatedWorker = currentWorker.copy(photoUris = updatedPhotos)
            workerRepository.updateWorker(updatedWorker)
        }
    }
    
    fun createImageFileUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = File(context.getExternalFilesDir(null), "worker_photos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        return photoUri!!
    }
}