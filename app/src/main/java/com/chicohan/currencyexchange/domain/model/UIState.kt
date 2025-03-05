package com.chicohan.samplelistapp.domain.model


sealed class UIState<out T> {
    data object Idle : UIState<Nothing>()
    data object Loading : UIState<Nothing>()
    data class Success<out T>(val result: T) : UIState<T>()
    data class Error(val errorMessage: String) : UIState<Nothing>()
}

