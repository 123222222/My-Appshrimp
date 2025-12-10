package com.dung.myapplication.models

import kotlinx.serialization.Serializable

@Serializable
data class MotorStatus(
    val pin: Int,
    val state: Int
)

@Serializable
data class GpioStatusResponse(
    val success: Boolean,
    val status: Map<String, MotorStatus>,
    val auto_mode: Boolean,
    val gpio_available: Boolean
)

@Serializable
data class MotorSchedule(
    val enabled: Boolean = false,
    val start_time: String = "08:00",
    val end_time: String = "18:00",
    val days: List<String> = emptyList()
)

@Serializable
data class ScheduleRequest(
    val motor_id: String,
    val schedule: MotorSchedule
)

@Serializable
data class ScheduleResponse(
    val success: Boolean,
    val motor_id: String? = null,
    val schedule: MotorSchedule? = null,
    val message: String? = null
)

@Serializable
data class ManualControlRequest(
    val motor_id: String,
    val state: Int
)

@Serializable
data class ManualControlResponse(
    val success: Boolean,
    val motor_id: String? = null,
    val pin: Int? = null,
    val state: Int? = null,
    val message: String? = null
)

@Serializable
data class ModeResponse(
    val success: Boolean,
    val mode: String? = null,
    val auto_mode_active: Boolean = false,
    val message: String? = null
)

@Serializable
data class AutoModeResponse(
    val success: Boolean,
    val message: String? = null,
    val schedules: Map<String, MotorSchedule>? = null
)

@Serializable
data class TimezoneDebugInfo(
    val server_time: String,
    val server_time_24h: String,
    val server_day: String,
    val timezone_env: String,
    val expected_timezone: String,
    val timezone_is_correct: Boolean
)

@Serializable
data class TimezoneDebugResponse(
    val success: Boolean,
    val debug: TimezoneDebugInfo? = null,
    val message: String? = null
)

enum class ControlMode {
    MANUAL,
    AUTO
}

data class MotorInfo(
    val id: String,
    val name: String,
    val pin: Int,
    val state: Boolean = false
)

