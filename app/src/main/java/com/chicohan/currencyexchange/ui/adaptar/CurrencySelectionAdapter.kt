package com.chicohan.currencyexchange.ui.adaptar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.data.db.entity.SupportedCurrencies
import com.chicohan.currencyexchange.databinding.ItemCurrencySelectionBinding

class CurrencySelectionAdapter(
    private val glide: RequestManager,
    private val onItemClick: (SupportedCurrencies) -> Unit
) : ListAdapter<SupportedCurrencies, CurrencySelectionAdapter.CurrencyViewHolder>(
    CurrencyDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val binding = ItemCurrencySelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CurrencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        with(holder.binding) {
            val currency = getItem(position)
            root.setOnClickListener {
                onItemClick.invoke(currency)
            }
            tvCurrencyCode.text = currency.currencyCode
            tvCurrencyName.text = currency.currencyName
            glide.load(currency.flag).into(ivFlag)
        }
    }
    inner class CurrencyViewHolder(
        val binding: ItemCurrencySelectionBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class CurrencyDiffCallback : DiffUtil.ItemCallback<SupportedCurrencies>() {
        override fun areItemsTheSame(
            oldItem: SupportedCurrencies,
            newItem: SupportedCurrencies
        ): Boolean = oldItem.currencyCode == newItem.currencyCode

        override fun areContentsTheSame(
            oldItem: SupportedCurrencies,
            newItem: SupportedCurrencies
        ): Boolean = oldItem == newItem

    }

}
