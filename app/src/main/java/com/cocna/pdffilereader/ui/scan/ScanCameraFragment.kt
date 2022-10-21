package com.cocna.pdffilereader.ui.scan

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentScanCameraBinding
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.home.CreateImageToPdfActivity
import com.cocna.pdffilereader.ui.scan.adapter.PageAdapter
import com.cocna.pdffilereader.ui.scan.adapter.PageModel
import com.cocna.pdffilereader.ui.scan.adapter.ScreenUtils
import com.cocna.pdffilereader.ui.scan.adapter.SliderLayoutManager
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Engine
import com.otaliastudios.cameraview.controls.Facing
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors


/**
 * Created by Thuytv on 12/10/2022.
 */
class ScanCameraFragment : BaseFragment<FragmentScanCameraBinding>(), View.OnClickListener {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentScanCameraBinding
        get() = FragmentScanCameraBinding::inflate
    val lstImageCapture: ArrayList<Image> = ArrayList()
    private var isSinglePage = false

    override fun initData() {
        binding.ttToolbarScan.text = getString(R.string.tt_scan_document)
        setupCamera()
        setupPageMode()
    }

    override fun initEvents() {
        listenClickViews(binding.imvBackScan, binding.imvCaptureImage, binding.btnCaptureNext)
    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
        when (v?.id) {
            R.id.imv_back_scan -> {
                getBaseActivity()?.finish()
            }
            R.id.imv_capture_image -> {
                if (binding.cameraViewScan.isTakingPicture) return
                binding.cameraViewScan.takePicture()
            }
            R.id.btn_capture_next -> {
                gotoCreatPdf()
            }
        }
    }

    private fun gotoCreatPdf() {
        val bundle = Bundle()
        bundle.putParcelableArrayList(AppKeys.KEY_BUNDLE_DATA, lstImageCapture)
        getBaseActivity()?.onNextScreen(CreateImageToPdfActivity::class.java, bundle, true)
    }

    override fun onResume() {
        super.onResume()
        binding.cameraViewScan.open()
    }

    override fun onPause() {
        super.onPause()
        binding.cameraViewScan.close()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        binding.cameraViewScan?.destroy()
//    }

    private fun setupCamera() {
        binding.cameraViewScan.facing = Facing.BACK
        binding.cameraViewScan.engine = Engine.CAMERA2
        binding.cameraViewScan.playSounds = true
        binding.cameraViewScan.setLifecycleOwner(this)
//        binding.cameraViewScan.addFrameProcessor {
//            it.size.apply {
//                mFaceDetector.process(
//                    Frame(
//                        data = it.getData(),
//                        rotation = it.rotationToUser,
//                        size = Size(width, height),
//                        format = it.format,
//                        lensFacing = if (binding.cameraViewScan.facing == Facing.BACK) LensFacing.BACK else LensFacing.FRONT
//                    )
//                )
//            }
//        }
//        mFaceDetector.setOnFaceDetectionResultListener(this)
        binding.cameraViewScan.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                val bitmap = BitmapFactory.decodeByteArray(result.data, 0, result.data.size)
                binding.imvThumbnail.setImageBitmap(bitmap)
                saveBitmapToDisk(bitmap)
                Logger.showLog("Thuytv------size: " + lstImageCapture.size)
            }
        })
    }

    private fun saveBitmapToDisk(bitmap: Bitmap?) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val appDir = File("/storage/emulated/0/DCIM/Camera")
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val fileName = "imageName" + System.currentTimeMillis() + ".jpg"
            val file = File(appDir, fileName)
            try {
                val fos = FileOutputStream(file)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                MediaScannerConnection.scanFile(
                    context, arrayOf(file.toString()),
                    null
                ) { _, uri ->
                    lstImageCapture.add(Image(uri = uri, name = fileName))
                    getBaseActivity()?.runOnUiThread {
                        if (isVisible) {
                            if (binding.btnCaptureNext.visibility == View.GONE) {
                                binding.btnCaptureNext.visible()
                                binding.vlCountImage.visible()
                            }
                            binding.vlCountImage.text = lstImageCapture.size.toString()
                            if (isSinglePage) {
                                gotoCreatPdf()
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupPageMode() {
        val lstPageMode = ArrayList<PageModel>()
        lstPageMode.add(PageModel(name = getString(R.string.vl_multiple_page), isSelected = true))
        lstPageMode.add(PageModel(name = getString(R.string.vl_single_page), isSelected = false))
        val pageAdapter = PageAdapter(getBaseActivity(), lstPageMode, object : PageAdapter.OnItemPageClickListener {
            override fun onClickItem(position: Int) {
                isSinglePage = position == 1
                binding.rcvPageMode.smoothScrollToPosition(position)
            }

        })
        val mLayoutManager = SliderLayoutManager(getBaseActivity(), LinearLayoutManager.HORIZONTAL, binding.rcvPageMode)
        binding.rcvPageMode.apply {
            layoutManager = mLayoutManager
            adapter = pageAdapter
        }
        val padding: Int = ScreenUtils.getScreenWidth(getBaseActivity()!!) / 2 - ScreenUtils.dpToPx(getBaseActivity()!!, 50)
        binding.rcvPageMode.setPadding(padding, 0, padding, 0)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rcvPageMode)
        mLayoutManager.callback = object : SliderLayoutManager.OnItemSelectedListener {
            override fun onItemSelected(layoutPosition: Int) {
                isSinglePage = layoutPosition == 1
                val data = lstPageMode[layoutPosition]
                pageAdapter.updateData(data)

            }

        }
    }

    private fun selectMiddleItem(layoutManager: LinearLayoutManager) {
        val firstVisibleIndex = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleIndex = layoutManager.findLastVisibleItemPosition()
        val visibleIndexes = listOf(firstVisibleIndex..lastVisibleIndex).flatten()
        val screenWidth: Int = Resources.getSystem().getDisplayMetrics().widthPixels
        for (i in visibleIndexes) {
            val vh = binding.rcvPageMode.findViewHolderForLayoutPosition(i)
            if (vh?.itemView == null) {
                continue
            }
            val location = IntArray(2)
            vh.itemView.getLocationOnScreen(location)
            val x = location[0]
            val halfWidth = vh.itemView.width * .5
            val rightSide = x + halfWidth
            val leftSide = x - halfWidth
            val isInMiddle = screenWidth * .5 in leftSide..rightSide
            if (isInMiddle) {
                // "i" is your middle index and implement selecting it as you want
                // optionsAdapter.selectItemAtIndex(i)
                return
            }
        }
    }
}