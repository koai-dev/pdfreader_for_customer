package com.cocna.pdffilereader.ui.home

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentMainBinding
import com.cocna.pdffilereader.imagepicker.model.GridCount
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.imagepicker.model.ImagePickerConfig
import com.cocna.pdffilereader.imagepicker.ui.imagepicker.registerImagePicker
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.home.dialog.BottomSheetToolsPdf
import com.cocna.pdffilereader.ui.home.dialog.RatingAppDialog
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.google.android.gms.ads.*
import com.google.android.material.navigation.NavigationBarView
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.InputStream


@Suppress("DEPRECATION")
class MainFragment : BaseFragment<FragmentMainBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding = FragmentMainBinding::inflate

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var myFilesFragment: MyFilesFragment
    private lateinit var settingFragment: SettingFragment
    private lateinit var favoriteFragment: FavoriteFragment
    private lateinit var browseFragment: BrowseFragment
    private var idViewSelected: Int? = null

    private lateinit var adView: AdView

    private var initialLayoutComplete = false
    private val lstImageSelected = ArrayList<Image>()

    // Determine the screen width (less decorations) to use for the ad width.
    // If the ad hasn't been laid out, default to the full screen width.
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

    override fun initData() {
        myFilesFragment = MyFilesFragment()
        settingFragment = SettingFragment()
        favoriteFragment = FavoriteFragment()
        browseFragment = BrowseFragment()

        loadFragment(myFilesFragment)
        loadFragment(settingFragment)
        loadFragment(favoriteFragment)
        loadFragment(browseFragment)
        hideShowFragment(myFilesFragment, settingFragment, favoriteFragment, browseFragment)
        binding.navigationBottom.itemIconTintList = null
        idViewSelected = R.id.navigation_my_file
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

//        getBaseActivity()?.apply {
//            InterstitialUtils.sharedInstance?.loadInterstAds(AppConfig.ID_ADS_INTERSTITIAL_FILE, this)
////            loadNativeAds(binding.frameAdsNative, AppConfig.ID_ADS_NATIVE_TOP_BAR)
//        }
    }

    override fun initEvents() {
        binding.navigationBottom.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                idViewSelected = item.itemId
                when (item.itemId) {
                    R.id.navigation_my_file -> {
                        if (binding.imvPurchase.visibility == View.GONE) {
                            binding.edtSearchAll.visible()
                            binding.imvPurchase.visible()
                            binding.ttSettingHome.gone()
                        }
                        hideShowFragment(myFilesFragment, settingFragment, favoriteFragment, browseFragment)
                    }
                    R.id.navigation_favorite -> {
                        if (binding.imvPurchase.visibility == View.GONE) {
                            binding.edtSearchAll.visible()
                            binding.imvPurchase.visible()
                            binding.ttSettingHome.gone()
                        }
//                        if (!binding.edtSearchAll.text?.toString().isNullOrEmpty()) {
//                            binding.edtSearchAll.setText("")
//                        }
                        hideShowFragment(favoriteFragment, myFilesFragment, settingFragment, browseFragment)
                        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_BOOKMARK_SCREEN, AppConfig.KEY_EVENT_FB_BOOKMARK_SCREEN)
                    }
                    R.id.navigation_browse -> {
                        if (binding.imvPurchase.visibility == View.GONE) {
                            binding.edtSearchAll.visible()
                            binding.imvPurchase.visible()
                            binding.ttSettingHome.gone()
                        }
                        hideShowFragment(browseFragment, myFilesFragment, favoriteFragment, browseFragment)
                        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_FOLDER_SCREEN, AppConfig.KEY_EVENT_FB_FOLDER_SCREEN)
                    }
                    R.id.navigation_setting -> {

                        if (binding.ttSettingHome.visibility == View.GONE) {
                            binding.edtSearchAll.gone()
                            binding.imvPurchase.gone()
                            binding.ttSettingHome.visible()
                        }
                        hideShowFragment(settingFragment, myFilesFragment, favoriteFragment, browseFragment)
                        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_SETTING_SCREEN, AppConfig.KEY_EVENT_FB_SETTING_SCREEN)
                    }

                }
                return true
            }
        })
        binding.edtSearchAll.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(strData: Editable?) {
                if (R.id.navigation_my_file == idViewSelected) {
                    myFilesFragment.onSearchFile(strData?.toString() ?: "")
                } else if (R.id.navigation_favorite == idViewSelected) {
                    favoriteFragment.onSearchFileFavorite(strData?.toString() ?: "")
                } else if (R.id.navigation_browse == idViewSelected) {
                    browseFragment.onSearchFolder(strData?.toString() ?: "")
                }
            }

        })

        binding.btnToolsPdf.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            getBaseActivity()?.let {
                BottomSheetToolsPdf(it, object : BottomSheetToolsPdf.OnItemClickToolsPdf {
                    override fun onItemClickImageToPdf() {
                        lstImageSelected.clear()
                        selectImage()
                    }
                }).show(childFragmentManager, "TOOLS_PDF")
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.add(R.id.fm_container, fragment, fragment.tag)
        transaction?.commit()
    }

    private fun hideShowFragment(showFragment: Fragment, hideFragment1: Fragment, hideFragment2: Fragment, hideFragment3: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()?.hide(hideFragment1)?.hide(hideFragment2)?.hide(hideFragment3)?.show(showFragment)?.commit()
    }

    private fun loadBannerAds() {
        adView.adUnitId = AppConfig.ID_ADS_BANNER_MAIN
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
                        adsId = AppConfig.ID_ADS_BANNER_MAIN, adsName = "Ads Banner Main", message = loadAdsError.message,
                        deviceName = Common.getDeviceName(getBaseActivity())
                    )
                )

            }

            override fun onAdLoaded() {
                Common.setEventAdsBanner(AppConfig.ID_ADS_BANNER_MAIN)
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
        val countRateUs = getBaseActivity()?.sharedPreferences?.getValueInteger(SharePreferenceUtils.KEY_COUNT_RATE_US, 0) ?: 0
        if (countRateUs <= AppConfig.MAX_COUNT_RATE_US) {
            getBaseActivity()?.apply {
                if (isVisible && Common.isFromPDFView == true && (Common.countRatingApp == 2 || Common.countRatingApp == 6)) {
                    RatingAppDialog(this, true).show()
                    Common.isFromPDFView = null
                }
            }
        }
    }

    /** Called before the activity is destroyed  */
    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    private fun selectImage() {
        val config = ImagePickerConfig(
            toolbarColor = "#FAFAFA",
            toolbarIconColor = "#292D32",
            toolbarTextColor = "#102027",
            isLightStatusBar = false,
            isFolderMode = false,
            isMultipleMode = true,
            isShowCamera = true,
            subDirectory = "Photos",
            imageTitle = getString(R.string.tt_select_photo),
            imageGridCount = GridCount(4, 5),
            selectedIndicatorColor = "#F44336",
            isShowNumberIndicator = true,
            selectedImages = lstImageSelected,
            // See more at configuration attributes table below
        )
        launcher.launch(config)
    }

    private val launcher = registerImagePicker { images ->
        // Selected images are ready to use
        Logger.showLog("Thuytv------registerImagePicker: " + images.size)
        if (images.isNotEmpty()) {
            lstImageSelected.clear()
            lstImageSelected.addAll(images)
            Logger.showLog("Thuytv------images: " + images.size)
            val bundle = Bundle()
            bundle.putParcelableArrayList(AppKeys.KEY_BUNDLE_DATA, lstImageSelected)
            getBaseActivity()?.onNextScreen(CreateImageToPdfActivity::class.java, bundle, false)
        }
    }
}