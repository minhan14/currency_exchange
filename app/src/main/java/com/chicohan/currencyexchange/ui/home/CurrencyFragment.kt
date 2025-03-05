package com.chicohan.currencyexchange.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chicohan.currencyexchange.R
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.databinding.FragmentCurrencyBinding
import com.chicohan.currencyexchange.helper.collectFlowWithLifeCycleAtStateStart
import com.chicohan.currencyexchange.helper.toast
import com.chicohan.currencyexchange.ui.adaptar.MyListAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CurrencyFragment : Fragment(R.layout.fragment_currency) {

    private lateinit var binding: FragmentCurrencyBinding

    private val currencyViewModel by activityViewModels<CurrencyViewModel>()

    private val myListAdapter by lazy {
        MyListAdapter { item: ExchangeRateEntity ->
            print(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCurrencyBinding.bind(view)
        initAdapter()

        collectFlowWithLifeCycleAtStateStart(currencyViewModel.currencyListState) {
            handleCurrencyListUiState(state = it)
        }

    }

    private fun initAdapter() = with(binding) {
        rvCurrency.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myListAdapter
        }
    }

    private fun handleCurrencyListUiState(state: CurrencyListUiState) = with(state) {
        with(binding) {
            loading.getContentIfNotHandled()?.let {
               Log.d("Currency Fragment","loading")
            }
            errorMessage.getContentIfNotHandled()?.let {
                requireContext().toast(it)
                Log.d("Currency Fragment",it)
            }
            isSuccess.getContentIfNotHandled()?.let { cList ->
                Log.d("Currency Fragment",cList.toString())
                myListAdapter.submitList(cList)
            }
        }
    }
}