package com.chicohan.currencyexchange.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.chicohan.currencyexchange.R
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.databinding.FragmentCurrencyBinding
import com.chicohan.currencyexchange.helper.collectFlowWithLifeCycleAtStateStart
import com.chicohan.currencyexchange.helper.createGenericAlertDialog
import com.chicohan.currencyexchange.helper.toast
import com.chicohan.currencyexchange.helper.visible
import com.chicohan.currencyexchange.ui.adaptar.MyListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class CurrencyFragment : Fragment(R.layout.fragment_currency) {

    lateinit var binding: FragmentCurrencyBinding
        private set

    private val currencyViewModel by activityViewModels<CurrencyViewModel>()

    @Inject
    lateinit var glide: RequestManager

    private val myListAdapter by lazy {
        MyListAdapter(glide) { item: ExchangeRateEntity ->
            print(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCurrencyBinding.bind(view)
        initViews()
        collectFlowWithLifeCycleAtStateStart(currencyViewModel.uiState) {
            handleCurrencyListUiState(it)
        }
    }

    private fun initViews() = with(binding) {
        rvCurrency.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myListAdapter
        }

        binding.edEnterAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val amount = s?.toString()?.toDoubleOrNull() ?: 1.0
                currencyViewModel.updateAmount(amount)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun handleCurrencyListUiState(state: CurrencyListUiState) = with(state) {
        with(binding) {
            progressBar.visible(loading)
            println(loading)
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
//        with(binding) {
//            loading.getContentIfNotHandled()?.let {
//                Log.d("Currency Fragment", "loading")
//            }
//            errorMessage.getContentIfNotHandled()?.let {
//                requireContext().toast(it)
//                Log.d("Currency Fragment", it)
//            }
//            isSuccess.getContentIfNotHandled()?.let { cList ->
//                Log.d("Currency Fragment", cList.toString())
//                myListAdapter.submitList(cList)
//            }
//        }
    }
}

// initSearch()
// Collect amount to update EditText
//        collectFlowWithLifeCycleAtStateStart(currencyViewModel.amount) { amount ->
//            Log.d("CurrencyFragment", amount.toString())
//            if (binding.edEnterAmount.text.toString().toDoubleOrNull() != amount) {
//                binding.edEnterAmount.setText(amount.toString())
//            }
//        }

//        collectFlowWithLifeCycleAtStateStart(currencyViewModel.convertedRates) {
//            withContext(Dispatchers.IO){
//                myListAdapter.submitList(it)
//            }
//        }
//        collectFlowWithLifeCycleAtStateStart(currencyViewModel.currencyListState) {
//            handleCurrencyListUiState(it)
//        }
//        collectFlowWithLifeCycleAtStateStart(currencyViewModel.state) {
//            when (it) {
//                is Resource.Error -> requireContext().toast(it.message)
//                is Resource.Loading -> print("loading")
//                is Resource.Success -> Unit
//            }
//        }
//    private fun initSearch() = with(binding.edEnterAmount) {
//        setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_GO) {
//                updatedAmountFromInput()
//                true
//            } else {
//                false
//            }
//        }
//        setOnKeyListener { _, keyCode, event ->
//            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
//                updatedAmountFromInput()
//                true
//            } else {
//                false
//            }
//        }
//    }