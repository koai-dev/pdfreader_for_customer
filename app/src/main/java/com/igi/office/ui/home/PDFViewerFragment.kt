package com.igi.office.ui.home

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.aspose.words.Document
import com.aspose.words.DocumentBuilder
import com.aspose.words.License
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.slider.Slider
import com.igi.office.R
import com.igi.office.common.AppKeys
import com.igi.office.common.Logger
import com.igi.office.common.listenClickViews
import com.igi.office.common.visible
import com.igi.office.databinding.FragmentPdfViewerBinding
import com.igi.office.ui.base.BaseFragment
import com.igi.office.ui.home.model.MyFilesModel
import java.io.*
import java.util.*


/**
 * Created by Thuytv on 10/06/2022.
 */
class PDFViewerFragment : BaseFragment<FragmentPdfViewerBinding>(), View.OnClickListener {
    private val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val outputPDF = File(storageDir, "Converted_PDF.pdf")
    private var myFileModel: MyFilesModel? = null
    private var isFirst = true

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPdfViewerBinding
        get() = FragmentPdfViewerBinding::inflate

    override fun initData() {
        myFileModel = arguments?.getParcelable(AppKeys.KEY_BUNDLE_DATA)

        myFileModel?.apply {
            binding.ttToolbarPdf.text = name
            if (extensionName?.lowercase() == "pdf") {
                openFDPFile(uriPath)
            } else if (extensionName?.lowercase() == "doc" || extensionName?.lowercase() == "docx") {
                Handler(Looper.myLooper()!!).postDelayed({
                    if (!outputPDF.exists()) {
                        outputPDF.mkdirs()
                    }
                    convertDocToPdf(uriPath)
                }, 100)
            }

//            viewPowpointFile(this)

        }
    }

    private fun convertDocToPdf(uriPath: String?) {
        try {
//            val license = License()
//            val inputs: InputStream = resources.openRawResource(R.raw.license)
//            license.setLicense(inputs)
            activity?.contentResolver!!.openInputStream(Uri.fromFile(File(uriPath))).use { inputStream ->
                val doc = Document(inputStream)
//                val bookmarks = doc.range.bookmarks
//                val builder = DocumentBuilder(doc)
//                while (bookmarks.count > 0) {
//                    for (b in bookmarks) {
//                       Logger.showLog("bookmark: " + b.getName())
//                        b.setText("")
//                        builder.moveToBookmark(b.getName())
//                        builder.currentParagraph.remove()
//                    }
//                }
                // save DOCX as PDF
                doc.save(outputPDF.path)
                viewPDFFile()
            }

//            val os = FileOutputStream(outputPDF)
//            val doc = Document(uriPath)
//            doc.save(os, com.aspose.words.SaveFormat.PDF)
//            os.close()
//            viewPDFFile()
//            val press = Presentation(document.path)
////            val fileOS = FileOutputStream(outputPDF)
//            press.save(outputPDF.path, SaveFormat.Pdf)
////            fileOS.close()
//            viewPDFFile()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(activity, "File not found: " + e.message, Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun viewPDFFile() {
        binding.pdfViewer.fromFile(File(outputPDF.path)).defaultPage(0)
            .spacing(10)
            .enableSwipe(true)
            .enableDoubletap(true)
            .swipeHorizontal(false)
            .onPageChange(onPageChangeListener)
            .load()
    }

    override fun initEvents() {
        listenClickViews(binding.imvPdfBack, binding.imvPdfMore)
        binding.seekbarJumpToPage.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                val page = binding.seekbarJumpToPage.value.toInt()
                binding.pdfViewer.jumpTo(page-1)
                binding.vlJumpPage.text = getString(R.string.vl_page, page)
            }
        })
//        binding.seekbarJumpToPage.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            private var timer = Timer()
//            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
//                Logger.showLog("Thuytv----onProgressChanged : $progress")
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {
//                Logger.showLog("Thuytv----onStartTrackingTouch ")
//            }
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {
//                binding.pdfViewer.jumpTo(binding.seekbarJumpToPage.progress)
//                Logger.showLog("Thuytv----onStopTrackingTouch:  " + binding.seekbarJumpToPage.progress)
//            }
//
//        })
    }

    private fun openFDPFile(uriPath: String?) {
//        val scrollHandle = DefaultScrollHandle(context, false)
//        scrollHandle.show()
        uriPath?.apply {
            binding.pdfViewer.fromFile(File(uriPath)).defaultPage(0)
                .spacing(10)
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeHorizontal(false)
                .onPageChange(onPageChangeListener)
                .pageFitPolicy(FitPolicy.WIDTH)
//            .scrollHandle(scrollHandle)
                .load()
        }
//        OnPageChangeListener
//    binding.pdfView.jumpTo(1)
    }

    private val onPageChangeListener = OnPageChangeListener { page, pageCount ->
        if (binding.groupPageViewer.visibility == View.GONE) {
            binding.groupPageViewer.visible()
        }
        binding.vlPageAndTotalPage.text = getString(R.string.vl_total_page, page + 1, pageCount)
        binding.vlJumpPage.text = getString(R.string.vl_page, page + 1)
        binding.seekbarJumpToPage.value = (page + 1).toFloat()
        if (isFirst) {
            binding.seekbarJumpToPage.valueTo = pageCount.toFloat()
            binding.vlTotalPage.text = "/ $pageCount"
            isFirst = false
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imvPdfBack -> {
                getBaseActivity()?.finish()
            }
            R.id.imvPdfMore -> {

            }
        }
    }
}