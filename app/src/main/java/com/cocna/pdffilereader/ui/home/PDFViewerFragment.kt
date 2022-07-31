package com.cocna.pdffilereader.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentPdfViewerBinding
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.myinterface.OnPopupMenuItemClickListener
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.home.adapter.PreviewAdapter
import com.cocna.pdffilereader.ui.home.dialog.DeleteFileDialog
import com.cocna.pdffilereader.ui.home.dialog.RenameFileDialog
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnRenderListener
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.gms.ads.*
import com.google.android.material.slider.Slider
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import java.io.*
import java.util.*


/**
 * Created by Thuytv on 10/06/2022.
 */
class PDFViewerFragment : BaseFragment<FragmentPdfViewerBinding>(), View.OnClickListener {
    private var myFileModel: MyFilesModel? = null
    private var isFirst = true
    private var isTouchSlider = true
    private lateinit var adView: AdView
    private var currentPage = 0
    private var pdfiumCore: PdfiumCore? = null
    private var pdfDocument: PdfDocument? = null
    private var previewAdapter: PreviewAdapter? = null

    private var initialLayoutComplete = false
    private var startTime: Long = 0L
    private var isSendShowAds = false

    // Determine the screen width (less decorations) to use for the ad width.
    // If the ad hasn't been laid out, default to the full screen width.
    @Suppress("DEPRECATION")
    private val adSize: AdSize
        get() {
            val display = getBaseActivity()?.windowManager?.defaultDisplay
            val outMetrics = DisplayMetrics()
            display?.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return if (getBaseActivity() != null) {
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getBaseActivity()!!, adWidth)
            } else {
                AdSize.BANNER
            }
        }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPdfViewerBinding
        get() = FragmentPdfViewerBinding::inflate

    override fun initData() {
        myFileModel = arguments?.getParcelable(AppKeys.KEY_BUNDLE_DATA)
        startTime = System.currentTimeMillis()
        myFileModel?.apply {
            binding.ttToolbarPdf.text = name
            if (extensionName?.lowercase() == "pdf") {
                openFDPFile(uriPath)
            }
            Handler(Looper.myLooper()!!).postDelayed({
                loadDataFile()
            }, 500)
        }
        getBaseActivity()?.apply {
            adView = AdView(this)
            binding.adViewContainer.addView(adView)
            // Since we're loading the banner based on the adContainerView size, we need to wait until this
            // view is laid out before we can get the width.
            binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
                if (!initialLayoutComplete) {
                    initialLayoutComplete = true
                    loadBannerAds()
                }
            }
        }

        Logger.showLog("Thuytv-------isCurrentNetwork: " + getBaseActivity()?.isCurrentNetwork)
        if (getBaseActivity()?.isCurrentNetwork == false) {
            getBaseActivity()?.enabaleNetwork()
        }
//        getBaseActivity()?.loadNativeAds(binding.frameAdsNativePdf, AppConfig.ID_ADS_NATIVE_TOP_BAR_PDF)
    }

    override fun initEvents() {
        listenClickViews(binding.imvPdfBack, binding.imvPdfMore)
        binding.seekbarJumpToPage.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
                binding.llPreviewPage.visible()
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                val page = binding.seekbarJumpToPage.value.toInt()
                binding.pdfViewer.jumpTo(page - 1)
                binding.vlJumpPage.text = getString(R.string.vl_page, page)
                binding.llPreviewPage.gone()
            }
        })
        var timer = Timer()
        binding.seekbarJumpToPage.addOnChangeListener { _, value, _ ->
            currentPage = value.toInt()
            timer.cancel()
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    getBaseActivity()?.runOnUiThread {
//                        previewAdapter?.updateCurrentPage(currentPage)
                        if (previewAdapter != null && isVisible && getBaseActivity()?.isFinishing == false) {
                            binding.rcvPreviewPage.scrollToPosition(currentPage)
                        }
                    }
                }

            }, 30)

        }
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
        uriPath?.apply {
            binding.pdfViewer.fromFile(File(uriPath))
                .spacing(10)
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeHorizontal(false)
                .fitEachPage(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .onPageChange(onPageChangeListener)
                .pageFitPolicy(FitPolicy.WIDTH)
                .scrollHandle(MyScrollHandle(context, false))
                .onTap(onTapListener)
                .onRender(onRenderListener)
                .onLoad {
                    val totalPage = binding.pdfViewer.pageCount
                    if (totalPage > 1) {
                        binding.seekbarJumpToPage.valueFrom = 1f
                    }
                }
                .load()


        }
    }

    private val onRenderListener = OnRenderListener { binding.pdfViewer.fitToWidth(binding.pdfViewer.currentPage) }

    @SuppressLint("SetTextI18n")
    private val onPageChangeListener = OnPageChangeListener { page, pageCount ->
        if (binding.groupPageViewer.visibility == View.GONE) {
            binding.groupPageViewer.visible()
        }
        currentPage = page + 1
        binding.vlPageAndTotalPage.text = getString(R.string.vl_total_page, currentPage, pageCount)
        binding.vlJumpPage.text = getString(R.string.vl_page, currentPage)
        binding.edtJumpPage.setText(currentPage.toString())
        isTouchSlider = false
        binding.seekbarJumpToPage.value = currentPage.toFloat()
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
                var lstPopupMenu = R.menu.menu_more_detail_file
                val lstFavorite = getBaseActivity()?.sharedPreferences?.getFavoriteFile()
                if (lstFavorite?.contains(myFileModel) == true) {
                    lstPopupMenu = R.menu.menu_more_detail_file_unfavorite
                }
                showPopupMenu(binding.imvPdfMore, lstPopupMenu, object : OnPopupMenuItemClickListener {
                    override fun onClickItemPopupMenu(menuItem: MenuItem?) {
                        when (menuItem?.itemId) {
                            R.id.menu_rename -> {
                                getBaseActivity()?.apply {
                                    RenameFileDialog(this, myFileModel!!, object : OnDialogItemClickListener {
                                        override fun onClickItemConfirm(mData: MyFilesModel) {
                                            binding.ttToolbarPdf.text = mData.name
                                        }

                                    }).show()
                                }
                            }
                            R.id.menu_favorite -> {
                                getBaseActivity()?.sharedPreferences?.setFavoriteFile(myFileModel!!)
                                RxBus.publish(EventsBus.RELOAD_FAVORITE)
                            }
                            R.id.menu_un_favorite -> {
                                getBaseActivity()?.sharedPreferences?.removeFavoriteFile(myFileModel!!)
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
        adView.adUnitId = AppConfig.ID_ADS_BANNER_READER
        adView.setAdSize(adSize)

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(loadAdsError: LoadAdError) {
                super.onAdFailedToLoad(loadAdsError)
                getBaseActivity()?.setLogDataToFirebase(
                    AdsLogModel(
                        adsId = AppConfig.ID_ADS_BANNER_READER, adsName = "Ads Banner PDF", message = loadAdsError.message,
                        deviceName = Common.getDeviceName(getBaseActivity())
                    )
                )
            }
        }

    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    /** Called when returning to the activity  */
    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    /** Called before the activity is destroyed  */
    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
        recycleMemory()
        val endTime = System.currentTimeMillis()
        Logger.showLog("Thuytv----onDestroy: " + ((endTime - startTime) >= 10 * 1000))
        if ((endTime - startTime) >= 10000 && !isSendShowAds) {
            isSendShowAds = true
            RxBus.publish(EventsBus.SHOW_ADS_BACK)
//                getBaseActivity()?.loadInterstAds(AppConfig.ID_ADS_INTERSTITIAL_BACK_FILE, null)
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val pageSelected = result.data?.getIntExtra(AppKeys.KEY_BUNDLE_ACTION, 1) ?: 1
            binding.pdfViewer.jumpTo(pageSelected)
        }
    }

    private fun openPreviewPdf() {
        myFileModel?.apply {
            val intent = Intent(getBaseActivity(), PdfPreviewActivity::class.java)
            intent.putExtra(AppKeys.KEY_BUNDLE_DATA, this)
            intent.putExtra(AppKeys.KEY_BUNDLE_ACTION, currentPage)
            resultLauncher.launch(intent)
        }
    }

    private fun loadDataFile() {
        loadPdfFile()
        if (pdfiumCore != null && pdfDocument != null) {
            val totalCount = pdfiumCore?.getPageCount(pdfDocument) ?: 0
            previewAdapter =
                PreviewAdapter(
                    getBaseActivity(),
                    pdfiumCore!!,
                    pdfDocument!!,
                    myFileModel?.name ?: "",
                    totalCount,
                    currentPage,
                    object : PreviewAdapter.OnItemClickListener {
                        override fun onClickItem(position: Int) {
                            binding.pdfViewer.jumpTo(position)
                        }
                    })
            binding.rcvPreviewPage.apply {
                layoutManager = LinearLayoutManager(getBaseActivity(), LinearLayoutManager.HORIZONTAL, false)
                adapter = previewAdapter
            }
            binding.rcvPreviewPage.scrollToPosition(currentPage)
        }
    }

    private fun loadPdfFile() {
        myFileModel?.uriPath?.apply {
            try {
                val file = File(this)
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfiumCore = PdfiumCore(getBaseActivity())
                if (pdfiumCore != null) {
                    pdfDocument = pdfiumCore!!.newDocument(pfd)
                }
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
}