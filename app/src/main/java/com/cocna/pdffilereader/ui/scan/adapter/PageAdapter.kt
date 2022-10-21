package com.cocna.pdffilereader.ui.scan.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.*
import com.cocna.pdffilereader.databinding.*

/**
 * Created by Thuytv on 10/06/2022.
 */
class PageAdapter(
    private val mContext: Context?,
    private var lstData: ArrayList<PageModel>, private val onItemPageClickListener: OnItemPageClickListener
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
            is MyFilesItemView -> holder.bind(lstData[position], position)
        }
    }

    override fun getItemCount(): Int {
        return lstData.size
    }

    inner class MyFilesItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mData: PageModel, position: Int) {
            val binding = ItemViewPageModeBinding.bind(itemView)
            binding.vlItemName.text = mData.name
            if (mData.isSelected == true) {
                binding.viewItemSelected.visible()
                Common.setTextColor(mContext, binding.vlItemName, R.color.text_color_all)
            } else {
                binding.viewItemSelected.invisible()
                Common.setTextColor(mContext, binding.vlItemName, R.color.text_color_search_home_hint)
            }
            binding.vlItemName.setOnClickListener {
                MultiClickPreventer.preventMultiClick(it)
                onItemPageClickListener.onClickItem(position)
                for (item in lstData) {
                    item.isSelected = mData.name == item.name
                }
                notifyDataSetChanged()
            }
        }
    }

    interface OnItemPageClickListener {
        fun onClickItem(position: Int)
    }

}