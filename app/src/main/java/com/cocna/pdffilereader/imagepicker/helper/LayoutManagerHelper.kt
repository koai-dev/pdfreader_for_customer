package com.cocna.pdffilereader.imagepicker.helper

import android.content.Context
import android.content.res.Configuration
import androidx.recyclerview.widget.GridLayoutManager
import com.cocna.pdffilereader.imagepicker.model.GridCount

object LayoutManagerHelper {

    fun newInstance(context: Context, gridCount: GridCount): GridLayoutManager {
        val spanCount = getSpanCountForCurrentConfiguration(context, gridCount)
        return GridLayoutManager(context, spanCount)
    }

    fun getSpanCountForCurrentConfiguration(context: Context, gridCount: GridCount): Int {
        val isPortrait =
            context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        return if (isPortrait) gridCount.portrait else gridCount.landscape
    }
}