package com.igi.office.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.igi.office.R
import com.igi.office.common.*
import com.igi.office.databinding.FragmentFavoriteBinding
import com.igi.office.myinterface.OnDialogItemClickListener
import com.igi.office.myinterface.OnPopupMenuItemClickListener
import com.igi.office.ui.base.BaseFragment
import com.igi.office.ui.home.adapter.MyFilesAdapter
import com.igi.office.ui.home.dialog.DeleteFileDialog
import com.igi.office.ui.home.dialog.RenameFileDialog
import com.igi.office.ui.home.model.MyFilesModel
import io.reactivex.disposables.Disposable

/**
 * Created by Thuytv on 09/06/2022.
 */
class FavoriteFragment : BaseFragment<FragmentFavoriteBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFavoriteBinding = FragmentFavoriteBinding::inflate
    private var eventsBusDisposable: Disposable? = null
    private var mFavoriteAdapter: MyFilesAdapter? = null
    private var isViewType = false

    override fun initData() {
        onListenReloadFile()
        isViewType = getBaseActivity()?.sharedPreferences?.getValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE) ?: false
        val lstFavorite = getBaseActivity()?.sharedPreferences?.getFavoriteFile() ?: ArrayList()
        val typeAdapter = if (isViewType) {
            MyFilesAdapter.TYPE_VIEW_FILE_GRID
        } else {
            MyFilesAdapter.TYPE_VIEW_FILE
        }
        mFavoriteAdapter = MyFilesAdapter(getBaseActivity(), lstFavorite, typeAdapter, object : MyFilesAdapter.OnItemClickListener {
            override fun onClickItem(documentFile: MyFilesModel) {
                val bundle = Bundle()
                bundle.putParcelable(AppKeys.KEY_BUNDLE_DATA, documentFile)
                getBaseActivity()?.onNextScreen(PdfViewActivity::class.java, bundle, false)
            }

            override fun onClickItemMore(view: View, documentFile: MyFilesModel) {
                showPopupItemFavoriteMore(view, documentFile)
            }

        })

        if (isViewType) {
            binding.rcvFavoriteFile.apply {
                layoutManager = GridLayoutManager(getBaseActivity(), 3)
                adapter = mFavoriteAdapter
            }
            mFavoriteAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE_GRID)
            binding.imvTypeAdapter.setImageResource(R.drawable.ic_list_type)
        } else {
            binding.rcvFavoriteFile.apply {
                layoutManager = LinearLayoutManager(getBaseActivity())
                adapter = mFavoriteAdapter
            }
            mFavoriteAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE)
            binding.imvTypeAdapter.setImageResource(R.drawable.ic_grid_type)
        }
    }

    override fun initEvents() {
        binding.imvTypeAdapter.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            changeTypeViewAdapter()
        }
    }

    private fun changeTypeViewAdapter() {
        if (isViewType) {
            binding.rcvFavoriteFile.apply {
                layoutManager = LinearLayoutManager(getBaseActivity())
                adapter = mFavoriteAdapter
            }
            mFavoriteAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE)
            binding.imvTypeAdapter.setImageResource(R.drawable.ic_grid_type)
        } else {
            binding.rcvFavoriteFile.apply {
                layoutManager = GridLayoutManager(getBaseActivity(), 3)
                adapter = mFavoriteAdapter
            }
            mFavoriteAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE_GRID)
            binding.imvTypeAdapter.setImageResource(R.drawable.ic_list_type)
        }
        isViewType = !isViewType
        getBaseActivity()?.sharedPreferences?.setValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE, isViewType)
    }

    private fun showPopupItemFavoriteMore(view: View, myFileModel: MyFilesModel) {
        showPopupMenu(view, R.menu.menu_more_file_favorite, object : OnPopupMenuItemClickListener {
            override fun onClickItemPopupMenu(menuItem: MenuItem?) {
                when (menuItem?.itemId) {
                    R.id.menu_rename -> {
                        getBaseActivity()?.apply {
                            RenameFileDialog(this, myFileModel, object : OnDialogItemClickListener {
                                override fun onClickItemConfirm(mData: MyFilesModel) {
                                    mFavoriteAdapter?.renameData(mData)
                                    if (mData.extensionName?.lowercase() == "pdf") {
                                        RxBus.publish(EventsBus.RELOAD_PDF_FILE)
                                    } else if (mData.extensionName?.lowercase() == "docx" || mData.extensionName?.lowercase() == "doc") {
                                        RxBus.publish(EventsBus.RELOAD_WORD_FILE)
                                    } else if (mData.extensionName?.lowercase() == "xlsx" || mData.extensionName?.lowercase() == "xls") {
                                        RxBus.publish(EventsBus.RELOAD_EXCEL_FILE)
                                    } else if (mData.extensionName?.lowercase() == "pptx" || mData.extensionName?.lowercase() == "ppt") {
                                        RxBus.publish(EventsBus.RELOAD_POWER_POINT_FILE)
                                    }
                                }

                            }).show()
                        }
                    }
                    R.id.menu_un_favorite -> {
                        mFavoriteAdapter?.deleteData(myFileModel)
                        getBaseActivity()?.sharedPreferences?.removeFavoriteFile(myFileModel)
                    }
                    R.id.menu_share -> {

                    }
                    R.id.menu_delete -> {
                        getBaseActivity()?.apply {
                            val deleteFileDialog = DeleteFileDialog(this, myFileModel, object : OnDialogItemClickListener {
                                override fun onClickItemConfirm(mData: MyFilesModel) {
                                    mFavoriteAdapter?.deleteData(mData)
                                    getBaseActivity()?.sharedPreferences?.removeFavoriteFile(myFileModel)
                                    getBaseActivity()?.sharedPreferences?.removeRecentFile(myFileModel)
                                    RxBus.publish(EventsBus.RELOAD_ALL_FILE)
                                }

                            })
                            deleteFileDialog.show()
                        }
                    }
                }
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (eventsBusDisposable?.isDisposed == false) eventsBusDisposable?.dispose()
    }

    private fun onListenReloadFile() {
        eventsBusDisposable = RxBus.listen(EventsBus::class.java).subscribe {
            if (EventsBus.RELOAD_FAVORITE == it || EventsBus.RELOAD_ALL_FILE == it) {
                reloadFavoriteFile()
            }
        }
    }

    private fun reloadFavoriteFile() {
        val lstFavorite = getBaseActivity()?.sharedPreferences?.getFavoriteFile()
        lstFavorite?.apply {
            getBaseActivity()?.runOnUiThread {
                mFavoriteAdapter?.updateData(this)
            }
        }
    }

    fun onSearchFileFavorite(strName: String) {
        mFavoriteAdapter?.filter?.filter(strName)
    }
}