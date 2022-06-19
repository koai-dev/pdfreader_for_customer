package com.cocna.pdffilereader.myinterface

import com.cocna.pdffilereader.ui.home.model.MyFilesModel

interface OnDialogItemClickListener {
    fun onClickItemConfirm(mData: MyFilesModel)
}