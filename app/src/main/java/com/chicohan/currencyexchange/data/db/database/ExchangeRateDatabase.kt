package com.chicohan.currencyexchange.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chicohan.currencyexchange.data.db.dao.ExchangeRateDao
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies


@Database(entities = [ExchangeRateEntity::class, SupportedCurrencies::class], version = 6)
abstract class ExchangeRateDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao
}