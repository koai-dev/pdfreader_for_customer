package com.cocna.pdffilereader.ui.home.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Common
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.databinding.DialogFileInfoBinding
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Thuytv on 13/06/2022.
 */
class FileInfoDialog(
    private val mContext: Context, private val myFileModel: MyFilesModel
) : Dialog(mContext, R.style.AlertDialogStyle) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.setBackgroundDrawable(ColorDrawable(mContext.resources.getColor(android.R.color.transparent, mContext.theme)))
        } else {
            window?.setBackgroundDrawable(ColorDrawable(mContext.resources.getColor(android.R.color.transparent)))
        }

        val binding: DialogFileInfoBinding = DialogFileInfoBinding.inflate(LayoutInflater.from(mContext))
        setContentView(binding.root)

        binding.imvCloseInfo.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            dismiss()
        }
        myFileModel.apply {
            binding.vlInfoFileName.text = name
            binding.vlInfoSize.text = Common.convertByteToString(this.length)
            binding.vlInfoLastModified.text = convertLongToTime(this.lastModified)
            binding.vlInfoLocation.text = locationFile
        }

    }



    private fun convertLongToTime(time: Long?): String {
        if (time == null) return ""
        val date = Date(time)
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        return format.format(date)
    }
}