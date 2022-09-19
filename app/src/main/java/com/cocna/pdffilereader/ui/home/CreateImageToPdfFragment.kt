package com.cocna.pdffilereader.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentCreatePdfFromImageBinding
import com.cocna.pdffilereader.imagepicker.model.GridCount
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.imagepicker.model.ImagePickerConfig
import com.cocna.pdffilereader.imagepicker.ui.imagepicker.registerImagePicker
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.home.adapter.ViewPagerImageAdapter
import com.cocna.pdffilereader.ui.home.adapter.ViewPagerRecyclerAdapter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.InputStream


/**
 * Created by Thuytv on 16/09/2022.
 */
class CreateImageToPdfFragment : BaseFragment<FragmentCreatePdfFromImageBinding>(), View.OnClickListener {
    private var lstImageSelected: ArrayList<Image>? = null
//    lateinit var viewPagerAdapter: ViewPagerImageAdapter
    lateinit var viewPagerAdapter: ViewPagerRecyclerAdapter
    private var isCropClick = false
    private var totalImage = 0

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
    }

    override fun initEvents() {
        listenClickViews(binding.btnImageAdd, binding.btnImageRotate, binding.btnImageCrop, binding.btnImageDelete, binding.btnSavePdf)

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
            R.id.btn_image_add -> {
                selectImage()
            }
            R.id.btn_image_rotate -> {
                val imageCurrent = binding.viewPagerImage.currentItem
                if (imageCurrent > -1) {
                    val itemView = binding.viewPagerImage.findViewWithTag<View>("VIEW$imageCurrent")
                    viewPagerAdapter.rotateImage(itemView, imageCurrent)
                }
            }
            R.id.btn_image_crop -> {
                isCropClick = !isCropClick
                val imageCurrent = binding.viewPagerImage.currentItem
                if (imageCurrent > -1) {
                    val itemView = binding.viewPagerImage.findViewWithTag<View>("VIEW$imageCurrent")
                    if (isCropClick) {
                        viewPagerAdapter.cropImage(itemView, imageCurrent)
                    } else {
                        viewPagerAdapter.saveCropImage(itemView, imageCurrent)
                    }
                }
            }
            R.id.btn_image_delete -> {
                val imageCurrent = binding.viewPagerImage.currentItem
                if (imageCurrent > -1) {
                    Logger.showLog("Thuytv--1----lstImageSelected---:" + lstImageSelected?.size)
                    viewPagerAdapter.deleteImage(imageCurrent)
                    Logger.showLog("Thuytv---2---lstImageSelected---:" + lstImageSelected?.size)
//                    lstImageSelected?.removeAt(imageCurrent)
                }

            }
            R.id.btn_save_pdf -> {
                saveImageToPDF()
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

    private fun saveImageToPDF() {
        val document = PDDocument()
        var isHorizontal = false
        lstImageSelected?.let {
            for (image in it) {
                image.uri?.let {
                    val inputStream: InputStream? = getBaseActivity()?.contentResolver?.openInputStream(it)
                    if (inputStream != null) {
                        var bimg = BitmapFactory.decodeStream(inputStream)
                        if(image.rotate > 0) {
                            val matrix = Matrix()
                            matrix.postRotate(image.rotate.toFloat())
                            val rotateBitmap = Bitmap.createBitmap(bimg,0,0,bimg.width, bimg.height,matrix, true)
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
                        val page = PDPage()
                        document.addPage(page)
//                        val img: PDImageXObject = LosslessFactory.createFromImage(document, bimg)
                        val img: PDImageXObject = JPEGFactory.createFromImage(document, bimg)
                        val contentStream = PDPageContentStream(document, page)

//                        contentStream.drawImage(img, 20f, 20f)
//                        contentStream.drawImage(img, 20f, 20f,img.width/300f*72, img.height/300f *72)
                        contentStream.drawImage(img, 20f, 20f, actualPDFWidth.toFloat() - 40f, actualPDFHeight.toFloat() - 40f)
                        contentStream.close()
                        inputStream.close()
                    }
                }
            }
        }
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + "test.pdf"
        document.save(path)
        document.close()
        Toast.makeText(getBaseActivity(), "Save Image Success", Toast.LENGTH_LONG).show()
    }
}