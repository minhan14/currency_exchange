package com.chicohan.currencyexchange.data.model

import com.google.gson.annotations.SerializedName

data class ApiErrorResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("info") val errorMessage: String
)
