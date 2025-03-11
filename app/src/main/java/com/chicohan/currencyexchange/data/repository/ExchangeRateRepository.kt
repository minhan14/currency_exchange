package com.chicohan.currencyexchange.data.repository

import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface ExchangeRateRepository {

    suspend fun fetchExchangeRates(
        baseCurrency: String
    ): Resource<List<ExchangeRateEntity>>

    suspend fun getSupportedCurrencies(): Resource<List<SupportedCurrencies>>

    suspend fun toggleFavoriteStatus(currencyCode: String, isFavorite: Boolean): Resource<Boolean>

    suspend fun initializeDefaultFavorites(): Resource<Boolean>

    suspend fun getCachedExchangedRates(): List<ExchangeRateEntity>

    /**
     * filter the exchange rates with the fav currency //with this approach we can reactively get the exchange rate with the user selected currency while toggling the favourite
     */
    fun getCachedFavouriteExchangeRateStream(): Flow<List<ExchangeRateEntity>>

    fun shouldRefresh(rates: List<ExchangeRateEntity>): Boolean

}