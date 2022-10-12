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
import com.cocna.pdffilereader.common.inflate
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

class ImagePickerAdapter(
    private val mContext: Context,
    private val config: ImagePickerConfig,
    private val imageSelectListener: OnImageSelectListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_HEADER = 1
    }

    private val selectedImages = arrayListOf<Image>()
    private val images: ArrayList<Image> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        if (images.get(position).header?.isNotEmpty() == true) {
            return TYPE_HEADER
        }
        return TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(parent.inflate(R.layout.imagepicker_item_image_group))
            else -> ImageViewHolder(parent.inflate(R.layout.imagepicker_item_image), config.isShowNumberIndicator, config.selectedIndicatorColor)
        }

//        val itemView = inflater.inflate(R.layout.imagepicker_item_image, parent, false)
//        return ImageViewHolder(
//            itemView,
//            config.isShowNumberIndicator,
//            config.selectedIndicatorColor
//        )
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (viewHolder is HeaderViewHolder) {
            viewHolder.bind(images[position], mContext)
        } else if (viewHolder is ImageViewHolder) {
            if (payloads.isEmpty()) {
                onBindViewHolder(viewHolder, position)
            } else {
                when {
                    payloads.any { it is ImageSelectedOrUpdated } -> {
                        if (config.isShowNumberIndicator) {
                            val image = images[position]
                            val selectedIndex = ImageHelper.findImageIndex(image, selectedImages)
                            viewHolder.selectedNumber.text = (selectedIndex + 1).toString()
                            viewHolder.selectedNumber.visibility = View.VISIBLE
                            viewHolder.selectedIcon.visibility = View.GONE
                        } else {
                            viewHolder.selectedIcon.visibility = View.VISIBLE
                            viewHolder.selectedNumber.visibility = View.GONE
                        }
                        setupItemForeground(viewHolder.image, true)
                    }
                    payloads.any { it is ImageUnselected } -> {
                        if (config.isShowNumberIndicator) viewHolder.selectedNumber.visibility =
                            View.GONE
                        else viewHolder.selectedIcon.visibility = View.GONE
                        setupItemForeground(viewHolder.image, false)
                    }
                    else -> {
                        onBindViewHolder(viewHolder, position)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ImageViewHolder) {
            val image = images[position]
            val selectedIndex = ImageHelper.findImageIndex(image, selectedImages)
            val isSelected = config.isMultipleMode && selectedIndex != -1
            image.uri?.let {
                GlideHelper.loadImage(viewHolder.image, it)
            }
            setupItemForeground(viewHolder.image, isSelected)

            viewHolder.gifIndicator.visibility =
                if (ImageHelper.isGifFormat(image)) View.VISIBLE else View.GONE
            viewHolder.selectedIcon.visibility =
                if (isSelected && !config.isShowNumberIndicator) View.VISIBLE else View.GONE
            if (isSelected && config.isShowNumberIndicator) {
                viewHolder.selectedNumber.visibility = View.VISIBLE
                viewHolder.unSelectedIcon.visibility = View.GONE
            } else {
                viewHolder.selectedNumber.visibility = View.GONE
                viewHolder.unSelectedIcon.visibility = View.VISIBLE
            }
//        viewHolder.selectedNumber.visibility =
//            if (isSelected && config.isShowNumberIndicator) View.VISIBLE else View.GONE
            if (viewHolder.selectedNumber.visibility == View.VISIBLE) {
                viewHolder.selectedNumber.text = (selectedIndex + 1).toString()
            }
            viewHolder.itemView.setOnClickListener {
                selectOrRemoveImage(image, position)
            }
        }
    }


    override fun getItemCount(): Int {
        return images.size
    }

    private fun setupItemForeground(view: View, isSelected: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.foreground = if (isSelected) ColorDrawable(
                ContextCompat.getColor(
                    mContext,
                    R.color.imagepicker_black_alpha_30
                )
            ) else null
        }
    }

    private fun selectOrRemoveImage(image: Image, position: Int) {
        if (config.isMultipleMode) {
            val selectedIndex = ImageHelper.findImageIndex(image, selectedImages)
            if (selectedIndex != -1) {
                selectedImages.removeAt(selectedIndex)
                notifyItemChanged(position, ImageUnselected())
                val indexes = ImageHelper.findImageIndexes(selectedImages, images)
                for (index in indexes) {
                    notifyItemChanged(index, ImageSelectedOrUpdated())
                }
            } else {
                if (selectedImages.size >= config.maxSize) {
                    val message =
                        if (config.limitMessage != null) config.limitMessage!! else String.format(
                            mContext.resources.getString(R.string.imagepicker_msg_limit_images),
                            config.maxSize
                        )
                    ToastHelper.show(mContext, message)
                    return
                } else {
                    selectedImages.add(image)
                    notifyItemChanged(position, ImageSelectedOrUpdated())
                }
            }
            imageSelectListener.onSelectedImagesChanged(selectedImages)
        } else {
            imageSelectListener.onSingleModeImageSelected(image)
        }
    }

    fun setData(images: List<Image>) {
        this.images.clear()
        this.images.addAll(images)
        notifyDataSetChanged()
    }

    fun setSelectedImages(selectedImages: ArrayList<Image>) {
        this.selectedImages.clear()
        this.selectedImages.addAll(selectedImages)
        notifyDataSetChanged()

    }

    class ImageViewHolder(itemView: View, isShowNumberIndicator: Boolean, indicatorColor: String) :
        RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image_thumbnail)
        val selectedIcon: ImageView = itemView.findViewById(R.id.image_selected_icon)
        val unSelectedIcon: ImageView = itemView.findViewById(R.id.image_un_selected_icon)
        val selectedNumber: TextView = itemView.findViewById(R.id.text_selected_number)
        val gifIndicator: View = itemView.findViewById(R.id.gif_indicator)

        init {
            val drawable: GradientDrawable =
                (if (isShowNumberIndicator) selectedNumber.background.mutate() else selectedIcon.background.mutate()) as GradientDrawable
            drawable.setColor(Color.parseColor(indicatorColor))
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageDate: TextView = itemView.findViewById(R.id.vl_item_image_date)
        val itemImageSize: TextView = itemView.findViewById(R.id.vl_item_image_size)
        fun bind(data: Image, mContext: Context) {
            itemImageDate.text = data.header
            itemImageSize.text = mContext.getString(R.string.vl_size_image, data.numberFiles)
        }
    }

    class ImageSelectedOrUpdated

    class ImageUnselected


}