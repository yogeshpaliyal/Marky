package com.yogeshpaliyal.marky.extension

import android.view.View
import androidx.databinding.BindingAdapter


@BindingAdapter(value = ["isVisible"])
fun View.isVisible(show: Boolean){
    visibility = if(show) View.VISIBLE else View.GONE
}