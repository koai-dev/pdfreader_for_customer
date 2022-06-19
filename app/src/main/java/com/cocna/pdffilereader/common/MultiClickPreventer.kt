package com.cocna.pdffilereader.common

import android.view.View

object MultiClickPreventer {
    private val DELAY_IN_MS: Long = 1000

    fun preventMultiClick(view: View?) {
        if (view?.isClickable == false) {
            return
        }
        view?.isClickable = false
        view?.postDelayed({ view.isClickable = true }, DELAY_IN_MS)
    }
}