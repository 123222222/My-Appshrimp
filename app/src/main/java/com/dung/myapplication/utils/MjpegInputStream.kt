package com.dung.myapplication.utils

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream

class MjpegInputStream(inputStream: InputStream) : DataInputStream(BufferedInputStream(inputStream, 1024)) {
    private val SOI_MARKER = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    private val EOF_MARKER = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

    fun readMjpegFrame(): ByteArray? {
        val header = readLine() ?: return null
        var contentLength = -1

        // Đọc headers
        var line: String?
        while (readLine().also { line = it } != null && line!!.isNotEmpty()) {
            if (line!!.startsWith("Content-Length:", ignoreCase = true)) {
                contentLength = line!!.substring(15).trim().toIntOrNull() ?: -1
            }
        }

        if (contentLength < 1) return null

        val frame = ByteArray(contentLength)
        readFully(frame)
        return frame
    }
}
