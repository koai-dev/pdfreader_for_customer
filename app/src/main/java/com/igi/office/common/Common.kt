package com.igi.office.common

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Thuytv on 12/06/2022.
 */
object Common {
    @JvmStatic
    fun covertTimeLongToString(time: Long?): String {
        time?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy     HH:mm", Locale.getDefault())
            return sdf.format(Date(time))
        }
        return ""
    }
    @JvmStatic
    fun covertTimeLongToStringGrid(time: Long?): String {
        time?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(time))
        }
        return ""
    }
}