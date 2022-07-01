package com.cocna.pdffilereader.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentMyFileDetailBinding
import com.cocna.pdffilereader.myinterface.OnDialogItemClickListener
import com.cocna.pdffilereader.myinterface.OnPopupMenuItemClickListener
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.base.OnCallbackTittleTab
import com.cocna.pdffilereader.ui.home.adapter.MyFilesAdapter
import com.cocna.pdffilereader.ui.home.dialog.DeleteFileDialog
import com.cocna.pdffilereader.ui.home.dialog.RenameFileDialog
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import io.reactivex.disposables.Disposable
import java.io.File


/**
 * Created by Thuytv on 10/06/2022.
 */
class MyFileDetailFragment(private val onCallbackTittleTab: OnCallbackTittleTab) : BaseFragment<FragmentMyFileDetailBinding>() {
    private var lstDataFile: ArrayList<MyFilesModel>? = null
    private var myFilesAdapter: MyFilesAdapter? = null

    private var isReloadRecent = false
    private lateinit var sharePreferenceUtils: SharePreferenceUtils
    private var rxBusDisposable: Disposable? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyFileDetailBinding
        get() = FragmentMyFileDetailBinding::inflate

    override fun initData() {
        sharePreferenceUtils = SharePreferenceUtils(getBaseActivity())
        lstDataFile = ArrayList()
        setupRecycleView()
        onListenDeleteFile()
    }

    override fun initEvents() {
    }

    fun changeTypeViewAdapter(isViewType: Boolean) {
        if (!isViewType) {
            binding.rcvAllFile.apply {
                layoutManager = LinearLayoutManager(getBaseActivity())
                adapter = myFilesAdapter
            }
            myFilesAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE)
        } else {
            binding.rcvAllFile.apply {
                layoutManager = GridLayoutManager(getBaseActivity(), 3)
                adapter = myFilesAdapter
            }
            myFilesAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE_GRID)
        }
//        mIsViewType = !mIsViewType
        sharePreferenceUtils.setValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE, isViewType)
    }

    private fun setupRecycleView() {
        lstDataFile?.apply {
            sortWith({ o1, o2 -> o1.name!!.compareTo(o2.name!!) })
            myFilesAdapter = MyFilesAdapter(context, lstDataFile!!, MyFilesAdapter.TYPE_VIEW_FILE, object : MyFilesAdapter.OnItemClickListener {
                override fun onClickItem(documentFile: MyFilesModel) {
                    Logger.showLog("Thuytv-----documentFile: " + documentFile.name)
                    sharePreferenceUtils.setRecentFile(documentFile)
                    isReloadRecent = true
                    val bundle = Bundle()
                    bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, documentFile)
                    getBaseActivity()?.onNextScreen(PdfViewActivity::class.java, bundle, false)
                    RxBus.publish(EventsBus.RELOAD_RECENT)
                }

                override fun onClickItemMore(view: View, documentFile: MyFilesModel) {
                    showPopupMenu(view, R.menu.menu_more_file, object : OnPopupMenuItemClickListener {
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
                                    sharePreferenceUtils.setFavoriteFile(documentFile)
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
                            }
                        }

                    })
                }
            })
            if (sharePreferenceUtils.getValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE) == true) {
                binding.rcvAllFile.apply {
                    layoutManager = GridLayoutManager(getBaseActivity(), 3)
                    adapter = myFilesAdapter
                }
                myFilesAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE_GRID)
            } else {
                binding.rcvAllFile.apply {
                    layoutManager = LinearLayoutManager(getBaseActivity())
                    adapter = myFilesAdapter
                }
                myFilesAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rxBusDisposable?.isDisposed == false) rxBusDisposable?.dispose()
    }

    private fun onListenDeleteFile() {
        rxBusDisposable = RxBus.listen(Any::class.java).subscribe {
            if (it is MyFilesModel) {
                if (it.isRename == true) {
                    myFilesAdapter?.renameData(it)
                }
                if (it.isDelete == true) {
                    lstDataFile?.remove(it)
                    myFilesAdapter?.deleteData(it)
                    onCallbackTittleTab.onCallbackUpdateTab(lstDataFile?.size ?: 0)
                }
            }
        }
    }

    fun updateData(lstData: ArrayList<MyFilesModel>) {
        lstDataFile = lstData
        myFilesAdapter?.updateData(lstData)
    }

    fun onSearchFile(strName: String) {
        myFilesAdapter?.filter?.filter(strName)
    }
}