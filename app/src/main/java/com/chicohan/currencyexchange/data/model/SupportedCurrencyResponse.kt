package com.chicohan.currencyexchange.data.model

import com.google.gson.annotations.SerializedName

data class SupportedCurrencyResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("currencies") val currencies: Map<String, String>?
)
