package com.chicohan.currencyexchange.ui.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.domain.Event
import com.chicohan.currencyexchange.domain.GetExchangeRateUseCase
import com.chicohan.currencyexchange.domain.model.Resource
import com.chicohan.currencyexchange.helper.PreferencesHelper
import com.chicohan.currencyexchange.domain.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val useCases: UseCases,
    private val preferencesHelper: PreferencesHelper
) : ViewModel() {

    /**
    -encapsulate the ui states
    -rates to be cached and recalculate upon rate changes
    -convertedRates is the calculated rates
    -error message should be collect once
     */

    // State for raw exchange rates
    private val _ratesState = MutableStateFlow<Resource<List<ExchangeRateEntity>>>(Resource.Loading(null))
    private val ratesState: StateFlow<Resource<List<ExchangeRateEntity>>> = _ratesState
    
    // State for amount
    private var _amount = MutableStateFlow(preferencesHelper.getSavedAmount())
    private val amount: StateFlow<Double> = _amount
    
    // Reactive calculation of converted rates
    private val convertedRates: StateFlow<List<ExchangeRateEntity>> =
        combine(ratesState, amount) { resource, amount ->
            if (resource is Resource.Success) {
                useCases.calculateExchangeRateUseCase(resource.data ?: emptyList(), amount)
            } else {
                emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Combined UI state that includes all relevant information
    val uiState: StateFlow<CurrencyListUiState> = 
        combine(ratesState, convertedRates, amount) { resource, converted, amt ->
            CurrencyListUiState(
                loading = resource is Resource.Loading,
                convertedRates = converted,
                errorMessage = if (resource is Resource.Error) Event(resource.message) else Event(null),
                amount = amt
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, CurrencyListUiState())
    
    init {
        fetchExchangeRates(false)
    }
    
    fun updateAmount(newAmount: Double) {
        if (_amount.value == newAmount) return
        _amount.value = newAmount
        preferencesHelper.saveAmount(newAmount)
    }
    
    fun fetchExchangeRates(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _ratesState.value = Resource.Loading(null)
            try {
                _ratesState.value = useCases.rateUseCase(forceRefresh,"USD")
            } catch (e: Exception) {
                _ratesState.value = Resource.Error(e.message ?: "Unknown error", null)
            }
        }
    }
}

data class CurrencyListUiState(
    val loading: Boolean = false,
    val convertedRates: List<ExchangeRateEntity> = emptyList(),
    val errorMessage: Event<String?> = Event(null),
    val amount: Double = 1.0
)


