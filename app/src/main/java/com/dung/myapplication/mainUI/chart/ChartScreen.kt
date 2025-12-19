package com.dung.myapplication.mainUI.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dung.myapplication.mainUI.common.MyTopBar
import com.dung.myapplication.mainUI.common.MyBottomBar
import com.dung.myapplication.models.ShrimpImage
import com.dung.myapplication.models.ShrimpDetection
import java.text.SimpleDateFormat
import java.util.*

// Data class để gắn timestamp vào detection
data class DetectionWithTime(
    val detection: ShrimpDetection,
    val timestamp: Long,
    val imageId: String
)

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
    // Gom tất cả detections từ tất cả ảnh và gắn timestamp
    val allDetectionsWithTime = images.flatMap { image ->
        image.detections.map { detection ->
            DetectionWithTime(
                detection = detection,
                timestamp = image.timestamp,
                imageId = image.id
            )
        }
    }.sortedBy { it.timestamp } // Sắp xếp theo thời gian

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Thống kê tất cả tôm đã phát hiện",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Tổng số: ${allDetectionsWithTime.size} con tôm từ ${images.size} ảnh",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (allDetectionsWithTime.isEmpty()) {
            Text(
                text = "Không có dữ liệu để hiển thị",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        } else {
            UnifiedShrimpLineChart(detectionsWithTime = allDetectionsWithTime)
        }
    }
}

@Composable
fun UnifiedShrimpLineChart(detectionsWithTime: List<DetectionWithTime>) {
    val scrollState = rememberScrollState()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Biểu đồ thống kê ${detectionsWithTime.size} con tôm",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Legend chung
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(
                color = Color(0xFF4CAF50),
                label = "Độ tin cậy (%)",
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

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas vẽ biểu đồ đường có thể cuộn ngang
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .horizontalScroll(scrollState)
                .background(Color.White)
        ) {
            Canvas(
                modifier = Modifier
                    .width((100 + detectionsWithTime.size * 60).dp.coerceAtLeast(350.dp))
                    .height(400.dp)
                    .pointerInput(detectionsWithTime) {
                        detectTapGestures { offset ->
                            val numShrimps = detectionsWithTime.size
                            val canvasWidth = size.width.toFloat()

                            // Tính khoảng cách giữa các điểm
                            val spacingX = if (numShrimps > 1) {
                                ((canvasWidth - 70f) / (numShrimps - 1)).coerceAtLeast(60f)
                            } else {
                                canvasWidth - 70f
                            }

                            // Tìm điểm gần nhất theo trục X (đơn giản hơn)
                            var closestIndex = -1
                            var minXDistance = Float.MAX_VALUE

                            detectionsWithTime.forEachIndexed { index, _ ->
                                val x = 50f + index * spacingX
                                val xDistance = kotlin.math.abs(offset.x - x)

                                if (xDistance < minXDistance) {
                                    minXDistance = xDistance
                                    closestIndex = index
                                }
                            }

                            // Nếu click trong phạm vi hợp lý (60px theo trục X)
                            selectedIndex = if (closestIndex >= 0 && minXDistance < 60f) closestIndex else null
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val numShrimps = detectionsWithTime.size

                // Tìm giá trị max để scale
                val allValues = detectionsWithTime.flatMap {
                    listOf(it.detection.confidence * 100, it.detection.weight, it.detection.length)
                }
                val maxValue = allValues.maxOrNull()?.coerceAtLeast(1f) ?: 1f

                // Vẽ lưới nền
                val gridLines = 8
                for (i in 0..gridLines) {
                    val y = 40f + (i * (canvasHeight - 100f) / gridLines)
                    drawLine(
                        color = Color(0xFFE0E0E0),
                        start = Offset(50f, y),
                        end = Offset(canvasWidth - 20f, y),
                        strokeWidth = 1f
                    )
                }

                // Vẽ trục Y
                drawLine(
                    color = Color.Black,
                    start = Offset(50f, 40f),
                    end = Offset(50f, canvasHeight - 60f),
                    strokeWidth = 2f
                )

                // Vẽ trục X
                drawLine(
                    color = Color.Black,
                    start = Offset(50f, canvasHeight - 60f),
                    end = Offset(canvasWidth - 20f, canvasHeight - 60f),
                    strokeWidth = 2f
                )

                // Tính toán khoảng cách giữa các điểm (ít nhất 60px giữa mỗi tôm)
                val spacingX = if (numShrimps > 1) {
                    ((canvasWidth - 70f) / (numShrimps - 1)).coerceAtLeast(60f)
                } else {
                    canvasWidth - 70f
                }

                // Vẽ 3 đường: Confidence, Weight, Length
                val lineData = listOf(
                    Pair(detectionsWithTime.map { it.detection.confidence * 100 }, Color(0xFF4CAF50)),
                    Pair(detectionsWithTime.map { it.detection.weight }, Color(0xFF2196F3)),
                    Pair(detectionsWithTime.map { it.detection.length }, Color(0xFFFF9800))
                )

                lineData.forEach { (values, color) ->
                    // Vẽ đường nối
                    for (i in 0 until values.size - 1) {
                        val x1 = 50f + i * spacingX
                        val y1 = canvasHeight - 60f - ((values[i] / maxValue) * (canvasHeight - 100f))
                        val x2 = 50f + (i + 1) * spacingX
                        val y2 = canvasHeight - 60f - ((values[i + 1] / maxValue) * (canvasHeight - 100f))

                        drawLine(
                            color = color,
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 3f
                        )
                    }

                    // Vẽ các điểm
                    values.forEachIndexed { index, value ->
                        val x = 50f + index * spacingX
                        val y = canvasHeight - 60f - ((value / maxValue) * (canvasHeight - 100f))

                        // Vẽ điểm tròn
                        val isSelected = index == selectedIndex
                        drawCircle(
                            color = color,
                            radius = if (isSelected) 10f else 7f,
                            center = Offset(x, y)
                        )

                        // Vẽ viền trắng cho điểm
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 5f else 3f,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFE0E0E0))
        Spacer(modifier = Modifier.height(12.dp))

        // Hiển thị chi tiết khi click vào điểm
        if (selectedIndex != null && selectedIndex!! < detectionsWithTime.size) {
            val selected = detectionsWithTime[selectedIndex!!]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Chi tiết tôm #${selectedIndex!! + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Thời gian:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = dateFormat.format(Date(selected.timestamp)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Độ tin cậy:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "${(selected.detection.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Khối lượng:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "${"%.1f".format(selected.detection.weight)}g",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2196F3),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Chiều dài:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "${"%.1f".format(selected.detection.length)}cm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF9800),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { selectedIndex = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Đóng")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Text(
                text = "💡 Nhấn vào điểm trên biểu đồ để xem chi tiết",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Thống kê tổng hợp
        HorizontalDivider(color = Color(0xFFE0E0E0))
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Thống kê tổng hợp:",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val avgConfidence = detectionsWithTime.map { it.detection.confidence * 100 }.average()
        val avgWeight = detectionsWithTime.map { it.detection.weight.toDouble() }.average()
        val avgLength = detectionsWithTime.map { it.detection.length.toDouble() }.average()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "TB độ tin cậy",
                value = "${"%.1f".format(avgConfidence)}%",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "TB khối lượng",
                value = "${"%.1f".format(avgWeight)}g",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "TB chiều dài",
                value = "${"%.1f".format(avgLength)}cm",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFF5F5F5), shape = MaterialTheme.shapes.small)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = TextAlign.Center
        )
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