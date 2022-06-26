package com.cocna.pdffilereader.ui.home.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.documentfile.provider.DocumentFile
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.common.RxBus
import com.cocna.pdffilereader.databinding.DialogDeleteFileBinding
import com.cocna.pdffilereader.databinding.DialogProgressBarBinding
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import java.io.File

/**
 * Created by Thuytv on 13/06/2022.
 */
class ProgressDialog(mContext: Context) : Dialog(mContext, R.style.AlertDialogStyle) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent, context.theme)))
        } else {
            window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent)))
        }
        val bind: DialogProgressBarBinding = DialogProgressBarBinding.inflate(LayoutInflater.from(context))
        setContentView(bind.root)
        setCancelable(true)
    }
    fun dismissDialog(){
        dismiss()
    }
}