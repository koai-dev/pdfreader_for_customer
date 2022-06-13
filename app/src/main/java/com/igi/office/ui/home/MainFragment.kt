package com.igi.office.ui.home

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import com.igi.office.R
import com.igi.office.databinding.FragmentMainBinding
import com.igi.office.ui.base.BaseFragment

class MainFragment : BaseFragment<FragmentMainBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding = FragmentMainBinding::inflate

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var myFilesFragment: MyFilesFragment
    private lateinit var settingFragment: SettingFragment
    private lateinit var favoriteFragment: FavoriteFragment
    private lateinit var browseFragment: BrowseFragment

    override fun initData() {
        myFilesFragment = MyFilesFragment()
        settingFragment = SettingFragment()
        favoriteFragment = FavoriteFragment()
        browseFragment = BrowseFragment()

        loadFragment(myFilesFragment)
        loadFragment(settingFragment)
        loadFragment(favoriteFragment)
        loadFragment(browseFragment)
        hideShowFragment(myFilesFragment, settingFragment, favoriteFragment, browseFragment)
        binding.navigationBottom.itemIconTintList = null
    }

    override fun initEvents() {
        binding.navigationBottom.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.navigation_my_file -> {
                        hideShowFragment(myFilesFragment, settingFragment, favoriteFragment, browseFragment)
                    }
                    R.id.navigation_setting -> {
                        hideShowFragment(settingFragment, myFilesFragment, favoriteFragment, browseFragment)
                    }
                    R.id.navigation_favorite -> {
                        hideShowFragment(favoriteFragment, myFilesFragment, settingFragment, browseFragment)
                    }
                    R.id.navigation_browse -> {
                        hideShowFragment(browseFragment, myFilesFragment, favoriteFragment, browseFragment)
                    }
                }
                return true
            }
        })
//        val iconColorStates = ColorStateList(
//            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
//                Color.parseColor("#123456"),
//                Color.parseColor("#654321")
//            )
//        )
//        binding.navigationBottom.itemTextColor = iconColorStates
//        binding.navigationBottom.itemIconTintList = iconColorStates
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.add(R.id.fm_container, fragment, fragment.tag)
        transaction?.commit()
    }

    private fun hideShowFragment(showFragment: Fragment, hideFragment1: Fragment, hideFragment2: Fragment, hideFragment3: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()?.hide(hideFragment1)?.hide(hideFragment2)?.hide(hideFragment3)?.show(showFragment)?.commit()
    }
//    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//        when (item.itemId) {
//            R.id.navigation_home -> {
//                hideShowFragment(homeFragment, notificationFragment, dashboardFragment, browserFragment)
//            }
//            R.id.navigation_dashboard -> {
//                hideShowFragment(dashboardFragment, notificationFragment, homeFragment, browserFragment)
//            }
//            R.id.navigation_notifications -> {
//                hideShowFragment(notificationFragment, dashboardFragment, homeFragment, browserFragment)
//            }
//            R.id.navigation_browser -> {
//                hideShowFragment(browserFragment, homeFragment, dashboardFragment, notificationFragment)
//            }
//        }
//        return@OnNavigationItemSelectedListener true
//    }
}