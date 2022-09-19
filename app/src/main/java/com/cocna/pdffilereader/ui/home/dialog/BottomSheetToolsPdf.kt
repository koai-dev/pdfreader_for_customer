package com.cocna.pdffilereader.ui.home.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.databinding.DialogToolsPdfBinding
import com.cocna.pdffilereader.imagepicker.helper.ToastHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Created by Thuytv on 17/09/2022.
 */
class BottomSheetToolsPdf(private val mContent: Context, private val mOnItemClickToolsPdf: OnItemClickToolsPdf) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bind: DialogToolsPdfBinding = DialogToolsPdfBinding.inflate(LayoutInflater.from(context))
        initDataView(bind)
        return bind.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    private fun initDataView(binding: DialogToolsPdfBinding) {

        binding.imvCloseToolsPdf.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            dismiss()
        }
        binding.imvScanDocument.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            Toast.makeText(mContent, mContent.getString(R.string.msg_coming_soon), Toast.LENGTH_SHORT).show()
        }
        binding.imvImagesToPdf.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            mOnItemClickToolsPdf.onItemClickImageToPdf()
            dismiss()
        }
    }

    interface OnItemClickToolsPdf {
        fun onItemClickImageToPdf()
    }
}