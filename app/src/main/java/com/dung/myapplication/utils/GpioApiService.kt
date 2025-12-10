package com.dung.myapplication.utils

import com.dung.myapplication.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GpioApiService(private val baseUrl: String, private val idToken: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // GPIO Status
    suspend fun getGpioStatus(): GpioStatusResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/gpio/status")
            .header("Authorization", idToken)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        json.decodeFromString<GpioStatusResponse>(responseBody)
    }

    // Manual Control
    suspend fun manualControlMotor(motorId: String, state: Int): ManualControlResponse = withContext(Dispatchers.IO) {
        val requestData = ManualControlRequest(motorId, state)
        val jsonBody = json.encodeToString(requestData)
        val body = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/api/gpio/manual/control")
            .header("Authorization", idToken)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        json.decodeFromString<ManualControlResponse>(responseBody)
    }

    // Auto Mode - Set Schedule
    suspend fun setSchedule(motorId: String, schedule: MotorSchedule): ScheduleResponse = withContext(Dispatchers.IO) {
        val requestData = ScheduleRequest(motorId, schedule)
        val jsonBody = json.encodeToString(requestData)
        val body = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/api/gpio/auto/schedule")
            .header("Authorization", idToken)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        json.decodeFromString<ScheduleResponse>(responseBody)
    }

    // Auto Mode - Get Schedule
    suspend fun getSchedule(motorId: String): ScheduleResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/gpio/auto/schedule/$motorId")
            .header("Authorization", idToken)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        json.decodeFromString<ScheduleResponse>(responseBody)
    }

    // Auto Mode - Start
    suspend fun startAutoMode(): AutoModeResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/gpio/auto/start")
            .header("Authorization", idToken)
            .post("".toRequestBody())
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        json.decodeFromString<AutoModeResponse>(responseBody)
    }

    // Auto Mode - Stop
    suspend fun stopAutoMode(): AutoModeResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/gpio/auto/stop")
            .header("Authorization", idToken)
            .post("".toRequestBody())
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        json.decodeFromString<AutoModeResponse>(responseBody)
    }

    // Get Current Mode
    suspend fun getMode(): ModeResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/gpio/mode")
            .header("Authorization", idToken)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        json.decodeFromString<ModeResponse>(responseBody)
    }

    // Get Timezone Info (for debugging/info display)
    suspend fun getTimezoneInfo(): TimezoneDebugResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/gpio/debug/timezone")
            .get()  // No auth required for debug endpoint
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        json.decodeFromString<TimezoneDebugResponse>(responseBody)
    }
}

