package com.chicohan.currencyexchange.data.repository

import android.content.Context
import android.util.Log
import com.chicohan.currencyexchange.data.api.CurrencyApi
import com.chicohan.currencyexchange.data.db.dao.ExchangeRateDao
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.FavoriteCurrency
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.data.model.CurrencyInfo
import com.chicohan.currencyexchange.data.network.SafeApiCall
import com.chicohan.currencyexchange.domain.model.Resource
import com.chicohan.currencyexchange.helper.Constants.API_KEY
import com.chicohan.currencyexchange.helper.PreferencesHelper
import com.chicohan.currencyexchange.helper.loadCurrencyMappings
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val api: CurrencyApi,
    private val currencyDao: ExchangeRateDao,
    private val context: Context,
    private val preferencesHelper: PreferencesHelper
) : ExchangeRateRepository, SafeApiCall {

    private val currencyMapping: Map<String, CurrencyInfo>? by lazy {
        loadCurrencyMappings(context)?.associateBy { it.code }
    }
    private val defaultFavoriteCurrencies = listOf("USD", "EUR", "AUD", "JPY", "MMK")

    override suspend fun getExchangeRates(
        forceRefresh: Boolean,
        baseCurrency: String
    ): Resource<List<ExchangeRateEntity>> = safeApiCall {
        val favoriteCurrencies = currencyDao.getFavoriteCurrencies().map { it.currencyCode }

        val cacheRates = currencyDao.getCurrencyRates()
        val shouldRefresh =
            forceRefresh || cacheRates.isEmpty() || (System.currentTimeMillis() - cacheRates.first().timestamp > 30 * 60 * 1000) // for quota and testing set 24 hour //only refresh when it is over 30 mins

        Log.d("ExchangeRateRepositoryImpl", "forceRefresh: $forceRefresh")
        Log.d("ExchangeRateRepositoryImpl", "currency map: $currencyMapping")
        Log.d("ExchangeRateRepositoryImpl", "caches: $cacheRates")

        if (shouldRefresh) {
            Log.d("ExchangeRateRepositoryImpl", "baseCurrency: $baseCurrency")
            val response = api.getExchangeRates(
                apiKey = API_KEY,
                currencies = "",
                source = baseCurrency.ifBlank { "USD" }
            )
            Log.d("ExchangeRateRepositoryImpl", "API response: $response")
            if (!response.success) {
                val errorMessage = response.apiError?.errorMessage ?: "Unknown API error"
                throw Exception(errorMessage)
            }

            val rates = response.rates?.mapNotNull { (key, value) ->
                val currencyCode = key.removePrefix(baseCurrency)
                val currencyInfo = currencyMapping?.get(currencyCode) ?: return@mapNotNull null
                val isFavorite = currencyCode in favoriteCurrencies

                ExchangeRateEntity(
                    currency = currencyCode,
                    rate = value,
                    timestamp = System.currentTimeMillis(),
                    currencyName = currencyInfo.name,
                    flagUrl = currencyInfo.flag ?: "",
                    isFavourite = isFavorite
                )
            } ?: emptyList()
            currencyDao.clearRates()
            currencyDao.insertRates(rates)
            Log.d("ExchangeRateRepositoryImpl", "Inserted rates: $rates")
            rates

        } else {
            val ratesWithFavorites =
                cacheRates.map { rate -> rate.copy(isFavourite = rate.currency in favoriteCurrencies) }
            currencyDao.clearRates()
            currencyDao.insertRates(ratesWithFavorites)
            ratesWithFavorites
        }
    }

    override suspend fun getSupportedCurrencies(): Resource<List<SupportedCurrencies>> =
        safeApiCall {
            val cachedSupportedCurrencies = currencyDao.getSupportedCurrencies()
            Log.d(
                "ExchangeRateRepositoryImpl",
                "cachedSupportedCurrencies: $cachedSupportedCurrencies"
            )
            Log.d("ExchangeRateRepositoryImpl", "currencyMapping: $currencyMapping")

            cachedSupportedCurrencies.ifEmpty {
                val response = api.getSupportedCurrencies(apiKey = API_KEY)
                Log.d("ExchangeRateRepositoryImpl", "API response: $response")

                if (!response.success) {
                    val errorMessage = response.apiError?.errorMessage ?: "Unknown API error"
                    throw Exception(errorMessage)
                }
                val supportedCurrencies = response.currencies?.mapNotNull { (key, value) ->
                    val currencyInfo = currencyMapping?.get(key) ?: return@mapNotNull null
                    SupportedCurrencies(
                        currencyCode = key,
                        currencyName = value,
                        flag = currencyInfo.flag ?: ""
                    )
                } ?: emptyList()

                currencyDao.clearCurrencies()
                currencyDao.insertCurrencies(supportedCurrencies)
                supportedCurrencies
            }
        }

    override suspend fun toggleFavoriteStatus(
        currencyCode: String,
        isFavorite: Boolean
    ): Resource<Boolean> {
        return try {
            // add or remove to fav table
            if (isFavorite) {
                currencyDao.addFavoriteCurrency(FavoriteCurrency(currencyCode))
            } else {
                currencyDao.removeFavoriteCurrency(currencyCode)
            }
            // Update the rate table with the current fav rate
            val rates = currencyDao.getCurrencyRates()
            val updatedRates = rates.map { rate ->
                if (rate.currency == currencyCode) {
                    rate.copy(isFavourite = isFavorite)
                } else {
                    rate
                }
            }
            currencyDao.clearRates()
            currencyDao.insertRates(updatedRates)
            Resource.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Failed to update favorite status", false)
        }
    }

    override suspend fun initializeDefaultFavorites(): Resource<Boolean> {
        return try {
            currencyDao.initializeDefaultFavorites(defaultFavoriteCurrencies)
            Resource.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Failed to initialize default favorites", false)
        }
    }
}