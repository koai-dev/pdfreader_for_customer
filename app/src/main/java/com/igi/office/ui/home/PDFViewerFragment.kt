package com.igi.office.ui.home

import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aspose.slides.Presentation
import com.aspose.slides.SaveFormat
import com.aspose.words.Document
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.igi.office.R
import com.igi.office.common.AppKeys
import com.igi.office.common.listenClickViews
import com.igi.office.databinding.FragmentPdfViewerBinding
import com.igi.office.ui.base.BaseFragment
import com.igi.office.ui.home.model.MyFilesModel
import java.io.*


/**
 * Created by Thuytv on 10/06/2022.
 */
class PDFViewerFragment : BaseFragment<FragmentPdfViewerBinding>(), View.OnClickListener {
    private val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val outputPDF = File(storageDir, "Converted_PDF.pdf")
    private var myFileModel: MyFilesModel? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPdfViewerBinding
        get() = FragmentPdfViewerBinding::inflate

    override fun initData() {
        myFileModel = arguments?.getParcelable(AppKeys.KEY_BUNDLE_DATA)

        myFileModel?.apply {
            binding.ttToolbarPdf.text = name
            if (extensionName?.lowercase() == "pdf") {
                openFDPFile(uri)
            } else if (extensionName?.lowercase() == "doc" || extensionName?.lowercase() == "docx") {
                Handler(Looper.myLooper()!!).postDelayed({
                    if (!outputPDF.exists()) {
                        outputPDF.mkdirs()
                    }
                    convertDocToPdf(uri!!)
                }, 100)
            }

//            viewPowpointFile(this)

        }
    }

    private fun convertDocToPdf(document: Uri) {
        try {
            activity?.contentResolver!!.openInputStream(document).use { inputStream ->
                val doc = Document(inputStream)
                // save DOCX as PDF
                doc.save(outputPDF.path)
                viewPDFFile()
            }
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
//            .scrollHandle(scrollHandle)
            .load()
    }

    private fun viewPowpointFile(uri: Uri) {
//        binding.pptViewer.loadPPT(activity,uri.path )
    }

    override fun initEvents() {
        listenClickViews(binding.imvPdfBack, binding.imvPdfMore)
    }

    private fun openFDPFile(uri: Uri?) {
//        val scrollHandle = DefaultScrollHandle(context, false)
//        scrollHandle.show()
        binding.pdfViewer.fromUri(uri).defaultPage(0)
            .spacing(10)
            .enableSwipe(true)
            .enableDoubletap(true)
            .swipeHorizontal(false)
            .onPageChange(onPageChangeListener)
            .pageFitPolicy(FitPolicy.WIDTH)
//            .scrollHandle(scrollHandle)
            .load()
//        OnPageChangeListener
//    binding.pdfView.jumpTo(1)
    }

    private val onPageChangeListener = object : OnPageChangeListener {
        override fun onPageChanged(page: Int, pageCount: Int) {
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