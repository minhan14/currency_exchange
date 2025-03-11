package com.chicohan.currencyexchange.domain.useCases

import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.repository.ExchangeRateRepository
import com.chicohan.currencyexchange.domain.model.Resource
import javax.inject.Inject

class FetchExchangeRateUseCase @Inject constructor(private val repository: ExchangeRateRepository) {
    suspend operator fun invoke(baseCurrency: String): Resource<List<ExchangeRateEntity>> = repository.fetchExchangeRates(baseCurrency)
}