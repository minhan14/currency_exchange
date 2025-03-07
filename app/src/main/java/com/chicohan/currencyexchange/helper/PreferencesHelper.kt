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

    fun saveBaseCurrency(currencyCode: String) {
        sharedPreferences.edit().putString("base_currency", currencyCode).apply()
    }

    fun getBaseCurrency(): String? {
        return sharedPreferences.getString("base_currency", "USD")
    }

    fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean("is_first_run", true)
    }

    fun setFirstRunCompleted() {
        sharedPreferences.edit().putBoolean("is_first_run", false).apply()
    }
}