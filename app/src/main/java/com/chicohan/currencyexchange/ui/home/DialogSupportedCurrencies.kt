
package com.chicohan.currencyexchange.ui.home
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.chicohan.currencyexchange.R
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.databinding.DialogCurrencySelectionBinding
import com.chicohan.currencyexchange.helper.collectFlowWithLifeCycleAtStateStart
import com.chicohan.currencyexchange.ui.adaptar.CurrencySelectionAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DialogSupportedCurrencies(private val mode: CurrencyDialogMode) : DialogFragment() {

    lateinit var binding: DialogCurrencySelectionBinding
        private set

    @Inject
    lateinit var glide: RequestManager

    private val currencyViewModel by activityViewModels<CurrencyViewModel>()

    private val currencyAdapter by lazy {
        CurrencySelectionAdapter(glide) { item: SupportedCurrencies ->
            handleCurrencySelection(item)
            print(item)
        }
    }

    private fun setupDialogSize() {
        val margin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        dialog?.window?.apply {
            decorView.findViewById<View>(R.id.dialog_root)?.let { rootView ->
                (rootView.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                    setMargins(margin, margin, margin, margin)
                    rootView.layoutParams = this
                }
            }
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogCurrencySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialogSize()
        initViews()
        collectFlowWithLifeCycleAtStateStart(currencyViewModel.supportedCurrencies) {
            currencyAdapter.submitList(it)
        }
        collectFlowWithLifeCycleAtStateStart(currencyViewModel.filteredCurrencies) { currencies ->
            currencyAdapter.submitList(currencies)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currencyViewModel.clearSearchQuery()
    }

    private fun initViews() = with(binding) {
        etSearch.setText(currencyViewModel.currencySearchQuery.value)
        when (mode) {
            CurrencyDialogMode.SELECT_CURRENCY -> {
                "Select Base Currency".also { tvTitle.text = it }
            }

            CurrencyDialogMode.ADD_CURRENCY -> {
                "Add Currency".also { tvTitle.text = it }
            }
        }
        rvCurrencies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = currencyAdapter
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currencyViewModel.updateCurrencySearchQuery(s?.toString() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun handleCurrencySelection(currency: SupportedCurrencies) {
        when (mode) {
            CurrencyDialogMode.SELECT_CURRENCY -> {
                currencyViewModel.updateBaseCurrency(currency.currencyCode)
            }

            CurrencyDialogMode.ADD_CURRENCY -> {
                currencyViewModel.toggleFavorite(currency.currencyCode, true)
            }
        }
        dismiss()
    }
}

enum class CurrencyDialogMode {
    SELECT_CURRENCY,
    ADD_CURRENCY
}

