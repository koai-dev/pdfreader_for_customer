package com.cocna.pdffilereader.ui.drive

import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.databinding.ActivityBaseBinding
import com.cocna.pdffilereader.ui.base.BaseActivity
import com.cocna.pdffilereader.ui.drive.DriveServiceHelper.getGoogleDriveService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.drive.Drive
import java.io.File


/**
 * Created by Thuytv on 16/10/2022.
 */
class DemoDriveActivity : BaseActivity<ActivityBaseBinding>(), ServiceListener {
    //    private lateinit var googleDriveService: GoogleDriveService
    private val REQUEST_CODE_SIGN_IN: Int = 100
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null

    override val bindingInflater: (LayoutInflater) -> ActivityBaseBinding
        get() = ActivityBaseBinding::inflate

    override fun initData() {
//        val config = GoogleDriveConfig(
//            "Google Drive",
//            GoogleDriveService.documentMimeTypes
//        )
//        googleDriveService = GoogleDriveService(this, config)
//        googleDriveService.serviceListener = this
//
        binding.btnLoginDrive.setOnClickListener {
//            googleDriveService.auth()
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
//
            if (account == null) {
                signIn()
            } else {
                mDriveServiceHelper = DriveServiceHelper(getGoogleDriveService(applicationContext, account, "appName"))
            }

        }
        binding.btnOpenFile.setOnClickListener {
            mDriveServiceHelper!!.searchFile("*.pdf","application/pdf").addOnSuccessListener {
                Logger.showLog("Thuytv------searchFolder---addOnSuccessListener: " + it.size)
                if(it.size > 0){
                    for(item in it){
                        Logger.showLog("Thuytv----item: " + item.name)
                    }
                }
            }.addOnFailureListener {
                Logger.showLog("Thuytv------searchFolder---addOnFailureListener")
                it.printStackTrace()
            }
//            mDriveServiceHelper?.searchFolder("testDummy")
//                ?.addOnSuccessListener(object : OnSuccessListener<List<GoogleDriveFileHolder>>() {
//
//        }
        }
    }

    override fun initEvents() {
    }

    override fun loggedIn() {
        Logger.showLog("Thuytv-------loggedIn")
    }

    override fun fileDownloaded(file: File) {
        Logger.showLog("Thuytv-------fileDownloaded")
    }

    override fun cancelled() {
        Logger.showLog("Thuytv-------cancelled")
    }

    override fun handleError(exception: Exception) {
        Logger.showLog("Thuytv-------handleError: " + exception.message)
    }

    //    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
////        googleDriveService.onActivityResult(requestCode, resultCode, data)
//    }
    private fun signIn() {
        mGoogleSignInClient = buildGoogleSignInClient()
        startActivityForResult(mGoogleSignInClient?.getSignInIntent(), REQUEST_CODE_SIGN_IN)
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestScopes(Scope("https://www.googleapis.com/auth/drive"))
            .requestScopes(Drive.SCOPE_FILE)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(applicationContext, signInOptions)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> if (resultCode == RESULT_OK && resultData != null) {
                handleSignInResult(resultData)
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    fun test() {
        println("test")
    }

    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleSignInAccount ->
                mDriveServiceHelper = DriveServiceHelper(getGoogleDriveService(applicationContext, googleSignInAccount, "appName"))
            }
            .addOnFailureListener { e -> e.printStackTrace() }
    }
}