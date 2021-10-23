package com.yogeshpaliyal.marky

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MyApplication: Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .componentRegistry {
                add(SvgDecoder(this@MyApplication))
            }
            .build()
    }
}