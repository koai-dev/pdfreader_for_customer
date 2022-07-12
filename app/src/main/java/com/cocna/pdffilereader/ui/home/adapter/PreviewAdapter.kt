package com.cocna.pdffilereader.ui.home.adapter

import android.content.Context
import android.content.res.Resources
import android.os.Build
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
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore

/**
 * Created by Thuytv on 10/06/2022.
 */
class PreviewAdapter(
    val mContext: Context?,
    val pdfiumCore: PdfiumCore,
    val pdfDocument: PdfDocument,
    val pdfName: String,
    val totalPageNum: Int,
    var currentPage: Int,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mBinding: ItemPreviewPdfViewerBinding? = null

    //    fun updateData(mlstData: ArrayList<MyFilesModel>) {
//        lstData = mlstData
//        notifyDataSetChanged()
//    }
    interface OnItemClickListener {
        fun onClickItem(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyFilesItemView(parent.inflate(R.layout.item_preview_pdf_viewer))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyFilesItemView -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int {
        return totalPageNum
    }
    fun updateCurrentPage(currentPage: Int){
        this.currentPage = currentPage
        notifyItemChanged(currentPage)
    }

    inner class MyFilesItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            mBinding = ItemPreviewPdfViewerBinding.bind(itemView)
            mBinding?.run {
                val mPage = position + 1
                PreviewUtils.getInstance().loadBitmapFromPdf(mContext, imvPreviewPdf, pdfiumCore, pdfDocument, pdfName, position)
                vlPagePreview.text = mPage.toString()
                if(currentPage == mPage){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        vlPagePreview.setTextColor(mContext!!.resources.getColor(R.color.color_E57373, mContext.theme))
                    }else{
                        vlPagePreview.setTextColor(mContext!!.resources.getColor(R.color.color_E57373))
                    }
                    llPreview.setBackgroundResource(R.drawable.bg_border_preview)
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        vlPagePreview.setTextColor(mContext!!.resources.getColor(R.color.text_color_all, mContext.theme))
                    }else{
                        vlPagePreview.setTextColor(mContext!!.resources.getColor(R.color.text_color_all))
                    }
                    llPreview.setBackgroundResource(R.drawable.bg_border_preview_normal)
                }
                imvPreviewPdf.setOnClickListener {
                    MultiClickPreventer.preventMultiClick(it)
                    onItemClickListener.onClickItem(position)
                }
            }

        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        try {
            mBinding?.run {
                PreviewUtils.getInstance().cancelLoadBitmapFromPdf(imvPreviewPdf.tag.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}