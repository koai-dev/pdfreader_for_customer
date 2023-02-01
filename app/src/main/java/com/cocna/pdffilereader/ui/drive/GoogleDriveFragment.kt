package com.cocna.pdffilereader.ui.drive

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.common.Logger.showLog
import com.cocna.pdffilereader.databinding.FragmentGoogleDriveBinding
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.drive.adapter.DriveModel
import com.cocna.pdffilereader.ui.drive.adapter.GGDriveAdapter
import com.cocna.pdffilereader.ui.home.PdfViewActivity
import com.cocna.pdffilereader.ui.home.dialog.ProgressDialogLoadingAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * Created by Thuytv on 24/10/2022.
 */
class GoogleDriveFragment : BaseFragment<FragmentGoogleDriveBinding>() {


    companion object {
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    }

    private var mCredential: GoogleAccountCredential? = null
    private val SCOPES = DriveScopes.DRIVE
    private val fileSaveLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/"

    private var ggDriveAdapter: GGDriveAdapter? = null
    private var mService: Drive? = null
    private var mProgressDialogLoadingData: ProgressDialogLoadingData? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGoogleDriveBinding
        get() = FragmentGoogleDriveBinding::inflate

    override fun initData() {
        binding.ttToolbarPdf.text = getString(R.string.vl_google_drive)
        getBaseActivity()?.let {
            mProgressDialogLoadingData = ProgressDialogLoadingData(it)
        }
        ggDriveAdapter = GGDriveAdapter(getBaseActivity(), ArrayList(), object : GGDriveAdapter.OnItemClickListener {
            override fun onClickItem(documentFile: DriveModel) {
                val strFileData = fileSaveLocation + documentFile.name
                val fileData = java.io.File(strFileData)
                if (fileData.exists()) {
                    val intent = Intent(getBaseActivity(), PdfViewActivity::class.java)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_NAME, documentFile.name)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_PATH, fileData.absolutePath)
                    startActivity(intent)
                } else {
                    mService?.let {
                        binding.prbLoadingGetData.visible()
                        downloadAndOpenFile(fileData, documentFile, it)
                    }
                }
            }
        })
        binding.rcvGoogleDrive.apply {
            layoutManager = LinearLayoutManager(getBaseActivity())
            adapter = ggDriveAdapter
        }

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(getBaseActivity(), listOf(SCOPES))
            .setBackOff(ExponentialBackOff())
        Handler(Looper.myLooper()!!).postDelayed({
            getResultsFromApi()
        }, 500)

    }

    override fun initEvents() {
        binding.imvPdfBack.setOnClickListener {
            MultiClickPreventer.preventMultiClick(it)
            getBaseActivity()?.finish()
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        return if (getBaseActivity() != null) {
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(getBaseActivity()!!)
            connectionStatusCode == ConnectionResult.SUCCESS
        } else {
            false
        }
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(getBaseActivity()!!)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    fun showGooglePlayServicesAvailabilityErrorDialog(
        connectionStatusCode: Int
    ) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog: Dialog? = apiAvailability.getErrorDialog(
            activity as Activity,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog?.show()
    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential?.selectedAccountName == null) {
            chooseAccount()
        } else if (getBaseActivity()?.isNetworkAvailable() == false) {
            Logger.showToast(getBaseActivity()!!, "No network connection available.")
        } else {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            mService = Drive.Builder(transport, jsonFactory, mCredential)
                .setApplicationName("Drive API Android Quickstart")
                .build()
            mService?.let {
//                binding.prbLoadingGetData.visible()
                mProgressDialogLoadingData?.show()
                getAllFileFromGGDrive(it)
            }
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    private fun chooseAccount() {
        Dexter.withContext(getBaseActivity()).withPermission(Manifest.permission.GET_ACCOUNTS).withListener(
            object : PermissionListener {
                override fun onPermissionGranted(report: PermissionGrantedResponse?) {
                    val accountName = getBaseActivity()?.sharedPreferences?.getValueString(SharePreferenceUtils.PREF_ACCOUNT_NAME)
                    if (accountName?.isNotEmpty() == true) {
                        mCredential!!.selectedAccountName = accountName
                        getResultsFromApi()
                    } else {
                        mCredential?.let {
                            resultLauncherAccountPicker.launch(it.newChooseAccountIntent())
                        }
                    }
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                }

                override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }

            }
        ).check()
    }

    val resultLauncherAccountPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            intent?.let {
                val accountName = it.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    getBaseActivity()?.sharedPreferences?.setValueString(SharePreferenceUtils.PREF_ACCOUNT_NAME, accountName)
                    mCredential?.selectedAccountName = accountName
                    getResultsFromApi()
                }
            }
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     * activity result.
     * @param data        Intent (containing result data) returned by incoming
     * activity result.
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Logger.showToast(
                    getBaseActivity()!!,
                    "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app."
                )
            } else {
                getResultsFromApi()
            }
//            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
//                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
//                if (accountName != null) {
//                    getBaseActivity()?.sharedPreferences?.setValueString(SharePreferenceUtils.PREF_ACCOUNT_NAME, accountName)
//                    mCredential!!.selectedAccountName = accountName
//                    getResultsFromApi()
//                }
//            }
//            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
//                getResultsFromApi()
//            }
        }
    }

    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private fun downloadAndOpenFile(fileSaveLocation: java.io.File, model: DriveModel, mService: Drive) {
        mExecutor.execute {
            val outputStream: OutputStream = FileOutputStream(fileSaveLocation)
            mService.files()[model.id].executeMediaAndDownloadTo(outputStream)
            showLog("Thuytv----downloadFile-----fileSaveLocation: " + fileSaveLocation.absolutePath)
            mainHandler.post {
                if (isVisible) {
                    val intent = Intent(getBaseActivity(), PdfViewActivity::class.java)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_NAME, model.name)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_PATH, fileSaveLocation.absolutePath)
                    //                        intent.putExtra(AppKeys.KEY_BUNDLE_DATA, myFilesModel);
                    startActivity(intent)
//                    mProgressDialogLoadingData?.dismiss()
                    binding.prbLoadingGetData.gone()
                }
            }
        }
    }

    private fun getAllFileFromGGDrive(mService: Drive) {
        mExecutor.execute {
            val result: ArrayList<File> = ArrayList()
            val request: Drive.Files.List = mService.files().list().setFields("nextPageToken, files(id, name,size,createdTime,modifiedTime)")
            do {
                try {
                    val files: FileList = request.execute()
                    result.addAll(files.files)
                    request.pageToken = files.nextPageToken
                } catch (e: IOException) {
                    e.printStackTrace()
                    request.pageToken = null
                }
            } while (request.pageToken != null &&
                request.pageToken.isNotEmpty()
            )
            showLog("Thuytv------result.getFiles(): " + result.size)
            try {
                if (result.isNotEmpty()) {
                    val lstDriveModel = ArrayList<DriveModel>()
                    for (file in result) {
                        if (file.name.endsWith(".pdf")) {
                            val driveModel = DriveModel(
                                id = file.id,
                                name = file.name,
                                createdTime = file.createdTime.value,
                                modifiedTime = file.modifiedTime.value,
                                extensionName = "pdf",
                                folderName = "Google Drive"
                            )
                            lstDriveModel.add(driveModel)

                        }
                    }
                    showLog("Thuytv------lstDriveModel: " + lstDriveModel.size)
                    mainHandler.post {
                        if (isVisible) {
                            ggDriveAdapter?.updateData(lstDriveModel)
                            mProgressDialogLoadingData?.dismiss()
//                            binding.prbLoadingGetData.gone()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}