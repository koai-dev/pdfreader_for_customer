package com.igi.office.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.igi.office.common.*
import com.igi.office.myinterface.OnPopupMenuItemClickListener
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Created by Thuytv on 09/06/2022.
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    private var eventsBusDisposableTheme: Disposable? = null

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onDisposableTheme()
    }

    fun getBaseActivity(): BaseActivity<*>? {
        return activity as BaseActivity<*>
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
}