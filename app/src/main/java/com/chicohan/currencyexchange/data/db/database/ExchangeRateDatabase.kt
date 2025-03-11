package com.chicohan.currencyexchange.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chicohan.currencyexchange.data.db.dao.ExchangeRateDao
import com.chicohan.currencyexchange.data.db.entity.BigDecimalConverter
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.FavoriteCurrency
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies


@Database(
    entities = [
        ExchangeRateEntity::class, 
        SupportedCurrencies::class,
        FavoriteCurrency::class
    ], 
    version = 9
)
@TypeConverters(BigDecimalConverter::class)
abstract class ExchangeRateDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao
}