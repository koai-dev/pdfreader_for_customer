package com.cocna.pdffilereader.imagepicker.helper

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.cocna.pdffilereader.R

class GlideHelper {

    companion object {
        private val options: RequestOptions =
            RequestOptions().placeholder(R.drawable.imagepicker_image_placeholder)
                .error(R.drawable.imagepicker_image_error)
                .centerCrop()

        fun loadImage(imageView: ImageView, uri: Uri) {
            Glide.with(imageView.context)
                .load(uri)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)

        }
    }
}