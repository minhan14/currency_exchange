package com.chicohan.currencyexchange.data.repository

import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.domain.model.Resource

interface ExchangeRateRepository {

    suspend fun getExchangeRates(
        forceRefresh: Boolean,
        baseCurrency: String
    ): Resource<List<ExchangeRateEntity>>

    suspend fun getSupportedCurrencies(): Resource<List<SupportedCurrencies>>

    suspend fun toggleFavoriteStatus(currencyCode: String, isFavorite: Boolean): Resource<Boolean>

    suspend fun initializeDefaultFavorites(): Resource<Boolean>

    suspend fun isFirstRun(): Boolean

}