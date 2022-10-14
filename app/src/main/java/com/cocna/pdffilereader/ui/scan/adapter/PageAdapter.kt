package com.cocna.pdffilereader.ui.scan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.*
import com.cocna.pdffilereader.ui.home.model.AdsLogModel
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by Thuytv on 10/06/2022.
 */
class PageAdapter(
    private val mContext: Context?,
    private var lstData: ArrayList<PageModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun updateData(data: PageModel) {
        for (item in lstData) {
            item.isSelected = data.name == item.name
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyFilesItemView(parent.inflate(R.layout.item_view_page_mode))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyFilesItemView -> holder.bind(lstData[position])
        }
    }

    override fun getItemCount(): Int {
        return lstData.size
    }

    inner class MyFilesItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mData: PageModel) {
            val binding = ItemViewPageModeBinding.bind(itemView)
            binding.vlItemName.text = mData.name
            if (mData.isSelected == true) {
                binding.viewItemSelected.visible()
                Common.setTextColor(mContext, binding.vlItemName, R.color.text_color_all)
            } else {
                binding.viewItemSelected.invisible()
                Common.setTextColor(mContext, binding.vlItemName, R.color.text_color_search_home_hint)
            }
        }
    }

}