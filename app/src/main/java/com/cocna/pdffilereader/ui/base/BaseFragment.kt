package com.cocna.pdffilereader.ui.base

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.myinterface.OnPopupMenuItemClickListener
import com.cocna.pdffilereader.ui.home.PdfViewActivity
import com.cocna.pdffilereader.ui.home.SplashScreenActivity
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.kochava.tracker.events.Event
import com.kochava.tracker.events.EventType
import io.reactivex.disposables.Disposable
import java.io.File
import java.net.URLConnection
import java.util.*


/**
 * Created by Thuytv on 09/06/2022.
 */
@Suppress("DEPRECATION")
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    private var eventsBusDisposableTheme: Disposable? = null

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() {
            return _binding as VB
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getBaseActivity()?.apply {
            ThemeUtils.onActivityCreateSetTheme(this)
        }
        _binding = bindingInflater.invoke(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onListenChangeTheme()
        initData()
        initEvents()
    }

    abstract fun initData()
    abstract fun initEvents()

//    override fun onDestroyView() {
//        super.onDestroyView()
//
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        onDisposableTheme()
    }

    fun getBaseActivity(): BaseActivity<*>? {
        return activity as? BaseActivity<*>
    }

    fun setAppLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    //    fun getSharedPreferences(): SharePreferenceUtils {
//        return getBaseActivity().sharedPreferences
//    }
    @SuppressLint("RestrictedApi")
    fun showPopupMenu(v: View, menuData: Int, onPopupMenuItemClickListener: OnPopupMenuItemClickListener) {
//        val popup = PopupMenu(getBaseActivity(), v)
//        popup.menuInflater.inflate(menuData, popup.menu)
//        popup.setOnMenuItemClickListener { menuItem ->
//            onPopupMenuItemClickListener.onClickItemPopupMenu(menuItem)
//            true
//        }
//        popup.show()

        getBaseActivity()?.apply {
            val menuBuilder = MenuBuilder(this)
            val inflater = MenuInflater(this)
            inflater.inflate(menuData, menuBuilder)
            val optionsMenu = MenuPopupHelper(this, menuBuilder, v)
            optionsMenu.setForceShowIcon(true)

            // Set Item Click Listener
            menuBuilder.setCallback(object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menu: MenuBuilder, menuItem: MenuItem): Boolean {
                    onPopupMenuItemClickListener.onClickItemPopupMenu(menuItem)
                    return true
                }

                override fun onMenuModeChange(menu: MenuBuilder) {
                }
            })
            // Display the menu
            optionsMenu.show()
        }

    }

    private fun onListenChangeTheme() {
        eventsBusDisposableTheme = RxBus.listen(EventsBus::class.java).subscribe {
            if (EventsBus.RELOAD_THEME == it) {
                getBaseActivity()?.apply {
                    ThemeUtils.changeToTheme(this)
                }
                onDisposableTheme()
            }
        }
    }

    private fun onDisposableTheme() {
        if (eventsBusDisposableTheme?.isDisposed == false) {
            eventsBusDisposableTheme?.dispose()
        }
    }

    fun shareFile(file: File) {
//        val intentShareFile = Intent(Intent.ACTION_SEND)
//        intentShareFile.type = URLConnection.guessContentTypeFromName(file.name)
//        intentShareFile.putExtra(
//            Intent.EXTRA_STREAM,
//            Uri.parse("content://" + file.absolutePath)
//        )
//        startActivity(Intent.createChooser(intentShareFile, getString(R.string.tt_share_file)))

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = URLConnection.guessContentTypeFromName(file.name)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.vl_share_document)
            )
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.vl_share_document_content)
            )
            try {
                val fileURI = FileProvider.getUriForFile(
                    getBaseActivity()!!, getBaseActivity()!!.packageName + ".provider",
                    file
                )
                putExtra(Intent.EXTRA_STREAM, fileURI)
            }catch (e: Exception){
            }

        }
        startActivity(shareIntent)
    }

    fun onBackFragment() {
        if (activity != null) {
            val count = getBaseActivity()?.supportFragmentManager?.backStackEntryCount ?: 0
            if (count > 0) {
                val manager = getBaseActivity()?.supportFragmentManager
                if (manager?.isStateSaved == false) {
                    manager.popBackStack()
                }
            } else {
                getBaseActivity()?.finish()
            }
        }
    }

    fun setUpShortCut(context: Context, myFileModel: MyFilesModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = ContextCompat.getSystemService(context, ShortcutManager::class.java)


            //Create an array of intents to create a more fluent user experience in the back stack
            val intentMessage = Intent(Intent.ACTION_VIEW, null, context, PdfViewActivity::class.java)
            val bundle = Bundle()
            bundle.putString(AppKeys.KEY_BUNDLE_SHORTCUT_NAME, myFileModel.name)
            bundle.putString(AppKeys.KEY_BUNDLE_SHORTCUT_PATH, myFileModel.uriPath)
            intentMessage.putExtras(bundle)
            val intents = arrayOf(
                Intent(Intent.ACTION_VIEW, null, context, SplashScreenActivity::class.java),
                intentMessage
            )

            val shortcut2 = ShortcutInfo.Builder(context, myFileModel?.uriPath)
                .setShortLabel(myFileModel.name ?: "PDF Reader")
                .setLongLabel(myFileModel.name ?: "Open PDF File")
                .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                .setIntents(intents)
                .build()


            shortcutManager!!.dynamicShortcuts = listOf(shortcut2)
            shortcutPin(context, myFileModel.uriPath ?: "shortcut_pdf_id", 1232)
        }
    }

    private fun shortcutPin(context: Context, shortcut_id: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = getBaseActivity()?.getSystemService(ShortcutManager::class.java)

            if (shortcutManager?.isRequestPinShortcutSupported == true) {
                val pinShortcutInfo =
                    ShortcutInfo.Builder(context, shortcut_id).build()

//                val pinnedShortcutCallbackIntent =
//                    shortcutManager.createShortcutResultIntent(pinShortcutInfo)

                val broadcastIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
                // create an anonymous broadcaster.  Unregister
                // to prevent leaks when done.
                // create an anonymous broadcaster.  Unregister
                // to prevent leaks when done.
                getBaseActivity()?.registerReceiver(
                    object : BroadcastReceiver() {
                        override fun onReceive(c: Context?, intent: Intent) {
                            getBaseActivity()?.unregisterReceiver(this)
                            getBaseActivity()?.apply {
                                Logger.showSnackbar(this, getString(R.string.msg_add_shortcut_success))
                            }

                        }
                    }, IntentFilter(Intent.ACTION_CREATE_SHORTCUT)
                )

                val successCallback: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getBroadcast(
                        context, /* request code */ requestCode,
                        broadcastIntent, /* flags */ PendingIntent.FLAG_MUTABLE
                    )
                } else {
                    PendingIntent.getBroadcast(
                        context, /* request code */ requestCode,
                        broadcastIntent, /* flags */ 0
                    )
                }

                shortcutManager.requestPinShortcut(
                    pinShortcutInfo,
                    successCallback.intentSender
                )
            }
        }
    }


}