package com.chicohan.currencyexchange.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_currencies")
data class FavoriteCurrency(
    @PrimaryKey val currencyCode: String
) 