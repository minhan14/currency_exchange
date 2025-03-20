package com.chicohan.currencyexchange.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class DetailFragmentModel(
    val currentCurrency: String,
    val currentCurrencyAmount: Double,
    val calculatedCurrency: String,
    val calculatedCurrencyAmount: BigDecimal
): Parcelable

