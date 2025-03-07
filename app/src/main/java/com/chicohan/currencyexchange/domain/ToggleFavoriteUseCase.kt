package com.chicohan.currencyexchange.domain

import com.chicohan.currencyexchange.data.repository.ExchangeRateRepository
import com.chicohan.currencyexchange.domain.model.Resource
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(currencyCode: String, isFavorite: Boolean): Resource<Boolean> {
        return repository.toggleFavoriteStatus(currencyCode, isFavorite)
    }
} 