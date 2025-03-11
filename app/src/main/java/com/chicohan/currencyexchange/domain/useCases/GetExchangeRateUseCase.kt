package com.chicohan.currencyexchange.domain.useCases

import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.repository.ExchangeRateRepository
import com.chicohan.currencyexchange.domain.model.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class GetExchangeRateUseCase @Inject constructor(
    private val repository: ExchangeRateRepository,
    private val scope: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend operator fun invoke(
        forceRefresh: Boolean = false,
        baseCurrency: String
    ): Resource<List<ExchangeRateEntity>> = withContext(scope) {
        return@withContext try {
            Resource.Error("", data = null)
          //  repository.getExchangeRates(forceRefresh = forceRefresh, baseCurrency = baseCurrency)
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