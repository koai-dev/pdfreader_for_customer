package com.cocna.pdffilereader.ui.home

import android.app.Activity
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.cocna.pdffilereader.common.AppKeys
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.common.PreviewUtils
import com.cocna.pdffilereader.databinding.ActivityPreviewPdfBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.home.adapter.PreviewAdapter
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import java.io.File

/**
 * Created by Thuytv on 03/07/2022.
 */
class PdfPreviewActivity : BaseActivity<ActivityPreviewPdfBinding>() {
    private var pdfiumCore: PdfiumCore? = null
    private var pdfDocument: PdfDocument? = null
    private var myFilesModel: MyFilesModel? = null
    private var currentPage = 1

    override val bindingInflater: (LayoutInflater) -> ActivityPreviewPdfBinding
        get() = ActivityPreviewPdfBinding::inflate

    override fun initData() {
        myFilesModel = intent?.getParcelableExtra(AppKeys.KEY_BUNDLE_DATA)
        currentPage = intent?.getIntExtra(AppKeys.KEY_BUNDLE_ACTION, 1) ?: 1
        myFilesModel?.apply {
            loadDataFile()
        }
    }

    override fun initEvents() {
    }

    private fun loadDataFile() {
        loadPdfFile()
        val totalCount = pdfiumCore?.getPageCount(pdfDocument) ?: 0
        val previewAdapter =
            PreviewAdapter(this, pdfiumCore!!, pdfDocument!!, myFilesModel?.name ?: "", totalCount, currentPage, object : PreviewAdapter.OnItemClickListener {
                override fun onClickItem(position: Int) {
                    Logger.showLog("Thuytv-----PreviewAdapter:  $position")
                    val intent = Intent()
                    intent.putExtra(AppKeys.KEY_BUNDLE_ACTION, position)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            })
        binding.rcvPreviewPdf.apply {
            layoutManager = GridLayoutManager(baseContext, 3)
            adapter = previewAdapter
        }
        binding.rcvPreviewPdf.scrollToPosition(currentPage)

    }

    private fun loadPdfFile() {
        myFilesModel?.uriPath?.apply {
            try {
                val file = File(this)
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfiumCore = PdfiumCore(this@PdfPreviewActivity)
                pdfDocument = pdfiumCore!!.newDocument(pfd)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun recycleMemory() {
        if (pdfiumCore != null && pdfDocument != null) {
            pdfiumCore?.closeDocument(pdfDocument)
            pdfiumCore = null
        }
        PreviewUtils.getInstance().imageCache.clearCache()
    }

    override fun onDestroy() {
        super.onDestroy()
        recycleMemory()
    }
}