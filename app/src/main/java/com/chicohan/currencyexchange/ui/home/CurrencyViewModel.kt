package com.chicohan.currencyexchange.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.data.repository.ExchangeRateRepository
import com.chicohan.currencyexchange.domain.model.Event
import com.chicohan.currencyexchange.domain.useCases.UseCases
import com.chicohan.currencyexchange.domain.model.Resource
import com.chicohan.currencyexchange.domain.model.UIState
import com.chicohan.currencyexchange.helper.PreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val useCases: UseCases,
    private val preferencesHelper: PreferencesHelper,
    private val repository: ExchangeRateRepository
) : ViewModel() {

    /**
    -encapsulate all states in the uiState
    -currencyListStream is the reactive stream of exchange rates with user selected currency
    -convertedRates is the calculated rates
    -error message should be collect once
     */

    private val currencyListStream: StateFlow<List<ExchangeRateEntity>> =
        repository.getCachedFavouriteExchangeRateStream()
            .onStart {
                val rates = repository.getCachedExchangedRates() // get the one time exchange list
                if (rates.isEmpty()){
                    Log.d("CurrencyViewModel","delayed for first time")
                    delay(3000L)
                }
                if (rates.isEmpty() || repository.shouldRefresh(rates)) { // refresh if rate is empty or 30 minutes is up
                    fetchExchangeRates()
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    //watch for the api call states
    private val _fetchRateState =
        MutableStateFlow<Resource<List<ExchangeRateEntity>>>(Resource.Idle)
    private val fetchRateState = _fetchRateState.asStateFlow()


    private val _supportedCurrencies = MutableStateFlow<List<SupportedCurrencies>>(emptyList())
    private val supportedCurrencies: StateFlow<List<SupportedCurrencies>> =
        _supportedCurrencies.asStateFlow()

    private var currencySearchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    val filteredCurrencies: StateFlow<List<SupportedCurrencies>> =
        combine(supportedCurrencies, currencySearchQuery.debounce(200).distinctUntilChanged())
        //debounce to avoid filtering on every keystroke, and distinctUntilChanged only trigger filtering when the query actually changes
        { currencies, query ->
            if (query.isBlank()) {
                currencies
            } else {
                currencies.filter { currency ->
                    currency.currencyCode.contains(
                        query,
                        ignoreCase = true
                    ) || currency.currencyName.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
            viewModelScope, SharingStarted.Eagerly, preferencesHelper.getBaseCurrency() ?: "USD"
        )

    val selectedSupportedCurrency: StateFlow<UIState<SupportedCurrencies>> =
        //should filter for supported country to be loaded
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
    private val convertedRatesFlow: StateFlow<List<ExchangeRateEntity>> =
        combine(
            amount.debounce(200).distinctUntilChanged(),
            currencyListStream
        ) { amount, rates ->
            useCases.calculateExchangeRateUseCase(rates, amount)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
    -Combined UI state that includes all states to preserve
    -some need to be separate
     */
    val uiState: StateFlow<CurrencyListUiState> = combine(
        fetchRateState,
        convertedRatesFlow,
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
        /*
        Check if this is the first run
        for one time event , initialize default faves
         */
        viewModelScope.launch {
            Log.d("CurrencyViewModel","isfirstRun ${preferencesHelper.isFirstRun()}")
            if (preferencesHelper.isFirstRun()) {
                initializeDefaultFavorites()
                preferencesHelper.setFirstRunCompleted()
            }

        }
        loadSupportedCurrencies()

    }

    private fun initializeDefaultFavorites() = viewModelScope.launch { Log.d("CurrencyViewModel","initializeDefaultFavoritesUseCase");useCases.initializeDefaultFavoritesUseCase() }

    fun updateAmount(newAmount: Double) {
        if (_amount.value == newAmount) return
        _amount.update { newAmount }
        preferencesHelper.saveAmount(newAmount)
    }

    fun fetchExchangeRates() = viewModelScope.launch {
        Log.d("CurrencyViewModel","called fetchExchangeRates")
        _fetchRateState.update { Resource.Loading(null) }
        _fetchRateState.update { useCases.refreshCurrenciesUseCase(baseCurrency.value) }
    }

    private fun loadSupportedCurrencies() = viewModelScope.launch {
        Log.d("CurrencyViewModel","called loadSupportedCurrencies")

        when (val res = useCases.getSupportedCurrenciesUseCase.invoke()) {
            is Resource.Success -> res.data?.let {data->
                _supportedCurrencies.update { data }
            }

            is Resource.Error -> _fetchRateState.update {
                Resource.Error(
                    res.message,
                    null
                )
            }  // use the rate state to listen error
            else -> Unit
        }
    }

    /**
     * When base currency change we need to refresh from api which will return updated exchanged currencies
     * add ui state control to baseCurrencyState
     * to prevent changing before api call succeed cause reactive stream (flow) updated immediately before the API call to fetch new exchange rates completes
     * only update the _base_Currency if the API call succeeds
     * if there exist err don't update the _base currency
     */
    fun updateBaseCurrency(currencyCode: String) = viewModelScope.launch {
        if (baseCurrency.value == currencyCode) return@launch
        _baseCurrencyState.update { UIState.Loading }
        _fetchRateState.update { Resource.Loading(null) }
        when (val result = useCases.refreshCurrenciesUseCase(currencyCode)) {
            is Resource.Success -> {
                _baseCurrencyState.update { UIState.Success(currencyCode) }
                _fetchRateState.update { result }
                preferencesHelper.saveBaseCurrency(currencyCode)
            }

            is Resource.Error -> {
                _fetchRateState.update { Resource.Error(result.message, null) }
                _baseCurrencyState.update { UIState.Success(baseCurrency.value) } // update with the last value 
            }

            else -> Unit
        }

    }

    fun updateCurrencySearchQuery(query: String) = currencySearchQuery.update { query }

    fun clearSearchQuery() = currencySearchQuery.update { "" }

    fun toggleFavorite(currencyCode: String, isFavorite: Boolean) =
        viewModelScope.launch { useCases.getToggleFavoriteUseCase(currencyCode, isFavorite) }

}

data class CurrencyListUiState(
    val loading: Boolean = false,
    val convertedRates: List<ExchangeRateEntity> = emptyList(),
    val errorMessage: Event<String?> = Event(null),
    val amount: Double = 1.0,
    val baseCurrency: String = "USD"
)

