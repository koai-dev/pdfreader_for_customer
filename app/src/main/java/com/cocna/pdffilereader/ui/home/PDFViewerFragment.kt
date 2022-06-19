package com.cocna.pdffilereader.ui.home

import android.annotation.SuppressLint
import android.os.Environment
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.cocna.pdffilereader.R
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.gms.ads.AdRequest
import com.google.android.material.slider.Slider
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentPdfViewerBinding
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.myinterface.OnPopupMenuItemClickListener
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.home.dialog.DeleteFileDialog
import com.cocna.pdffilereader.ui.home.dialog.RenameFileDialog
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import java.io.*


/**
 * Created by Thuytv on 10/06/2022.
 */
class PDFViewerFragment : BaseFragment<FragmentPdfViewerBinding>(), View.OnClickListener {
    private val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private val outputPDF = File(storageDir, "Converted_PDF.pdf")
    private var myFileModel: MyFilesModel? = null
    private var isFirst = true
    private var isTouchSlider = true

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPdfViewerBinding
        get() = FragmentPdfViewerBinding::inflate

    override fun initData() {
        loadBannerAds()
        myFileModel = arguments?.getParcelable(AppKeys.KEY_BUNDLE_DATA)

        myFileModel?.apply {
            binding.ttToolbarPdf.text = name
            if (extensionName?.lowercase() == "pdf") {
                openFDPFile(uriPath)
            }
        }
    }

//    private fun convertDocToPdf(uriPath: String?) {
//        try {
////            val license = License()
////            val inputs: InputStream = resources.openRawResource(R.raw.license)
////            license.setLicense(inputs)
//            activity?.contentResolver!!.openInputStream(Uri.fromFile(File(uriPath))).use { inputStream ->
//                val doc = Document(inputStream)
////                val bookmarks = doc.range.bookmarks
////                val builder = DocumentBuilder(doc)
////                while (bookmarks.count > 0) {
////                    for (b in bookmarks) {
////                       Logger.showLog("bookmark: " + b.getName())
////                        b.setText("")
////                        builder.moveToBookmark(b.getName())
////                        builder.currentParagraph.remove()
////                    }
////                }
//                // save DOCX as PDF
//                doc.save(outputPDF.path)
//                viewPDFFile()
//            }
//
////            val os = FileOutputStream(outputPDF)
////            val doc = Document(uriPath)
////            doc.save(os, com.aspose.words.SaveFormat.PDF)
////            os.close()
////            viewPDFFile()
////            val press = Presentation(document.path)
//////            val fileOS = FileOutputStream(outputPDF)
////            press.save(outputPDF.path, SaveFormat.Pdf)
//////            fileOS.close()
////            viewPDFFile()
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//            Toast.makeText(activity, "File not found: " + e.message, Toast.LENGTH_LONG).show()
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
//        }
//    }

    override fun initEvents() {
        listenClickViews(binding.imvPdfBack, binding.imvPdfMore)
        binding.seekbarJumpToPage.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
                Logger.showLog("Thuytv-----onStartTrackingTouch")
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                Logger.showLog("Thuytv-----onStopTrackingTouch--isTouchSlider: $isTouchSlider")
//                if (isTouchSlider) {
                val page = binding.seekbarJumpToPage.value.toInt()
                binding.pdfViewer.jumpTo(page - 1)
                binding.vlJumpPage.text = getString(R.string.vl_page, page)
//                } else {
//                    isTouchSlider = true
//                }
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
        binding.vlJumpPage.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            binding.groupPageViewer.gone()
            binding.llJumpToPageEdit.visible()
            getBaseActivity()?.showKeyboard()
            binding.edtJumpPage.requestFocus()
        }
        binding.edtJumpPage.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                Logger.showLog("Thuytv----actionId : $actionId ---action: ${event?.action}---keyCode: ${event?.keyCode}")
                if (actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN
                    || event?.keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    binding.pdfViewer.jumpTo(getEdittextNumber() - 1)
                    binding.groupPageViewer.visible()
                    binding.llJumpToPageEdit.gone()
                    getBaseActivity()?.hideKeyboard()
                    return true
                }
                return false
            }

        })
    }

    private fun getEdittextNumber(): Int {
        if (binding.edtJumpPage.text.toString().isNullOrEmpty()) return 0
        return binding.edtJumpPage.text.toString().toInt()
    }

    private fun openFDPFile(uriPath: String?) {
//        val scrollHandle = DefaultScrollHandle(context, false)
//        scrollHandle.show()
        uriPath?.apply {
            binding.pdfViewer.fromFile(File(uriPath))
                .spacing(10)
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeHorizontal(false)
                .fitEachPage(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .onPageChange(onPageChangeListener)
                .onPageScroll(onPageScrollListener)
                .pageFitPolicy(FitPolicy.WIDTH)
                .scrollHandle(MyScrollHandle(context, false))
                .onTap(onTapListener)
                .load()
        }
//        OnPageChangeListener
//    binding.pdfView.jumpTo(1)
    }

    private val onPageScrollListener = OnPageScrollListener { page, positionOffset -> }

    private val onPageChangeListener = OnPageChangeListener { page, pageCount ->
        if (binding.groupPageViewer.visibility == View.GONE) {
            binding.groupPageViewer.visible()
        }
        val mPage = page + 1
        binding.vlPageAndTotalPage.text = getString(R.string.vl_total_page, mPage, pageCount)
        binding.vlJumpPage.text = getString(R.string.vl_page, mPage)
        binding.edtJumpPage.setText(mPage.toString())
        isTouchSlider = false
        binding.seekbarJumpToPage.value = mPage.toFloat()
        if (isFirst) {
            binding.seekbarJumpToPage.valueTo = pageCount.toFloat()
            binding.vlTotalPage.text = "/ $pageCount"
            binding.vlJumpTotalPageEdit.text = "/ $pageCount"
            isFirst = false
        }
    }
    private val onTapListener = OnTapListener {
        if (binding.llToolbarPdf.visibility == View.GONE) {
            binding.llToolbarPdf.visible()
            binding.groupPageViewer.visible()
        } else {
            binding.llToolbarPdf.gone()
            binding.groupPageViewer.gone()
        }
        true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imvPdfBack -> {
                getBaseActivity()?.finish()
            }
            R.id.imvPdfMore -> {
                showPopupMenu(binding.imvPdfMore, R.menu.menu_more_detail_file, object : OnPopupMenuItemClickListener {
                    override fun onClickItemPopupMenu(menuItem: MenuItem?) {
                        when (menuItem?.itemId) {
                            R.id.menu_rename -> {
                                getBaseActivity()?.apply {
                                    RenameFileDialog(this, myFileModel!!, object : OnDialogItemClickListener {
                                        override fun onClickItemConfirm(mData: MyFilesModel) {
                                            binding.ttToolbarPdf.text = mData.name
//                                            sharedPreferences.updateRecentFile(mData)
//                                            sharedPreferences.updateFavoriteFile(mData)
//                                            if (mData.extensionName?.lowercase() == "pdf") {
//                                                RxBus.publish(EventsBus.RELOAD_PDF_FILE)
//                                            } else if (mData.extensionName?.lowercase() == "docx" || mData.extensionName?.lowercase() == "doc") {
//                                                RxBus.publish(EventsBus.RELOAD_WORD_FILE)
//                                            } else if (mData.extensionName?.lowercase() == "xlsx" || mData.extensionName?.lowercase() == "xls") {
//                                                RxBus.publish(EventsBus.RELOAD_EXCEL_FILE)
//                                            } else if (mData.extensionName?.lowercase() == "pptx" || mData.extensionName?.lowercase() == "ppt") {
//                                                RxBus.publish(EventsBus.RELOAD_POWER_POINT_FILE)
//                                            }
                                        }

                                    }).show()
                                }
                            }
                            R.id.menu_favorite -> {
                                getBaseActivity()?.sharedPreferences?.setFavoriteFile(myFileModel!!)
                                RxBus.publish(EventsBus.RELOAD_FAVORITE)
                            }
                            R.id.menu_share -> {
                                myFileModel?.uriPath?.let { File(it) }?.let { shareFile(it) }
                            }
                            R.id.menu_delete -> {
                                getBaseActivity()?.apply {
                                    val deleteFileDialog = DeleteFileDialog(this, myFileModel!!, object : OnDialogItemClickListener {
                                        override fun onClickItemConfirm(mData: MyFilesModel) {
//                                            sharedPreferences.removeFavoriteFile(myFileModel!!)
//                                            sharedPreferences.removeRecentFile(myFileModel!!)
//                                            RxBus.publish(EventsBus.RELOAD_ALL_FILE)
//                                            RxBus.publish(myFileModel!!)
                                            finish()
                                        }

                                    })
                                    deleteFileDialog.show()
                                }
                            }
                            R.id.menu_print -> {
                                myFileModel?.apply {
                                    getBaseActivity()?.printFile(this)
                                }
                            }
                        }
                    }

                })
            }
        }
    }

    private fun loadBannerAds() {
        val adRequest = AdRequest.Builder().build()
        binding.adViewBannerViewer.loadAd(adRequest)
    }

    override fun onPause() {
        binding.adViewBannerViewer.pause()
        super.onPause()
    }

    override fun onResume() {
        binding.adViewBannerViewer.resume()
        super.onResume()
    }

    override fun onDestroy() {
        binding.adViewBannerViewer.destroy()
        super.onDestroy()
    }
}