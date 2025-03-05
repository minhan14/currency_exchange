package com.chicohan.currencyexchange.ui.adaptar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chicohan.currencyexchange.data.db.entity.ExchangeRateEntity
import com.chicohan.currencyexchange.databinding.ItemCurrencyBinding

class MyListAdapter(private val onMoreClickCallback: ((item: ExchangeRateEntity) -> Unit)? = null) :
    ListAdapter<ExchangeRateEntity, MyListAdapter.MyListItemViewHolder>(
        ListDiffCallBack()
    ) {
    class ListDiffCallBack : DiffUtil.ItemCallback<ExchangeRateEntity>() {
        override fun areItemsTheSame(oldItem: ExchangeRateEntity, newItem: ExchangeRateEntity): Boolean {
            return oldItem.currency == newItem.currency
        }
        override fun areContentsTheSame(oldItem: ExchangeRateEntity, newItem: ExchangeRateEntity): Boolean {
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
            tvCurrencyCode.text = currencyItm.currency
            tvExchangeRate.text = currencyItm.rate.toString()
//            Glide.with(this.root.context).load(currencyItm.imageUri)
//                .into(ivCountryFlag)
        }
    }

}