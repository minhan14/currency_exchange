package com.chicohan.currencyexchange.domain

import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import javax.inject.Inject

class CalculateExchangeRateUseCase @Inject constructor() {

    operator fun invoke(
        rates: List<ExchangeRateEntity>?,
        amount: Double
    ): List<ExchangeRateEntity> {
        return rates?.map { rate ->
            rate.copy(rate = rate.convertAmount(amount))
        } ?: emptyList()
    }

}