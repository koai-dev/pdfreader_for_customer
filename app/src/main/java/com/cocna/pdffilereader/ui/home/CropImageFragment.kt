package com.cocna.pdffilereader.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentCropImageBinding
import com.cocna.pdffilereader.myinterface.OnShowAdsBackListener
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.base.OnCallbackLoadAds
import com.cocna.pdffilereader.ui.base.OnCallbackTittleTab
import com.cocna.pdffilereader.ui.home.dialog.ConfirmDeleteDialog
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * Created by Thuytv on 10/08/2022.
 */
class CropImageFragment : BaseFragment<FragmentCropImageBinding>(), View.OnClickListener {
    //    private var strBitmap: String? = null
    private var mBitmap: Bitmap? = null
    private var strFileName = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCropImageBinding
        get() = FragmentCropImageBinding::inflate

    companion object {
        private var mOnCallbackLoadAds: OnShowAdsBackListener? = null
        fun newInstance(onCallbackLoadAds: OnShowAdsBackListener?): CropImageFragment {
            val fragment = CropImageFragment()
            mOnCallbackLoadAds = onCallbackLoadAds
            return fragment
        }
    }

    override fun initData() {
//        strBitmap = arguments?.getString(AppKeys.KEY_BUNDLE_DATA)
        val timeStamp: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        strFileName = "Screen_capture-$timeStamp"
        binding.ttToolbarCrop.text = strFileName
        mBitmap = arguments?.getParcelable(AppKeys.KEY_BUNDLE_DATA)

        mBitmap?.apply {
            binding.imvCropImage.setImageBitmap(this)
            binding.imvViewImage.setImageBitmap(this)
        }
    }

    override fun initEvents() {
        listenClickViews(binding.imvBackCrop, binding.btnImageCrop, binding.btnImageSave, binding.btnImageDelete)
    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
        when (v?.id) {
            R.id.imv_back_crop -> {
                mOnCallbackLoadAds?.onShowAds()
                onBackFragment()
            }
            R.id.btn_image_delete -> {
                getBaseActivity()?.apply {
                    ConfirmDeleteDialog(this, object : ConfirmDeleteDialog.OnDialogItemClickListener {
                        override fun onClickItemYes() {
                            mOnCallbackLoadAds?.onShowAds()
                            getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_CAPTURE_FILE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_DELETE)
                            onBackFragment()
                        }
                    }).show()
                }

            }
            R.id.btn_image_crop -> {
                if (binding.imvViewImage.visibility == View.VISIBLE) {
                    binding.imvViewImage.gone()
                    binding.imvCropImage.visible()
                    binding.imvImageCrop.setImageResource(R.drawable.ic_crop_selected)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.vlImageCrop.setTextColor(resources.getColor(R.color.rgb_F44336, getBaseActivity()?.theme))
                    } else {
                        binding.vlImageCrop.setTextColor(resources.getColor(R.color.rgb_F44336))
                    }
                } else {
                    binding.imvViewImage.visible()
                    binding.imvCropImage.gone()
                    binding.imvImageCrop.setImageResource(R.drawable.ic_crop)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.vlImageCrop.setTextColor(resources.getColor(R.color.rgb_62757F, getBaseActivity()?.theme))
                    } else {
                        binding.vlImageCrop.setTextColor(resources.getColor(R.color.rgb_62757F))
                    }
                }
            }
            R.id.btn_image_save -> {
                getBaseActivity()?.apply {
                    if (binding.imvCropImage.visibility == View.VISIBLE) {
                        mBitmap = binding.imvCropImage.croppedImage
                    }
                    if (mBitmap != null) {
                        saveBitmapToStorage(this, mBitmap!!)
                    }
                    this.logEventFirebase(AppConfig.KEY_EVENT_FB_CAPTURE_FILE, AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_SAVE)

                }
            }
        }
    }

    private fun saveBitmapToStorage(context: Context, bitmap: Bitmap) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val storePath = "/storage/emulated/0/DCIM/Screenshots"
        executor.execute {
            val appDir = File(storePath)
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val fileName = strFileName + System.currentTimeMillis() + ".jpg"
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
            } catch (e: IOException) {
                e.printStackTrace()
                bitmap.recycle()
            }
            handler.post {
                bitmap.recycle()
                showSnackbarSaveImage()
            }
        }
    }

    private fun showSnackbarSaveImage() {
        binding.llShowNotify.vlContentNotify.text = getString(R.string.msg_save_image_success)
        binding.llShowNotify.llShowNotification.visible()
        binding.llShowNotify.imvIconNotify.setOnClickListener {
            binding.llShowNotify.llShowNotification.gone()
        }
        Handler(Looper.myLooper()!!).postDelayed({
            if (isVisible && getBaseActivity()?.isFinishing == false) {
                binding.llShowNotify.llShowNotification.gone()
                mOnCallbackLoadAds?.onShowAds()
                onBackFragment()
            }
        }, 1000)
    }
}