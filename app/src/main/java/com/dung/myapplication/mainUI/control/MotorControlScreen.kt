package com.dung.myapplication.mainUI.control

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dung.myapplication.models.ControlMode
import com.dung.myapplication.models.MotorSchedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorControlScreen(
    baseUrl: String,
    onNavigateBack: () -> Unit,
    viewModel: MotorControlViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
    var raspDeviceId by remember { mutableStateOf(prefs.getString("rasp_device_id", null)) }
    var raspIp by remember { mutableStateOf(prefs.getString("rasp_ip", null)) }
    val uiState by viewModel.uiState.collectAsState()

    // Check device binding
    LaunchedEffect(Unit) {
        if (raspDeviceId == null || raspIp == null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val freshToken = if (user != null) {
                        val result = com.google.android.gms.tasks.Tasks.await(user.getIdToken(true))
                        result.token
                    } else null

                    if (freshToken != null) {
                        val client = okhttp3.OkHttpClient()
                        val request = okhttp3.Request.Builder()
                            .url("${com.dung.myapplication.models.Config.BACKEND_URL}/api/devices/my-device")
                            .get()
                            .addHeader("Authorization", freshToken)
                            .build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val jsonResponse = org.json.JSONObject(response.body?.string() ?: "{}")
                            val bound = jsonResponse.optBoolean("bound", false)
                            if (bound) {
                                val deviceId = jsonResponse.optString("device_id")
                                val deviceIp = jsonResponse.optString("device_ip", null)
                                if (deviceId != null && deviceIp != null) {
                                    prefs.edit()
                                        .putString("rasp_ip", deviceIp)
                                        .putString("rasp_device_id", deviceId)
                                        .apply()
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        raspIp = deviceIp
                                        raspDeviceId = deviceId
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MotorControl", "Error checking device", e)
                }
            }
        }
    }

    LaunchedEffect(baseUrl, raspDeviceId) {
        if (raspDeviceId != null) {
            viewModel.initialize(baseUrl)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Motor Control") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStatus() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Device binding warning
                if (raspDeviceId == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Column {
                                Text(
                                    text = "Chưa kết nối Raspberry Pi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Vui lòng kết nối thiết bị Raspberry Pi trước khi điều khiển động cơ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // Error display
                uiState.error?.let { error ->
                    ErrorBanner(
                        message = error,
                        onDismiss = { viewModel.clearError() }
                    )
                }

                // GPIO Status Card
                StatusCard(
                    gpioAvailable = uiState.gpioAvailable,
                    autoModeRunning = uiState.autoModeRunning
                )

                // Mode Selector
                ModeSelector(
                    currentMode = uiState.currentMode,
                    onModeChanged = { viewModel.switchMode(it) },
                    enabled = !uiState.isLoading && raspDeviceId != null
                )

                // Content based on mode
                AnimatedContent(
                    targetState = uiState.currentMode,
                    label = "mode_content"
                ) { mode ->
                    when (mode) {
                        ControlMode.MANUAL -> ManualControlSection(
                            motors = uiState.motors,
                            onToggleMotor = { viewModel.toggleMotor(it) },
                            enabled = !uiState.isLoading && !uiState.autoModeRunning && raspDeviceId != null
                        )
                        ControlMode.AUTO -> AutoControlSection(
                            motors = uiState.motors,
                            schedules = uiState.schedules,
                            autoModeRunning = uiState.autoModeRunning,
                            onUpdateSchedule = { motorId, schedule ->
                                viewModel.updateSchedule(motorId, schedule)
                            },
                            onStartAutoMode = { viewModel.startAutoMode() },
                            onStopAutoMode = { viewModel.stopAutoMode() },
                            enabled = !uiState.isLoading && raspDeviceId != null
                        )
                    }
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Dismiss")
            }
        }
    }
}

@Composable
fun StatusCard(gpioAvailable: Boolean, autoModeRunning: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "System Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            StatusRow("GPIO Available", gpioAvailable)
            StatusRow("Auto Mode Running", autoModeRunning)
        }
    }
}

@Composable
fun StatusRow(label: String, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isActive) Color.Green else Color.Gray)
        )
    }
}

@Composable
fun ModeSelector(
    currentMode: ControlMode,
    onModeChanged: (ControlMode) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Control Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeButton(
                    text = "Manual",
                    icon = Icons.Default.Build,
                    isSelected = currentMode == ControlMode.MANUAL,
                    onClick = { onModeChanged(ControlMode.MANUAL) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )

                ModeButton(
                    text = "Auto",
                    icon = Icons.Default.Schedule,
                    isSelected = currentMode == ControlMode.AUTO,
                    onClick = { onModeChanged(ControlMode.AUTO) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ModeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isSelected,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun ManualControlSection(
    motors: List<com.dung.myapplication.models.MotorInfo>,
    onToggleMotor: (String) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Manual Control",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Control each motor independently",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            motors.forEach { motor ->
                MotorControlCard(
                    motor = motor,
                    onToggle = { onToggleMotor(motor.id) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun MotorControlCard(
    motor: com.dung.myapplication.models.MotorInfo,
    onToggle: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                    text = motor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GPIO Pin: ${motor.pin}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = motor.state,
                onCheckedChange = { onToggle() },
                enabled = enabled
            )
        }
    }
}

@Composable
fun AutoControlSection(
    motors: List<com.dung.myapplication.models.MotorInfo>,
    schedules: Map<String, MotorSchedule>,
    autoModeRunning: Boolean,
    onUpdateSchedule: (String, MotorSchedule) -> Unit,
    onStartAutoMode: () -> Unit,
    onStopAutoMode: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Auto Mode",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Set schedules for automatic control",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            // Auto mode controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartAutoMode,
                    enabled = enabled && !autoModeRunning,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start")
                }

                Button(
                    onClick = onStopAutoMode,
                    enabled = enabled && autoModeRunning,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Stop")
                }
            }

            HorizontalDivider()

            // Schedule for each motor
            motors.forEach { motor ->
                MotorScheduleCard(
                    motor = motor,
                    schedule = schedules[motor.id] ?: MotorSchedule(),
                    onUpdateSchedule = { schedule ->
                        onUpdateSchedule(motor.id, schedule)
                    },
                    enabled = enabled && !autoModeRunning
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorScheduleCard(
    motor: com.dung.myapplication.models.MotorInfo,
    schedule: MotorSchedule,
    onUpdateSchedule: (MotorSchedule) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var localSchedule by remember(schedule) { mutableStateOf(schedule) }
    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = motor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GPIO Pin: ${motor.pin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Enable/Disable
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Schedule")
                        Switch(
                            checked = localSchedule.enabled,
                            onCheckedChange = {
                                localSchedule = localSchedule.copy(enabled = it)
                            },
                            enabled = enabled
                        )
                    }

                    if (localSchedule.enabled) {
                        // Start Time
                        OutlinedButton(
                            onClick = { showTimePickerStart = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = enabled
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Time: ${localSchedule.start_time}")
                        }

                        // End Time
                        OutlinedButton(
                            onClick = { showTimePickerEnd = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = enabled
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("End Time: ${localSchedule.end_time}")
                        }

                        // Days of week
                        Text(
                            text = "Active Days (Chọn ít nhất 1 ngày)",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (localSchedule.days.isEmpty())
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        )

                        DaySelector(
                            selectedDays = localSchedule.days,
                            onDaysChanged = { days ->
                                localSchedule = localSchedule.copy(days = days)
                            },
                            enabled = enabled
                        )

                        // Validation warning
                        if (localSchedule.days.isEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Vui lòng chọn ít nhất 1 ngày hoạt động",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        // Info when schedule is disabled
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Bật 'Enable Schedule' để cấu hình lịch tự động",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Save button
                    Button(
                        onClick = { onUpdateSchedule(localSchedule) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled && (!localSchedule.enabled || localSchedule.days.isNotEmpty())
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Schedule")
                    }
                }
            }
        }
    }

    // Time pickers
    if (showTimePickerStart) {
        TimePickerDialog(
            initialTime = localSchedule.start_time,
            onDismiss = { showTimePickerStart = false },
            onTimeSelected = { time ->
                localSchedule = localSchedule.copy(start_time = time)
                showTimePickerStart = false
            }
        )
    }

    if (showTimePickerEnd) {
        TimePickerDialog(
            initialTime = localSchedule.end_time,
            onDismiss = { showTimePickerEnd = false },
            onTimeSelected = { time ->
                localSchedule = localSchedule.copy(end_time = time)
                showTimePickerEnd = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    // Parse initial time (HH:mm format)
    val timeParts = initialTime.split(":")
    val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
    val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(16.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hour = timePickerState.hour.toString().padStart(2, '0')
                    val minute = timePickerState.minute.toString().padStart(2, '0')
                    onTimeSelected("$hour:$minute")
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DaySelector(
    selectedDays: List<String>,
    onDaysChanged: (List<String>) -> Unit,
    enabled: Boolean
) {
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        daysOfWeek.chunked(2).forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowDays.forEach { day ->
                    val isSelected = selectedDays.contains(day)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newDays = if (isSelected) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                            onDaysChanged(newDays)
                        },
                        label = { Text(day.take(3)) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number
                if (rowDays.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

