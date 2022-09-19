package com.cocna.pdffilereader.imagepicker.listener

import com.cocna.pdffilereader.imagepicker.model.Image

interface OnImageSelectListener {
    fun onSelectedImagesChanged(selectedImages: ArrayList<Image>)
    fun onSingleModeImageSelected(image: Image)
}