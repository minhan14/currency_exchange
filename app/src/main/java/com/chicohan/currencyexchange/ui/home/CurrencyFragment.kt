package com.chicohan.currencyexchange.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.chicohan.currencyexchange.R
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.data.model.DetailFragmentModel
import com.chicohan.currencyexchange.databinding.FragmentCurrencyBinding
import com.chicohan.currencyexchange.domain.model.UIState
import com.chicohan.currencyexchange.helper.collectFlowWithLifeCycleAtStateStart
import com.chicohan.currencyexchange.helper.createGenericAlertDialog
import com.chicohan.currencyexchange.helper.invisible
import com.chicohan.currencyexchange.helper.toast
import com.chicohan.currencyexchange.helper.visible
import com.chicohan.currencyexchange.ui.adaptar.MyListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class CurrencyFragment : Fragment(R.layout.fragment_currency) {

    companion object {
        private const val DIALOG_CURRENCY_TAG = "dialogCurrency"
        private const val DIALOG_ADD_CURRENCY_TAG = "dialogAddCurrency"
    }

    lateinit var binding: FragmentCurrencyBinding
        private set

    private val currencyViewModel by activityViewModels<CurrencyViewModel>()

    @Inject
    lateinit var glide: RequestManager

    private val myListAdapter by lazy {
        MyListAdapter(glide) { item: ExchangeRateEntity ->
            print(item)
            handleNavigationToDetail(item)
        }
    }

    private val dialogSupportedCurrencies by lazy {
        DialogSupportedCurrencies(mode = CurrencyDialogMode.SELECT_CURRENCY)
    }
    private val dialogAddCurrencies by lazy {
        DialogSupportedCurrencies(mode = CurrencyDialogMode.ADD_CURRENCY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCurrencyBinding.bind(view)
        initViews()
        collectFlowWithLifeCycleAtStateStart(currencyViewModel.uiState) {
            handleCurrencyListUiState(it)
        }
        collectFlowWithLifeCycleAtStateStart(currencyViewModel.selectedSupportedCurrency) {
            handleSelectedCurrencyState(it)
        }
    }

    private fun initViews() = with(binding) {
        rvCurrency.apply {
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
            itemAnimator = null
            adapter = myListAdapter
        }

        edEnterAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val amount = s?.toString()?.toDoubleOrNull() ?: 1.0
                currencyViewModel.updateAmount(amount)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        btnCurrency.setOnClickListener {
            showOrReplaceDialog()
        }
        btnAddCurrency.setOnClickListener {
            showOrReplaceAddCurrencyDialog()
        }

    }

    private fun handleSelectedCurrencyState(state: UIState<SupportedCurrencies>) = with(binding) {
        progressBarCurrencyChange.visible(state is UIState.Loading)
        btnCurrency.invisible(state is UIState.Loading)
        when (state) {
            is UIState.Idle -> Unit
            is UIState.Loading -> Unit //already handled
            is UIState.Success -> {
                glide.load(state.result.flag).into(btnCurrency)
                textViewCurrency.text = state.result.currencyCode
            }

            is UIState.Error -> requireContext().toast(state.errorMessage)
        }
    }

    private fun handleCurrencyListUiState(state: CurrencyListUiState) = with(state) {
        with(binding) {
            progressBar.visible(loading)
            txtEmptyView.visible(convertedRates.isEmpty() && !loading)
            if (edEnterAmount.text.toString().toDoubleOrNull() != amount) {
                edEnterAmount.setText(amount.toString())
            }
            // reserving the one time event
            errorMessage.getContentIfNotHandled()?.let { message ->
                lifecycleScope.launch {
                    requireContext().createGenericAlertDialog(
                        "Error",
                        message,
                        "Retry",
                        "Dismiss"
                    ) {
                        if (it) {
                            currencyViewModel.fetchExchangeRates(true)
                            println("retry")
                        }
                    }
                }
                Log.d("Currency Fragment", message)
            }
            myListAdapter.submitList(convertedRates)
        }
    }

    private fun showOrReplaceDialog() {
        val oldDialog = childFragmentManager.findFragmentByTag(DIALOG_CURRENCY_TAG)
        if (oldDialog == null) {
            dialogSupportedCurrencies.show(childFragmentManager, DIALOG_CURRENCY_TAG)
        } else {
            childFragmentManager.beginTransaction().remove(oldDialog).commit()
        }
    }

    private fun showOrReplaceAddCurrencyDialog() {
        val oldDialog = childFragmentManager.findFragmentByTag(DIALOG_ADD_CURRENCY_TAG)
        if (oldDialog == null) {
            dialogAddCurrencies.show(childFragmentManager, DIALOG_ADD_CURRENCY_TAG)
        } else {
            childFragmentManager.beginTransaction().remove(oldDialog).commit()
        }
    }

    private fun handleNavigationToDetail(item: ExchangeRateEntity) {
        val currencyState = currencyViewModel.selectedSupportedCurrency.value
        if (currencyState is UIState.Success) {
            val detailModel = DetailFragmentModel(
                currentCurrency = currencyState.result.currencyName,
                currentCurrencyAmount = currencyViewModel.amount.value,
                calculatedCurrency = item.currencyName,
                calculatedCurrencyAmount = item.rate
            )
            val action =
                CurrencyFragmentDirections.actionCurrencyFragmentToDetailFragment(detailModel)
            findNavController().navigate(action)
        } else {
            requireContext().toast("Please wait")
        }
    }
}