package com.chicohan.currencyexchange.domain.useCases

import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import java.math.BigDecimal
import javax.inject.Inject

class CalculateExchangeRateUseCase @Inject constructor() {

    operator fun invoke(
        rates: List<ExchangeRateEntity>?,
        amount: Double
    ): List<ExchangeRateEntity> {
        val amountBigDecimal = BigDecimal.valueOf(amount)
        return rates?.map { rate ->
            rate.copy(rate = rate.convertAmount(amountBigDecimal))
        } ?: emptyList()
    }

}