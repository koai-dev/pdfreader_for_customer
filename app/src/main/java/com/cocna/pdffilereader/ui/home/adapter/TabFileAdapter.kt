package com.cocna.pdffilereader.ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.content.Context
import com.google.android.material.tabs.TabLayout
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentStatePagerAdapter
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.databinding.ItemTabFileBinding


/**
 * Create by tieut on 4/14/2019
 */
internal class TabFileAdapter(val mContext: Context, manager: FragmentManager) :
    FragmentStatePagerAdapter(
        manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
    private val mFragmentList: ArrayList<Fragment> = ArrayList()
    private val mFragmentTitleList: ArrayList<String> = ArrayList()

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    fun clearFragment() {
        mFragmentList.clear()
        mFragmentTitleList.clear()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleList[position]
    }

    fun getTabView(position: Int, countNotify: Int, tabLayout: TabLayout): View {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.item_tab_file, tabLayout, false)
        val binding = ItemTabFileBinding.bind(view)
        binding.vlItemTabName.text = mFragmentTitleList[position]

        return view
    }

    fun updateTitleTab(position: Int, strTab: String) {
        mFragmentTitleList[position] = strTab
        notifyDataSetChanged()
    }
}