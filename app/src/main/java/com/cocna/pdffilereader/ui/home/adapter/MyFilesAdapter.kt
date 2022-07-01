package com.cocna.pdffilereader.ui.home.adapter

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
import com.cocna.pdffilereader.common.Common
import com.cocna.pdffilereader.common.MultiClickPreventer
import com.cocna.pdffilereader.common.inflate
import com.cocna.pdffilereader.databinding.ItemViewAdsBinding
import com.cocna.pdffilereader.databinding.ItemViewFilesGridBinding
import com.cocna.pdffilereader.databinding.ItemViewMyFilesBinding
import com.cocna.pdffilereader.databinding.ItemViewMyFolderBinding
import com.cocna.pdffilereader.ui.home.model.MyFilesModel
import com.cocna.pdffilereader.R

/**
 * Created by Thuytv on 10/06/2022.
 */
class MyFilesAdapter(
    val mContext: Context?,
    private var lstData: ArrayList<MyFilesModel>,
    private var typeAdapter: Int,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val ID_AD_NATIVE_FILE = "ca-app-pub-3940256099942544/2247696110"
    var mFileFilterList = ArrayList<MyFilesModel>()
    var mFileListWithAds = ArrayList<MyFilesModel>()
    private var adsPosition = 0

    companion object {
        const val TYPE_VIEW_FILE = 0
        const val TYPE_VIEW_ADS = 1
        const val TYPE_VIEW_ADS_GRID = 4
        const val TYPE_VIEW_FILE_GRID = 2
        const val TYPE_VIEW_FOLDER = 3
    }

    init {
//        mFileFilterList = lstData
        updateDataAds(lstData)
    }

    private fun updateDataAds(mListData: ArrayList<MyFilesModel>): ArrayList<MyFilesModel> {
        if (mListData.isNotEmpty()) {
            mFileListWithAds.clear()
            adsPosition = 0
            for (item in mListData) {
                if (adsPosition == 5) {
                    val adsFile = MyFilesModel()
                    adsFile.isAds = true
                    mFileListWithAds.add(adsFile)
                    adsPosition = 0
                }
                item.isAds = false
                mFileListWithAds.add(item)
                adsPosition++
            }
        }
//        else if (typeAdapter == TYPE_VIEW_FILE_GRID || typeAdapter == TYPE_VIEW_FOLDER) {
//            mFileListWithAds.clear()
//            mFileListWithAds.addAll(mListData)
//        }
        return mFileListWithAds
    }

    fun updateData(mlstData: ArrayList<MyFilesModel>) {
        lstData = mlstData
//        mFileFilterList = lstData
        updateDataAds(lstData)
        notifyDataSetChanged()
    }

    fun renameData(mData: MyFilesModel) {
        val indexItem = mFileListWithAds.indexOf(mData)
        if (indexItem > -1) {
            mFileListWithAds[indexItem] = mData
            notifyItemChanged(indexItem)
        }
    }

    fun deleteData(mData: MyFilesModel) {
        val indexItem = mFileListWithAds.indexOf(mData)
        mFileListWithAds.remove(mData)
        notifyItemRemoved(indexItem)
    }

    fun updateTypeAdapter(typeAdapter: Int) {
        this.typeAdapter = typeAdapter
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if ((mFileListWithAds[position].lstChildFile?.size ?: 0) > 0) return TYPE_VIEW_FOLDER
        if (mFileListWithAds[position].isAds == true) {
            if (typeAdapter == TYPE_VIEW_FILE_GRID) {
                return TYPE_VIEW_ADS_GRID
            } else {
                return TYPE_VIEW_ADS
            }
        }
        return typeAdapter
    }

    interface OnItemClickListener {
        fun onClickItem(documentFile: MyFilesModel)
        fun onClickItemMore(view: View, documentFile: MyFilesModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_VIEW_ADS -> AdsItemView(parent.inflate(R.layout.item_view_ads))
            TYPE_VIEW_ADS_GRID -> AdsItemGridView(parent.inflate(R.layout.item_view_ads))
            TYPE_VIEW_FILE_GRID -> GridItemView(parent.inflate(R.layout.item_view_files_grid))
            TYPE_VIEW_FOLDER -> MyFolderItemView(parent.inflate(R.layout.item_view_my_folder))
            else -> MyFilesItemView(parent.inflate(R.layout.item_view_my_files))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyFilesItemView -> holder.bind(mFileListWithAds[position])
            is GridItemView -> holder.bind(mFileListWithAds[position])
            is AdsItemView -> holder.bind()
            is AdsItemGridView -> holder.bind()
            is MyFolderItemView -> holder.bind(mFileListWithAds[position])
        }
    }

    override fun getItemCount(): Int {
        return mFileListWithAds.size
    }

    inner class MyFilesItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mData: MyFilesModel) {
            val binding = ItemViewMyFilesBinding.bind(itemView)
            binding.vlItemName.text = mData.name
            binding.vlItemDate.text = Common.covertTimeLongToString(mData.lastModified)
            setIconFile(binding.imvItemFile, mData.extensionName ?: "")

            binding.llItemMyFile.setOnClickListener {
                MultiClickPreventer.preventMultiClick(it)
                onItemClickListener.onClickItem(mData)
            }
            binding.imvItemMore.setOnClickListener {
                MultiClickPreventer.preventMultiClick(it)
                onItemClickListener.onClickItemMore(binding.imvItemMore, mData)
            }
            binding.llItemMyFile.setOnLongClickListener {
                onItemClickListener.onClickItemMore(binding.imvItemMore, mData)
                true
            }
        }
    }

    inner class GridItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mData: MyFilesModel) {
            val binding = ItemViewFilesGridBinding.bind(itemView)
            binding.vlItemName.text = mData.name
            binding.vlItemDate.text = Common.covertTimeLongToStringGrid(mData.lastModified)
            setIconFile(binding.imvItemFile, mData.extensionName ?: "")
            binding.llItemMyFile.setOnClickListener {
                onItemClickListener.onClickItem(mData)
            }
            binding.llItemMyFile.setOnLongClickListener {
                onItemClickListener.onClickItemMore(binding.llItemMyFile, mData)
                true
            }
        }

    }

    inner class MyFolderItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mData: MyFilesModel) {
            val binding = ItemViewMyFolderBinding.bind(itemView)
            binding.vlItemFolderName.text = mData.folderName
            binding.vlItemSizeFolder.text = mContext?.getString(R.string.vl_size_folder, mData.lstChildFile?.size ?: 0)

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

    inner class AdsItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            val binding = ItemViewAdsBinding.bind(itemView)
            mContext?.apply {
                val builder = AdLoader.Builder(mContext, ID_AD_NATIVE_FILE)

                builder.forNativeAd { nativeAd ->
                    val adView = LayoutInflater.from(mContext)
                        .inflate(R.layout.ads_unfield_item_file, null) as NativeAdView
                    populateNativeAdView(nativeAd, adView, true)
                    binding.frameAdsFrame.removeAllViews()
                    binding.frameAdsFrame.addView(adView)
                }

//                val videoOptions = VideoOptions.Builder()
//            .setStartMuted(start_muted_checkbox.isChecked)
//                    .build()

//                val adOptions = NativeAdOptions.Builder()
////                    .setVideoOptions(videoOptions)
//                    .build()
//
//                builder.withNativeAdOptions(adOptions)

                val adLoader = builder.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    }
                }).build()

                adLoader.loadAd(AdRequest.Builder().build())
            }


        }
    }

    inner class AdsItemGridView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            val binding = ItemViewAdsBinding.bind(itemView)
            mContext?.apply {
                val builder = AdLoader.Builder(mContext, ID_AD_NATIVE_FILE)

                builder.forNativeAd { nativeAd ->
                    val adView = LayoutInflater.from(mContext)
                        .inflate(R.layout.ads_unfield_item_file_grid, null) as NativeAdView
                    populateNativeAdView(nativeAd, adView, false)
                    binding.frameAdsFrame.removeAllViews()
                    binding.frameAdsFrame.addView(adView)
                }
                val adLoader = builder.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    }
                }).build()

                adLoader.loadAd(AdRequest.Builder().build())
            }


        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView, isListView: Boolean) {

        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        if (isListView) {
            adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        }
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }
        if (isListView) {
            if (nativeAd.advertiser == null) {
                adView.advertiserView?.visibility = View.INVISIBLE
            } else {
                (adView.advertiserView as TextView).text = nativeAd.advertiser
                adView.advertiserView?.visibility = View.VISIBLE
            }
        }
        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    mFileFilterList = updateDataAds(lstData)
                } else {
                    val resultList = ArrayList<MyFilesModel>()
                    for (row in lstData) {
                        if (row.name?.lowercase()?.contains(charSearch.lowercase()) == true || row.folderName?.lowercase()?.contains(charSearch.lowercase()) == true) {
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
                mFileListWithAds = results?.values as ArrayList<MyFilesModel>
                notifyDataSetChanged()
            }
        }
    }

}