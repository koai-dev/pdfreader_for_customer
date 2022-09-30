package com.cocna.pdffilereader.ui.home.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Window
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.DialogRenameFileBinding
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import java.io.File

/**
 * Created by Thuytv on 13/06/2022.
 */
class SaveFilePdfDialog(
    val mContext: Context,
    private val onDialogItemClickListener: OnDialogItemClickListener
) : Dialog(mContext, R.style.AlertDialogStyle) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent, context.theme)))
        } else {
            window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent)))
        }
        val bind: DialogRenameFileBinding = DialogRenameFileBinding.inflate(LayoutInflater.from(context))
        setContentView(bind.root)

        bind.ttDialogFile.text = mContext.getString(R.string.tt_save_file)
        bind.btnConfirm.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            val strName = bind.edtRenameFile.text?.toString()
            if (strName.isNullOrEmpty()) {
                bind.vlError.visible()
                bind.vlError.text = context.getString(R.string.msg_rename_file_empty)
            } else {
                bind.vlError.invisible()
                onDialogItemClickListener.onClickItemConfirm(MyFilesModel(name = strName))
                dismiss()
            }
        }
        bind.btnCancel.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            dismiss()
        }

        bind.edtRenameFile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(data: Editable?) {
                if (data?.toString().isNullOrEmpty()) {
                    bind.vlError.visible()
                    bind.vlError.text = context.getString(R.string.msg_rename_file_empty)
                } else {
                    bind.vlError.invisible()
                }
            }

        })
    }
}