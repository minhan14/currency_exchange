package com.chicohan.currencyexchange.helper

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chicohan.currencyexchange.data.model.CurrencyInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.createGenericAlertDialog(
    title: String,
    message: String,
    positiveButtonText: String,
    negativeButtonText: String,
    callback: (Boolean) -> Unit
) {
    val builder = AlertDialog.Builder(this)
    builder.apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(positiveButtonText) { dialog, _ ->
            dialog.dismiss()
            callback(true)

        }
        setNegativeButton(negativeButtonText) { dialog, _ ->
            dialog.dismiss()
            callback(false)
        }
    }

    val alertDialog = builder.create()
    alertDialog.show()
}

/**
 * etx function to collect flow in different states
 */

fun <T> Fragment.collectFlowWithLifeCycleAtStateStart(flow: Flow<T>, collect: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collect)
        }
    }
}

fun <T> Fragment.collectFlowWithLifeCycleAtStateResume(
    flow: Flow<T>,
    collect: suspend (T) -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            flow.collect(collect)
        }
    }
}

fun loadCurrencyMappings(context: Context): List<CurrencyInfo> {
    val jsonString =
        context.assets.open("currencies-with-flags.json").bufferedReader().use { it.readText() }
    val listType = object : TypeToken<List<CurrencyInfo>>() {}.type
    return Gson().fromJson(jsonString, listType)
}