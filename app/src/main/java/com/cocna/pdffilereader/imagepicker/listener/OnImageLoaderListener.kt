package com.cocna.pdffilereader.imagepicker.listener

import com.cocna.pdffilereader.imagepicker.model.Image

interface OnImageLoaderListener {
    fun onImageLoaded(images: ArrayList<Image>)
    fun onFailed(throwable: Throwable)
}