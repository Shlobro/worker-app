package com.example.workertracking.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import coil.compose.AsyncImage
import com.example.workertracking.R
import com.example.workertracking.ui.viewmodel.WorkerPhotoGalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerPhotoGalleryScreen(
    workerId: Long,
    workerName: String,
    onNavigateUp: () -> Unit,
    viewModel: WorkerPhotoGalleryViewModel = viewModel(
        factory = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer {
                WorkerPhotoGalleryViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application)
            }
        }
    )
) {
    val context = LocalContext.current
    val worker by viewModel.worker.collectAsState()
    val photos = worker?.photoUris ?: emptyList()
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPhoto(workerId, it.toString()) }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.photoUri?.let { uri ->
                viewModel.addPhoto(workerId, uri.toString())
            }
        }
    }
    
    LaunchedEffect(workerId) {
        viewModel.loadWorker(workerId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.worker_photos_title, workerName)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val photoUri = viewModel.createImageFileUri(context)
                            cameraLauncher.launch(photoUri)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.take_photo))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { photoPickerLauncher.launch("image/*") }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_photo))
            }
        }
    ) { paddingValues ->
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.no_photos),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { photoPickerLauncher.launch("image/*") }) {
                        Text(stringResource(R.string.add_first_photo))
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = paddingValues,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(photos) { photoUri ->
                    PhotoItem(
                        photoUri = photoUri,
                        onDelete = { viewModel.removePhoto(workerId, photoUri) },
                        onShare = { 
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, Uri.parse(photoUri))
                                type = "image/*"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_photo)))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoItem(
    photoUri: String,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photoUri,
                contentDescription = stringResource(R.string.worker_photo),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = stringResource(R.string.share_photo),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_photo),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_photo)) },
            text = { Text(stringResource(R.string.delete_photo_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}