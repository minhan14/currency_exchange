package com.chicohan.currencyexchange.ui.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.domain.Event
import com.chicohan.currencyexchange.domain.GetExchangeRateUseCase
import com.chicohan.currencyexchange.domain.model.Resource
import com.chicohan.currencyexchange.helper.PreferencesHelper
import com.chicohan.currencyexchange.domain.UseCases
import com.chicohan.currencyexchange.domain.model.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
    private val _ratesState =
        MutableStateFlow<Resource<List<ExchangeRateEntity>>>(Resource.Loading(null))
    private val ratesState: StateFlow<Resource<List<ExchangeRateEntity>>> = _ratesState

    // Base currency code  i.e user selected county from the dialog
//    private val _baseCurrency = MutableStateFlow(preferencesHelper.getBaseCurrency() ?: "USD")
//    private val baseCurrency: StateFlow<String> = _baseCurrency.asStateFlow()

    // State for supported currencies
    private val _supportedCurrencies = MutableStateFlow<List<SupportedCurrencies>>(emptyList())
    val supportedCurrencies: StateFlow<List<SupportedCurrencies>> =
        _supportedCurrencies.asStateFlow()

    // Search query for currency filtering
    var currencySearchQuery = MutableStateFlow("")
        private set
//    val currencySearchQuery: StateFlow<String> = _currencySearchQuery.asStateFlow()

    @OptIn(FlowPreview::class)
    val filteredCurrencies: StateFlow<List<SupportedCurrencies>> = combine(
        supportedCurrencies,
        currencySearchQuery.debounce(300)
            .distinctUntilChanged() //debounce to avoid filtering on every keystroke, and distinctUntilChanged only trigger filtering when the query actually changes
    ) { currencies, query ->
        if (query.isBlank()) {
            currencies
        } else {
            currencies.filter { currency ->
                currency.currencyCode.contains(query, ignoreCase = true) ||
                        currency.currencyName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // State for base currency update process
//    private val _baseCurrencyUpdateState = MutableStateFlow<UIState<SupportedCurrencies>>(UIState.Idle)
//    val baseCurrencyUpdateState: StateFlow<UIState<SupportedCurrencies>> = _baseCurrencyUpdateState.asStateFlow()

    private val _baseCurrencyState = MutableStateFlow<UIState<String>>(
        UIState.Success(preferencesHelper.getBaseCurrency() ?: "USD")
    )

    // value for internal use
    private val baseCurrency: StateFlow<String> = _baseCurrencyState
        .map { state ->
            when (state) {
                is UIState.Success -> state.result
                else -> preferencesHelper.getBaseCurrency() ?: "USD"
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            preferencesHelper.getBaseCurrency() ?: "USD"
        )

    val selectedSupportedCurrency: StateFlow<UIState<SupportedCurrencies>> =
        //should filter not empty for supported country to be loaded
        combine(
            _supportedCurrencies.filter { it.isNotEmpty() },
            _baseCurrencyState
        ) { currencies, baseCurrencyState ->
            Log.d("currencies", "supported currency list $currencies")
            when (baseCurrencyState) {
                is UIState.Idle -> UIState.Idle
                is UIState.Loading -> UIState.Loading
                is UIState.Success -> {
                    val currencyCode = baseCurrencyState.result
                    val currency = currencies.find { it.currencyCode == currencyCode }
                    if (currency != null) {
                        UIState.Success(currency)
                    } else {
                        UIState.Error("Currency not found: $currencyCode")
                    }
                }

                is UIState.Error -> UIState.Error(baseCurrencyState.errorMessage)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, UIState.Idle)

    // user select amt
    private var _amount = MutableStateFlow(preferencesHelper.getSavedAmount())
    val amount: StateFlow<Double> = _amount

    // Reactive calculation of converted rates
    @OptIn(FlowPreview::class)
    private val convertedRates: StateFlow<List<ExchangeRateEntity>> =
        combine(ratesState, amount.debounce(300).distinctUntilChanged()) { resource, amount ->
            if (resource is Resource.Success) {
                useCases.calculateExchangeRateUseCase(resource.data ?: emptyList(), amount)
            } else {
                emptyList()
            }
        }.map { rates ->
            rates.filter { it.isFavourite }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    /**
    -Combined UI state that includes all states to preserve
    -some need to be separate
     */
    val uiState: StateFlow<CurrencyListUiState> =
        combine(
            ratesState,
            convertedRates,
            amount,
            baseCurrency
        ) { resource, converted, amt, base ->
            CurrencyListUiState(
                loading = resource is Resource.Loading,
                convertedRates = converted,
                errorMessage = if (resource is Resource.Error) Event(resource.message) else Event(
                    null
                ),
                amount = amt,
                baseCurrency = base
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, CurrencyListUiState())

    init {
        loadSupportedCurrencies()
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
                _ratesState.value = useCases.rateUseCase(forceRefresh, baseCurrency.value)
            } catch (e: Exception) {
                _ratesState.value = Resource.Error(e.message ?: "Unknown error", null)
            }
        }
    }

    private fun loadSupportedCurrencies() = viewModelScope.launch {
        when (val res = useCases.getSupportedCurrenciesUseCase.invoke()) {
            is Resource.Success ->
                res.data?.let {
                    _supportedCurrencies.value = it
                }

            is Resource.Error -> _ratesState.value =
                Resource.Error(res.message, null) // use the rate state to listen error
            is Resource.Loading -> Unit
        }

    }

    fun updateBaseCurrency(currencyCode: String) = viewModelScope.launch {
        if (baseCurrency.value == currencyCode) return@launch
        try {
            _baseCurrencyState.value = UIState.Loading
            // When base currency change we need to force refresh from api which will return updated exchanged currencies
            /**
             * add ui state control to baseCurrencyState
             * to prevent changing before api call succeed cause reactive stream (flow)
             * updated immediately before the API call to fetch new exchange rates completes
             */
            when (val result = useCases.rateUseCase(true, currencyCode)) {
                is Resource.Success -> {
                    // Only update the _basecurrency if the API call succeeds
                    _baseCurrencyState.value = UIState.Success(currencyCode)
                    preferencesHelper.saveBaseCurrency(currencyCode)
                    _ratesState.value = result
                }

                is Resource.Error -> {
                    // If there exist err don't update the _basecurrency
                    _ratesState.value = result
                    _baseCurrencyState.value = UIState.Error(result.message)
                }

                is Resource.Loading -> _baseCurrencyState.value = UIState.Loading
            }
        } catch (e: Exception) {
            _ratesState.value = Resource.Error(e.message ?: "Unknown error", null)
            _baseCurrencyState.value = UIState.Error(e.message ?: "Unknown error")
        }
    }

    fun updateCurrencySearchQuery(query: String) {
        currencySearchQuery.value = query
    }

    fun clearSearchQuery() {
        currencySearchQuery.value = ""
    }

    fun toggleFavorite(currencyCode: String, isFavorite: Boolean) {
        viewModelScope.launch {
            useCases.getToggleFavoriteUseCase(currencyCode, isFavorite)
            fetchExchangeRates(false)
        }
    }
    //reactive state for selected currency to be load in the image view , this is error prone cu
//    val selectedSupportedCurrency: StateFlow<SupportedCurrencies?> =
//        combine(_supportedCurrencies, _baseCurrency) { currencies, base ->
//
//            currencies.find { it.currencyCode == base }?.let {
//                SupportedCurrencies(
//                    currencyCode = it.currencyCode,
//                    currencyName = it.currencyName,
//                    flag = it.flag
//                )
//            }
//        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

}

data class CurrencyListUiState(
    val loading: Boolean = false,
    val convertedRates: List<ExchangeRateEntity> = emptyList(),
    val errorMessage: Event<String?> = Event(null),
    val amount: Double = 1.0,
    val baseCurrency: String = "USD"
)


