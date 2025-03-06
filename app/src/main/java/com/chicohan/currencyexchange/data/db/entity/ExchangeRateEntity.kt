package com.chicohan.currencyexchange.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val currency: String,
    val rate: Double,
    val timestamp: Long,
    val countryName: String,
    val flagUrl: String
){
    fun convertAmount(amount: Double): Double {
        return amount * rate
    }
}