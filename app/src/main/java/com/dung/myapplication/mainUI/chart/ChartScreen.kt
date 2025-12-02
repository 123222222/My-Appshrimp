package com.dung.myapplication.mainUI.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dung.myapplication.mainUI.common.MyTopBar
import com.dung.myapplication.mainUI.common.MyBottomBar
import com.dung.myapplication.models.ShrimpImage
import com.dung.myapplication.models.ShrimpDetection
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onChartClick: () -> Unit = {},
    onControlClick: () -> Unit = {},
    viewModel: ChartViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            MyTopBar(
                title = "Biểu đồ thống kê",
                onBack = onBackClick
            )
        },
        bottomBar = {
            MyBottomBar(
                onHomeClick = onHomeClick,
                onGalleryClick = onGalleryClick,
                onProfileClick = onProfileClick,
                onLogoutClick = onLogoutClick,
                onChartClick = {}, // đang ở Chart
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
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chưa có dữ liệu",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    ChartContent(images = viewModel.imageList)
                }
            }
        }
    }
}

@Composable
fun ChartContent(images: List<ShrimpImage>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Thống kê các ảnh đã phát hiện",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        images.forEachIndexed { index, image ->
            ImageChartCard(
                imageIndex = index + 1,
                image = image
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ImageChartCard(imageIndex: Int, image: ShrimpImage) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val date = Date(image.timestamp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Ảnh #$imageIndex",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = dateFormat.format(date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Số tôm: ${image.detections.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Biểu đồ cho tất cả tôm trong 1 ảnh
            if (image.detections.isEmpty()) {
                Text(
                    text = "Không phát hiện tôm nào",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                CombinedShrimpBarChart(detections = image.detections)
            }
        }
    }
}

@Composable
fun CombinedShrimpBarChart(detections: List<ShrimpDetection>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
    ) {
        Text(
            text = "Thống kê ${detections.size} con tôm",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Canvas vẽ biểu đồ tổng hợp
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val numShrimps = detections.size

            // Tính toán layout
            val groupSpacing = 40f
            val barSpacing = 8f
            val barWidth = (canvasWidth - 60f - (numShrimps - 1) * groupSpacing) / (numShrimps * 3)

            // Tìm giá trị max để scale
            val allValues = detections.flatMap {
                listOf(it.confidence * 100, it.weight, it.length)
            }
            val maxValue = allValues.maxOrNull()?.coerceAtLeast(1f) ?: 1f

            // Vẽ trục Y
            drawLine(
                color = Color.Gray,
                start = Offset(40f, 20f),
                end = Offset(40f, canvasHeight - 50f),
                strokeWidth = 2f
            )

            // Vẽ trục X
            drawLine(
                color = Color.Gray,
                start = Offset(40f, canvasHeight - 50f),
                end = Offset(canvasWidth - 20f, canvasHeight - 50f),
                strokeWidth = 2f
            )

            // Vẽ các cột cho từng con tôm
            detections.forEachIndexed { shrimpIndex, detection ->
                val groupX = 50f + shrimpIndex * ((barWidth * 3) + groupSpacing + barSpacing * 2)

                // Dữ liệu 3 cột: Confidence, Weight, Length
                val bars = listOf(
                    Triple(detection.confidence * 100, Color(0xFF4CAF50), "C"),
                    Triple(detection.weight, Color(0xFF2196F3), "W"),
                    Triple(detection.length, Color(0xFFFF9800), "L")
                )

                bars.forEachIndexed { barIndex, (value, color, label) ->
                    val barHeight = ((value / maxValue) * (canvasHeight - 90f)).coerceAtLeast(2f)
                    val x = groupX + barIndex * (barWidth + barSpacing)
                    val y = canvasHeight - 50f - barHeight

                    // Vẽ cột
                    drawRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend chung
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(
                color = Color(0xFF4CAF50),
                label = "Tỉ lệ nhận diện (%)",
                value = ""
            )
            LegendItem(
                color = Color(0xFF2196F3),
                label = "Khối lượng (g)",
                value = ""
            )
            LegendItem(
                color = Color(0xFFFF9800),
                label = "Chiều dài (cm)",
                value = ""
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        // Bảng chi tiết từng con
        Text(
            text = "Chi tiết:",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        detections.forEachIndexed { index, detection ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tôm ${index + 1}:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(detection.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${"%.1f".format(detection.weight)}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${"%.1f".format(detection.length)}cm",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ShrimpBarChart(
    shrimpNumber: Int,
    confidence: Float,
    weight: Float,
    length: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
    ) {
        Text(
            text = "Tôm #$shrimpNumber",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Canvas vẽ biểu đồ
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = canvasWidth / 5
            val spacing = barWidth / 4
            val maxValue = maxOf(confidence * 100, weight, length).coerceAtLeast(1f)

            // Vẽ trục Y
            drawLine(
                color = Color.Gray,
                start = Offset(30f, 0f),
                end = Offset(30f, canvasHeight - 40f),
                strokeWidth = 2f
            )

            // Vẽ trục X
            drawLine(
                color = Color.Gray,
                start = Offset(30f, canvasHeight - 40f),
                end = Offset(canvasWidth, canvasHeight - 40f),
                strokeWidth = 2f
            )

            // Vẽ 3 cột
            val bars = listOf(
                Triple(confidence * 100, Color(0xFF4CAF50), "Tỉ lệ (%)"),
                Triple(weight, Color(0xFF2196F3), "Khối lượng (g)"),
                Triple(length, Color(0xFFFF9800), "Chiều dài (cm)")
            )

            bars.forEachIndexed { index, (value, color, _) ->
                val barHeight = (value / maxValue) * (canvasHeight - 60f)
                val x = 50f + (index * (barWidth + spacing))
                val y = canvasHeight - 40f - barHeight

                // Vẽ cột
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend và giá trị
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(
                color = Color(0xFF4CAF50),
                label = "Tỉ lệ",
                value = "${(confidence * 100).toInt()}%"
            )
            LegendItem(
                color = Color(0xFF2196F3),
                label = "Khối lượng",
                value = "${"%.1f".format(weight)}g"
            )
            LegendItem(
                color = Color(0xFFFF9800),
                label = "Chiều dài",
                value = "${"%.1f".format(length)}cm"
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
