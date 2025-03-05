package com.chicohan.currencyexchange.domain

import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.repository.ExchangeRateRepository
import com.chicohan.currencyexchange.domain.model.Resource
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GetExchangeRateUseCase @Inject constructor(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Resource<List<ExchangeRateEntity>> {
        return try {
            repository.getExchangeRates(forceRefresh)
        } catch (e: Throwable) {
            e.printStackTrace()
            Resource.Error(e.message.toString(), data = null)
        } catch (e: CancellationException) {
            e.printStackTrace()
            //in case job cancelled
            throw CancellationException(e.message)
        }
    }

}