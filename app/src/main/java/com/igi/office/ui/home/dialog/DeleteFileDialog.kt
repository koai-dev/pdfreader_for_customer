package com.igi.office.ui.home.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.documentfile.provider.DocumentFile
import com.igi.office.R
import com.igi.office.common.Logger
import com.igi.office.common.MultiClickPreventer
import com.igi.office.databinding.DialogDeleteFileBinding
import com.igi.office.myinterface.OnDialogItemClickListener
import com.igi.office.ui.home.model.MyFilesModel
import java.io.File

/**
 * Created by Thuytv on 13/06/2022.
 */
class DeleteFileDialog(
    private val mContext: Context, private val myFileModel: MyFilesModel,
    private val onDialogItemClickListener: OnDialogItemClickListener
) : Dialog(mContext, R.style.AlertDialogStyle) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent)))
//        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val bind: DialogDeleteFileBinding = DialogDeleteFileBinding.inflate(LayoutInflater.from(context))
        setContentView(bind.root)

        bind.btnYes.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            if (deleteFile()) {
                onDialogItemClickListener.onClickItemConfirm(myFileModel)
            } else {
                Logger.showToast(mContext, mContext.getString(R.string.msg_delete_file_failed))
            }
            dismiss()
        }
        bind.btnCancel.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            dismiss()
        }
    }

    private fun deleteFile(): Boolean {
        if (myFileModel.uriPath?.isNotEmpty() == true) {
            val mDocumentFile = DocumentFile.fromFile(File(myFileModel.uriPath))
            return mDocumentFile.delete()
        }
        return false
    }

}