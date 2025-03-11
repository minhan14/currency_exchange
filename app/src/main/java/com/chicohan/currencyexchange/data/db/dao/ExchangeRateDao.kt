package com.chicohan.currencyexchange.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.FavoriteCurrency
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rates")
    suspend fun getCurrencyRates(): List<ExchangeRateEntity>

    @Query("SELECT * FROM exchange_rates")
    fun getCurrencyRatesStream(): Flow<List<ExchangeRateEntity>>

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

    @Query("SELECT * FROM favorite_currencies")
    fun getFavoriteCurrenciesStream(): Flow<List<FavoriteCurrency>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteCurrency(favoriteCurrency: FavoriteCurrency)

    @Query("DELETE FROM favorite_currencies WHERE currencyCode = :currencyCode")
    suspend fun removeFavoriteCurrency(currencyCode: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_currencies WHERE currencyCode = :currencyCode LIMIT 1)")
    suspend fun isCurrencyFavorite(currencyCode: String): Boolean

    @Transaction
    suspend fun initializeDefaultFavorites(defaultCurrencies: List<String>) {
        for (currencyCode in defaultCurrencies) {
            if (!isCurrencyFavorite(currencyCode)) {
                addFavoriteCurrency(FavoriteCurrency(currencyCode))
            }
        }
    }

    @Query("UPDATE exchange_rates SET isFavourite = :isFavorite WHERE currency = :currencyCode")
    suspend fun updateFavoriteStatus(currencyCode: String, isFavorite: Boolean)

}