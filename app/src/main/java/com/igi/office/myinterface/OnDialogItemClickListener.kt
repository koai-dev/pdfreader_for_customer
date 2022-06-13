package com.igi.office.myinterface

import android.view.MenuItem
import com.igi.office.ui.home.model.MyFilesModel

interface OnDialogItemClickListener {
    fun onClickItemConfirm(mData: MyFilesModel)
}