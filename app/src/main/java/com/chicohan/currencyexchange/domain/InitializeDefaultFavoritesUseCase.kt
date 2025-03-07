package com.chicohan.currencyexchange.domain

import com.chicohan.currencyexchange.data.repository.ExchangeRateRepository
import com.chicohan.currencyexchange.domain.model.Resource
import javax.inject.Inject

class InitializeDefaultFavoritesUseCase @Inject constructor(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(): Resource<Boolean> {
        return repository.initializeDefaultFavorites()
    }
} 