package com.igi.office.ui.home

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.anggrayudi.storage.file.*
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.google.gson.Gson
import com.igi.office.R
import com.igi.office.common.*
import com.igi.office.databinding.FragmentMyFilesBinding
import com.igi.office.ui.base.BaseFragment
import com.igi.office.ui.home.adapter.MyFilesAdapter
import com.igi.office.ui.home.model.MyFilesModel
import io.reactivex.disposables.Disposable

/**
 * Created by Thuytv on 09/06/2022.
 */
class MyFilesFragment : BaseFragment<FragmentMyFilesBinding>(), View.OnClickListener {
    private lateinit var externalLauncher: ActivityResultLauncher<String>
    private lateinit var launcher: ActivityResultLauncher<String>
    private lateinit var mRecentAdapter: MyFilesAdapter

    private lateinit var lstAllFile: ArrayList<MyFilesModel>
    private lateinit var lstFilePdf: ArrayList<MyFilesModel>
    private lateinit var lstFileWord: ArrayList<MyFilesModel>
    private lateinit var lstFileExcel: ArrayList<MyFilesModel>
    private lateinit var lstFilePowerPoint: ArrayList<MyFilesModel>

    private var eventsBusDisposable: Disposable? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyFilesBinding = FragmentMyFilesBinding::inflate
    override fun initData() {
        lstAllFile = ArrayList()
        lstFilePdf = ArrayList()
        lstFileWord = ArrayList()
        lstFileExcel = ArrayList()
        lstFilePowerPoint = ArrayList()
        onListenReloadFile()
        setupRecycleView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            executeWithPerm {
                Handler(Looper.myLooper()!!).postDelayed({
                    getAllFilePdf(false)
                },500)
            }
        }

    }

    override fun initEvents() {
        listenClickViews(binding.llMyFile, binding.llPdf, binding.llWord, binding.llExcel, binding.llPowerPoint)
    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
        when (v?.id) {
            R.id.llMyFile -> {
                gotoMyFileDetail(lstAllFile, getString(R.string.tt_all_file))
            }
            R.id.llPdf -> {
                gotoMyFileDetail(lstFilePdf, getString(R.string.tt_pdf_file))
            }
            R.id.llWord -> {
                gotoMyFileDetail(lstFileWord, getString(R.string.tt_word_file))
            }
            R.id.llExcel -> {
                gotoMyFileDetail(lstFileExcel, getString(R.string.tt_excel_file))
            }
            R.id.llPowerPoint -> {
                gotoMyFileDetail(lstFilePowerPoint, getString(R.string.tt_power_point_file))
            }
        }
    }

    private fun gotoMyFileDetail(lstData: ArrayList<MyFilesModel>, strTitle: String) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(AppKeys.KEY_BUNDLE_DATA, lstData)
        bundle.putString(AppKeys.KEY_BUNDLE_SCREEN, strTitle)
        getBaseActivity()?.onNextScreen(MyFileDetailActivity::class.java, bundle, false)
    }

    private fun setupRecycleView() {
        mRecentAdapter = MyFilesAdapter(context, ArrayList(), 0, object : MyFilesAdapter.OnItemClickListener {
            override fun onClickItem(documentFile: MyFilesModel) {
                Logger.showLog("Thuytv-----documentFile: " + documentFile.name)
                val bundle = Bundle()
                bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, documentFile.uri)
                getBaseActivity()?.onNextScreen(PdfViewActivity::class.java, bundle, false)
            }

            override fun onClickItemMore(view: View, documentFile: MyFilesModel) {
            }
        })
        binding.rcvRecentFile.apply {
            layoutManager = LinearLayoutManager(getBaseActivity())
            adapter = mRecentAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (eventsBusDisposable?.isDisposed == false) eventsBusDisposable?.dispose()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun <T> executeWithPerm(code: () -> T) {
        if (!PermissionUtil.checkExternalStoragePermission(requireContext())) {
            PermissionUtil.sdk29andMore {
                requestManageExternalStoragePermission(code)
            } ?: requestReadExternalStoragePermission(code)
        } else code()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun <T> requestManageExternalStoragePermission(permissionGranted: () -> T) {
        val alertBox = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.tt_request_store_permission))
            .setMessage(getString(R.string.vl_request_store_permission_content))
            .setPositiveButton(getString(R.string.btn_continue)) { _, _ ->
                launcher.launch(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
        launcher = registerForActivityResult(StoragePermissionContract()) {
            when (it) {
                true -> {
                    permissionGranted()
                }
                false -> alertBox.show()
            }
        }

        alertBox.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun <T> requestReadExternalStoragePermission(permissionGranted: () -> T) {
        val settingsDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.tt_request_store_permission))
            .setMessage(getString(R.string.vl_request_read_permission_content))
            .setPositiveButton(getString(R.string.btn_continue)) { _, _ ->
                launcher.launch(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            }


        launcher = registerForActivityResult(AppSettingsContracts()) {
            when (it) {
                true -> {
                    permissionGranted()
                }
                false -> settingsDialog.show()
            }
        }

        val permissionDeniedDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.tt_request_store_permission))
            .setMessage(getString(R.string.vl_request_read_permission_content))
            .setPositiveButton(getString(R.string.btn_continue)) { _, _ ->
                externalLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

        externalLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            when {
                it -> permissionGranted()
                !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    Log.e("write permission", "denied forever")
                    settingsDialog.show()
                }
                else -> {
                    Log.e("write permission", "denied")
                    permissionDeniedDialog.show()
                }

            }
        }
        externalLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }

    private fun getAllFilePdf(isReload: Boolean) {
        val root = DocumentFileCompat.getRootDocumentFile(requireContext(), "primary", true)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")!!
        val pdfArray = root?.search(true, DocumentFileType.FILE, arrayOf(mime))
        Log.e("lstFilePdf", pdfArray?.size.toString())
        if ((pdfArray?.size ?: 0) > 0) {
//            mRecentAdapter.updateData(pdfArray as ArrayList<DocumentFile>)
            if (isReload) {
                lstFilePdf.clear()
            }
            for (item in pdfArray!!) {
                val model = MyFilesModel(name = item.name, uri = item.uri, lastModified = item.lastModified(), extensionName = item.extension, length = item.length())
                lstFilePdf.add(model)
                lstAllFile.add(model)
            }
        }
        binding.vlHomePdf.text = getString(R.string.vl_home_pdf, lstFilePdf.size)
        if (!isReload) {
            getAllFileWord(isReload)
        }
    }

    private fun getAllFileWord(isReload: Boolean) {
        val root = DocumentFileCompat.getRootDocumentFile(requireContext(), "primary", true)
        val mimeDoc = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")!!
        val mimeDocX = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")!!
        val pdfArray = root?.search(true, DocumentFileType.FILE, arrayOf(mimeDoc, mimeDocX))
        Log.e("lstFileWord", pdfArray?.size.toString())
        if ((pdfArray?.size ?: 0) > 0) {
            if (isReload) {
                lstFileWord.clear()
            }
            for (item in pdfArray!!) {
                val model = MyFilesModel(name = item.name, uri = item.uri, lastModified = item.lastModified(), extensionName = item.extension, length = item.length())
                lstFileWord.add(model)
                lstAllFile.add(model)
            }
        }
        binding.vlHomeWord.text = getString(R.string.vl_home_word, lstFileWord.size)
        if (!isReload) {
            getAllFileExcel(isReload)
        }
    }

    private fun getAllFileExcel(isReload: Boolean) {
        val root = DocumentFileCompat.getRootDocumentFile(requireContext(), "primary", true)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx")!!
        val mimeXls = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls")!!
        val pdfArray = root?.search(true, DocumentFileType.FILE, arrayOf(mime, mimeXls))
        Log.e("lstFileExcel", pdfArray?.size.toString())
        if ((pdfArray?.size ?: 0) > 0) {
            if (isReload) {
                lstFileExcel.clear()
            }
            for (item in pdfArray!!) {
                val model = MyFilesModel(name = item.name, uri = item.uri, lastModified = item.lastModified(), extensionName = item.extension, length = item.length())
                lstFileExcel.add(model)
                lstAllFile.add(model)
            }
        }
        binding.vlHomeExcel.text = getString(R.string.vl_home_excel, lstFileExcel.size)
        if (!isReload) {
            getAllFilePowpoint(isReload)
        }
    }

    private fun getAllFilePowpoint(isReload: Boolean) {
        val root = DocumentFileCompat.getRootDocumentFile(requireContext(), "primary", true)
        val mimePPT = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt")!!
        val mimePPTX = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx")!!
        val pdfArray = root?.search(true, DocumentFileType.FILE, arrayOf(mimePPT, mimePPTX))
        Log.e("lstFilePowpoint", pdfArray?.size.toString())
        if ((pdfArray?.size ?: 0) > 0) {
            if (isReload) {
                lstFilePowerPoint.clear()
            }
            for (item in pdfArray!!) {
                val model = MyFilesModel(name = item.name, uri = item.uri, lastModified = item.lastModified(), extensionName = item.extension, length = item.length())
                lstFilePowerPoint.add(model)
                lstAllFile.add(model)
            }
        }
        binding.vlHomePowerPoint.text = getString(R.string.vl_home_power_point, lstFilePowerPoint.size)
        binding.vlHomeMyFile.text = getString(R.string.vl_home_my_file, lstAllFile.size)
        Log.e("lstAllFile", lstAllFile.size.toString())
    }

    private fun onListenReloadFile() {
        eventsBusDisposable = RxBus.listenDeBounce(EventsBus::class.java).subscribe {
            when {
                EventsBus.RELOAD_ALL_FILE == it -> {
                    lstAllFile.clear()
                    lstFilePdf.clear()
                    lstFileWord.clear()
                    lstFileExcel.clear()
                    lstFilePowerPoint.clear()
                    getAllFilePdf(false)
                }
                EventsBus.RELOAD_PDF_FILE == it -> {
                    lstAllFile.removeAll(lstFilePdf.toSet())
                    getAllFilePdf(true)
                }
                EventsBus.RELOAD_WORD_FILE == it -> {
                    lstAllFile.removeAll(lstFileWord.toSet())
                    getAllFileWord(true)
                }
                EventsBus.RELOAD_EXCEL_FILE == it -> {
                    lstAllFile.removeAll(lstFileExcel.toSet())
                    getAllFileExcel(true)
                }
                EventsBus.RELOAD_POWER_POINT_FILE == it -> {
                    lstAllFile.removeAll(lstFilePowerPoint.toSet())
                    getAllFilePowpoint(true)
                }
            }
            RxBus.removeEvent()
        }
    }
}