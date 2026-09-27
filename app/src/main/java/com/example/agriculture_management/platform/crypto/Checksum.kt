package com.example.agriculture_management.platform.crypto

/**
 * Template checksum/hash cho backup integrity. Không giấu secret nghiệp vụ.
 */
interface Checksum {
    fun sha256Hex(bytes: ByteArray): String
}

object DummyChecksum : Checksum {
    override fun sha256Hex(bytes: ByteArray): String =
        bytes.fold(0) { acc, b -> acc * 31 + b }.toUInt().toString(16)
}
