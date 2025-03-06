package com.chicohan.currencyexchange.data.repository

import android.content.Context
import android.util.Log
import com.chicohan.currencyexchange.data.api.CurrencyApi
import com.chicohan.currencyexchange.data.db.dao.ExchangeRateDao
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.model.CurrencyInfo
import com.chicohan.currencyexchange.domain.model.Resource
import com.chicohan.currencyexchange.helper.Constants.API_KEY
import com.chicohan.currencyexchange.helper.loadCurrencyMappings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

class ExchangeRateRepositoryImpl @Inject constructor(
    private val api: CurrencyApi,
    private val currencyDao: ExchangeRateDao,
    private val context: Context
) : ExchangeRateRepository {

    override suspend fun getExchangeRates(
        forceRefresh: Boolean,
        baseCurrency: String
    ): Resource<List<ExchangeRateEntity>> {
        return try {

            val currencyMapping: Map<String, CurrencyInfo> = loadCurrencyMappings(context)
                .associateBy { it.code }
            val cacheRates = currencyDao.getCurrencyRates()
            val shouldRefresh =
                forceRefresh || cacheRates.isEmpty() || (System.currentTimeMillis() - cacheRates.first().timestamp > 30 * 60 * 1000)

            Log.d("ExchangeRateRepositoryImpl", forceRefresh.toString())
            Log.d("ExchangeRateRepositoryImpl", "currency map $currencyMapping")

            if (shouldRefresh) {
                val response =
                    api.getExchangeRates(apiKey = API_KEY, currencies = "", source = baseCurrency)
                val rates = response.rates.mapNotNull { (key, value) ->
                    val currencyCode = key.removePrefix(baseCurrency)
                    val currencyInfo = currencyMapping[currencyCode] ?: return@mapNotNull null
                    ExchangeRateEntity(
                        currency = currencyCode,
                        rate = value,
                        timestamp = System.currentTimeMillis(),
                        countryName = currencyInfo.country,
                        flagUrl = currencyInfo.flag ?: ""
                    )
                }
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

}