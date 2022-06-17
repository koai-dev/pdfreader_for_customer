package com.igi.office.ui.home.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.igi.office.R
import com.igi.office.common.*
import com.igi.office.databinding.DialogDeleteFileBinding
import com.igi.office.databinding.DialogRenameFileBinding
import com.igi.office.myinterface.OnDialogItemClickListener
import com.igi.office.ui.home.model.MyFilesModel
import java.io.File

/**
 * Created by Thuytv on 13/06/2022.
 */
class RenameFileDialog(
    val mContext: Context, private val myFileModel: MyFilesModel,
    private val onDialogItemClickListener: OnDialogItemClickListener
) : Dialog(mContext, R.style.AlertDialogStyle) {
    private var oldName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent)))

        val bind: DialogRenameFileBinding = DialogRenameFileBinding.inflate(LayoutInflater.from(context))
        setContentView(bind.root)
        val strExtension = "." + myFileModel.extensionName
        oldName = myFileModel.name
        bind.edtRenameFile.setText(myFileModel.name?.replace(strExtension, ""))

        bind.btnConfirm.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            val strName = bind.edtRenameFile.text?.toString() ?: ""
            if (strName.isNotEmpty()) {
                bind.vlError.invisible()
                val isRenameFile = renameFile(strName + strExtension)
                if (isRenameFile) {
                    myFileModel.name = strName + strExtension
                    val mUrilOldPath = myFileModel.uriPath
                    myFileModel.uriOldPath = mUrilOldPath
                    myFileModel.uriPath = mUrilOldPath?.replace(oldName ?: "", strName + strExtension)
                    myFileModel.isRename = true
                    Logger.showLog("Thuytv------RenameDialog: " + Gson().toJson(myFileModel))
                    onDialogItemClickListener.onClickItemConfirm(mData = myFileModel)
                    RxBus.publish(myFileModel)
                    dismiss()
                } else {
                    bind.vlError.visible()
                    bind.vlError.text = mContext.getString(R.string.msg_rename_file_failed)
                }
            } else {
                bind.vlError.visible()
                bind.vlError.text = mContext.getString(R.string.msg_rename_file_empty)
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

    private fun renameFile(strName: String): Boolean {
        if (myFileModel.uriPath?.isNotEmpty() == true) {
            val mDocumentFile = DocumentFile.fromFile(File(myFileModel.uriPath))
            return mDocumentFile.renameTo(strName)
        }
        return false
    }

}