package com.igi.office.ui.home

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.callback.StorageAccessCallback
import com.anggrayudi.storage.file.*
import com.anggrayudi.storage.permission.ActivityPermissionRequest
import com.anggrayudi.storage.permission.PermissionCallback
import com.anggrayudi.storage.permission.PermissionReport
import com.anggrayudi.storage.permission.PermissionResult
import com.google.gson.Gson
import com.igi.office.R
import com.igi.office.common.*
import com.igi.office.databinding.FragmentMyFilesBinding
import com.igi.office.myinterface.OnDialogItemClickListener
import com.igi.office.myinterface.OnPopupMenuItemClickListener
import com.igi.office.ui.base.BaseFragment
import com.igi.office.ui.home.adapter.MyFilesAdapter
import com.igi.office.ui.home.dialog.DeleteFileDialog
import com.igi.office.ui.home.dialog.RenameFileDialog
import com.igi.office.ui.home.model.MyFilesModel
import io.reactivex.disposables.Disposable
import java.io.File
import java.security.Permissions

/**
 * Created by Thuytv on 09/06/2022.
 */
class MyFilesFragment : BaseFragment<FragmentMyFilesBinding>(), View.OnClickListener {
    private lateinit var externalLauncher: ActivityResultLauncher<String>
    private lateinit var launcher: ActivityResultLauncher<String>
    private var mRecentAdapter: MyFilesAdapter? = null

    //    private lateinit var lstAllFile: ArrayList<MyFilesModel>
    private lateinit var lstFilePdf: ArrayList<MyFilesModel>
//    private lateinit var lstFileWord: ArrayList<MyFilesModel>
//    private lateinit var lstFileExcel: ArrayList<MyFilesModel>
//    private lateinit var lstFilePowerPoint: ArrayList<MyFilesModel>

    private var eventsBusDisposable: Disposable? = null
    private var rxBusDisposable: Disposable? = null
    private var PATH_DEFAULT_STORE = "/storage/emulated/0"

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyFilesBinding = FragmentMyFilesBinding::inflate
    override fun initData() {
//        lstAllFile = ArrayList()
        lstFilePdf = ArrayList()
//        lstFileWord = ArrayList()
//        lstFileExcel = ArrayList()
//        lstFilePowerPoint = ArrayList()
        onListenReloadFile()
        onListenUpdateFile()
        setupRecycleView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            executeWithPerm {
                binding.prbLoadingFile.visible()
                Handler(Looper.myLooper()!!).postDelayed({
                    getAllFilePdf(false)
                }, 500)
            }
        }
    }

    override fun initEvents() {
        listenClickViews(binding.llMyFile, binding.llPdf, binding.llWord, binding.llExcel, binding.llPowerPoint, binding.imvReloadRecent, binding.imvRecentMore)
    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
        when (v?.id) {
//            R.id.llMyFile -> {
//                gotoMyFileDetail(lstAllFile, getString(R.string.tt_all_file))
//            }
            R.id.llPdf -> {
                gotoMyFileDetail(lstFilePdf, getString(R.string.tt_pdf_file))
            }
//            R.id.llWord -> {
//                gotoMyFileDetail(lstFileWord, getString(R.string.tt_word_file))
//            }
//            R.id.llExcel -> {
//                gotoMyFileDetail(lstFileExcel, getString(R.string.tt_excel_file))
//            }
//            R.id.llPowerPoint -> {
//                gotoMyFileDetail(lstFilePowerPoint, getString(R.string.tt_power_point_file))
//            }
            R.id.imvReloadRecent -> {
                reloadRecentFile()
            }
            R.id.imvRecentMore -> {
                showPopupRecentMore()
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
        val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
        Logger.showLog("Thuytv------lstRecent: " + Gson().toJson(lstRecent))
        mRecentAdapter = MyFilesAdapter(context, lstRecent ?: ArrayList(), 0, object : MyFilesAdapter.OnItemClickListener {
            override fun onClickItem(documentFile: MyFilesModel) {
                val bundle = Bundle()
                bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, documentFile)
                getBaseActivity()?.onNextScreen(PdfViewActivity::class.java, bundle, false)
            }

            override fun onClickItemMore(view: View, documentFile: MyFilesModel) {
                showPopupItemRecentMore(view, documentFile)
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
        if (rxBusDisposable?.isDisposed == false) rxBusDisposable?.dispose()
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    launcher.launch(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                }
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
        var root = DocumentFileCompat.getRootDocumentFile(requireContext(), "primary", true)
        if (root == null) {
            root = DocumentFile.fromFile(File(PATH_DEFAULT_STORE))
        }
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")!!
        val pdfArray = root.search(true, DocumentFileType.FILE, arrayOf(mime))
        Log.e("lstFilePdf", pdfArray?.size.toString())
        if ((pdfArray?.size ?: 0) > 0) {
//            mRecentAdapter.updateData(pdfArray as ArrayList<DocumentFile>)
            if (isReload) {
//                lstAllFile.removeAll(lstFilePdf.toSet())
                lstFilePdf.clear()
            }
            for (item in pdfArray!!) {
                val model =
                    MyFilesModel(
                        name = item.name,
                        uriPath = item.uri.path,
                        uriOldPath = item.uri.path,
                        lastModified = item.lastModified(),
                        extensionName = item.extension,
                        length = item.length()
                    )
                lstFilePdf.add(model)
//                lstAllFile.add(model)
            }
        }
        binding.vlHomePdf.text = getString(R.string.vl_home_pdf, lstFilePdf.size)
//        if (!isReload) {
//            getAllFileWord(isReload)
//        }
        binding.prbLoadingFile.gone()
    }
//
//    private fun getAllFileWord(isReload: Boolean) {
//        var root = DocumentFileCompat.getRootDocumentFile(requireContext(), "primary", true)
//        if (root == null) {
//            root = DocumentFile.fromFile(File(PATH_DEFAULT_STORE))
//        }
//        val mimeDoc = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")!!
//        val mimeDocX = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")!!
//        val pdfArray = root.search(true, DocumentFileType.FILE, arrayOf(mimeDoc, mimeDocX))
//        Log.e("lstFileWord", pdfArray?.size.toString())
//        if ((pdfArray?.size ?: 0) > 0) {
//            if (isReload) {
//                lstAllFile.removeAll(lstFileWord.toSet())
//                lstFileWord.clear()
//            }
//            for (item in pdfArray!!) {
//                val model =
//                    MyFilesModel(
//                        name = item.name,
//                        uriPath = item.uri.path,
//                        uriOldPath = item.uri.path,
//                        lastModified = item.lastModified(),
//                        extensionName = item.extension,
//                        length = item.length()
//                    )
//                lstFileWord.add(model)
//                lstAllFile.add(model)
//            }
//        }
//        binding.vlHomeWord.text = getString(R.string.vl_home_word, lstFileWord.size)
//        if (!isReload) {
//            getAllFileExcel(isReload)
//        }
//    }
//
//    private fun getAllFileExcel(isReload: Boolean) {
//        var root = DocumentFileCompat.getRootDocumentFile(requireContext(), "primary", true)
//        if (root == null) {
//            root = DocumentFile.fromFile(File(PATH_DEFAULT_STORE))
//        }
//        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx")!!
//        val mimeXls = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls")!!
//        val pdfArray = root?.search(true, DocumentFileType.FILE, arrayOf(mime, mimeXls))
//        Log.e("lstFileExcel", pdfArray?.size.toString())
//        if ((pdfArray?.size ?: 0) > 0) {
//            if (isReload) {
//                lstAllFile.removeAll(lstFileExcel.toSet())
//                lstFileExcel.clear()
//            }
//            for (item in pdfArray!!) {
//                val model =
//                    MyFilesModel(
//                        name = item.name,
//                        uriPath = item.uri.path,
//                        uriOldPath = item.uri.path,
//                        lastModified = item.lastModified(),
//                        extensionName = item.extension,
//                        length = item.length()
//                    )
//                lstFileExcel.add(model)
//                lstAllFile.add(model)
//            }
//        }
//        binding.vlHomeExcel.text = getString(R.string.vl_home_excel, lstFileExcel.size)
//        if (!isReload) {
//            getAllFilePowpoint(isReload)
//        }
//    }
//
//    private fun getAllFilePowpoint(isReload: Boolean) {
//        var root = DocumentFileCompat.getRootDocumentFile(requireContext(), "primary", true)
//        if (root == null) {
//            root = DocumentFile.fromFile(File(PATH_DEFAULT_STORE))
//        }
//        val mimePPT = MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt")!!
//        val mimePPTX = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx")!!
//        val pdfArray = root?.search(true, DocumentFileType.FILE, arrayOf(mimePPT, mimePPTX))
//        Log.e("lstFilePowpoint", pdfArray?.size.toString())
//        if ((pdfArray?.size ?: 0) > 0) {
//            if (isReload) {
//                lstAllFile.removeAll(lstFilePowerPoint.toSet())
//                lstFilePowerPoint.clear()
//            }
//            for (item in pdfArray!!) {
//                val model =
//                    MyFilesModel(
//                        name = item.name,
//                        uriPath = item.uri.path,
//                        uriOldPath = item.uri.path,
//                        lastModified = item.lastModified(),
//                        extensionName = item.extension,
//                        length = item.length()
//                    )
//                lstFilePowerPoint.add(model)
//                lstAllFile.add(model)
//            }
//        }
//        binding.vlHomePowerPoint.text = getString(R.string.vl_home_power_point, lstFilePowerPoint.size)
//        binding.vlHomeMyFile.text = getString(R.string.vl_home_my_file, lstAllFile.size)
//        Log.e("lstAllFile", lstAllFile.size.toString())
//        binding.prbLoadingFile.gone()
//    }

    private fun onListenReloadFile() {
        eventsBusDisposable = RxBus.listen(EventsBus::class.java).subscribe {
            when {
//                EventsBus.RELOAD_ALL_FILE == it -> {
//                    Handler(Looper.myLooper()!!).postDelayed({
//                        lstAllFile.clear()
//                        lstFilePdf.clear()
//                        lstFileWord.clear()
//                        lstFileExcel.clear()
//                        lstFilePowerPoint.clear()
//                        reloadRecentFile()
//                        getAllFilePdf(false)
//                    }, 500)
//                }
//                EventsBus.RELOAD_PDF_FILE == it -> {
//                    getAllFilePdf(true)
//                }
//                EventsBus.RELOAD_WORD_FILE == it -> {
//                    getAllFileWord(true)
//                }
//                EventsBus.RELOAD_EXCEL_FILE == it -> {
//                    getAllFileExcel(true)
//                }
//                EventsBus.RELOAD_POWER_POINT_FILE == it -> {
//                    getAllFilePowpoint(true)
//                }
                EventsBus.RELOAD_RECENT == it -> {
                    reloadRecentFile()
                }
            }
//            RxBus.removeEvent()
        }
    }

    private fun onListenUpdateFile() {
        rxBusDisposable = RxBus.listen(Any::class.java).subscribe {
            if (it is MyFilesModel) {
                Logger.showLog("Thuytv------onListenUpdateFile: " + Gson().toJson(it))
                if (it.isRename == true) {
//                    it.isRename = false
//                    if (lstAllFile.contains(it)) {
//                        val indexFile = lstAllFile.indexOf(it)
//                        lstAllFile[indexFile] = it
//                    }
                    if (lstFilePdf.contains(it)) {
                        val indexFile = lstFilePdf.indexOf(it)
                        lstFilePdf[indexFile] = it
                    }
//                    if (lstFileWord.contains(it)) {
//                        val indexFile = lstFileWord.indexOf(it)
//                        lstFileWord[indexFile] = it
//                    }
//                    if (lstFileExcel.contains(it)) {
//                        val indexFile = lstFileExcel.indexOf(it)
//                        lstFileExcel[indexFile] = it
//                    }
//                    if (lstFilePowerPoint.contains(it)) {
//                        val indexFile = lstFilePowerPoint.indexOf(it)
//                        lstFilePowerPoint[indexFile] = it
//                    }
                    val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
                    Logger.showLog("Thuytv------lstRecent: " + Gson().toJson(lstRecent))
                    if (lstRecent?.contains(it) == true) {
                        val indexFile = lstRecent.indexOf(it)
                        lstRecent[indexFile] = it
                        getBaseActivity()?.sharedPreferences?.updateRecentFile(it)
                        getBaseActivity()?.runOnUiThread {
                            mRecentAdapter?.renameData(it)
                        }
                    }

                } else if (it.isDelete == true) {
//                    lstAllFile.remove(it)
                    if (lstFilePdf.contains(it)) {
                        lstFilePdf.remove(it)
                    }
//                    if (lstFileWord.contains(it)) {
//                        lstFileWord.remove(it)
//                    }
//                    if (lstFileExcel.contains(it)) {
//                        lstFileExcel.remove(it)
//                    }
//                    if (lstFilePowerPoint.contains(it)) {
//                        lstFilePowerPoint.remove(it)
//                    }
                    val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
                    if (lstRecent?.contains(it) == true) {
                        getBaseActivity()?.sharedPreferences?.removeRecentFile(it)
                        getBaseActivity()?.runOnUiThread {
                            mRecentAdapter?.deleteData(it)
                        }
                    }
                }
            }
        }
    }

    private fun reloadRecentFile() {
        val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
        lstRecent?.apply {
            getBaseActivity()?.runOnUiThread {
                mRecentAdapter?.updateData(this)
            }
        }
    }

    private fun showPopupRecentMore() {
        val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
        showPopupMenu(binding.imvRecentMore, R.menu.menu_more_all_file, object : OnPopupMenuItemClickListener {
            override fun onClickItemPopupMenu(menuItem: MenuItem?) {
                when (menuItem?.itemId) {
                    R.id.menu_all_size -> {
                        lstRecent?.apply {
                            sortWith(Comparator { o1, o2 -> o1.length!!.compareTo(o2.length!!) })
                            mRecentAdapter?.updateData(this)
                        }

                    }
                    R.id.menu_all_name_a_z -> {
                        lstRecent?.apply {
                            sortWith(Comparator { o1, o2 -> o1.name!!.compareTo(o2.name!!) })
                            mRecentAdapter?.updateData(this)
                        }
                    }
                    R.id.menu_all_name_z_a -> {
                        lstRecent?.apply {
                            sortWith(Comparator { o1, o2 -> o2.name!!.compareTo(o1.name!!) })
                            mRecentAdapter?.updateData(this)
                        }
                    }
                    R.id.menu_all_date_modified -> {
                        lstRecent?.apply {
                            sortWith(Comparator { o1, o2 -> o1.lastModified!!.compareTo(o2.lastModified!!) })
                            mRecentAdapter?.updateData(this)
                        }
                    }
                    R.id.menu_all_date_added -> {
                        lstRecent?.apply {
                            sortWith(Comparator { o1, o2 -> o2.lastModified!!.compareTo(o1.lastModified!!) })
                            mRecentAdapter?.updateData(this)
                        }
                    }
                }
            }

        })
    }

    private fun showPopupItemRecentMore(view: View, myFileModel: MyFilesModel) {
        showPopupMenu(view, R.menu.menu_more_file, object : OnPopupMenuItemClickListener {
            override fun onClickItemPopupMenu(menuItem: MenuItem?) {
                when (menuItem?.itemId) {
                    R.id.menu_rename -> {
                        getBaseActivity()?.apply {
                            RenameFileDialog(this, myFileModel, object : OnDialogItemClickListener {
                                override fun onClickItemConfirm(mData: MyFilesModel) {
//                                    mRecentAdapter?.renameData(mData)
//                                    sharedPreferences.updateRecentFile(mData)
//                                    sharedPreferences.updateFavoriteFile(mData)
//                                    if (mData.extensionName?.lowercase() == "pdf") {
//                                        getAllFilePdf(true)
//                                    } else if (mData.extensionName?.lowercase() == "docx" || mData.extensionName?.lowercase() == "doc") {
//                                        getAllFileWord(true)
//                                    } else if (mData.extensionName?.lowercase() == "xlsx" || mData.extensionName?.lowercase() == "xls") {
//                                        getAllFileExcel(true)
//                                    } else if (mData.extensionName?.lowercase() == "pptx" || mData.extensionName?.lowercase() == "ppt") {
//                                        getAllFilePowpoint(true)
//                                    }
                                }

                            }).show()
                        }
                    }
                    R.id.menu_favorite -> {
                        getBaseActivity()?.sharedPreferences?.setFavoriteFile(myFileModel)
                        RxBus.publish(EventsBus.RELOAD_FAVORITE)
                    }
                    R.id.menu_share -> {
                        myFileModel.uriPath?.let { File(it) }?.let { shareFile(it) }
                    }
                    R.id.menu_delete -> {
                        getBaseActivity()?.apply {
                            val deleteFileDialog = DeleteFileDialog(this, myFileModel, object : OnDialogItemClickListener {
                                override fun onClickItemConfirm(mData: MyFilesModel) {
//                                    mRecentAdapter?.deleteData(mData)
//                                    getBaseActivity()?.sharedPreferences?.removeFavoriteFile(myFileModel)
//                                    getBaseActivity()?.sharedPreferences?.removeRecentFile(myFileModel)
//                                    RxBus.publish(EventsBus.RELOAD_ALL_FILE)
                                }

                            })
                            deleteFileDialog.show()
                        }
                    }
                }
            }

        })
    }

    fun onSearchFile(strName: String) {
        mRecentAdapter?.filter?.filter(strName)
    }
}