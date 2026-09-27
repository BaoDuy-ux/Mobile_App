package com.example.agriculture_management.platform.share

/**
 * Template share. Chỉ mở hệ thống chia sẻ; không format tiền ở đây.
 */
fun interface ShareGateway {
    fun sharePlainText(title: String, body: String)
}
