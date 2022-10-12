package com.cocna.pdffilereader.imagepicker.ui.camera

import com.cocna.pdffilereader.imagepicker.model.Image


interface OnImageReadyListener {
    fun onImageReady(images: ArrayList<Image>)
    fun onImageNotReady()
}