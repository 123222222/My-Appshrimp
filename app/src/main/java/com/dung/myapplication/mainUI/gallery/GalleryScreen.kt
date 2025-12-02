package com.dung.myapplication.mainUI.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dung.myapplication.mainUI.common.MyBottomBar
import com.dung.myapplication.mainUI.common.MyTopBar
import com.dung.myapplication.models.ShrimpImage
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = hiltViewModel(),
    onHomeClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onChartClick: () -> Unit = {},
    onControlClick: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadImages()
    }
    Scaffold(
        topBar = {
            MyTopBar(
                title = "Thư viện ảnh",
                onBack = onHomeClick
            )
        },
        bottomBar = {
            MyBottomBar(
                onHomeClick = onHomeClick,
                onGalleryClick = onGalleryClick,
                onProfileClick = onProfileClick,
                onLogoutClick = onLogoutClick,
                onChartClick = onChartClick,
                onControlClick = onControlClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.loadImages() }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                viewModel.isLoading.value -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                viewModel.errorMessage.value.isNotEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.errorMessage.value,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadImages() }) {
                            Text("Thử lại")
                        }
                    }
                }
                viewModel.imageList.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chưa có ảnh nào",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.imageList) { image ->
                            ImageGridItem(
                                image = image,
                                onClick = { onImageClick(image.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageGridItem(
    image: ShrimpImage,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = image.cloudinaryUrl,
                contentDescription = "Shrimp Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "${image.detections.size} tôm phát hiện",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateFormat.format(Date(image.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
