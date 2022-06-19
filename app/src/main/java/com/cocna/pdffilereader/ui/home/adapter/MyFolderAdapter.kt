package com.cocna.pdffilereader.ui.home.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.anggrayudi.storage.file.extension
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Common
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.common.inflate
import com.cocna.pdffilereader.databinding.ItemViewFilesGridBinding
import com.cocna.pdffilereader.databinding.ItemViewMyFilesBinding

/**
 * Created by Thuytv on 10/06/2022.
 */
class MyFolderAdapter(
    private var lstData: ArrayList<DocumentFile>,
    private var typeAdapter: Int,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    var mFileFilterList = ArrayList<DocumentFile>()

    companion object {
        const val TYPE_VIEW_FILE = 0
        const val TYPE_VIEW_FILE_GRID = 2
        const val TYPE_VIEW_ADS = 1
    }

    init {
        mFileFilterList = lstData
    }

    fun updateData(mlstData: ArrayList<DocumentFile>) {
        lstData = mlstData
        mFileFilterList = lstData
        notifyDataSetChanged()
    }

    fun renameData(mData: DocumentFile) {
        val indexItem = mFileFilterList.indexOf(mData)
        if (indexItem > -1) {
            mFileFilterList[indexItem] = mData
            notifyItemChanged(indexItem)
        }
    }

    fun deleteData(mData: DocumentFile) {
        val indexItem = mFileFilterList.indexOf(mData)
        mFileFilterList.remove(mData)
        notifyItemRemoved(indexItem)
    }

    fun updateTypeAdapter(typeAdapter: Int) {
        this.typeAdapter = typeAdapter
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
//        if (position > 0 && position % 4 == 0) return TYPE_VIEW_ADS
        return typeAdapter
    }

    interface OnItemClickListener {
        fun onClickItem(documentFile: DocumentFile)
        fun onClickItemMore(view: View, documentFile: DocumentFile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_VIEW_FILE_GRID -> GridItemView(parent.inflate(R.layout.item_view_files_grid))
            else -> MyFilesItemView(parent.inflate(R.layout.item_view_my_files))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyFilesItemView -> holder.bind(mFileFilterList[position])
            is GridItemView -> holder.bind(mFileFilterList[position])
        }
    }

    override fun getItemCount(): Int {
        return mFileFilterList.size
    }

    inner class MyFilesItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mData: DocumentFile) {
            val binding = ItemViewMyFilesBinding.bind(itemView)
            binding.vlItemName.text = mData.name
            binding.vlItemDate.text = Common.covertTimeLongToString(mData.lastModified())
            setIconFile(binding.imvItemFile, mData.extension ?: "")

            binding.llItemMyFile.setOnClickListener {
                MultiClickPreventer.preventMultiClick(it)
                onItemClickListener.onClickItem(mData)
            }
            binding.imvItemMore.setOnClickListener {
                MultiClickPreventer.preventMultiClick(it)
                onItemClickListener.onClickItemMore(binding.imvItemMore, mData)
            }
        }
    }

    inner class GridItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mData: DocumentFile) {
            val binding = ItemViewFilesGridBinding.bind(itemView)
            binding.vlItemName.text = mData.name
            binding.vlItemDate.text = Common.covertTimeLongToStringGrid(mData.lastModified())
            setIconFile(binding.imvItemFile, mData.extension ?: "")
            binding.llItemMyFile.setOnClickListener {
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
            "docx", "doc" -> {
                imageView.setImageResource(R.drawable.ic_doc_file)
            }
            else -> {
                imageView.setImageResource(R.drawable.ic_crown)
            }
        }
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    mFileFilterList = lstData
                } else {
                    val resultList = ArrayList<DocumentFile>()
                    for (row in lstData) {
                        if (row.name?.lowercase()?.contains(charSearch.lowercase()) == true) {
                            resultList.add(row)
                        }
                    }
                    mFileFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = mFileFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mFileFilterList = results?.values as ArrayList<DocumentFile>
                notifyDataSetChanged()
            }
        }
    }

}