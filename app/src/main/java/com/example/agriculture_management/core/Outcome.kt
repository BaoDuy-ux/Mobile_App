package com.example.agriculture_management.core

/**
 * Kết quả thống nhất cho command/query — không dùng exception để điều khiển luồng nghiệp vụ.
 */
sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Rejected(val reasons: List<String>) : Outcome<Nothing>
}

fun <T> Outcome<T>.getOrNull(): T? = when (this) {
    is Outcome.Ok -> value
    is Outcome.Rejected -> null
}
