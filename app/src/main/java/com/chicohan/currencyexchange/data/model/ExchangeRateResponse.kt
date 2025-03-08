package com.chicohan.currencyexchange.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ExchangeRateResponse(
    @SerializedName("success")  val success: Boolean,
    @SerializedName("quotes") val rates: Map<String, BigDecimal>?,
    @SerializedName("error") val apiError: ApiErrorResponse? = null
)