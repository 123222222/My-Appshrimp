package com.dung.myapplication.mainUI.home

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dung.myapplication.models.IpDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor() : ViewModel() {

    val deviceList = mutableStateListOf<IpDevice>()
    val scanStatus = mutableStateOf("")
    private val TAG = "IPScan"

    // ✅ URL ngrok của bạn
    private val NGROK_URL = "unstrengthening-elizabeth-nondispensible.ngrok-free.dev"

    // OkHttp client dùng cho probe HTTP
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(2000, TimeUnit.MILLISECONDS)
        .callTimeout(3000, TimeUnit.MILLISECONDS)
        .readTimeout(2000, TimeUnit.MILLISECONDS)
        .build()


    init {
        Log.d("debug","hello ne bé")
    }

    // ➕ Hàm add ngrok device
    fun addNgrokDevice() {
        viewModelScope.launch {
            deviceList.clear()
            scanStatus.value = "Connecting to camera server..."

            withContext(Dispatchers.IO) {
                val url = "https://$NGROK_URL/blynk_feed"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                try {
                    httpClient.newCall(request).execute().use { resp ->
                        if (resp.isSuccessful || resp.code == 401) {
                            withContext(Dispatchers.Main) {
                                deviceList.add(
                                    IpDevice(
                                        name = "Camera Server (Internet)",
                                        ipAddress = NGROK_URL
                                    )
                                )
                                scanStatus.value = "Camera server ready"
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                scanStatus.value = "Server not responding (${resp.code})"
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        scanStatus.value = "Cannot connect: ${e.message}"
                    }
                }
            }
        }
    }
}
