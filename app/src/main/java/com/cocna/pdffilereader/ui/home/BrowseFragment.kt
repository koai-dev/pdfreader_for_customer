package com.cocna.pdffilereader.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.anggrayudi.storage.file.DocumentFileType
import com.anggrayudi.storage.file.extension
import com.anggrayudi.storage.file.search
import com.google.gson.Gson
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentBrowseBinding
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.myinterface.OnPopupMenuItemClickListener
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.home.adapter.MyFilesAdapter
import com.cocna.pdffilereader.ui.home.dialog.DeleteFileDialog
import com.cocna.pdffilereader.ui.home.dialog.FileInfoDialog
import com.cocna.pdffilereader.ui.home.dialog.RenameFileDialog
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import io.reactivex.disposables.Disposable
import java.io.File

/**
 * Created by Thuytv on 09/06/2022.
 */
class BrowseFragment : BaseFragment<FragmentBrowseBinding>() {
    private var PATH_DEFAULT_STORE = "/storage/emulated/0"
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBrowseBinding = FragmentBrowseBinding::inflate
    private lateinit var mFolderAdapter: MyFilesAdapter
    private var rxBusDisposable: Disposable? = null
    private var eventBusDisposable: Disposable? = null
    private lateinit var lstAllFolder: ArrayList<MyFilesModel>

    override fun initData() {
        onListenUpdateFile()
        lstAllFolder = ArrayList()
        mFolderAdapter = MyFilesAdapter(getBaseActivity(), lstAllFolder, 0, AppConfig.TYPE_FILTER_FOLDER, object : MyFilesAdapter.OnItemClickListener {
            override fun onClickItem(documentFile: MyFilesModel) {
                if ((documentFile.lstChildFile?.size ?: 0) > 0) {
                    val bundle = Bundle()
//                    bundle.putParcelableArrayList(AppKeys.KEY_BUNDLE_DATA, documentFile.lstChildFile)
                    Common.lstDataDetail = documentFile.lstChildFile
                    bundle.putString(AppKeys.KEY_BUNDLE_SCREEN, getString(R.string.tt_pdf_file))
                    bundle.putBoolean(AppKeys.KEY_BUNDLE_ACTION, true)
                    getBaseActivity()?.onNextScreen(MyFileDetailActivity::class.java, bundle, false)
                } else {
                    val bundle = Bundle()
                    bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, documentFile)
                    getBaseActivity()?.onNextScreen(PdfViewActivity::class.java, bundle, false)
                }
            }

            override fun onClickItemMore(view: View, documentFile: MyFilesModel) {
                var lstPopupMenu = R.menu.menu_more_file
                val lstFavorite = getBaseActivity()?.sharedPreferences?.getFavoriteFile()
                if (lstFavorite?.contains(documentFile) == true) {
                    lstPopupMenu = R.menu.menu_more_file_favorite
                }
                showPopupMenu(view, lstPopupMenu, object : OnPopupMenuItemClickListener {
                    override fun onClickItemPopupMenu(menuItem: MenuItem?) {
                        when (menuItem?.itemId) {
                            R.id.menu_rename -> {
                                getBaseActivity()?.apply {
                                    RenameFileDialog(this, documentFile, object : OnDialogItemClickListener {
                                        override fun onClickItemConfirm(mData: MyFilesModel) {
                                        }

                                    }).show()
                                }
                            }
                            R.id.menu_favorite -> {
                                getBaseActivity()?.sharedPreferences?.setFavoriteFile(documentFile)
                                RxBus.publish(EventsBus.RELOAD_FAVORITE)
                            }
                            R.id.menu_un_favorite -> {
                                getBaseActivity()?.sharedPreferences?.removeFavoriteFile(documentFile)
                                RxBus.publish(EventsBus.RELOAD_FAVORITE)
                            }
                            R.id.menu_share -> {
                                documentFile.uriPath?.let { File(it) }?.let { shareFile(it) }
                            }
                            R.id.menu_delete -> {
                                getBaseActivity()?.apply {
                                    val deleteFileDialog = DeleteFileDialog(this, documentFile, object : OnDialogItemClickListener {
                                        override fun onClickItemConfirm(mData: MyFilesModel) {
                                        }
                                    })
                                    deleteFileDialog.show()
                                }
                            }
                            R.id.menu_shortcut -> {
                                getBaseActivity()?.apply {
                                    setUpShortCut(this, documentFile)
                                }
                            }
                            R.id.menu_file_info -> {
                                getBaseActivity()?.apply {
                                    FileInfoDialog(this, documentFile).show()
                                }
                            }
                        }
                    }

                })
            }

        })
        binding.rcvFolderList.apply {
            layoutManager = LinearLayoutManager(getBaseActivity())
            adapter = mFolderAdapter
        }
        Handler(Looper.myLooper()!!).postDelayed({
            getBaseActivity()?.apply {
                if (Common.listAllFolder.isNullOrEmpty()) {
                    if (PermissionUtil.checkExternalStoragePermission(this)) {
                        getAllFileInDevice()
                    } else {
                        binding.llGoToSetting.visible()
                        binding.rcvFolderList.gone()
                    }
                } else {
                    lstAllFolder = Common.listAllFolder!!
                    mFolderAdapter.updateData(lstAllFolder)
                }
            }
        }, 200)
        onListenEventBus()
    }

    override fun initEvents() {
        binding.btnGoToSetting.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            requestPermission()
        }

    }

    private fun getAllFileInDevice() {
        Thread {
            Logger.showLog("Thuytv---------getAllFileInDevice---Browse")
            if (isVisible) {
                getBaseActivity()?.runOnUiThread {
                    binding.llGoToSetting.gone()
                    binding.rcvFolderList.visible()
                }
            }
            val root = DocumentFile.fromFile(File(PATH_DEFAULT_STORE))
            val mimePDF = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")!!
            for (item in root.listFiles()) {
                if (!item.isFile) {
                    val rootFile = item.search(true, DocumentFileType.FILE, arrayOf(mimePDF))
                    if (rootFile.isNotEmpty()) {
                        val lstChildFile: ArrayList<MyFilesModel> = ArrayList()
                        for (mFile in rootFile) {
                            val model =
                                MyFilesModel(
                                    name = mFile.name,
                                    uriPath = mFile.uri.path,
                                    uriOldPath = mFile.uri.path,
                                    lastModified = mFile.lastModified(),
                                    extensionName = mFile.extension,
                                    length = mFile.length(),
                                    locationFile = mFile.parentFile?.uri?.path,
                                    folderName = mFile.parentFile?.name
                                )
                            lstChildFile.add(model)
                        }
                        val mFolder = MyFilesModel(folderName = item.name, lstChildFile = lstChildFile)
                        lstAllFolder.add(mFolder)
                    }
                } else if (item.extension.lowercase() == "pdf") {
                    val model =
                        MyFilesModel(
                            name = item.name,
                            uriPath = item.uri.path,
                            uriOldPath = item.uri.path,
                            lastModified = item.lastModified(),
                            extensionName = item.extension,
                            length = item.length(),
                            locationFile = item.parentFile?.uri?.path,
                            folderName = item.parentFile?.name
                        )
                    lstAllFolder.add(model)
                }
            }
            getBaseActivity()?.runOnUiThread {
                mFolderAdapter.updateData(lstAllFolder)
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rxBusDisposable?.isDisposed == false) rxBusDisposable?.dispose()
        if (eventBusDisposable?.isDisposed == false) eventBusDisposable?.dispose()
    }

    private fun onListenUpdateFile() {
        rxBusDisposable = RxBus.listen(Any::class.java).subscribe {
            if (it is MyFilesModel) {
                Logger.showLog("Thuytv------onListenUpdateFile: " + Gson().toJson(it))
                if (it.isRename == true) {
                    if (lstAllFolder.contains(it)) {
                        val indexFile = lstAllFolder.indexOf(it)
                        lstAllFolder[indexFile] = it
                    } else {
                        for (item in lstAllFolder) {
                            if (item.lstChildFile?.contains(it) == true) {
                                val indexFile = item.lstChildFile!!.indexOf(it)
                                item.lstChildFile!![indexFile] = it
                                break
                            }
                        }
                    }

                } else if (it.isDelete == true) {
                    if (lstAllFolder.contains(it)) {
                        lstAllFolder.remove(it)
                    } else {
                        for (item in lstAllFolder) {
                            if (item.lstChildFile?.contains(it) == true) {
                                item.lstChildFile!!.remove(it)
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onListenEventBus() {
        eventBusDisposable = RxBus.listenDeBounce(EventsBus::class.java).subscribe {
            if (it == EventsBus.PERMISSION_STORED_GRANTED) {
                getBaseActivity()?.apply {
                    if (PermissionUtil.checkExternalStoragePermission(this)) {
                        getAllFileInDevice()
                    }
                }
            }
//            else if(it == EventsBus.RELOAD_ALL_FOLDER){
//                getBaseActivity()?.runOnUiThread {
//                    lstAllFolder = Common.listAllFolder!!
//                    mFolderAdapter.updateData(lstAllFolder)
//                }
//            }
        }
    }

    fun onSearchFolder(strName: String) {
        mFolderAdapter.filter.filter(strName)
    }

    private fun requestPermission() {
        getBaseActivity()?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(java.lang.String.format("package:%s", getBaseActivity()?.packageName))
                    resultLauncher.launch(intent)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    resultLauncher.launch(intent)
                }
            } else {
//                requestPermissionLauncher.launch(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        getBaseActivity()?.apply {
            Logger.showLog("Thuytv---------resultLauncher--:" + PermissionUtil.checkExternalStoragePermission(this))
            if (PermissionUtil.checkExternalStoragePermission(this)) {
                getAllFileInDevice()
                RxBus.publish(EventsBus.PERMISSION_STORED_GRANTED)
            } else {
                binding.llGoToSetting.visible()
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.entries.all {
            it.value == true
        }
        Logger.showLog("Thuytv---------requestPermissionLauncher : $isGranted")
        if (isGranted) {
            getAllFileInDevice()
            RxBus.publish(EventsBus.PERMISSION_STORED_GRANTED)
        } else {
            binding.llGoToSetting.visible()
        }
    }
}