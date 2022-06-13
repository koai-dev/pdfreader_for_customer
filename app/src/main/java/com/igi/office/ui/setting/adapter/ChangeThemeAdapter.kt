package com.igi.office.ui.setting.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.igi.office.R
import com.igi.office.common.MultiClickPreventer
import com.igi.office.common.inflate
import com.igi.office.databinding.ItemViewChangeThemeBinding
import com.igi.office.ui.home.model.MyFilesModel
import com.igi.office.ui.setting.model.ThemeModel

/**
 * Created by Thuytv on 4/11/2019.
 */
class ChangeThemeAdapter(
    val mContext: Context,
    private var lstData: ArrayList<ThemeModel>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MainItemView(parent.inflate(R.layout.item_view_change_theme))
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
        fun bind(themeModel: ThemeModel) {
            val binding = ItemViewChangeThemeBinding.bind(itemView)
            if (themeModel.isSelected == true) {
                binding.imvItemChangeTheme.setImageResource(R.drawable.ic_theme_circle_selected)
            } else {
                binding.imvItemChangeTheme.setImageResource(R.drawable.ic_theme_circle_unselected)
            }
//            binding.imvItemChangeTheme.setColorFilter(mContext.resources.getColor(themeModel.idTheme!!, mContext.theme))
            binding.imvItemChangeTheme.setColorFilter(ContextCompat.getColor(mContext, themeModel.idTheme!!))

            binding.llItemChangeTheme.setOnClickListener {
                MultiClickPreventer.preventMultiClick(it)
                if (themeModel.isSelected == false) {
                    var indexOld = 0
                    for (item in lstData) {
                        if (item.isSelected == true) {
                            indexOld = lstData.indexOf(item)
                            item.isSelected = false
                        }
                    }
                    notifyItemChanged(indexOld)
                    val indexNew = lstData.indexOf(themeModel)
                    themeModel.isSelected = true
                    notifyItemChanged(indexNew)
                    onItemClickListener.onClickItem(themeModel)
                }
            }
        }
    }
    interface OnItemClickListener {
        fun onClickItem(themeModel: ThemeModel)
    }
}