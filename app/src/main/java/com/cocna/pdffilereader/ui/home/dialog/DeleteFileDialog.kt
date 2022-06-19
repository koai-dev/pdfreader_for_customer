package com.cocna.pdffilereader.ui.home.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.documentfile.provider.DocumentFile
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.common.RxBus
import com.cocna.pdffilereader.databinding.DialogDeleteFileBinding
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
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
                myFileModel.isDelete = true
                onDialogItemClickListener.onClickItemConfirm(myFileModel)
                RxBus.publish(myFileModel)
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