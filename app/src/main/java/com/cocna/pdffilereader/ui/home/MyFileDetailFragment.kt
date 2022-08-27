package com.cocna.pdffilereader.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.cocna.pdffilereader.ui.home.dialog.FileInfoDialog
import com.cocna.pdffilereader.ui.home.dialog.RenameFileDialog
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.Disposable
import java.io.File


/**
 * Created by Thuytv on 10/06/2022.
 */
class MyFileDetailFragment : BaseFragment<FragmentMyFileDetailBinding>(), View.OnClickListener {
    private var lstDataFile: ArrayList<MyFilesModel>? = null
    private var myFilesAdapter: MyFilesAdapter? = null
    private var isViewType = false
    private var isReloadRecent = false
    private lateinit var sharePreferenceUtils: SharePreferenceUtils
    private var rxBusDisposable: Disposable? = null
    private var isFromDetail = false

    companion object {
        private var mOnCallbackTittleTab: OnCallbackTittleTab? = null
        fun newInstance(onCallbackTittleTab: OnCallbackTittleTab?): MyFileDetailFragment {
            val fragment = MyFileDetailFragment()
            mOnCallbackTittleTab = onCallbackTittleTab
            return fragment
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyFileDetailBinding
        get() = FragmentMyFileDetailBinding::inflate

    override fun initData() {
        sharePreferenceUtils = SharePreferenceUtils(getBaseActivity())
        isViewType = sharePreferenceUtils.getValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE) ?: false
        isFromDetail = arguments?.getBoolean(AppKeys.KEY_BUNDLE_ACTION, false) ?: false
//        lstDataFile = arguments?.getParcelableArrayList(AppKeys.KEY_BUNDLE_DATA)
//        val strData = arguments?.getString(AppKeys.KEY_BUNDLE_DATA)
//        strData?.apply {
//            lstDataFile = Gson().fromJson<ArrayList<MyFilesModel>>(
//                strData,
//                object : TypeToken<ArrayList<MyFilesModel>>() {}.type
//            )
//        }
        lstDataFile = Common.lstDataDetail
        if (lstDataFile == null) {
            lstDataFile = ArrayList()
        }
        setupRecycleView()

        if (isFromDetail) {
            binding.llToolbarAll.visible()
            val titleData = arguments?.getString(AppKeys.KEY_BUNDLE_SCREEN)
            binding.ttToolbar.text = titleData
        }
        onListenDeleteFile()
        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_FILE_LISTING, AppConfig.KEY_PARAM_FB_VIEW_MODE, AppConfig.KEY_FB_LIST)
    }

    override fun initEvents() {
        listenClickViews(binding.imvAllBack, binding.imvTypeAdapter, binding.imvToolbarSearch, binding.imvToolbarMore, binding.imvCloseSearch)

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                myFilesAdapter?.filter?.filter(p0?.toString())
            }

        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imvTypeAdapter -> {
                isViewType = !isViewType
                if (isViewType) {
                    binding.imvTypeAdapter.setImageResource(R.drawable.ic_list_type)
                    getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_FILE_LISTING, AppConfig.KEY_PARAM_FB_VIEW_MODE, AppConfig.KEY_FB_GRID)
                } else {
                    binding.imvTypeAdapter.setImageResource(R.drawable.ic_grid_type)
                    getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_FILE_LISTING, AppConfig.KEY_PARAM_FB_VIEW_MODE, AppConfig.KEY_FB_LIST)
                }
                changeTypeViewAdapter(isViewType)
            }
            R.id.imvAllBack -> {
                MultiClickPreventer.preventMultiClick(v)
                getBaseActivity()?.finish()
            }
            R.id.imvToolbarSearch -> {
                binding.llToolbarSearch.visible()
                binding.llToolbarAll.gone()
            }
            R.id.imvCloseSearch -> {
                binding.llToolbarSearch.gone()
                binding.llToolbarAll.visible()
                binding.edtSearch.setText("")
                getBaseActivity()?.hideKeyboard()
            }
            R.id.imvToolbarMore -> {
                showPopupMenu(binding.imvToolbarMore, R.menu.menu_more_all_file, object : OnPopupMenuItemClickListener {
                    override fun onClickItemPopupMenu(menuItem: MenuItem?) {
                        when (menuItem?.itemId) {
                            R.id.menu_all_size -> {
                                lstDataFile?.apply {
                                    sortWith { o1, o2 -> o1.length!!.compareTo(o2.length!!) }
                                    myFilesAdapter?.updateData(this)
                                }
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_FILE_LISTING, AppConfig.KEY_PARAM_FB_SORT_FILE, AppConfig.KEY_FB_SIZE)
                            }
                            R.id.menu_all_name_a_z -> {
                                lstDataFile?.apply {
                                    sortWith { o1, o2 -> o1.name!!.lowercase().compareTo(o2.name!!.lowercase()) }
                                    myFilesAdapter?.updateData(this)
                                }
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_FILE_LISTING, AppConfig.KEY_PARAM_FB_SORT_FILE, AppConfig.KEY_FB_A_Z)
                            }
                            R.id.menu_all_name_z_a -> {
                                lstDataFile?.apply {
                                    sortWith { o1, o2 -> o2.name!!.lowercase().compareTo(o1.name!!.lowercase()) }
                                    myFilesAdapter?.updateData(this)
                                }
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_FILE_LISTING, AppConfig.KEY_PARAM_FB_SORT_FILE, AppConfig.KEY_FB_A_Z)
                            }
                            R.id.menu_all_date_modified -> {
                                lstDataFile?.apply {
                                    sortWith { o1, o2 -> o1.lastModified!!.compareTo(o2.lastModified!!) }
                                    myFilesAdapter?.updateData(this)
                                }
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_FILE_LISTING, AppConfig.KEY_PARAM_FB_SORT_FILE, AppConfig.KEY_FB_DATE)
                            }
                            R.id.menu_all_date_added -> {
                                lstDataFile?.apply {
                                    sortWith { o1, o2 -> o2.lastModified!!.compareTo(o1.lastModified!!) }
                                    myFilesAdapter?.updateData(this)
                                }
                                getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_FILE_LISTING, AppConfig.KEY_PARAM_FB_SORT_FILE, AppConfig.KEY_FB_DATE)
                            }
                        }
                    }
                })
            }
        }
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
        sharePreferenceUtils.setValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE, isViewType)
    }

    private fun setupRecycleView() {
        lstDataFile?.apply {
            sortWith { o1, o2 -> o1.name!!.compareTo(o2.name!!) }
            myFilesAdapter =
                MyFilesAdapter(context, lstDataFile!!, MyFilesAdapter.TYPE_VIEW_FILE, AppConfig.TYPE_FILTER_FILE, object : MyFilesAdapter.OnItemClickListener {
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
                                        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_RENAME_FILE,AppConfig.KEY_EVENT_FB_RENAME_FILE)
                                    }
                                    R.id.menu_favorite -> {
                                        sharePreferenceUtils.setFavoriteFile(documentFile)
                                        RxBus.publish(EventsBus.RELOAD_FAVORITE)
                                        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_BOOKMARK_FILE,AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_BOOKMARK)
                                    }
                                    R.id.menu_un_favorite -> {
                                        getBaseActivity()?.sharedPreferences?.removeFavoriteFile(documentFile)
                                        RxBus.publish(EventsBus.RELOAD_FAVORITE)
                                        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_BOOKMARK_FILE,AppConfig.KEY_PARAM_FB_STATUS, AppConfig.KEY_FB_UN_BOOKMARK)
                                    }
                                    R.id.menu_share -> {
                                        documentFile.uriPath?.let { File(it) }?.let { shareFile(it) }
                                        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_SHARE_FILE,AppConfig.KEY_EVENT_FB_SHARE_FILE)
                                    }
                                    R.id.menu_delete -> {
                                        getBaseActivity()?.apply {
                                            val deleteFileDialog = DeleteFileDialog(this, documentFile, object : OnDialogItemClickListener {
                                                override fun onClickItemConfirm(mData: MyFilesModel) {
                                                    getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_DELETE_FILE,AppConfig.KEY_EVENT_FB_DELETE_FILE)
                                                }
                                            })
                                            deleteFileDialog.show()
                                        }
                                    }
                                    R.id.menu_shortcut -> {
                                        getBaseActivity()?.apply {
                                            setUpShortCut(this, documentFile)
                                        }
                                        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_SHORTCUT_FILE,AppConfig.KEY_EVENT_FB_SHORTCUT_FILE)
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
            if (sharePreferenceUtils.getValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE) == true) {
                binding.rcvAllFile.apply {
                    layoutManager = GridLayoutManager(getBaseActivity(), 3)
                    adapter = myFilesAdapter
                }
                myFilesAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE_GRID)
                binding.imvTypeAdapter.setImageResource(R.drawable.ic_list_type)
            } else {
                binding.rcvAllFile.apply {
                    layoutManager = LinearLayoutManager(getBaseActivity())
                    adapter = myFilesAdapter
                }
                myFilesAdapter?.updateTypeAdapter(MyFilesAdapter.TYPE_VIEW_FILE)
                binding.imvTypeAdapter.setImageResource(R.drawable.ic_grid_type)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.showLog("Thuytv-----onDestroy: $isReloadRecent")
        if (isReloadRecent && isFromDetail) {
            RxBus.publish(EventsBus.RELOAD_RECENT)
        }
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
                    mOnCallbackTittleTab?.onCallbackUpdateTab(lstDataFile?.size ?: 0)
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
        getBaseActivity()?.logEventFirebase(AppConfig.KEY_EVENT_FB_SEARCH_FILE, AppConfig.KEY_EVENT_FB_SEARCH_FILE)
    }
}