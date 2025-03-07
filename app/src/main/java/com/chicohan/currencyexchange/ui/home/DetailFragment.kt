package com.chicohan.currencyexchange.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.chicohan.currencyexchange.R
import com.chicohan.currencyexchange.databinding.FragmentDetailBinding


class DetailFragment : Fragment(R.layout.fragment_detail) {
    lateinit var binding: FragmentDetailBinding
        private set

    val args: DetailFragmentArgs by navArgs<DetailFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailBinding.bind(view)
        setUpViews()
    }

    private fun setUpViews() = with(binding) {

        args.detailModel?.let { model ->
            "${model.currentCurrencyAmount} ${model.currentCurrency} =".also {
                currentCurrency.text = it
            }
            "${model.calculatedCurrencyAmount} ${model.calculatedCurrency}".also {
                calculatedCurrency.text = it
            }
            "Convert ${model.currentCurrency} to ${model.calculatedCurrency}".also {
                txtHeader.text = it
            }
        }

    }
}


