package com.cocna.pdffilereader.imagepicker.ui.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.imagepicker.helper.GlideHelper
import com.cocna.pdffilereader.imagepicker.helper.ImageHelper
import com.cocna.pdffilereader.imagepicker.helper.LayoutManagerHelper
import com.cocna.pdffilereader.imagepicker.helper.ToastHelper
import com.cocna.pdffilereader.imagepicker.listener.OnImageSelectListener
import com.cocna.pdffilereader.imagepicker.model.GridCount
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.imagepicker.model.ImagePickerConfig
import com.cocna.pdffilereader.imagepicker.widget.GridSpacingItemDecoration
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ImageGroupAdapter(
    private val mContext: Context,
    private val config: ImagePickerConfig,
    private val imageSelectListener: OnImageSelectListener,
    private val gridCount: GridCount
) : BaseRecyclerViewAdapter<ImageGroupAdapter.ImageViewHolder?>(mContext) {

    private var lstData: SortedMap<String, ArrayList<Image>> = TreeMap()
    private var lstKeys = ArrayList<String>()
    private var mPickedAdapter: ImagePickerAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = inflater.inflate(R.layout.imagepicker_item_image_group, parent, false)

        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(
        viewHolder: ImageViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        onBindViewHolder(viewHolder, position)
    }

    override fun onBindViewHolder(viewHolder: ImageViewHolder, position: Int) {
        mPickedAdapter = ImagePickerAdapter(mContext, config, imageSelectListener)
        mPickedAdapter?.apply {
            viewHolder.bind(lstKeys[position], lstData, mContext, this, gridCount, config)
        }
    }

    override fun getItemCount(): Int {
        return lstData.size
    }

    fun setData(mData: SortedMap<String, ArrayList<Image>>) {
        this.lstData = mData
        lstKeys.clear()
        for (item in lstData.entries) {
            lstKeys.add(item.key)
        }
        notifyDataSetChanged()
    }

    fun setSelectedImages(selectedImages: ArrayList<Image>) {
        mPickedAdapter?.setSelectedImages(selectedImages)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageDate: TextView = itemView.findViewById(R.id.vl_item_image_date)
        val itemImageSize: TextView = itemView.findViewById(R.id.vl_item_image_size)
        val rcvPickedGroup: RecyclerView = itemView.findViewById(R.id.rcv_image_picked_group)

        fun bind(
            mHeader: String, lstData: SortedMap<String, ArrayList<Image>>, mContext: Context,
            mPickedAdapter: ImagePickerAdapter,
            gridCount: GridCount, mConfig: ImagePickerConfig
        ) {
            val lstImage = lstData[mHeader]
            itemImageDate.text = mHeader
            lstImage?.let {
                itemImageSize.text = mContext.getString(R.string.vl_size_image, it.size)

                val gridLayoutManager = LayoutManagerHelper.newInstance(mContext, gridCount)
                rcvPickedGroup.apply {
                    layoutManager = gridLayoutManager
                    addItemDecoration(
                        GridSpacingItemDecoration(
                            gridLayoutManager.spanCount,
                            resources.getDimension(R.dimen.imagepicker_grid_spacing).toInt()
                        )
                    )
                    setHasFixedSize(true)
                    adapter = mPickedAdapter
                }
                mPickedAdapter.setData(it)
                if (mConfig.selectedImages.size > 0) {
                    mPickedAdapter.setSelectedImages(mConfig.selectedImages)
                }
            }

        }
    }

}