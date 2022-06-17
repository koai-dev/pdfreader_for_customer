package com.igi.office.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.DocumentFileType
import com.anggrayudi.storage.file.extension
import com.anggrayudi.storage.file.search
import com.google.gson.Gson
import com.igi.office.R
import com.igi.office.common.AppKeys
import com.igi.office.common.Logger
import com.igi.office.common.RxBus
import com.igi.office.common.gone
import com.igi.office.databinding.FragmentBrowseBinding
import com.igi.office.ui.base.BaseFragment
import com.igi.office.ui.home.adapter.MyFilesAdapter
import com.igi.office.ui.home.adapter.MyFolderAdapter
import com.igi.office.ui.home.model.MyFilesModel
import com.igi.office.ui.home.model.MyFolderModel
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
    private lateinit var lstAllFolder: ArrayList<MyFilesModel>

    override fun initData() {
        onListenUpdateFile()
        lstAllFolder = ArrayList()
        mFolderAdapter = MyFilesAdapter(getBaseActivity(), lstAllFolder, 0, object : MyFilesAdapter.OnItemClickListener {
            override fun onClickItem(documentFile: MyFilesModel) {
                if ((documentFile.lstChildFile?.size ?: 0) > 0) {
                    val bundle = Bundle()
                    bundle.putParcelableArrayList(AppKeys.KEY_BUNDLE_DATA, documentFile.lstChildFile)
                    bundle.putString(AppKeys.KEY_BUNDLE_SCREEN, getString(R.string.tt_pdf_file))
                    getBaseActivity()?.onNextScreen(MyFileDetailActivity::class.java, bundle, false)
                } else {
                    val bundle = Bundle()
                    bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, documentFile)
                    getBaseActivity()?.onNextScreen(PdfViewActivity::class.java, bundle, false)
                }
            }

            override fun onClickItemMore(view: View, documentFile: MyFilesModel) {
            }

        })
        binding.rcvFolderList.apply {
            layoutManager = LinearLayoutManager(getBaseActivity())
            adapter = mFolderAdapter
        }
        Handler(Looper.myLooper()!!).postDelayed({
            getAllFileInDevice()
        }, 1000)
    }

    override fun initEvents() {
    }

    private fun getAllFileInDevice() {
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
                                length = mFile.length()
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
                        length = item.length()
                    )
                lstAllFolder.add(model)
            }
        }
        mFolderAdapter.updateData(lstAllFolder)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rxBusDisposable?.isDisposed == false) rxBusDisposable?.dispose()
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
    fun onSearchFolder(strName: String) {
        mFolderAdapter.filter.filter(strName)
    }
}