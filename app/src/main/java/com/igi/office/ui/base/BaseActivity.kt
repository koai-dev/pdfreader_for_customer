package com.igi.office.ui.base

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.MobileAds
import com.igi.office.common.*
import com.igi.office.ui.home.model.MyFilesModel
import java.io.File
import java.util.*


/**
 * Created by Thuytv on 09/06/2022.
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater) -> VB

    private var broadcastReceiver: ConnectivityReceiver? = null
    lateinit var sharedPreferences: SharePreferenceUtils
    private var primaryBaseActivity : Context? = null

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.onActivityCreateSetTheme(this)
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(requireNotNull(_binding).root)
        onHandleNetworkListener()
        MobileAds.initialize(this) { }
        broadcastReceiver = ConnectivityReceiver()
        registerReceiver(
            broadcastReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        sharedPreferences = SharePreferenceUtils(this)
        initData()
        initEvents()

    }

    abstract fun initData()
    abstract fun initEvents()
    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        unregisterReceiver(broadcastReceiver)
    }

    override fun attachBaseContext(newBase: Context) {
        primaryBaseActivity = newBase
        val localeToSwitchTo = SharePreferenceUtils(newBase).getLanguage()
        val localeUpdatedContext: ContextWrapper = LanguageUtils.updateLocale(newBase, Locale(localeToSwitchTo))
        super.attachBaseContext(localeUpdatedContext)
    }

    private fun isNetworkAvailable(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val networkInfo = manager!!.activeNetworkInfo
        var isAvailable = false
        if (networkInfo != null && networkInfo.isConnected) {
            // Network is present and connected
            isAvailable = true
        }
        return isAvailable
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
//        Logger.showToast(this, "Network: $isConnected")
    }

    fun onHandleNetworkListener() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
//                Logger.showToast(this@BaseActivity, "Network Available")
            }

            override fun onUnavailable() {
//                Logger.showToast(this@BaseActivity, "Network UnAvailable")
            }
        }
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    fun onNextScreen(nextActivity: Class<*>, bundle: Bundle?, isFinish: Boolean) {
        val intent = Intent(this, nextActivity)
        bundle?.let {
            intent.putExtras(it)
        }
        startActivity(intent)
        if (isFinish) {
            finish()
        }
    }
    fun onNextScreenClearTop(nextActivity: Class<*>, bundle: Bundle?) {
        val intent = Intent(this, nextActivity)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        bundle?.let {
            intent.putExtras(it)
        }
        startActivity(intent)
    }
    fun replaceFragment(fragment: Fragment, bundle: Bundle?, containerId: Int) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        bundle?.let {
            fragment.arguments = it
        }
        fragmentTransaction.replace(containerId, fragment)
        fragmentTransaction.commit()
    }

    fun printFile(myFilesModel: MyFilesModel) {
        val printManager: PrintManager = primaryBaseActivity?.getSystemService(Context.PRINT_SERVICE) as PrintManager
        try {
            myFilesModel.uriPath?.apply {
                val file = File(this)
                val printAdapter = PdfDocumentAdapter(file.absolutePath, myFilesModel.name ?: "")
                printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}