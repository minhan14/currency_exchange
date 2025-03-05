package com.chicohan.currencyexchange.data.repository

import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.domain.model.Resource

interface ExchangeRateRepository {

    suspend fun getExchangeRates(forceRefresh: Boolean): Resource<List<ExchangeRateEntity>>

}