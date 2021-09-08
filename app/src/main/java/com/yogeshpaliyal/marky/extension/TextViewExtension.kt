package com.yogeshpaliyal.marky.extension

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.yogeshpaliyal.marky.MarkedView


@BindingAdapter("markdown")
fun MarkedView.renderMarkdown(text: CharSequence){
    setMDText(text)
}