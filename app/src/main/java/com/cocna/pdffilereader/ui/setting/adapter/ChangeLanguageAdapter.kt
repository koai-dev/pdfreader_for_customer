package com.cocna.pdffilereader.ui.setting.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.common.inflate
import com.cocna.pdffilereader.common.invisible
import com.cocna.pdffilereader.common.visible
import com.cocna.pdffilereader.databinding.ItemViewChangeLanguageBinding
import com.cocna.pdffilereader.ui.setting.model.LanguageModel

/**
 * Created by Thuytv on 4/11/2019.
 */
class ChangeLanguageAdapter(
    private var lstData: ArrayList<LanguageModel>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MainItemView(parent.inflate(R.layout.item_view_change_language))
    }

    override fun getItemCount(): Int {
        return lstData.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MainItemView) {
            holder.bind(lstData[position])
        }
    }

    inner class MainItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mData: LanguageModel) {
            val binding = ItemViewChangeLanguageBinding.bind(itemView)
            if (mData.isSelected == true) {
                binding.imvSelected.visible()
            } else {
                binding.imvSelected.invisible()
            }
            binding.imvItemLanguage.setImageResource(mData.languageIcon!!)
            binding.vlItemLanguageName.text = mData.languageName

            binding.llItemChangeLanguage.setOnClickListener {
                MultiClickPreventer.preventMultiClick(it)
                if (mData.isSelected == false) {
                    var indexOld = 0
                    for (item in lstData) {
                        if (item.isSelected == true) {
                            indexOld = lstData.indexOf(item)
                            item.isSelected = false
                        }
                    }
                    notifyItemChanged(indexOld)
                    val indexNew = lstData.indexOf(mData)
                    mData.isSelected = true
                    notifyItemChanged(indexNew)
                    onItemClickListener.onClickItem(mData)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onClickItem(mData: LanguageModel)
    }
}