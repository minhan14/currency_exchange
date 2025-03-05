package com.chicohan.currencyexchange.data.model

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("quotes") val rates: Map<String, Double>
)