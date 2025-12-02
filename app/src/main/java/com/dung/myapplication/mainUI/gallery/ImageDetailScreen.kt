package com.dung.myapplication.mainUI.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dung.myapplication.models.ShrimpDetection
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    imageId: String,
    onBackClick: () -> Unit = {}
) {
    val viewModel: GalleryViewModel = hiltViewModel()
    // Make image reactive - recompute when imageList changes
    val image by remember(viewModel.imageList.size) {
        derivedStateOf {
            viewModel.imageList.find { it.id == imageId }
        }
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Debug logging
    LaunchedEffect(imageId, viewModel.imageList.size) {
        android.util.Log.d("ImageDetailScreen", "Looking for imageId: $imageId")
        android.util.Log.d("ImageDetailScreen", "ImageList size: ${viewModel.imageList.size}")
        viewModel.imageList.forEachIndexed { index, img ->
            android.util.Log.d("ImageDetailScreen", "  [$index] id: ${img.id}")
        }
        if (image != null) {
            android.util.Log.d("ImageDetailScreen", "Found image: ${image!!.cloudinaryUrl}")
        } else {
            android.util.Log.e("ImageDetailScreen", "Image NOT found!")
        }
    }

    // Load images if list is empty
    LaunchedEffect(Unit) {
        if (viewModel.imageList.isEmpty()) {
            android.util.Log.d("ImageDetailScreen", "ImageList is empty, loading...")
            viewModel.loadImages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết ảnh") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Share functionality */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            viewModel.isLoading.value -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            image == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Không tìm thấy ảnh")
                        if (viewModel.errorMessage.value.isNotEmpty()) {
                            Text(
                                text = viewModel.errorMessage.value,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Button(onClick = { viewModel.loadImages() }) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            else -> {
                // Create local non-null reference for smart cast
                val currentImage = image ?: return@Scaffold

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Image
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            AsyncImage(
                                model = currentImage.cloudinaryUrl,
                                contentDescription = "Shrimp Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 3f),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    // Info
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Thông tin",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoRow("Số tôm phát hiện", "${currentImage.detections.size}")
                                InfoRow("Nguồn", currentImage.capturedFrom)
                                InfoRow(
                                    "Thời gian",
                                    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                        .format(Date(currentImage.timestamp))
                                )
                            }
                        }
                    }

                    // Detections
                    item {
                        Text(
                            text = "Chi tiết nhận diện",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(currentImage.detections) { detection ->
                        DetectionCard(detection)
                    }
                }
            }
        }
    }

    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa ảnh này?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteImage(imageId)
                        showDeleteDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DetectionCard(detection: ShrimpDetection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = detection.className,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Vị trí: (${detection.bbox.x.toInt()}, ${detection.bbox.y.toInt()})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Kích thước: ${detection.bbox.width.toInt()} x ${detection.bbox.height.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Chiều dài: ${"%.1f".format(detection.length)} cm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Khối lượng: ${"%.1f".format(detection.weight)} g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "${(detection.confidence * 100).toInt()}%",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
