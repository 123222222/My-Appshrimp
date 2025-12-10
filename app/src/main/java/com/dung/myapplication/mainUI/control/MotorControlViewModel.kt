package com.dung.myapplication.mainUI.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dung.myapplication.models.*
import com.dung.myapplication.utils.GpioApiService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MotorControlViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MotorControlUiState())
    val uiState: StateFlow<MotorControlUiState> = _uiState.asStateFlow()

    private var apiService: GpioApiService? = null

    private val motors = listOf(
        MotorInfo(id = "motor1", name = "Motor 1", pin = 17),
        MotorInfo(id = "motor2", name = "Motor 2", pin = 27),
        MotorInfo(id = "motor3", name = "Motor 3", pin = 22)
    )

    private suspend fun getAuthHeaders(): Map<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                val context = application.applicationContext
                val currentUser = com.dung.myapplication.utils.UserSession.getCurrentUser(context)
                val loginType = com.dung.myapplication.utils.UserSession.getLoginType(context)

                if (loginType == com.dung.myapplication.utils.UserSession.LoginType.PHONE && currentUser?.phone != null) {
                    // Phone authentication
                    mapOf("X-Phone-Auth" to currentUser.phone)
                } else {
                    // Google authentication
                    val token = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.let { task ->
                        Tasks.await(task)?.token
                    }
                    if (token != null) {
                        mapOf("Authorization" to token)
                    } else {
                        emptyMap()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
        }
    }

    fun initialize(baseUrl: String) {
        viewModelScope.launch {
            try {
                val authHeaders = getAuthHeaders()
                if (authHeaders.isNotEmpty()) {
                    apiService = GpioApiService(baseUrl, authHeaders)
                    _uiState.value = _uiState.value.copy(motors = motors)
                    refreshStatus()  // This will now properly sync autoModeRunning state
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Authentication required"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Initialization failed: ${e.message}"
                )
            }
        }
    }

    fun refreshStatus() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val statusResponse = apiService?.getGpioStatus()
                val modeResponse = apiService?.getMode()

                if (statusResponse != null && modeResponse != null) {
                    val updatedMotors = motors.map { motor ->
                        val motorStatus = statusResponse.status[motor.id]
                        motor.copy(state = motorStatus?.state == 1)
                    }

                    _uiState.value = _uiState.value.copy(
                        motors = updatedMotors,
                        currentMode = if (modeResponse.auto_mode_active) ControlMode.AUTO else ControlMode.MANUAL,
                        autoModeRunning = modeResponse.auto_mode_active,  // FIX: Sync auto mode state
                        isLoading = false,
                        gpioAvailable = statusResponse.gpio_available
                    )

                    // Load schedules if in auto mode
                    if (modeResponse.auto_mode_active) {
                        loadAllSchedules()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to refresh: ${e.message}"
                )
            }
        }
    }

    fun switchMode(mode: ControlMode) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                when (mode) {
                    ControlMode.AUTO -> {
                        // When switching to auto, load schedules first
                        loadAllSchedules()
                    }
                    ControlMode.MANUAL -> {
                        // When switching to manual, stop auto mode if running
                        if (_uiState.value.autoModeRunning) {
                            apiService?.stopAutoMode()
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    currentMode = mode,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to switch mode: ${e.message}"
                )
            }
        }
    }

    // Manual Control Functions
    fun toggleMotor(motorId: String) {
        viewModelScope.launch {
            try {
                val motor = _uiState.value.motors.find { it.id == motorId } ?: return@launch
                val newState = if (motor.state) 0 else 1

                val response = apiService?.manualControlMotor(motorId, newState)

                if (response?.success == true) {
                    val updatedMotors = _uiState.value.motors.map {
                        if (it.id == motorId) it.copy(state = newState == 1) else it
                    }
                    _uiState.value = _uiState.value.copy(motors = updatedMotors)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response?.message ?: "Failed to control motor"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle motor: ${e.message}"
                )
            }
        }
    }

    // Auto Mode Functions
    private fun loadAllSchedules() {
        viewModelScope.launch {
            try {
                val schedules = mutableMapOf<String, MotorSchedule>()

                for (motor in motors) {
                    val response = apiService?.getSchedule(motor.id)
                    if (response?.success == true && response.schedule != null) {
                        schedules[motor.id] = response.schedule
                    }
                }

                _uiState.value = _uiState.value.copy(schedules = schedules)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load schedules: ${e.message}"
                )
            }
        }
    }

    fun updateSchedule(motorId: String, schedule: MotorSchedule) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val response = apiService?.setSchedule(motorId, schedule)

                if (response?.success == true) {
                    val updatedSchedules = _uiState.value.schedules.toMutableMap()
                    updatedSchedules[motorId] = schedule
                    _uiState.value = _uiState.value.copy(
                        schedules = updatedSchedules,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response?.message ?: "Failed to update schedule"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update schedule: ${e.message}"
                )
            }
        }
    }

    fun startAutoMode() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val response = apiService?.startAutoMode()

                if (response?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        autoModeRunning = true,
                        isLoading = false
                    )
                    refreshStatus()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response?.message ?: "Failed to start auto mode"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to start auto mode: ${e.message}"
                )
            }
        }
    }

    fun stopAutoMode() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val response = apiService?.stopAutoMode()

                if (response?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        autoModeRunning = false,
                        isLoading = false
                    )
                    refreshStatus()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response?.message ?: "Failed to stop auto mode"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to stop auto mode: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class MotorControlUiState(
    val motors: List<MotorInfo> = emptyList(),
    val currentMode: ControlMode = ControlMode.MANUAL,
    val autoModeRunning: Boolean = false,
    val schedules: Map<String, MotorSchedule> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val gpioAvailable: Boolean = false
)


