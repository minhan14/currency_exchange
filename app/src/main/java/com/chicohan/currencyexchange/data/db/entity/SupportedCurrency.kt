package com.chicohan.currencyexchange.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "supported_currencies")
data class SupportedCurrencies(
    @PrimaryKey val currencyCode: String,
    val currencyName: String,
    val flag: String
)
