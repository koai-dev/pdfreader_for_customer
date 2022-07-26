package com.cocna.pdffilereader.ui.home

import android.os.*
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentHistoryBinding
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
 * Created by Thuytv on 09/06/2022.
 */
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(), View.OnClickListener {
    private var mRecentAdapter: MyFilesAdapter? = null
    private var eventsBusDisposable: Disposable? = null
    private var rxBusDisposable: Disposable? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHistoryBinding = FragmentHistoryBinding::inflate
    companion object {
        private var mOnCallbackTittleTab: OnCallbackTittleTab? = null
        fun newInstance(onCallbackTittleTab: OnCallbackTittleTab?): HistoryFragment {
            val fragment = HistoryFragment()
            mOnCallbackTittleTab = onCallbackTittleTab
            return fragment
        }
    }


    override fun initData() {
        onListenReloadFile()
        onListenUpdateFile()
        setupRecycleView()
    }

    override fun initEvents() {
        listenClickViews()
    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
//        when (v?.id) {
////            R.id.llMyFile -> {
////                gotoMyFileDetail(lstAllFile, getString(R.string.tt_all_file))
////            }
//            R.id.btnClickToRead -> {
//                gotoMyFileDetail(lstFilePdf, getString(R.string.tt_pdf_file))
//            }
////            R.id.llWord -> {
////                gotoMyFileDetail(lstFileWord, getString(R.string.tt_word_file))
////            }
////            R.id.llExcel -> {
////                gotoMyFileDetail(lstFileExcel, getString(R.string.tt_excel_file))
////            }
////            R.id.llPowerPoint -> {
////                gotoMyFileDetail(lstFilePowerPoint, getString(R.string.tt_power_point_file))
////            }
//            R.id.imvReloadRecent -> {
//                reloadRecentFile()
//            }
//            R.id.imvRecentMore -> {
//                showPopupRecentMore()
//            }
//        }
    }
@Suppress("DEPRECATION")
private fun setBackgroundColor(isEmpty: Boolean){
    if(isEmpty){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.llHistory.setBackgroundColor(resources.getColor(R.color.color_bg_menu, getBaseActivity()?.theme))
        }else{
            binding.llHistory.setBackgroundColor(resources.getColor(R.color.color_bg_menu))
        }
    }else{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.llHistory.setBackgroundColor(resources.getColor(R.color.color_bg, getBaseActivity()?.theme))
        }else{
            binding.llHistory.setBackgroundColor(resources.getColor(R.color.color_bg))
        }
    }
}
    private fun setupRecycleView() {
        val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
        Logger.showLog("Thuytv------lstRecent: " + Gson().toJson(lstRecent))
        if (lstRecent.isNullOrEmpty()) {
            binding.llEmptyRecent.visible()
            binding.rcvRecentFile.gone()
            setBackgroundColor(true)
        } else {
            binding.llEmptyRecent.gone()
            binding.rcvRecentFile.visible()
            setBackgroundColor(false)
        }
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
        if (getBaseActivity()?.sharedPreferences?.getValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE) == true) {
            binding.rcvRecentFile.apply {
                layoutManager = GridLayoutManager(getBaseActivity(), 3)
                adapter = mRecentAdapter
            }
            mRecentAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE_GRID)
        } else {
            binding.rcvRecentFile.apply {
                layoutManager = LinearLayoutManager(getBaseActivity())
                adapter = mRecentAdapter
            }
            mRecentAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (eventsBusDisposable?.isDisposed == false) eventsBusDisposable?.dispose()
        if (rxBusDisposable?.isDisposed == false) rxBusDisposable?.dispose()
    }

    private fun onListenReloadFile() {
        eventsBusDisposable = RxBus.listen(EventsBus::class.java).subscribe {
            if (EventsBus.RELOAD_RECENT == it) {
                reloadRecentFile()
            }
        }
    }

    private fun onListenUpdateFile() {
        rxBusDisposable = RxBus.listen(Any::class.java).subscribe {
            if (it is MyFilesModel) {
                Logger.showLog("Thuytv------onListenUpdateFile: " + Gson().toJson(it))
                if (it.isRename == true) {
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

                }
                if (it.isDelete == true) {
                    val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
                    if (lstRecent?.contains(it) == true) {
                        getBaseActivity()?.sharedPreferences?.removeRecentFile(it)
                        getBaseActivity()?.runOnUiThread {
                            mRecentAdapter?.deleteData(it)
                        }
                    }
                    val sizeRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()?.size ?: 0
                    if (sizeRecent == 0) {
                        binding.llEmptyRecent.visible()
                        binding.rcvRecentFile.gone()
                        setBackgroundColor(true)
                    }
                    Logger.showLog("Thuytv------History----onCallbackUpdateTab: " + sizeRecent)
                    mOnCallbackTittleTab?.onCallbackUpdateTab(sizeRecent)
                }
            }
        }
    }

    private fun reloadRecentFile() {
        val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
        lstRecent?.apply {
            getBaseActivity()?.runOnUiThread {
                if (lstRecent.isNullOrEmpty()) {
                    binding.llEmptyRecent.visible()
                    binding.rcvRecentFile.gone()
                    setBackgroundColor(true)
                } else {
                    binding.llEmptyRecent.gone()
                    binding.rcvRecentFile.visible()
                    setBackgroundColor(false)
                }
                mRecentAdapter?.updateData(this)
                mOnCallbackTittleTab?.onCallbackUpdateTab(this.size)
            }
        }
    }

    private fun showPopupItemRecentMore(view: View, myFileModel: MyFilesModel) {
        showPopupMenu(view, R.menu.menu_more_file, object : OnPopupMenuItemClickListener {
            override fun onClickItemPopupMenu(menuItem: MenuItem?) {
                when (menuItem?.itemId) {
                    R.id.menu_rename -> {
                        getBaseActivity()?.apply {
                            RenameFileDialog(this, myFileModel, object : OnDialogItemClickListener {
                                override fun onClickItemConfirm(mData: MyFilesModel) {
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
                                }

                            })
                            deleteFileDialog.show()
                        }
                    }
                }
            }

        })
    }

    fun changeTypeViewAdapter(isViewType: Boolean) {
        if (!isViewType) {
            binding.rcvRecentFile.apply {
                layoutManager = LinearLayoutManager(getBaseActivity())
                adapter = mRecentAdapter
            }
            mRecentAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE)
        } else {
            binding.rcvRecentFile.apply {
                layoutManager = GridLayoutManager(getBaseActivity(), 3)
                adapter = mRecentAdapter
            }
            mRecentAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE_GRID)
        }
        getBaseActivity()?.sharedPreferences?.setValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE, isViewType)
    }

    fun onSearchFile(strName: String) {
        mRecentAdapter?.filter?.filter(strName)
    }

    fun updateData(lstData: ArrayList<MyFilesModel>) {
        mRecentAdapter?.updateData(lstData)
    }
}