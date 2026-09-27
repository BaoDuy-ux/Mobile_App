package com.example.agriculture_management.platform.file

/**
 * Template file port. Recovery/backup dùng port này; UI picker nằm app/feature, không nằm data transaction.
 */
interface AppFileStore {
    fun write(relativePath: String, content: String)
    fun read(relativePath: String): String?
}

class InMemoryAppFileStore : AppFileStore {
    private val files = mutableMapOf<String, String>()

    override fun write(relativePath: String, content: String) {
        files[relativePath] = content
    }

    override fun read(relativePath: String): String? = files[relativePath]
}
