package com.chicohan.currencyexchange.domain

import javax.inject.Inject

data class UseCases @Inject constructor(
    val rateUseCase: GetExchangeRateUseCase,
    val calculateExchangeRateUseCase: CalculateExchangeRateUseCase,
    val getSupportedCurrenciesUseCase: GetSupportedCurrenciesUseCase,
    val getToggleFavoriteUseCase: ToggleFavoriteUseCase,
    val initializeDefaultFavoritesUseCase: InitializeDefaultFavoritesUseCase
)
