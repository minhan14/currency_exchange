package com.chicohan.currencyexchange.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chicohan.currencyexchange.data.db.dao.ExchangeRateDao
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity


@Database(entities = [ExchangeRateEntity::class], version = 2)
abstract class ExchangeRateDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao
}