package com.chicohan.currencyexchange.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.math.RoundingMode

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val currency: String,
    val rate: BigDecimal,
    val timestamp: Long,
    val currencyName: String,
    val flagUrl: String,
    val isFavourite: Boolean = false
) {
    fun convertAmount(amount: BigDecimal): BigDecimal {
        return amount.multiply(rate).setScale(6, RoundingMode.HALF_UP)
    }
}