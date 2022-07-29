package com.cocna.pdffilereader.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.myinterface.OnPopupMenuItemClickListener
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
    fun showPopupMenu(v: View, menuData: Int, onPopupMenuItemClickListener: OnPopupMenuItemClickListener) {
        val popup = PopupMenu(getBaseActivity(), v)
        popup.menuInflater.inflate(menuData, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            onPopupMenuItemClickListener.onClickItemPopupMenu(menuItem)
            true
        }
        popup.show()
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
            val fileURI = FileProvider.getUriForFile(
                getBaseActivity()!!, getBaseActivity()!!.packageName + ".provider",
                file
            )
            putExtra(Intent.EXTRA_STREAM, fileURI)
        }
        startActivity(shareIntent)
    }


}