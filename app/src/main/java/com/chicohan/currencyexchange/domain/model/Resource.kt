package com.chicohan.currencyexchange.domain.model

sealed class Resource<out T> {

    data class Success<out T>(val data: T?) : Resource<T>()
    data class Error<out T>(val message: String, val data: T?) : Resource<T>()
    data class Loading<out T>(val data: T?) : Resource<T>()
    data object Idle : Resource<Nothing>()
}
