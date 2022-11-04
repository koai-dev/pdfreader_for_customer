package com.cocna.pdffilereader.ui.drive

import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocna.pdffilereader.PdfApplication
import com.cocna.pdffilereader.databinding.FragmentDropBoxBinding
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.Metadata
import com.google.gson.Gson
import androidx.lifecycle.lifecycleScope
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.ui.drive.adapter.DriveModel
import com.cocna.pdffilereader.ui.drive.adapter.DropBoxAdapter
import com.cocna.pdffilereader.ui.drive.dropbox.*
import com.cocna.pdffilereader.ui.home.PdfViewActivity
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import kotlinx.coroutines.launch
import java.io.File


/**
 * Created by Thuytv on 25/10/2022.
 */
class DropBoxFragment : BaseFragment<FragmentDropBoxBinding>() {
    private val fileSaveLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/"

    private val appGraph: AppGraph get() = (getBaseActivity()!!.applicationContext as PdfApplication).appGraph

    protected val dropboxOAuthUtil: DropboxOAuthUtil get() = appGraph.dropboxOAuthUtil

    private val dropboxCredentialUtil: DropboxCredentialUtil get() = appGraph.dropboxCredentialUtil

    protected val dropboxApiWrapper: DropboxApiWrapper get() = appGraph.dropboxApiWrapper

    private var ggDriveAdapter: DropBoxAdapter? = null
    private val lstDataDropBox = MutableLiveData<ArrayList<FileMetadata>>()
    private var isAuthentDropBox = false
    private var mProgressDialogLoadingData: ProgressDialogLoadingData? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDropBoxBinding
        get() = FragmentDropBoxBinding::inflate

    override fun initData() {
        binding.ttToolbarDropbox.text = getString(R.string.vl_dropbox)
        getBaseActivity()?.let {
            mProgressDialogLoadingData = ProgressDialogLoadingData(it)
        }
        ggDriveAdapter = DropBoxAdapter(getBaseActivity(), java.util.ArrayList(), object : DropBoxAdapter.OnItemClickListener {
            override fun onClickItem(documentFile: FileMetadata) {
                val strFileData = fileSaveLocation + documentFile.name
                val fileData = File(strFileData)
                if (fileData.exists()) {
                    val intent = Intent(getBaseActivity(), PdfViewActivity::class.java)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_NAME, documentFile.name)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_PATH, fileData.absolutePath)
                    startActivity(intent)
                } else {
                    binding.prbLoadingDropbox.visible()
                    downloadFile(documentFile)
                }
            }
        })
        binding.rcvDropbox.apply {
            layoutManager = LinearLayoutManager(getBaseActivity())
            adapter = ggDriveAdapter
        }
//        binding.prbLoadingDropbox.visible()
        lstDataDropBox.observe(this, Observer {
            if (isVisible) {
//                binding.prbLoadingDropbox.gone()
                mProgressDialogLoadingData?.dismiss()
                ggDriveAdapter?.updateData(it)
            }
        })
        isAuthentDropBox = isAuthenticated()
        if (isAuthentDropBox) {

            dropboxApiWrapper.dropboxClient.let {
                mProgressDialogLoadingData?.show()
                getAllFile()
            }
        } else {
            getBaseActivity()?.let {
                dropboxOAuthUtil.startDropboxAuthorizationOAuth2(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dropboxOAuthUtil.onResume()
        Logger.showLog("Thuytv------------isAuthentDropBox: $isAuthentDropBox")
        if (!isAuthentDropBox) {
            isAuthentDropBox = isAuthenticated()
            if (isAuthentDropBox) {
                dropboxApiWrapper.dropboxClient.let {
                    getAllFile()
                }
            } else {
                getBaseActivity()?.let {
                    dropboxOAuthUtil.startDropboxAuthorizationOAuth2(it)
                }
            }
        }
        val uid = Auth.getUid()
        val storedUid = getBaseActivity()?.sharedPreferences?.getValueString(SharePreferenceUtils.PREF_ACCOUNT_NAME_DROPBOX, null)
        if (uid != null && uid != storedUid) {
            getBaseActivity()?.sharedPreferences?.setValueString(SharePreferenceUtils.PREF_ACCOUNT_NAME_DROPBOX, uid)
        }
    }

    private fun isAuthenticated(): Boolean {
        return dropboxCredentialUtil.isAuthenticated()
    }

    override fun initEvents() {
        binding.imvBackDropBox.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            getBaseActivity()?.finish()
        }
    }

    private fun getAllFile() {
//        Thread {
//            var result: ListFolderResult = client.files().listFolder("")
//            val lstData = ArrayList<DriveModel>()
//            while (true) {
//                for (metadata in result.entries) {
////                    if(metadata.name)
//                    Logger.showLog("Thuytv----file: " + metadata.pathLower + "---name: " + metadata.name)
////                    var result2: ListFolderResult = client.files().listFolder("/" + metadata.name)
//                    if (metadata.name.endsWith(".pdf")) {
//                        val driveModel = DriveModel(
//                            id = metadata.pathLower,
//                            name = metadata.name,
//                            createdTime = 0,
//                            modifiedTime = 0,
//                            extensionName = "pdf",
//                            folderName = "Dropbox"
//                        )
//                        lstData.add(driveModel)
//                    } else {
//                        getSubFile(client, metadata, lstData)
//                    }
//                }
//                if (!result.hasMore) {
//                    break
//                }
//                result = client.files().listFolderContinue(result.cursor)
//            }
//            Logger.showLog("Thuytv-----lstData: " + Gson().toJson(lstData))
//            getBaseActivity()?.runOnUiThread {
//                ggDriveAdapter?.updateData(lstData)
//                binding.prbLoadingDropbox.gone()
//            }
//        }.start()
        Logger.showLog("Thuytv-----getAll File ")
//        lifecycleScope.launch {
//            when (val apiResult = dropboxApiWrapper.listFolders("")) {
//                is ListFolderApiResult.Error -> {
//                    getBaseActivity()?.let {
//                        Logger.showToast(it, "Failed to get account details." + apiResult.e.message)
//                    }
//                    apiResult.e
//                }
//                is ListFolderApiResult.Success -> {
//                    val lstData = ArrayList<FileMetadata>()
//                    val result = apiResult.result.entries
//                    if (result != null && result.size > 0) {
//                        for (metadata in result) {
//                            if (metadata is FileMetadata) {
//                                if (metadata.name.endsWith(".pdf")) {
////                                    val driveModel = DriveModel(
////                                        id = metadata.pathLower,
////                                        name = metadata.name,
////                                        createdTime = 0,
////                                        modifiedTime = 0,
////                                        extensionName = "pdf",
////                                        folderName = "Dropbox"
////                                    )
////                                    lstData.add(driveModel)
//                                    lstData.add(metadata)
//                                }
//
//                            } else if (metadata is FolderMetadata) {
//                                fetchSubFile(metadata, lstData)
//                            }
//                        }
//                    }
//                    getBaseActivity()?.runOnUiThread {
//                        Logger.showLog("Thuytv-----lstData: " + Gson().toJson(lstData))
//                        ggDriveAdapter?.updateData(lstData)
//                        binding.prbLoadingDropbox.gone()
//                    }
//                }
//            }
//        }
        lifecycleScope.launch {
            dropboxApiWrapper.getFilesForFolderFlow("").collect {
                if (it is GetFilesResponse.Failure) {
                    it.exception.printStackTrace()
                    if (it.exception is InvalidAccessTokenException) {
                        getBaseActivity()?.let {
//                            dropboxOAuthUtil.revokeDropboxAuthorization(dropboxApiWrapper)
                            dropboxOAuthUtil.startDropboxAuthorizationOAuth2(it)
                        }
                    } else {
                        getBaseActivity()?.let {
                            Logger.showToast(it, "Get file fail")
                        }

                    }
//                    if (isVisible) {
//                        binding.prbLoadingDropbox.gone()
//                    }
                } else if (it is GetFilesResponse.Success) {
                    val lstData = ArrayList<FileMetadata>()
                    val result = it.result
                    if (result.isNotEmpty()) {
                        for (metadata in result) {
                            if (metadata is FileMetadata) {
                                if (metadata.name.endsWith(".pdf")) {
                                    lstData.add(metadata)
                                }

                            } else if (metadata is FolderMetadata) {
                                fetchSubFile(metadata, lstData)
                            }
                        }
                        lstDataDropBox.value = lstData
                    }
//                    getBaseActivity()?.runOnUiThread {
//                        Logger.showLog("Thuytv-----lstData: " + Gson().toJson(lstData))
//                        ggDriveAdapter?.updateData(lstData)
//                        binding.prbLoadingDropbox.gone()
//                    }
                }
            }
//            when (val apiResult = dropboxApiWrapper.getFilesForFolderFlow("")) {
//                is GetFilesApiResponse.Failure -> {
//                    getBaseActivity()?.let {
//                        Logger.showToast(it, "Failed to get account details." + apiResult.e.message)
//                    }
//                    apiResult.e
//                }
//                is ListFolderApiResult.Success -> {
//                    val lstData = ArrayList<FileMetadata>()
//                    val result = apiResult.result.entries
//                    if (result != null && result.size > 0) {
//                        for (metadata in result) {
//                            if (metadata is FileMetadata) {
//                                if (metadata.name.endsWith(".pdf")) {
////                                    val driveModel = DriveModel(
////                                        id = metadata.pathLower,
////                                        name = metadata.name,
////                                        createdTime = 0,
////                                        modifiedTime = 0,
////                                        extensionName = "pdf",
////                                        folderName = "Dropbox"
////                                    )
////                                    lstData.add(driveModel)
//                                    lstData.add(metadata)
//                                }
//
//                            } else if (metadata is FolderMetadata) {
//                                fetchSubFile(metadata, lstData)
//                            }
//                        }
//                    }
//                    getBaseActivity()?.runOnUiThread {
//                        Logger.showLog("Thuytv-----lstData: " + Gson().toJson(lstData))
//                        ggDriveAdapter?.updateData(lstData)
//                        binding.prbLoadingDropbox.gone()
//                    }
//                }
//            }
        }
    }

    private fun getSubFile(client: DbxClientV2, metadata: Metadata, lstData: ArrayList<DriveModel>): ArrayList<DriveModel> {
        try {
            Logger.showLog("Thuytv-----pathLower: " + metadata.pathLower)
            val result2: ListFolderResult = client.files().listFolder(metadata.pathLower)
            result2.let {
                for (metadata2 in result2.entries) {
                    val strName = metadata2.name
                    Logger.showLog("Thuytv-----metadata2: " + strName)
                    if (strName.endsWith(".pdf")) {
                        val driveModel = DriveModel(
                            id = metadata.pathLower,
                            name = metadata.name,
                            createdTime = 0,
                            modifiedTime = 0,
                            extensionName = "pdf",
                            folderName = "Dropbox"
                        )
                        lstData.add(driveModel)
                    }
                    Logger.showLog("Thuytv--2--file: " + metadata2.pathLower + "---name: " + metadata2.name)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lstData;
    }

    private fun fetchAccountInfo() {
        lifecycleScope.launch {
            when (val accountResult = dropboxApiWrapper.getCurrentAccount()) {
                is GetCurrentAccountResult.Error -> {
                    Logger.showToast(getBaseActivity()!!, "Failed to get account details." + accountResult.e.message)
                    accountResult.e
                }
                is GetCurrentAccountResult.Success -> {
                    val account = accountResult.account
                    Logger.showLog("Thuytv----account: " + Gson().toJson(account))
                }
            }
        }
    }

    private fun fetchSubFile(metadata: Metadata, lstData: ArrayList<FileMetadata>) {
        lifecycleScope.launch {
            when (val apiResult = dropboxApiWrapper.listFolders(metadata.pathLower)) {
                is ListFolderApiResult.Error -> {
                    getBaseActivity()?.let {
                        if (isVisible) {
                            Logger.showToast(it, "Failed to get account details." + apiResult.e.message)
//                            binding.prbLoadingDropbox.gone()
                            mProgressDialogLoadingData?.dismiss()
                        }
                    }
                    apiResult.e

                }
                is ListFolderApiResult.Success -> {
                    val result = apiResult.result.entries
                    if (result != null && result.size > 0) {
                        for (metadataSub in result) {
                            if (metadataSub is FileMetadata) {
                                if (metadataSub.name.endsWith(".pdf")) {
//                                    val driveModel = DriveModel(
//                                        id = metadataSub.pathLower,
//                                        name = metadataSub.name,
//                                        createdTime = 0,
//                                        modifiedTime = 0,
//                                        extensionName = "pdf",
//                                        folderName = "Dropbox"
//                                    )
//                                    lstData.add(driveModel)
                                    lstData.add(metadataSub)
                                    Logger.showLog("Thuytv---222-lstData: " + Gson().toJson(lstData))
                                }
                            } else if (metadataSub is FolderMetadata) {
                                fetchSubFile(metadataSub, lstData)
                            }
                        }
                        lstDataDropBox.value = lstData
                    }
                }
            }
        }
    }

    private fun downloadFile(metadata: FileMetadata) {
        lifecycleScope.launch {
            when (val apiResult = dropboxApiWrapper.download(getBaseActivity()!!, metadata)) {
                is DownloadFileTaskResult.Error -> {
                    getBaseActivity()?.let {
                        Logger.showToast(it, "Failed to download file." + apiResult.e.message)
                    }
                    apiResult.e
                    binding.prbLoadingDropbox.gone()
                }
                is DownloadFileTaskResult.Success -> {
                    val intent = Intent(getBaseActivity(), PdfViewActivity::class.java)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_NAME, metadata.name)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_PATH, apiResult.result.absolutePath)
                    startActivity(intent)
                    binding.prbLoadingDropbox.gone()
                }
            }
        }
    }
}