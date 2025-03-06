package com.chicohan.currencyexchange.helper

import android.content.SharedPreferences
import javax.inject.Inject

class PreferencesHelper @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun saveAmount(amount: Double) {
        sharedPreferences.edit().putFloat("saved_amount", amount.toFloat()).apply()
    }

    fun getSavedAmount(): Double {
        return sharedPreferences.getFloat("saved_amount", 1.0f).toDouble()
    }
}