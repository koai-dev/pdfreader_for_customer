package com.igi.office.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView

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