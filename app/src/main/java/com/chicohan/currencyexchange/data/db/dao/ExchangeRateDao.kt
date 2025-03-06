package com.chicohan.currencyexchange.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    //api handle the response to remove the selected country in the list

//    @Query("SELECT * FROM exchange_rates WHERE currency != :selectedCurrency")
//    suspend fun getCurrencyRates(selectedCurrency:String): List<ExchangeRateEntity>

  //  fun getCurrencyRates(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates")
    suspend fun getCurrencyRates(): List<ExchangeRateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>)

    @Query("DELETE FROM exchange_rates")
    suspend fun clearRates()

}