package com.chicohan.currencyexchange.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.repository.ExchangeRateRepository
import com.chicohan.currencyexchange.domain.Event
import com.chicohan.currencyexchange.domain.GetExchangeRateUseCase
import com.chicohan.currencyexchange.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val exchangeRatesUseCase: GetExchangeRateUseCase
) : ViewModel() {

    var currencyListState = MutableStateFlow(CurrencyListUiState())
        private set

    init {
        fetchExchangeRates(false)
    }

    private fun fetchExchangeRates(forceRefresh: Boolean = false) = viewModelScope.launch {
        currencyListState.update { it.copy(loading = Event(true)) }
        when (val state = exchangeRatesUseCase.invoke(
            forceRefresh = forceRefresh
        )) {
            is Resource.Error -> currencyListState.update {
                it.copy(
                    loading = Event(false),
                    isSuccess = Event(emptyList()),
                    errorMessage = Event(state.message)
                )
            }
            is Resource.Loading -> Unit

            is Resource.Success -> currencyListState.update {
                it.copy(
                    loading = Event(false),
                    isSuccess = Event(state.data),
                    errorMessage = Event(null)
                )
            }
        }
    }
}

data class CurrencyListUiState(
    var loading: Event<Boolean> = Event(false),
    val isSuccess: Event<List<ExchangeRateEntity>?> = Event(emptyList()),
    val errorMessage: Event<String?> = Event(null),
)