package com.cocna.pdffilereader.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.*
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentPdfViewerBinding
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.myinterface.OnPopupMenuItemClickListener
import com.cocna.pdffilereader.myinterface.OnShowAdsBackListener
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.home.adapter.PreviewAdapter
import com.cocna.pdffilereader.ui.home.dialog.DeleteFileDialog
import com.cocna.pdffilereader.ui.home.dialog.FileInfoDialog
import com.cocna.pdffilereader.ui.home.dialog.LoadingAdsDialog
import com.cocna.pdffilereader.ui.home.dialog.RenameFileDialog
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.gms.ads.*
import com.google.android.material.slider.Slider
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import java.io.*
import java.util.*


/**
 * Created by Thuytv on 10/06/2022.
 */
@Suppress("DEPRECATION")
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
    private var isLoadedAds = false
    private var isJumpEdit = false
    private var pdfConfig: PDFView.Configurator? = null
    private var isNightMode = false
    private var isPageMode = false
    private var isBookmark = false
    private var oldPage = 0

    // Determine the screen width (less decorations) to use for the ad width.
    // If the ad hasn't been laid out, default to the full screen width.
    @Suppress("DEPRECATION")
    private val adSize: AdSize
        get() {
            if (isVisible) {
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
            } else {
                return AdSize.BANNER
            }
        }
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPdfViewerBinding
        get() = FragmentPdfViewerBinding::inflate

    override fun initData() {
        startTime = System.currentTimeMillis()
        val pickIt = PickiT(context, object : PickiTCallbacks {
            override fun PickiTonUriReturned() {
                Logger.showLog("Thuytv------PickiTonUriReturned")
            }

            override fun PickiTonStartListener() {
                Logger.showLog("Thuytv------PickiTonStartListener")
            }

            override fun PickiTonProgressUpdate(progress: Int) {
            }

            override fun PickiTonCompleteListener(path: String?, wasDriveFile: Boolean, wasUnknownProvider: Boolean, wasSuccessful: Boolean, Reason: String?) {
                Logger.showLog("Thuytv------PickiTonCompleteListener : $path")
                Logger.showLog("Thuytv------PickiTonCompleteListener wasDriveFile: $wasDriveFile---wasUnknownProvider: $wasUnknownProvider---wasSuccessful: $wasSuccessful----Reason: $Reason")
                if (wasSuccessful) {
                    if (isVisible) {
                        myFileModel = MyFilesModel(uriPath = path, uriOldPath = path, extensionName = "pdf")

                        if (path?.isNotEmpty() == true) {
                            val cut = path.lastIndexOf("/")
                            if (cut != -1) {
                                val fileName = path.substring(cut + 1)
                                binding.ttToolbarPdf.text = fileName
                                myFileModel?.name = fileName
                            }
                        }
                        openFDPFile(path)
                        Handler(Looper.myLooper()!!).postDelayed({
                            loadDataFile()
                        }, 500)
                    }
                } else if (Reason?.isNotEmpty() == true) {
                    getBaseActivity()?.apply {
                        Logger.showToast(this, Reason)
                    }
                }
            }

            override fun PickiTonMultipleCompleteListener(paths: ArrayList<String>?, wasSuccessful: Boolean, Reason: String?) {
                Logger.showLog("Thuytv------PickiTonMultipleCompleteListener")
            }

        }, getBaseActivity())

        myFileModel = arguments?.getParcelable(AppKeys.KEY_BUNDLE_DATA)
        isNightMode = getBaseActivity()?.sharedPreferences?.getValueBoolean(SharePreferenceUtils.KEY_NIGHT_MODE) ?: false
        isPageMode = getBaseActivity()?.sharedPreferences?.getValueBoolean(SharePreferenceUtils.KEY_PAGE_MODE) ?: false
        if (isPageMode) {
            binding.imvChangeView.setImageResource(R.drawable.ic_continuos_page)
        } else {
            binding.imvChangeView.setImageResource(R.drawable.ic_page_by_page)
        }
        if (myFileModel == null) {
            var uriPath = arguments?.getParcelable<Uri>(Intent.EXTRA_STREAM)
            if (uriPath == null) {
                uriPath = arguments?.getParcelable(AppKeys.KEY_BUNDLE_URI)
                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_OPEN_PDF, AppConfig.KEY_PARAM_FB_OPEN_SOURCE, AppConfig.KEY_FB_CLICK_SUGGEST_APP)
            } else {
                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_OPEN_PDF, AppConfig.KEY_PARAM_FB_OPEN_SOURCE, AppConfig.KEY_FB_CLICK_SHORT_CUT)

            }
            uriPath?.apply {
//                val fullPath = RealPathUtil.getRealPath(getBaseActivity()!!, this)
                pickIt.getPath(this, Build.VERSION.SDK_INT)
//                myFileModel = MyFilesModel(uriPath = fullPath, uriOldPath = fullPath, extensionName = "pdf")
//
//                if (fullPath?.isNotEmpty() == true) {
//                    val cut = fullPath.lastIndexOf("/")
//                    if (cut != -1) {
//                        val fileName = fullPath.substring(cut + 1)
//                        binding.ttToolbarPdf.text = fileName
//                        myFileModel?.name = fileName
//                    }
//                }
//                openFDPFile(fullPath)
//                Handler(Looper.myLooper()!!).postDelayed({
//                    loadDataFile()
//                }, 500)
            }
        } else {
            myFileModel?.apply {
                binding.ttToolbarPdf.text = name
                if (extensionName?.lowercase() == "pdf") {
                    openFDPFile(uriPath)
                }
                Handler(Looper.myLooper()!!).postDelayed({
                    loadDataFile()
                }, 500)
            }
            getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_OPEN_PDF, AppConfig.KEY_PARAM_FB_OPEN_SOURCE, AppConfig.KEY_FB_CLICK_LIST_FILE)
        }

        val lstFavorite = getBaseActivity()?.sharedPreferences?.getFavoriteFile()
        if (lstFavorite?.contains(myFileModel) == true) {
            isBookmark = true
            binding.imvViewBookmark.setImageResource(R.drawable.ic_bookmark_selected)
        } else {
            isBookmark = false
            binding.imvViewBookmark.setImageResource(R.drawable.ic_bookmark)
        }
        changeViewMode(isNightMode)

        getBaseActivity()?.apply {
            adView = AdView(this)
            binding.adViewContainer.addView(adView)
            // Since we're loading the banner based on the adContainerView size, we need to wait until this
            // view is laid out before we can get the width.
            binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
                if (!initialLayoutComplete) {
                    initialLayoutComplete = true
                    if (getBaseActivity()?.sharedPreferences?.getAdsConfig()?.ads_banner_reader == true) {
                        loadBannerAds()
                    }
                }
            }
        }

        if (getBaseActivity()?.isCurrentNetwork == false) {
            getBaseActivity()?.enabaleNetwork()
        }
//        getBaseActivity()?.loadNativeAds(binding.frameAdsNativePdf, AppConfig.ID_ADS_NATIVE_TOP_BAR_PDF)
        var countRateUs = getBaseActivity()?.sharedPreferences?.getValueInteger(SharePreferenceUtils.KEY_COUNT_RATE_US, 0) ?: 0
        Common.isFromPDFView = true
        if (countRateUs <= AppConfig.MAX_COUNT_RATE_US && Common.countRatingApp <= AppConfig.MAX_COUNT_RATE_US) {
            Common.countRatingApp++
        }
    }

    override fun initEvents() {
        listenClickViews(
            binding.imvPdfBack,
            binding.imvPdfMore,
            binding.imvChangeView,
            binding.imvChangeMode,
            binding.btnViewBookmark,
            binding.btnViewCapture,
            binding.btnShareFile
        )
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
                        if (previewAdapter != null && isVisible && getBaseActivity()?.isFinishing == false && currentPage > 0) {
                            binding.rcvPreviewPage.scrollToPosition(currentPage)
                        }
                    }
                }

            }, 30)

        }
        binding.vlJumpPage.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            isLoadedAds = true
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
                    isLoadedAds = false
                    binding.pdfViewer.jumpTo(getEdittextNumber() - 1)
                    isLoadedAds = true
                    binding.groupPageViewer.visible()
                    binding.llJumpToPageEdit.gone()
                    getBaseActivity()?.hideKeyboard()
                    isJumpEdit = true
                    return true
                }
                return false
            }

        })
    }

    private fun getEdittextNumber(): Int {
        if (binding.edtJumpPage.text?.toString().isNullOrEmpty()) return 0
        return binding.edtJumpPage.text.toString().toInt()
    }

    private fun openFDPFile(uriPath: String?) {
        uriPath?.apply {
            pdfConfig = binding.pdfViewer.fromFile(File(uriPath))
            pdfConfig?.apply {
                this.spacing(10)
                    .defaultPage(0)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .nightMode(isNightMode)
                    .fitEachPage(true)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .onPageChange(onPageChangeListener)
                    .onPageScroll(onPageScrollListener)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .onTap(onTapListener)
                    .onLoad {
                        val totalPage = binding.pdfViewer.pageCount
                        Logger.showLog("Thuytv-----onLoad---totalPage: $totalPage ---isFirst: $isFirst")
                        if (totalPage > 1 && isFirst) {
                            binding.seekbarJumpToPage.valueFrom = 1f
                        }
                    }
                if (isPageMode) {
                    this.swipeHorizontal(true)
                        .pageSnap(true)
                        .autoSpacing(true)
                        .pageFling(true)
                        .fitEachPage(true)
                        .scrollHandle(null)
                } else {
                    this.swipeHorizontal(false)
                        .pageSnap(false)
                        .autoSpacing(false)
                        .pageFling(false)
                        .scrollHandle(MyScrollHandle(context, false))
                }
                this.load()
            }
        }
    }

    private val onPageScrollListener = OnPageScrollListener { page, positionOffset ->
        Logger.showLog("Thuytv-----onPageScrollListener---page: $page ---oldPage: $oldPage ----isJumpEdit: $isJumpEdit")
        if (binding.llToolbarPdf.visibility == View.VISIBLE && page > 0 && oldPage != page && !isJumpEdit) {
            oldPage = page
            binding.llToolbarPdf.gone()
            binding.groupPageViewer.gone()
        } else {
            if (isJumpEdit) {
                isJumpEdit = false
            }
            oldPage = page
        }
    }

    @SuppressLint("SetTextI18n")
    private val onPageChangeListener = OnPageChangeListener { page, pageCount ->
        if (!isLoadedAds) {
            currentPage = page + 1
            binding.vlPageAndTotalPage.text = getString(R.string.vl_total_page, currentPage, pageCount)
            binding.vlJumpPage.text = getString(R.string.vl_page, currentPage)
            binding.edtJumpPage.setText(currentPage.toString())
            isTouchSlider = false
            binding.seekbarJumpToPage.value = currentPage.toFloat()
            if (isFirst) {
                if (binding.groupPageViewer.visibility == View.GONE) {
                    binding.groupPageViewer.visible()
                }
                binding.seekbarJumpToPage.valueTo = pageCount.toFloat()
                binding.vlTotalPage.text = "/ $pageCount"
                binding.vlJumpTotalPageEdit.text = "/ $pageCount"
                isFirst = false
            }
            Logger.showLog("Thuytv-----onPageChangeListener---currentPage: $currentPage ---isFirst: $isFirst")
        } else {
            isLoadedAds = false
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
        false
    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
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
                                    if (myFileModel != null) {
                                        RenameFileDialog(this, myFileModel!!, object : OnDialogItemClickListener {
                                            override fun onClickItemConfirm(mData: MyFilesModel) {
                                                binding.ttToolbarPdf.text = mData.name
                                            }

                                        }).show()
                                    }
                                }
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_RENAME_FILE, AppConfig.KEY_EVENT_FB_RENAME_FILE)
                            }
                            R.id.menu_favorite -> {
                                bookMarkFile()
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_BOOKMARK_FILE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_BOOKMARK)
                            }
                            R.id.menu_un_favorite -> {
                                bookMarkFile()
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_BOOKMARK_FILE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_UN_BOOKMARK)
                            }
                            R.id.menu_delete -> {
                                getBaseActivity()?.apply {
                                    if (myFileModel != null) {
                                        val deleteFileDialog = DeleteFileDialog(this, myFileModel!!, object : OnDialogItemClickListener {
                                            override fun onClickItemConfirm(mData: MyFilesModel) {
                                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_DELETE_FILE, AppConfig.KEY_EVENT_FB_DELETE_FILE)
                                                finish()
                                            }

                                        })
                                        deleteFileDialog.show()
                                    }
                                }
                            }
                            R.id.menu_print -> {
                                myFileModel?.apply {
                                    getBaseActivity()?.printFile(this)
                                }
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_PRINT_FILE, AppConfig.KEY_EVENT_FB_PRINT_FILE)
                            }
                            R.id.menu_shortcut -> {
                                getBaseActivity()?.apply {
                                    if (myFileModel != null) {
                                        setUpShortCut(this, myFileModel!!)
                                    }
                                }
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_SHORTCUT_FILE, AppConfig.KEY_EVENT_FB_SHORTCUT_FILE)
                            }
                            R.id.menu_file_info -> {
                                getBaseActivity()?.apply {
                                    if (myFileModel != null) {
                                        FileInfoDialog(this, myFileModel!!).show()
                                    }
                                }
                            }
                        }
                    }
                })
            }
            R.id.imv_change_mode -> {
                isNightMode = !isNightMode
                getBaseActivity()?.sharedPreferences?.setValueBoolean(SharePreferenceUtils.KEY_NIGHT_MODE, isNightMode)
                changeViewMode(isNightMode)
                if (isNightMode) {
                    getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_DARK_MODE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_ON)
                } else {
                    getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_DARK_MODE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_OFF)
                }
            }
            R.id.imv_change_view -> {
                isPageMode = !isPageMode
                getBaseActivity()?.sharedPreferences?.setValueBoolean(SharePreferenceUtils.KEY_PAGE_MODE, isPageMode)
                changePageMode(isPageMode)
                if (isPageMode) {
                    getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_SWIPE_MODE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_CONTINUOUS)
                } else {
                    getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_SWIPE_MODE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_PAGE_BY_PAGE)
                }
            }
            R.id.btn_view_bookmark -> {
                bookMarkFile()
            }
            R.id.btn_view_capture -> {
                captureScreen(binding.pdfViewer)?.apply {
//                    val outputStream = ByteArrayOutputStream()
//                    this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                    val b = outputStream.toByteArray()
//                    val encodedBitmap = Base64.encodeToString(b, Base64.DEFAULT)
                    val bundle = Bundle()
                    bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, this)
//                    bundle.putString(AppKeys.KEY_BUNDLE_DATA, encodedBitmap)
                    getBaseActivity()?.addFragment(CropImageFragment.newInstance(object : OnShowAdsBackListener {
                        override fun onShowAds() {
                            getBaseActivity()?.let {
                                if (it.sharedPreferences.getAdsConfig().ads_inter_back) {
                                    LoadingAdsDialog.newInstance(it, AppConfig.ID_ADS_INTERSTITIAL_BACK).show(childFragmentManager, "LOADING_ADS")
                                }
                            }
                        }

                    }), bundle, R.id.layout_container)
                }
            }
            R.id.btn_share_file -> {
//                val bundle = Bundle()
//                bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, myFileModel)
//                getBaseActivity()?.onNextScreen(PdfPreviewActivity::class.java, bundle, false)
                myFileModel?.uriPath?.let { File(it) }?.let { shareFile(it) }
                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_SHARE_FILE, AppConfig.KEY_EVENT_FB_SHARE_FILE)
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

            override fun onAdLoaded() {
                super.onAdLoaded()
                Logger.showLog("Thuytv--------onAdLoaded")
                isLoadedAds = true
                Handler(Looper.myLooper()!!).postDelayed({
                    isLoadedAds = false
                }, 500)
                Common.setEventAdsBanner(AppConfig.ID_ADS_BANNER_READER)
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
        if (isVisible) {
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

    private fun changeViewMode(isNightMode: Boolean) {
        getBaseActivity()?.apply {
            pdfConfig?.apply {
                this.nightMode(isNightMode).load()
            }
            if (isNightMode) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.llToolbarPdf.setBackgroundColor(resources.getColor(R.color.text_rgb_102027, this.theme))
                    binding.llBottomMenu.setBackgroundColor(resources.getColor(R.color.text_rgb_102027, this.theme))
                    binding.vlViewBookmark.setTextColor(resources.getColor(R.color.color_white, this.theme))
                    binding.vlViewCapture.setTextColor(resources.getColor(R.color.color_white, this.theme))
                    binding.vlViewShareImage.setTextColor(resources.getColor(R.color.color_white, this.theme))
                    binding.ttToolbarPdf.setTextColor(resources.getColor(R.color.color_white, this.theme))
                } else {
                    binding.llToolbarPdf.setBackgroundColor(resources.getColor(R.color.text_rgb_102027))
                    binding.llBottomMenu.setBackgroundColor(resources.getColor(R.color.text_rgb_102027))
                    binding.vlViewBookmark.setBackgroundColor(resources.getColor(R.color.color_white))
                    binding.vlViewCapture.setBackgroundColor(resources.getColor(R.color.color_white))
                    binding.vlViewShareImage.setBackgroundColor(resources.getColor(R.color.color_white))
                    binding.ttToolbarPdf.setBackgroundColor(resources.getColor(R.color.color_white))
                }
                binding.imvPdfBack.setColorFilter(ContextCompat.getColor(this, R.color.color_white))
                binding.imvChangeMode.setImageResource(R.drawable.ic_light_mode)
                binding.imvChangeView.setColorFilter(ContextCompat.getColor(this, R.color.color_white))
                binding.imvPdfMore.setColorFilter(ContextCompat.getColor(this, R.color.color_white))
                binding.imvViewBookmark.setColorFilter(ContextCompat.getColor(this, R.color.color_white))
                binding.imvViewCapture.setColorFilter(ContextCompat.getColor(this, R.color.color_white))
                binding.imvViewShareImage.setColorFilter(ContextCompat.getColor(this, R.color.color_white))
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.llToolbarPdf.setBackgroundColor(resources.getColor(R.color.color_bg_toolbar, this.theme))
                    binding.llBottomMenu.setBackgroundColor(resources.getColor(R.color.color_bg_toolbar, this.theme))

                    binding.vlViewBookmark.setTextColor(resources.getColor(R.color.rgb_62757F, this.theme))
                    binding.vlViewCapture.setTextColor(resources.getColor(R.color.rgb_62757F, this.theme))
                    binding.vlViewShareImage.setTextColor(resources.getColor(R.color.rgb_62757F, this.theme))
                    binding.ttToolbarPdf.setTextColor(resources.getColor(R.color.text_rgb_102027, this.theme))
                } else {
                    binding.llToolbarPdf.setBackgroundColor(resources.getColor(R.color.color_bg_toolbar))
                    binding.llBottomMenu.setBackgroundColor(resources.getColor(R.color.color_bg_toolbar))
                    binding.vlViewBookmark.setBackgroundColor(resources.getColor(R.color.rgb_62757F))
                    binding.vlViewCapture.setBackgroundColor(resources.getColor(R.color.rgb_62757F))
                    binding.vlViewShareImage.setBackgroundColor(resources.getColor(R.color.rgb_62757F))
                    binding.ttToolbarPdf.setBackgroundColor(resources.getColor(R.color.text_rgb_102027))
                }
                binding.imvChangeMode.setImageResource(R.drawable.ic_dark_mode)
                binding.imvPdfBack.setColorFilter(ContextCompat.getColor(this, R.color.text_rgb_102027))
                binding.imvChangeView.setColorFilter(ContextCompat.getColor(this, R.color.rgb_34515E))
                binding.imvPdfMore.setColorFilter(ContextCompat.getColor(this, R.color.text_rgb_102027))
                if (isBookmark) {
                    binding.imvViewBookmark.setColorFilter(ContextCompat.getColor(this, R.color.rgb_F44336))
                } else {
                    binding.imvViewBookmark.setColorFilter(ContextCompat.getColor(this, R.color.rgb_62757F))
                }
                binding.imvViewCapture.setColorFilter(ContextCompat.getColor(this, R.color.rgb_62757F))
                binding.imvViewShareImage.setColorFilter(ContextCompat.getColor(this, R.color.rgb_62757F))
            }
        }
    }

    private fun changePageMode(isPageMode: Boolean) {
        if (isPageMode) { // continues page
            pdfConfig?.apply {
                this.swipeHorizontal(true)
                    .pageSnap(true)
                    .autoSpacing(true)
                    .pageFling(true)
                    .scrollHandle(null).load()
            }
            binding.imvChangeView.setImageResource(R.drawable.ic_continuos_page)
        } else { // page by page
            pdfConfig?.apply {
                this.swipeHorizontal(false)
                    .pageSnap(false)
                    .autoSpacing(false)
                    .pageFling(false)
                    .scrollHandle(MyScrollHandle(context, false)).load()
            }
            binding.imvChangeView.setImageResource(R.drawable.ic_page_by_page)
        }
    }

    private fun bookMarkFile() {
        myFileModel?.apply {
            val lstFavorite = getBaseActivity()?.sharedPreferences?.getFavoriteFile()
            if (lstFavorite?.contains(myFileModel) == true) { // da bookmark
                getBaseActivity()?.sharedPreferences?.removeFavoriteFile(myFileModel!!)
                RxBus.publish(EventsBus.RELOAD_FAVORITE)
                binding.llShowNotification.vlContentNotify.text = getString(R.string.msg_remove_bookmark_success)
                binding.llShowNotification.llShowNotification.visible()
                binding.imvViewBookmark.setImageResource(R.drawable.ic_bookmark)
                getBaseActivity()?.apply {
                    binding.imvViewBookmark.setColorFilter(ContextCompat.getColor(this, R.color.rgb_62757F))
                }
                isBookmark = false
                binding.llShowNotification.imvClosePopup.setOnClickListener {
                    binding.llShowNotification.llShowNotification.gone()
                }
                Handler(Looper.myLooper()!!).postDelayed({
                    if (isVisible && getBaseActivity()?.isFinishing == false
                        && binding.llShowNotification.llShowNotification.visibility == View.VISIBLE
                    ) {
                        binding.llShowNotification.llShowNotification.gone()
                    }
                }, 2000)
                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_BOOKMARK_FILE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_UN_BOOKMARK)
            } else {
                getBaseActivity()?.sharedPreferences?.setFavoriteFile(myFileModel!!)
                RxBus.publish(EventsBus.RELOAD_FAVORITE)
                binding.imvViewBookmark.setImageResource(R.drawable.ic_bookmark_selected)
                getBaseActivity()?.apply {
                    binding.imvViewBookmark.setColorFilter(ContextCompat.getColor(this, R.color.rgb_F44336))
                }
                isBookmark = true
                binding.llShowNotification.vlContentNotify.text = getString(R.string.msg_add_bookmark_success)
                binding.llShowNotification.llShowNotification.visible()
                binding.llShowNotification.imvClosePopup.setOnClickListener {
                    binding.llShowNotification.llShowNotification.gone()
                }
                Handler(Looper.myLooper()!!).postDelayed({
                    if (isVisible && getBaseActivity()?.isFinishing == false
                        && binding.llShowNotification.llShowNotification.visibility == View.VISIBLE
                    ) {
                        binding.llShowNotification.llShowNotification.gone()
                    }
                }, 2000)
                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_BOOKMARK_FILE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_BOOKMARK)
            }
        }
    }

    private fun captureScreen(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}