package com.dung.myapplication.models

object Config {
    // ⚠️ UPDATE URL này khi Ngrok restart
    const val BACKEND_URL = "https://unstrengthening-elizabeth-nondispensible.ngrok-free.dev"

    // Camera stream endpoint (sử dụng Ngrok URL cho remote access)
    const val STREAM_URL = "$BACKEND_URL/blynk_feed"

    // UDP Discovery port cho lần đầu bind device (cần cùng WiFi)
    const val UDP_DISCOVERY_PORT = 50000
    const val UDP_DISCOVERY_MESSAGE = "DISCOVER_RASP"
}