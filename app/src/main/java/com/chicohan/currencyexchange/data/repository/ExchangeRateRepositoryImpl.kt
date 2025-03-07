package com.chicohan.currencyexchange.data.repository

import android.content.Context
import android.util.Log
import com.chicohan.currencyexchange.data.api.CurrencyApi
import com.chicohan.currencyexchange.data.db.dao.ExchangeRateDao
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.data.model.CurrencyInfo
import com.chicohan.currencyexchange.domain.model.Resource
import com.chicohan.currencyexchange.helper.Constants.API_KEY
import com.chicohan.currencyexchange.helper.loadCurrencyMappings
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val api: CurrencyApi,
    private val currencyDao: ExchangeRateDao,
    private val context: Context
) : ExchangeRateRepository {

//    private val currencyMapping: Map<String, CurrencyInfo> by lazy {
//        loadCurrencyMappings(context).associateBy { it.code }
//    }

    override suspend fun getExchangeRates(
        forceRefresh: Boolean,
        baseCurrency: String
    ): Resource<List<ExchangeRateEntity>> {
        return try {
            val currencyMapping: Map<String, CurrencyInfo>? = loadCurrencyMappings(context)
                ?.associateBy { it.code }
            val cacheRates = currencyDao.getCurrencyRates()
            val shouldRefresh =
                forceRefresh || cacheRates.isEmpty() || (System.currentTimeMillis() - cacheRates.first().timestamp > 30 * 60 * 1000000)

            Log.d("ExchangeRateRepositoryImpl", forceRefresh.toString())
            Log.d("ExchangeRateRepositoryImpl", "currency map $currencyMapping")
            Log.d("ExchangeRateRepositoryImpl", "caches $cacheRates")


            if (shouldRefresh) {
                Log.d("ExchangeRateRepositoryImpl", "baseCurrency $baseCurrency")
                val response =
                    api.getExchangeRates(apiKey = API_KEY, currencies = "", source = baseCurrency.ifBlank { "USD" })
                Log.d("ExchangeRateRepositoryImpl", "res getExchangeRates $response")
                val rates = response.rates?.mapNotNull { (key, value) ->
                    val currencyCode = key.removePrefix(baseCurrency)
                    val currencyInfo = currencyMapping?.get(currencyCode) ?: return@mapNotNull null
                    ExchangeRateEntity(
                        currency = currencyCode,
                        rate = value,
                        timestamp = System.currentTimeMillis(),
                        currencyName = currencyInfo.name,
                        flagUrl = currencyInfo.flag ?: ""
                    )
                } ?: emptyList()
                currencyDao.clearRates()
                currencyDao.insertRates(rates)
                Log.d("ExchangeRateRepositoryImpl", "calling api $rates")
                Resource.Success(rates)
            } else {
                Log.d("ExchangeRateRepositoryImpl", "getting from cache $cacheRates")
                Resource.Success(cacheRates)
            }
//            Resource.Error("mocking error error", null)

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown error", null)
        }
    }

    override suspend fun getSupportedCurrencies(): Resource<List<SupportedCurrencies>> {
        return try {
            val currencyMapping: Map<String, CurrencyInfo>? = loadCurrencyMappings(context)
                ?.associateBy { it.code }
            val cachedSupportedCurrencies = currencyDao.getSupportedCurrencies()
            Log.d("ExchangeRateRepositoryImpl", "cachedSupportedCurrencies $cachedSupportedCurrencies")
            Log.d("ExchangeRateRepositoryImpl", "currencyMapping >>$currencyMapping")
            if (cachedSupportedCurrencies.isEmpty()) {
                val response = api.getSupportedCurrencies(apiKey = API_KEY)
                Log.d("ExchangeRateRepositoryImpl", "resp >>$response")
                val supportedCurrencies = response.currencies.mapNotNull { (key, value) ->
                    val currencyInfo = currencyMapping?.get(key) ?: return@mapNotNull null
                    SupportedCurrencies(
                        currencyCode = key,
                        currencyName = value,
                        flag = currencyInfo.flag ?: ""
                    )
                }
                Log.d("ExchangeRateRepositoryImpl", "supportedCurrencies >>$supportedCurrencies")
                currencyDao.clearCurrencies()
                currencyDao.insertCurrencies(supportedCurrencies)
                Log.d("ExchangeRateRepositoryImpl", "calling api $supportedCurrencies")
                Resource.Success(supportedCurrencies)
            } else {
                Resource.Success(cachedSupportedCurrencies)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown error", null)
        }
    }

    override suspend fun toggleFavoriteStatus(
        currencyCode: String,
        isFavorite: Boolean
    ): Resource<Boolean> {
        return try {
            currencyDao.updateFavoriteStatus(currencyCode, isFavorite)
            Resource.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Failed to update favorite status", false)
        }
    }

}