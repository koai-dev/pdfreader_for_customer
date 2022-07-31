package com.cocna.pdffilereader.ui.home

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.anggrayudi.storage.file.*
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.FragmentMyFilesBinding
import com.cocna.pdffilereader.myinterface.OnPopupMenuItemClickListener
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.base.OnCallbackTittleTab
import com.cocna.pdffilereader.ui.home.adapter.TabFileAdapter
import com.cocna.pdffilereader.ui.home.dialog.ProgressDialog
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import java.io.File


/**
 * Created by Thuytv on 09/06/2022.
 */
class MyFilesFragment : BaseFragment<FragmentMyFilesBinding>(), View.OnClickListener {
    private lateinit var externalLauncher: ActivityResultLauncher<String>
    private lateinit var launcher: ActivityResultLauncher<String>
    private lateinit var lstFilePdf: ArrayList<MyFilesModel>

    private var PATH_DEFAULT_STORE = "/storage/emulated/0"

    private var myFileDetailFragment: MyFileDetailFragment? = null
    private var historyFragment: HistoryFragment? = null
    private lateinit var mAdapter: TabFileAdapter

    private var isViewType = false
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyFilesBinding = FragmentMyFilesBinding::inflate
    override fun initData() {
        lstFilePdf = ArrayList()
        setupViewPager(binding.viewPagerMyFile)

        getBaseActivity()?.loadNativeAds(binding.frameAdsNativeAllFile, AppConfig.ID_ADS_NATIVE_TOP_BAR_PDF)

        if (SDK_INT >= Build.VERSION_CODES.M) {
//            executeWithPerm {
//                if (Common.listAllData.isNullOrEmpty()) {
//                    getAllFilePdf()
//                } else {
//                    lstFilePdf = Common.listAllData!!
//                    Handler(Looper.myLooper()!!).postDelayed({
//                        myFileDetailFragment!!.updateData(lstFilePdf)
//                        mAdapter.updateTitleTab(0, getString(R.string.vl_home_my_file, lstFilePdf.size))
//                    }, 200)
//                }
//            }
            if (Common.listAllData.isNullOrEmpty()) {
                getBaseActivity()?.apply {
                    if (PermissionUtil.checkExternalStoragePermission(this)) {
                        getAllFilePdf(true)
                    } else {
                        showPopupPermission()
                    }
                }
            } else {
                lstFilePdf = Common.listAllData!!
                Handler(Looper.myLooper()!!).postDelayed({
                    myFileDetailFragment!!.updateData(lstFilePdf)
                    mAdapter.updateTitleTab(0, getString(R.string.vl_home_my_file, lstFilePdf.size))
                }, 200)
            }
        }
        isViewType = getBaseActivity()?.sharedPreferences?.getValueBoolean(SharePreferenceUtils.KEY_TYPE_VIEW_FILE) ?: false
        if (isViewType) {
            binding.imvAdapterType.setImageResource(R.drawable.ic_list_type)
        } else {
            binding.imvAdapterType.setImageResource(R.drawable.ic_grid_type)
        }
    }

    private fun getAllFilePdf(isShowDialog: Boolean) {
        if (binding.llGoToSetting.visibility == View.VISIBLE) {
            binding.llGoToSetting.gone()
        }
        val dialogProgress = ProgressDialog(requireContext())
        if (isShowDialog) {
            dialogProgress.show()
        }
        Handler(Looper.myLooper()!!).postDelayed({
            getAllFilePdf(dialogProgress, isShowDialog)
        }, 100)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
        val sizeRecent = lstRecent?.size ?: 0
        myFileDetailFragment = MyFileDetailFragment.newInstance(object : OnCallbackTittleTab {
            override fun onCallbackUpdateTab(numberTab: Int) {
                mAdapter.updateTitleTab(0, getString(R.string.vl_home_my_file, numberTab))
            }

        })
        historyFragment = HistoryFragment.newInstance(object : OnCallbackTittleTab {
            override fun onCallbackUpdateTab(numberTab: Int) {
                mAdapter.updateTitleTab(1, getString(R.string.vl_history_file, numberTab))
            }

        })
        mAdapter = TabFileAdapter(getBaseActivity()!!, childFragmentManager)
        mAdapter.addFragment(myFileDetailFragment!!, getString(R.string.tt_all_file))
        mAdapter.addFragment(historyFragment!!, getString(R.string.vl_history_file, sizeRecent))
        viewPager.adapter = mAdapter
        viewPager.offscreenPageLimit = 2
    }

    override fun initEvents() {
        binding.tabMyFile.apply {
            binding.tabMyFile.setupWithViewPager(binding.viewPagerMyFile)
            for (i in 0 until binding.tabMyFile.tabCount) {
                val tab = binding.tabMyFile.getTabAt(i)
                if (tab != null) {
                    tab.customView = mAdapter.getTabView(i, 0, binding.tabMyFile)

                    val viewTab = (binding.tabMyFile.getChildAt(0) as ViewGroup).getChildAt(i)
                    val params = viewTab.layoutParams as ViewGroup.MarginLayoutParams
                    params.setMargins(0, 0, 25, 0)
                    viewTab.requestLayout()
                }
            }
            binding.tabMyFile.tabRippleColor = null
        }
        listenClickViews(binding.imvAdapterType, binding.imvFilterFile, binding.btnGoToSetting)
        swipeRefreshLayout = binding.swRefreshData
        swipeRefreshLayout?.setOnRefreshListener {
            swipeRefreshLayout?.isRefreshing = true
            getBaseActivity()?.apply {
                if (PermissionUtil.checkExternalStoragePermission(this)) {
                    getAllFilePdf(false)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        MultiClickPreventer.preventMultiClick(v)
        when (v?.id) {
            R.id.imv_adapter_type -> {
                isViewType = !isViewType
                if (isViewType) {
                    binding.imvAdapterType.setImageResource(R.drawable.ic_list_type)
                } else {
                    binding.imvAdapterType.setImageResource(R.drawable.ic_grid_type)
                }
                myFileDetailFragment?.changeTypeViewAdapter(isViewType)
                historyFragment?.changeTypeViewAdapter(isViewType)
            }
            R.id.imv_filter_file -> {
                val lstRecent = getBaseActivity()?.sharedPreferences?.getRecentFile()
                showPopupMenu(binding.imvFilterFile, R.menu.menu_more_all_file, object : OnPopupMenuItemClickListener {
                    override fun onClickItemPopupMenu(menuItem: MenuItem?) {
                        when (menuItem?.itemId) {
                            R.id.menu_all_size -> {
                                lstFilePdf.apply {
                                    sortWith { o1, o2 -> o1.length!!.compareTo(o2.length!!) }
                                    myFileDetailFragment?.updateData(this)
                                }
                                lstRecent?.apply {
                                    sortWith { o1, o2 -> o1.length!!.compareTo(o2.length!!) }
                                    historyFragment?.updateData(this)
                                }

                            }
                            R.id.menu_all_name_a_z -> {
                                lstFilePdf.apply {
                                    sortWith { o1, o2 -> o1.name!!.compareTo(o2.name!!) }
                                    myFileDetailFragment?.updateData(this)
                                }
                                lstRecent?.apply {
                                    sortWith { o1, o2 -> o1.name!!.compareTo(o2.name!!) }
                                    historyFragment?.updateData(this)
                                }
                            }
                            R.id.menu_all_name_z_a -> {
                                lstFilePdf.apply {
                                    sortWith { o1, o2 -> o2.name!!.compareTo(o1.name!!) }
                                    myFileDetailFragment?.updateData(this)
                                }
                                lstRecent?.apply {
                                    sortWith { o1, o2 -> o2.name!!.compareTo(o1.name!!) }
                                    historyFragment?.updateData(this)
                                }
                            }
                            R.id.menu_all_date_modified -> {
                                lstFilePdf.apply {
                                    sortWith { o1, o2 -> o1.lastModified!!.compareTo(o2.lastModified!!) }
                                    myFileDetailFragment?.updateData(this)
                                }
                                lstRecent?.apply {
                                    sortWith { o1, o2 -> o1.lastModified!!.compareTo(o2.lastModified!!) }
                                    historyFragment?.updateData(this)
                                }
                            }
                            R.id.menu_all_date_added -> {
                                lstFilePdf.apply {
                                    sortWith { o1, o2 -> o2.lastModified!!.compareTo(o1.lastModified!!) }
                                    myFileDetailFragment?.updateData(this)
                                }
                                lstRecent?.apply {
                                    sortWith { o1, o2 -> o2.lastModified!!.compareTo(o1.lastModified!!) }
                                    historyFragment?.updateData(this)
                                }
                            }
                        }
                    }

                })
            }
            R.id.btn_go_to_setting -> {
                requestPermission()
            }
        }
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
                if (SDK_INT >= Build.VERSION_CODES.R) {
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
            .setMessage(getString(R.string.vl_request_store_permission_content))
            .setPositiveButton(getString(R.string.btn_continue)) { _, _ ->
                launcher.launch(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            }


        launcher = registerForActivityResult(AppSettingsContracts(getBaseActivity())) {
            when (it) {
                true -> {
                    permissionGranted()
                }
                false -> settingsDialog.show()
            }
        }

        val permissionDeniedDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.tt_request_store_permission))
            .setMessage(getString(R.string.vl_request_store_permission_content))
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

    private fun getAllFilePdf(dialogProgress: ProgressDialog, isShowDialog: Boolean) {
        Thread {
            getBaseActivity()?.apply {
                var root = DocumentFileCompat.getRootDocumentFile(this, "primary", true)
                if (root == null) {
                    root = DocumentFile.fromFile(File(PATH_DEFAULT_STORE))
                }
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")!!
                val pdfArray = root.search(true, DocumentFileType.FILE, arrayOf(mime))
                lstFilePdf = ArrayList()
                if (pdfArray.isNotEmpty()) {
                    for (item in pdfArray) {
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
                    }
                }
                getBaseActivity()?.runOnUiThread {
                    myFileDetailFragment?.updateData(lstFilePdf)
                    mAdapter.updateTitleTab(0, getString(R.string.vl_home_my_file, lstFilePdf.size))
                    Handler(Looper.myLooper()!!).postDelayed({
                        if (isVisible && getBaseActivity()?.isFinishing == false && !isShowDialog) {
                            dialogProgress.dismiss()
                        }
                    }, 100)
                }
                if (getBaseActivity()?.isCurrentNetwork == false) {
                    getBaseActivity()?.enabaleNetwork()
                }
                swipeRefreshLayout?.isRefreshing = false
            }
        }.start()
    }

    fun onSearchFile(strName: String) {
        historyFragment?.onSearchFile(strName)
        myFileDetailFragment?.onSearchFile(strName)
    }

    private fun showPopupPermission() {
        val permissionDeniedDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.tt_request_store_permission))
            .setMessage(getString(R.string.vl_request_store_permission_content))
            .setPositiveButton(getString(R.string.btn_continue)) { _, _ ->
                requestPermission()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { _, _ ->
                binding.llGoToSetting.visible()
            }
        permissionDeniedDialog.show()
    }

    private fun requestPermission() {
        getBaseActivity()?.apply {
            if (SDK_INT >= Build.VERSION_CODES.R) {
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
                requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        getBaseActivity()?.apply {
            Logger.showLog("Thuytv---------resultLauncher--:" + PermissionUtil.checkExternalStoragePermission(this))
            if (PermissionUtil.checkExternalStoragePermission(this)) {
                getAllFilePdf(true)
                RxBus.publish(EventsBus.PERMISSION_STORED_GRANTED)
            } else {
                binding.llGoToSetting.visible()
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Logger.showLog("Thuytv---------requestPermissionLauncher : $isGranted")
        if (isGranted) {
            getAllFilePdf(true)
            RxBus.publish(EventsBus.PERMISSION_STORED_GRANTED)
        } else {
            binding.llGoToSetting.visible()
        }
    }
}