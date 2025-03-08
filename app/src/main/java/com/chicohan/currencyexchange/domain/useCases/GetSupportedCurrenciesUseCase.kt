package com.chicohan.currencyexchange.domain.useCases

import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.data.repository.ExchangeRateRepository
import com.chicohan.currencyexchange.domain.model.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetSupportedCurrenciesUseCase @Inject constructor(
    private val repository: ExchangeRateRepository,
    private val scope: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend operator fun invoke(): Resource<List<SupportedCurrencies>> = withContext(scope){
        repository.getSupportedCurrencies()
    }

}
