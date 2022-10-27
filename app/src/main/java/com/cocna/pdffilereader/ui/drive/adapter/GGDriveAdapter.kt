package com.cocna.pdffilereader.ui.drive.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.cocna.pdffilereader.common.Common
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.common.inflate
import com.cocna.pdffilereader.databinding.ItemViewMyFilesBinding
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.invisible

/**
 * Created by Thuytv on 10/06/2022.
 */
class GGDriveAdapter(
    val mContext: Context?,
    private val lstData: ArrayList<DriveModel>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_VIEW_FILE = 0
    }

    fun updateData(mlstData: ArrayList<DriveModel>) {
        this.lstData.clear()
        this.lstData.addAll(mlstData)
        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        return TYPE_VIEW_FILE
    }

    interface OnItemClickListener {
        fun onClickItem(documentFile: DriveModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyFilesItemView(parent.inflate(R.layout.item_view_my_files))
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
        fun bind(mData: DriveModel) {
            val binding = ItemViewMyFilesBinding.bind(itemView)
            binding.vlItemName.text = mData.name
            binding.vlItemDate.text = mContext?.getString(R.string.vl_accessed, Common.covertTimeLongToString(mData.createdTime))
//            binding.vlItemDate.text = mContext?.getString(R.string.vl_accessed, mData.createdTime)
            setIconFile(binding.imvItemFile, mData.extensionName ?: "")
            binding.vlItemSize.text = Common.convertByteToString(mData.size)
            binding.vlItemLocation.text = mData.folderName
            binding.imvItemMore.invisible()

            binding.llItemMyFile.setOnClickListener {
                MultiClickPreventer.preventMultiClick(it)
                onItemClickListener.onClickItem(mData)
            }
        }
    }

    private fun setIconFile(imageView: ImageView, strExtension: String) {
        when (strExtension.lowercase()) {
            "pdf" -> {
                imageView.setImageResource(R.drawable.ic_pdf_file)
            }
            "xlsx", "xls" -> {
                imageView.setImageResource(R.drawable.ic_excel_file)
            }
            "pptx", "ppt" -> {
                imageView.setImageResource(R.drawable.ic_powpoint_file)
            }
            else -> {
                imageView.setImageResource(R.drawable.ic_pdf_file)
            }
        }
    }

}