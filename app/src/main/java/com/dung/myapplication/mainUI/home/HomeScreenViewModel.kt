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
import java.net.InetAddress
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor() : ViewModel() {

    val deviceList = mutableStateListOf<IpDevice>()
    val scanStatus = mutableStateOf("")
    private val TAG = "IPScan"

    fun startScan() {
        viewModelScope.launch {
            val subnet = "192.168.111" // ⚙️ chỉnh subnet nếu cần
            deviceList.clear()
            scanStatus.value = "Scanning subnet $subnet ..."
            Log.d(TAG, scanStatus.value)

            withContext(Dispatchers.IO) {
                for (i in 1..255) {
                    val host = "$subnet.$i"
                    try {
                        val address = InetAddress.getByName(host)
                        if (address.isReachable(100)) {
                            Log.d(TAG, "Found: $host - ${address.hostName}")
                            withContext(Dispatchers.Main) {
                                deviceList.add(
                                    IpDevice(
                                        name = address.hostName ?: "Unknown Device",
                                        ipAddress = host
                                    )
                                )
                            }
                        }
                    } catch (_: Exception) {}
                }
            }

            val message = "Scan finished, found ${deviceList.size} devices"
            scanStatus.value = message
            Log.d(TAG, message)
        }
    }
}
