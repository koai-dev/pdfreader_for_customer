package com.cocna.pdffilereader.ui.home.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.databinding.DialogDeleteFileBinding
import com.cocna.pdffilereader.myinterface.OnUpdateVersionClickListener

/**
 * Created by Thuytv on 13/06/2022.
 */
class UpdateVersionDialog(
    private val mContext: Context,
    private val onDialogItemClickListener: OnUpdateVersionClickListener
) : Dialog(mContext, R.style.AlertDialogStyle) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent, context.theme)))
        } else {
            window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(android.R.color.transparent)))
        }
//        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val bind: DialogDeleteFileBinding = DialogDeleteFileBinding.inflate(LayoutInflater.from(context))
        setContentView(bind.root)

        bind.ttDialog.text = mContext.getString(R.string.tt_update_version)
        bind.vlContentDialog.text = mContext.getString(R.string.vl_contetn_update_version)
        bind.btnYes.text = mContext.getString(R.string.btn_update_now)
        bind.btnCancel.text = mContext.getString(R.string.btn_later)

        bind.btnYes.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            onDialogItemClickListener.onClickButtonDialog(true)
            dismiss()
        }
        bind.btnCancel.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            onDialogItemClickListener.onClickButtonDialog(false)
            dismiss()
        }
    }

}