package com.example.demo_aidl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object UiState {
    private val _lastCommand = MutableStateFlow("Esperando...")
    val lastCommand: StateFlow<String> = _lastCommand

    fun updateCommand(command: String) {
        _lastCommand.value = command
    }
}