package com.chicohan.currencyexchange.ui.adaptar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.databinding.ItemCurrencyBinding
import java.text.DecimalFormat

class CurrencyRatesAdapter(
    private val glide: RequestManager,
    private val onMoreClickCallback: ((item: ExchangeRateEntity) -> Unit)? = null,
    private val onLongClick: ((item: ExchangeRateEntity) -> Boolean)
) :
    ListAdapter<ExchangeRateEntity, CurrencyRatesAdapter.MyListItemViewHolder>(
        ListDiffCallBack()
    ) {
    private val formatter = DecimalFormat("#,###.######")

    class ListDiffCallBack : DiffUtil.ItemCallback<ExchangeRateEntity>() {
        override fun areItemsTheSame(
            oldItem: ExchangeRateEntity,
            newItem: ExchangeRateEntity
        ): Boolean {
            return oldItem.currency == newItem.currency
        }

        override fun areContentsTheSame(
            oldItem: ExchangeRateEntity,
            newItem: ExchangeRateEntity
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class MyListItemViewHolder(val binding: ItemCurrencyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyListItemViewHolder {
        val binding = ItemCurrencyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyListItemViewHolder, position: Int) {
        with(holder.binding) {
            val currencyItm = getItem(position)
            root.setOnClickListener {
                onMoreClickCallback?.invoke(currencyItm)
            }
            root.setOnLongClickListener {
                onLongClick.invoke(currencyItm)
            }
            tvCurrencyCode.text = currencyItm.currency
            tvExchangeRate.text = formatter.format(currencyItm.rate)
            glide.load(currencyItm.flagUrl).into(ivCountryFlag)
        }
    }

}