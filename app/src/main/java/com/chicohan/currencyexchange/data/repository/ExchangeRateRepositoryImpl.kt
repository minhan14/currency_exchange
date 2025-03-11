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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
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

    override suspend fun fetchExchangeRates(baseCurrency: String): Resource<List<ExchangeRateEntity>> =
        safeApiCall {
            Log.d("ExchangeRateRepositoryImpl", "fetching api")
            val response = api.getExchangeRates(
                apiKey = API_KEY,
                currencies = "",
                source = baseCurrency.ifBlank { "USD" })
            Log.d("ExchangeRateRepositoryImpl", "API response: $response")
            if (!response.success) {
                val errorMessage = response.apiError?.errorMessage ?: "Unknown API error"
                throw Exception(errorMessage)
            }
            val rates = response.rates?.mapNotNull { (key, value) ->
                val currencyCode = key.removePrefix(baseCurrency)
                val currencyInfo = currencyMapping?.get(currencyCode) ?: return@mapNotNull null
                ExchangeRateEntity(
                    currency = currencyCode,
                    rate = value,
                    timestamp = System.currentTimeMillis(),
                    currencyName = currencyInfo.name,
                    flagUrl = currencyInfo.flag ?: "",
                    isFavourite = false
                )
            } ?: emptyList()
            currencyDao.apply { clearRates();insertRates(rates) }
            Log.d("ExchangeRateRepositoryImpl", "Inserted rates: $rates")
            rates
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
                currencyDao.apply { clearCurrencies();insertCurrencies(supportedCurrencies) }
                supportedCurrencies
            }
        }

    override suspend fun toggleFavoriteStatus(currencyCode: String, isFavorite: Boolean): Resource<Boolean> {
        return try { currencyDao.apply {
                if (isFavorite) {
                    addFavoriteCurrency(FavoriteCurrency(currencyCode))
                } else {
                    removeFavoriteCurrency(currencyCode)
                }
                updateFavoriteStatus(currencyCode, isFavorite)
            }
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

    // single calling
    override suspend fun getCachedExchangedRates(): List<ExchangeRateEntity> = currencyDao.getCurrencyRates()

    /**
     *  watch for the exchange_rates table and favorite_currencies table
     *  get the favourite currency from exchange rates table
     *  filter the exchange rates with user selected currency
     */
    override fun getCachedFavouriteExchangeRateStream(): Flow<List<ExchangeRateEntity>> {
        return combine(currencyDao.getCurrencyRatesStream(), currencyDao.getFavoriteCurrenciesStream()) { rates, fav ->
            val favorites = fav.map { it.currencyCode }.toSet()
            rates.filter { it.currency in favorites }
        }.flowOn(Dispatchers.IO)
    }

    override fun shouldRefresh(rates: List<ExchangeRateEntity>): Boolean {
        val lastUpdated = rates.firstOrNull()?.timestamp ?: 0L
        return (System.currentTimeMillis() - lastUpdated) > 12 * 60 * 60 * 1000
    }
}