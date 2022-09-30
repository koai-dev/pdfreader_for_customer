package com.cocna.pdffilereader.imagepicker.ui.imagepicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.databinding.ImagepickerActivityImagepickerBinding
import com.cocna.pdffilereader.imagepicker.helper.*
import com.cocna.pdffilereader.imagepicker.listener.OnFolderClickListener
import com.cocna.pdffilereader.imagepicker.listener.OnImageSelectListener
import com.cocna.pdffilereader.imagepicker.model.Folder
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.imagepicker.model.ImagePickerConfig
import com.cocna.pdffilereader.imagepicker.ui.camera.CameraModule
import com.cocna.pdffilereader.imagepicker.ui.camera.OnImageReadyListener
import com.google.gson.Gson

class ImagePickerActivity : AppCompatActivity(), OnFolderClickListener, OnImageSelectListener {

    private lateinit var binding: ImagepickerActivityImagepickerBinding
    private lateinit var config: ImagePickerConfig

    private lateinit var viewModel: ImagePickerViewModel
    private val cameraModule = CameraModule()

    private val backClickListener = View.OnClickListener { onBackPressed() }
    private val cameraClickListener = View.OnClickListener { captureImageWithPermission() }
    private val doneClickListener = View.OnClickListener { onDone() }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cameraModule.saveImage(
                    this@ImagePickerActivity,
                    config,
                    object : OnImageReadyListener {
                        override fun onImageReady(images: ArrayList<Image>) {
                            Logger.showLog("Thuytv-----onImageReady: " + Gson().toJson(images))
                            MediaScannerConnection.scanFile(
                                this@ImagePickerActivity, arrayOf(images.get(0).uri?.path), null
                            ) { p0, p1 ->
                                Handler(Looper.myLooper()!!).postDelayed({
                                    fetchDataWithPermission()
                                }, 1500)
                            }
                        }

                        override fun onImageNotReady() {
                            fetchDataWithPermission()
                        }
                    })
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null) {
            finish()
            return
        }

        config = intent.getParcelableExtra(Constants.EXTRA_CONFIG)!!
        config.initDefaultValues(this@ImagePickerActivity)

        // Setup status bar theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor(config.statusBarColor)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                config.isLightStatusBar
        }

        binding = ImagepickerActivityImagepickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, ImagePickerViewModelFactory(this.application)).get(
            ImagePickerViewModel::class.java
        )
        viewModel.setConfig(config)
        viewModel.selectedImages.observe(this) {
//            binding.toolbar.showDoneButton(config.isAlwaysShowDoneButton || it.isNotEmpty())
            if (binding.btnSelectDone.visibility == View.GONE) {
                binding.btnSelectDone.visibility = View.VISIBLE
            }
            if (!isFinishing && !isDestroyed) {
                if (it.isNullOrEmpty()) {
                    binding.btnSelectDone.text = getString(R.string.btn_add_image)
                } else {
                    binding.btnSelectDone.text = getString(R.string.btn_add_image_num, it.size)
                }
            }
            binding.btnSelectDone.isEnabled = it.isNotEmpty()
        }

        setupViews()

    }

    override fun onResume() {
        super.onResume()
        fetchDataWithPermission()
    }

    private fun setupViews() {
        binding.toolbar.apply {
            config(config)
            setOnBackClickListener(backClickListener)
            setOnCameraClickListener(cameraClickListener)
            setOnDoneClickListener(doneClickListener)
        }
        binding.btnSelectDone.setOnClickListener {
            onDone()
        }

        val initialFragment =
            if (config.isFolderMode) FolderFragment.newInstance(config.folderGridCount)
            else ImageFragment.newInstance(config.imageGridCount)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, initialFragment)
            .commit()
    }


    private fun fetchDataWithPermission() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        PermissionHelper.checkPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            object : PermissionHelper.PermissionAskListener {
                override fun onNeedPermission() {
                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        permissions,
                        Constants.RC_READ_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionPreviouslyDenied() {
                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        permissions,
                        Constants.RC_READ_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionDisabled() {
                    binding.snackbar.show(
                        R.string.imagepicker_msg_no_external_storage_permission
                    ) {
                        PermissionHelper.openAppSettings(this@ImagePickerActivity)
                    }
                }

                override fun onPermissionGranted() {
                    fetchData()
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.RC_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (PermissionHelper.hasGranted(grantResults)) {
                    fetchData()
                } else {
                    finish()
                }
            }
            Constants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                if (PermissionHelper.hasGranted(grantResults)) {
                    captureImage()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun fetchData() {
        viewModel.fetchImages()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment != null && fragment is FolderFragment) {
            binding.toolbar.setTitle(config.folderTitle)
        }
    }

    private fun onDone() {
        val selectedImages = viewModel.selectedImages.value
        finishPickImages(selectedImages ?: arrayListOf())
    }


    private fun captureImageWithPermission() {
        if (DeviceHelper.isMinSdk29) {
            captureImage()
            return
        }

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        PermissionHelper.checkPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            object : PermissionHelper.PermissionAskListener {
                override fun onNeedPermission() {
                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        permissions,
                        Constants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionPreviouslyDenied() {
                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        permissions,
                        Constants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionDisabled() {
                    binding.snackbar.show(
                        R.string.imagepicker_msg_no_external_storage_permission
                    ) {
                        PermissionHelper.openAppSettings(this@ImagePickerActivity)
                    }
                }

                override fun onPermissionGranted() {
                    captureImage()
                }
            })
    }


    fun captureImage() {
        if (!DeviceHelper.checkCameraAvailability(this)) {
            return
        }

        val intent = cameraModule.getCameraIntent(this@ImagePickerActivity, config)
        if (intent == null) {
            ToastHelper.show(this, getString(R.string.imagepicker_error_open_camera))
            return
        }

        resultLauncher.launch(intent)
    }

    private fun finishPickImages(images: ArrayList<Image>) {
        val data = Intent()
        data.putParcelableArrayListExtra(Constants.EXTRA_IMAGES, images)
        setResult(Activity.RESULT_OK, data)
        finish()
    }


    override fun onFolderClick(folder: Folder) {
        supportFragmentManager.beginTransaction().add(
            R.id.fragmentContainer,
            ImageFragment.newInstance(folder.bucketId ?: 0, config.imageGridCount)
        )
            .addToBackStack(null)
            .commit()
        binding.toolbar.setTitle(folder.name)
    }

    override fun onSelectedImagesChanged(selectedImages: ArrayList<Image>) {
        viewModel.selectedImages.value = selectedImages
    }

    override fun onSingleModeImageSelected(image: Image) {
        finishPickImages(ImageHelper.singleListFromImage(image))
    }
}