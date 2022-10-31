package com.cocna.pdffilereader.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.viewpager.widget.ViewPager
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentCreatePdfFromImageBinding
import com.cocna.pdffilereader.imagepicker.model.GridCount
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.imagepicker.model.ImagePickerConfig
import com.cocna.pdffilereader.imagepicker.ui.imagepicker.registerImagePicker
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.home.adapter.ViewPagerRecyclerAdapter
import com.cocna.pdffilereader.ui.home.dialog.DeleteImageDialog
import com.cocna.pdffilereader.ui.home.dialog.SaveFilePdfDialog
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import kotlin.math.roundToInt


/**
 * Created by Thuytv on 16/09/2022.
 */
class CreateImageToPdfFragment : BaseFragment<FragmentCreatePdfFromImageBinding>(), View.OnClickListener {
    private var lstImageSelected: ArrayList<Image>? = null

    //    lateinit var viewPagerAdapter: ViewPagerImageAdapter
    lateinit var viewPagerAdapter: ViewPagerRecyclerAdapter
    private var isCropClick = false
    private var totalImage = 0
    private lateinit var adView: AdView
    private var initialLayoutComplete = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCreatePdfFromImageBinding
        get() = FragmentCreatePdfFromImageBinding::inflate

    override fun initData() {
        lstImageSelected = arguments?.getParcelableArrayList(AppKeys.KEY_BUNDLE_DATA)
        if (lstImageSelected == null) {
            lstImageSelected = ArrayList()
        }
        lstImageSelected?.let {
//            viewPagerAdapter = ViewPagerImageAdapter(getBaseActivity(), childFragmentManager, it)
            viewPagerAdapter = ViewPagerRecyclerAdapter(getBaseActivity(), it)
            binding.viewPagerImage.adapter = viewPagerAdapter
            totalImage = it.size
            binding.vlPageAndTotalPage.text = getString(R.string.vl_total_page, 1, totalImage)
        }
        binding.ttToolbarPdf.text = Common.getDateCreatePdf()
//        binding.viewPagerImage.beginFakeDrag()
        getBaseActivity()?.apply {
            adView = AdView(this)
            binding.adViewContainer.addView(adView)
            binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
                if (!initialLayoutComplete) {
                    initialLayoutComplete = true
                    loadBannerAds()
                }
            }
        }
    }

    override fun initEvents() {
        listenClickViews(binding.btnImageAdd, binding.btnImageRotate, binding.btnImageCrop, binding.btnImageDelete, binding.btnSavePdf, binding.imvPdfBack)

        binding.viewPagerImage.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                binding.vlPageAndTotalPage.text = getString(R.string.vl_total_page, position + 1, totalImage)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
        when (v?.id) {
            R.id.imvPdfBack -> {
                getBaseActivity()?.finish()
            }
            R.id.btn_image_add -> {
                selectImage()
            }
            R.id.btn_image_rotate -> {
                if ((lstImageSelected?.size ?: 0) > 0) {
                    val imageCurrent = binding.viewPagerImage.currentItem
                    if (imageCurrent > -1) {
                        viewPagerAdapter.rotateImage(imageCurrent)
                    }
                }
            }
            R.id.btn_image_crop -> {
                if ((lstImageSelected?.size ?: 0) > 0) {
                    isCropClick = !isCropClick
                    val imageCurrent = binding.viewPagerImage.currentItem
                    val imageSelected = lstImageSelected?.get(imageCurrent)
                    if (imageCurrent > -1) {
                        if (isCropClick) {
//                        viewPagerAdapter.cropImage(imageCurrent)
                            binding.cropImageViewEdit.setImageUriAsync(imageSelected?.uri)
                            binding.viewPagerImage.invisible()
                            binding.cropImageViewEdit.visible()
                            binding.imvImageCrop.setImageResource(R.drawable.ic_crop_selected)
                            binding.btnImageAdd.isEnabled = false
                            binding.btnImageDelete.isEnabled = false
                            binding.btnImageRotate.isEnabled = false
                        } else {
                            binding.cropImageViewEdit.croppedImage?.let {
                                if (getBaseActivity() != null && imageSelected?.uri != null) {
                                    val uri = saveBitmapToStorage(getBaseActivity()!!, it, imageSelected.uri!!)
                                    Logger.showLog("Thuytv---------croppedImage: " + uri?.path)
                                    imageSelected.uri = uri
                                    viewPagerAdapter.cropImage(imageSelected, imageCurrent)
                                }
                            }
                            binding.viewPagerImage.visible()
                            binding.cropImageViewEdit.invisible()
                            binding.imvImageCrop.setImageResource(R.drawable.ic_crop)
                            binding.btnImageAdd.isEnabled = true
                            binding.btnImageDelete.isEnabled = true
                            binding.btnImageRotate.isEnabled = true
                        }
                    }
                }
            }
            R.id.btn_image_delete -> {
                if ((lstImageSelected?.size ?: 0) > 0) {
                    val imageCurrent = binding.viewPagerImage.currentItem
                    if (imageCurrent > -1) {
                        Logger.showLog("Thuytv--1----lstImageSelected---:" + lstImageSelected?.size)
                        getBaseActivity()?.let {
                            DeleteImageDialog(it, object : OnDialogItemClickListener {
                                override fun onClickItemConfirm(mData: MyFilesModel) {
                                    viewPagerAdapter.deleteImage(imageCurrent)
                                    Logger.showLog("Thuytv---2---lstImageSelected---:" + lstImageSelected?.size)
                                    totalImage = lstImageSelected?.size ?: 0
                                    if (totalImage > 0) {
                                        val currentPosition = binding.viewPagerImage.currentItem + 1
                                        binding.vlPageAndTotalPage.text = getString(R.string.vl_total_page, currentPosition, totalImage)
                                    } else {
                                        binding.vlPageAndTotalPage.text = ""
                                        selectImage()
                                    }
                                }

                            }).show()
                        }
                    }
//                    lstImageSelected?.removeAt(imageCurrent)
                }

            }
            R.id.btn_save_pdf -> {
                if ((lstImageSelected?.size ?: 0) > 0) {
                    getBaseActivity()?.let {
                        SaveFilePdfDialog(it, object : OnDialogItemClickListener {
                            override fun onClickItemConfirm(mData: MyFilesModel) {
                                val myFilesModel = saveImageToPDF(mData.name)
                                myFilesModel?.let {
                                    val bundle = Bundle()
                                    bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, it)
                                    getBaseActivity()?.replaceFragment(CreateImageResultFragment(), bundle, R.id.layout_container)
                                }
                            }

                        }).show()
                    }
                }
            }
        }
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
            selectedImages = lstImageSelected ?: ArrayList(),
        )
        launcher.launch(config)
    }

    private val launcher = registerImagePicker { images ->
        // Selected images are ready to use
        if (images.isNotEmpty()) {
            lstImageSelected?.clear()
            lstImageSelected?.addAll(images)
            viewPagerAdapter.updateImage(images)
            totalImage = images.size
            val currentPosition = binding.viewPagerImage.currentItem + 1
            binding.vlPageAndTotalPage.text = getString(R.string.vl_total_page, currentPosition, totalImage)
        }
    }

    private fun saveImageToPDF(strName: String?): MyFilesModel? {
        try {
            val document = PDDocument()
            var isHorizontal = false
            lstImageSelected?.let {
                for (image in it) {
                    image.uri?.let {
                        val inputStream: InputStream? = getBaseActivity()?.contentResolver?.openInputStream(it)
                        if (inputStream != null) {
                            var bimg = BitmapFactory.decodeStream(inputStream)
                            if (image.rotate > 0) {
                                val matrix = Matrix()
                                matrix.postRotate(image.rotate.toFloat())
                                val rotateBitmap = Bitmap.createBitmap(bimg, 0, 0, bimg.width, bimg.height, matrix, true)
                                bimg = rotateBitmap
                            }

                            if (bimg.width > bimg.height) {
                                isHorizontal = true
                            }
                            var actualPDFWidth = 0
                            var actualPDFHeight = 0
                            if (isHorizontal) {
                                actualPDFWidth = PDRectangle.A4.height.toInt()
                                actualPDFHeight = PDRectangle.A4.width.toInt()
                            } else {
                                actualPDFWidth = PDRectangle.A4.width.toInt()
                                actualPDFHeight = PDRectangle.A4.height.toInt()
                            }
                            val mBitmap = scaleImage(bimg, actualPDFHeight.toFloat(), actualPDFWidth.toFloat(), true)
                            mBitmap?.let {
                                val page = PDPage()
                                document.addPage(page)
//                        val img: PDImageXObject = LosslessFactory.createFromImage(document, bimg)
                                val img: PDImageXObject = JPEGFactory.createFromImage(document, mBitmap)
                                val contentStream = PDPageContentStream(document, page)
                                var imageWidth = mBitmap.width
                                var imageHeight = mBitmap.height
//                            if (imageHeight > actualPDFHeight) {
//                                imageHeight = actualPDFHeight - 40
//                            }
//                            if (imageWidth > actualPDFWidth) {
//                                imageWidth = actualPDFWidth - 40
//                            }

//                        contentStream.drawImage(img, 20f, 20f)
//                        contentStream.drawImage(img, 20f, 20f,img.width/300f*72, img.height/300f *72)
                                contentStream.drawImage(img, 20f, 20f, imageWidth.toFloat(), imageHeight.toFloat())
                                contentStream.close()
                                inputStream.close()
                            }

                        }
                    }
                }
            }
            var mFileName = strName
            if (mFileName?.contains(".pdf") == true) {
                mFileName.replace(".pdf", "")
            }

            if (mFileName.isNullOrEmpty()) {
                mFileName = binding.ttToolbarPdf.text?.toString()
            }
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + mFileName + ".pdf"
            document.save(path)
            document.close()
            Toast.makeText(getBaseActivity(), "Save Image Success", Toast.LENGTH_LONG).show()
            return MyFilesModel(
                name = "$mFileName.pdf",
                uriPath = path,
                uriOldPath = path,
                extensionName = "pdf",
                lastModified = System.currentTimeMillis(),
                uriSaveFile = lstImageSelected?.get(0)?.uri
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(getBaseActivity(), "Save Image Fail", Toast.LENGTH_LONG).show()
        }
        return null
    }

    fun saveBitmapToStorage(context: Context, bitmap: Bitmap, uri: Uri): Uri? {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val storePath = "/storage/emulated/0/DCIM/Screenshots"
//        executor.execute {
        val appDir = File(storePath)
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val fileName = "Crop_image" + System.currentTimeMillis() + ".jpg"
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            MediaScannerConnection.scanFile(
                context, arrayOf(file.toString()),
                null, null
            )
            return file.toUri()
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap.recycle()
        }
        handler.post {
            bitmap.recycle()
        }
        return null
//        }
    }

    fun scaleImage(
        realImage: Bitmap, maxHeight: Float, maxWidth: Float,
        filter: Boolean
    ): Bitmap? {
        val ratio = (maxWidth / realImage.width).coerceAtMost(maxHeight / realImage.height)
        val width = (ratio * realImage.width).roundToInt()
        val height = (ratio * realImage.height).roundToInt()
        return Bitmap.createScaledBitmap(realImage, width, height, filter)
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        adView.resume()
        super.onResume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    private fun loadBannerAds() {
        adView.adUnitId = AppConfig.ID_ADS_BANNER_EDIT_SCAN
        adView.setAdSize(getAdSize(binding.adViewContainer))

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(loadAdsError: LoadAdError) {
                super.onAdFailedToLoad(loadAdsError)
                getBaseActivity()?.setLogDataToFirebase(
                    AdsLogModel(
                        adsId = AppConfig.ID_ADS_BANNER_EDIT_SCAN, adsName = "Ads Banner Edit Scan", message = loadAdsError.message,
                        deviceName = Common.getDeviceName(getBaseActivity())
                    )
                )
            }

            override fun onAdLoaded() {
                Common.setEventAdsBanner(AppConfig.ID_ADS_BANNER_EDIT_SCAN)
            }
        }

    }
}