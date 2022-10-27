package com.cocna.pdffilereader.ui.drive

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.common.Logger.showLog
import com.cocna.pdffilereader.databinding.FragmentGoogleDriveBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.base.BaseFragment
import com.cocna.pdffilereader.ui.drive.adapter.DriveModel
import com.cocna.pdffilereader.ui.drive.adapter.GGDriveAdapter
import com.cocna.pdffilereader.ui.home.PdfViewActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * Created by Thuytv on 24/10/2022.
 */
class GoogleDriveFragment : BaseFragment<FragmentGoogleDriveBinding>(), PermissionCallbacks {
    private var mCredential: GoogleAccountCredential? = null
    private val SCOPES = DriveScopes.DRIVE
    private val fileSaveLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/"

    private var ggDriveAdapter: GGDriveAdapter? = null
    private var mService: Drive? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGoogleDriveBinding
        get() = FragmentGoogleDriveBinding::inflate

    override fun initData() {
        ggDriveAdapter = GGDriveAdapter(getBaseActivity(), ArrayList(), object : GGDriveAdapter.OnItemClickListener {
            override fun onClickItem(documentFile: DriveModel) {
                binding.prbLoadingGetData.visible()
                val strFileData = fileSaveLocation + documentFile.name
                val fileData = java.io.File(strFileData)
                if(fileData.exists()){
                    val intent = Intent(getBaseActivity(), PdfViewActivity::class.java)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_NAME, documentFile.name)
                    intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_PATH, fileData.absolutePath)
                    startActivity(intent)
                    binding.prbLoadingGetData.gone()
                }else {
                    mService?.let {
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
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private fun isDeviceOnline(): Boolean {
        val connMgr = getBaseActivity()?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
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
            MainActivity.REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog?.show()
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask constructor(
        private val context: BaseActivity<*>?,
        private val mService: Drive?,
        private val ggDriveAdapter: GGDriveAdapter,
        private val prbLoadingData: ProgressBar
    ) :
        AsyncTask<Void?, Void?, List<String?>?>() {
        private var mLastError: Exception? = null

        /**
         * Background task to call Drive API.
         *
         * @param params no parameters needed for this task.
         */

        override fun doInBackground(vararg p0: Void?): List<String?>? {
            try {
                return dataFromApi
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                return null
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         *
         * @return List of Strings describing files, or an empty list if no files
         * found.
         * @throws IOException
         */
        @get:Throws(IOException::class)
        private val dataFromApi: List<String?>
            get() {
                // Get a list of up to 10 files.
                val fileInfo: MutableList<String?> = ArrayList()
//                val result = mService!!.files().list() //                 .setPageSize(100)
//                    .setFields("nextPageToken, files(id, name,size,createdTime,modifiedTime)")
//                    .execute()

//                val files = result.files
                val files = getAllFilesGdrive(mService!!)
                showLog("Thuytv------result.getFiles(): " + files?.size)
                try {

                    if (files != null) {
                        val lstDriveModel = ArrayList<DriveModel>()
                        for (file in files) {
//                        showLog("Thuytv------file: " + file.name)
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
                        context?.runOnUiThread {
                            ggDriveAdapter.updateData(lstDriveModel)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return fileInfo
            }

        @Throws(IOException::class)
        private fun getAllFilesGdrive(service: Drive): List<File>? {
            val result: ArrayList<File> = ArrayList()
            val request: Drive.Files.List = service.files().list().setFields("nextPageToken, files(id, name,size,createdTime,modifiedTime)")
            do {
                try {
                    val files: FileList = request.execute()
                    result.addAll(files.files)
                    request.pageToken = files.nextPageToken
                } catch (e: IOException) {
                    println("An error occurred: $e")
                    request.pageToken = null
                }
            } while (request.pageToken != null &&
                request.pageToken.isNotEmpty()
            )
            return result
        }

        override fun onPreExecute() {
//            mOutputText.setText("")
//            mProgress.show()
            prbLoadingData.visible()
        }

        override fun onPostExecute(output: List<String?>?) {
            prbLoadingData.gone()
//            mProgress.hide()
//            if (output == null || output.size == 0) {
//                mOutputText.setText("No results returned.")
//            } else {
//                output.add(0, "Data retrieved using the Drive API:")
//                mOutputText.setText(TextUtils.join("\n", output))
//            }
        }

        override fun onCancelled() {
//            mProgress.hide()
//            if (mLastError != null) {
//                if (mLastError is GooglePlayServicesAvailabilityIOException) {
//                    showGooglePlayServicesAvailabilityErrorDialog(
//                        (mLastError as GooglePlayServicesAvailabilityIOException)
//                            .connectionStatusCode
//                    )
//                } else if (mLastError is UserRecoverableAuthIOException) {
//                    startActivityForResult(
//                        (mLastError as UserRecoverableAuthIOException).intent,
//                        MainActivity.REQUEST_AUTHORIZATION
//                    )
//                } else {
//                    mOutputText.setText(
//                        "The following error occurred:\n"
//                                + mLastError!!.message
//                    )
//                }
//            } else {
//                mOutputText.setText("Request cancelled.")
//            }
        }


    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
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
        } else if (mCredential!!.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            Logger.showToast(getBaseActivity()!!, "No network connection available.")
        } else {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            mService = Drive.Builder(transport, jsonFactory, mCredential)
                .setApplicationName("Drive API Android Quickstart")
                .build()
            ggDriveAdapter?.let {
                MakeRequestTask(getBaseActivity(), mService, it, binding.prbLoadingGetData).execute()
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
    @AfterPermissionGranted(MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                getBaseActivity()!!, Manifest.permission.GET_ACCOUNTS
            )
        ) {
            val accountName = getBaseActivity()?.sharedPreferences?.getValueString(SharePreferenceUtils.PREF_ACCOUNT_NAME)
            if (accountName?.isNotEmpty() == true) {
                mCredential!!.selectedAccountName = accountName
                getResultsFromApi()
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                    mCredential!!.newChooseAccountIntent(),
                    MainActivity.REQUEST_ACCOUNT_PICKER
                )
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                this,
                "This app needs to access your Google account (via Contacts).",
                MainActivity.REQUEST_PERMISSION_GET_ACCOUNTS,
                Manifest.permission.GET_ACCOUNTS
            )
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
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MainActivity.REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Logger.showToast(
                    getBaseActivity()!!,
                    "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app."
                )
            } else {
                getResultsFromApi()
            }
            MainActivity.REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    getBaseActivity()?.sharedPreferences?.setValueString(SharePreferenceUtils.PREF_ACCOUNT_NAME, accountName)
                    mCredential!!.selectedAccountName = accountName
                    getResultsFromApi()
                }
            }
            MainActivity.REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi()
            }
        }
    }

    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    fun downloadAndOpenFile(fileSaveLocation: java.io.File, model: DriveModel, mService: Drive): Task<Void> {
        return Tasks.call(mExecutor) { // Retrieve the metadata as a File object.
            val outputStream: OutputStream = FileOutputStream(fileSaveLocation)
            mService.files()[model.id].executeMediaAndDownloadTo(outputStream)
            showLog("Thuytv----downloadFile-----fileSaveLocation: " + fileSaveLocation.absolutePath)
            getBaseActivity()?.runOnUiThread(Runnable { //                        MyFilesModel myFilesModel = new MyFilesModel();
                //                        myFilesModel.setUriPath(fileSaveLocation.getAbsolutePath());
                val intent = Intent(getBaseActivity(), PdfViewActivity::class.java)
                intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_NAME, model.name)
                intent.putExtra(AppKeys.KEY_BUNDLE_SHORTCUT_PATH, fileSaveLocation.absolutePath)
                //                        intent.putExtra(AppKeys.KEY_BUNDLE_DATA, myFilesModel);
                startActivity(intent)
                binding.prbLoadingGetData.gone()
            })
            null
        }
    }


}