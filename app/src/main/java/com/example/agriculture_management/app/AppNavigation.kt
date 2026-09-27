package com.example.agriculture_management.app

/**
 * Template navigation. Feature không tự điều hướng chéo bằng Intent sâu trong ViewModel.
 */
sealed interface Destination {
    data object Farm : Destination
    data object Cycle : Destination
    data object Activity : Destination
    data object Cash : Destination
    data object Stock : Destination
    data object Task : Destination
    data object Report : Destination
    data object Backup : Destination
    data object Settings : Destination
}

fun interface AppNavigator {
    fun go(to: Destination)
}
