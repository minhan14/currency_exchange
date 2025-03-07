package com.chicohan.currencyexchange.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rates")
    suspend fun getCurrencyRates(): List<ExchangeRateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>)

    @Query("DELETE FROM exchange_rates")
    suspend fun clearRates()

    @Query("SELECT * FROM supported_currencies")
    suspend fun getSupportedCurrencies(): List<SupportedCurrencies>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(rates: List<SupportedCurrencies>)

    @Query("DELETE FROM supported_currencies")
    suspend fun clearCurrencies()

    @Query("UPDATE exchange_rates SET isFavourite = :isFavorite WHERE currency = :currencyCode")
    suspend fun updateFavoriteStatus(currencyCode: String, isFavorite: Boolean)

}