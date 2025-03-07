package com.chicohan.currencyexchange.data.api

import com.chicohan.currencyexchange.data.model.ExchangeRateResponse
import com.chicohan.currencyexchange.data.model.SupportedCurrencyResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {

    @GET("live")
    suspend fun getExchangeRates(
        @Query("access_key") apiKey: String,
        @Query("currencies") currencies: String,
        @Query("source") source: String = "USD",
        @Query("format") format: Int = 1
    ): ExchangeRateResponse

    @GET("list")
    suspend fun getSupportedCurrencies(
        @Query("access_key") apiKey: String
    ): SupportedCurrencyResponse

}