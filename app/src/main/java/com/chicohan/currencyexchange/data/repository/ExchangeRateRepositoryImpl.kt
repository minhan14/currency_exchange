package com.chicohan.currencyexchange.data.repository

import android.util.Log
import com.chicohan.currencyexchange.data.api.CurrencyApi
import com.chicohan.currencyexchange.data.db.dao.ExchangeRateDao
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.domain.model.Resource
import com.chicohan.currencyexchange.helper.Constants.API_KEY
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

class ExchangeRateRepositoryImpl @Inject constructor(
    private val api: CurrencyApi,
    private val currencyDao: ExchangeRateDao
): ExchangeRateRepository {

    override suspend fun getExchangeRates(forceRefresh: Boolean): Resource<List<ExchangeRateEntity>> {
        return try {
            val cacheRates = currencyDao.getCurrencyRates()
            val shouldRefresh = forceRefresh || cacheRates.isEmpty() || (System.currentTimeMillis() - cacheRates.first().timestamp > 30 * 60 * 1000)

            Log.d("ExchangeRateRepositoryImpl",forceRefresh.toString())

            if (shouldRefresh) {
                val response = api.getExchangeRates(API_KEY, "")
                val rates = response.rates.map { ExchangeRateEntity(it.key, it.value, System.currentTimeMillis()) }
                currencyDao.clearRates()
                currencyDao.insertRates(rates)
                Log.d("ExchangeRateRepositoryImpl","calling api")
                Resource.Success(rates)
            } else {
                Log.d("ExchangeRateRepositoryImpl","getting from cache")
                Resource.Success(cacheRates)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Unknown error", null)
        }
    }

}