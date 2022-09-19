package com.cocna.pdffilereader.ui.home.adapter

import android.content.Context
import com.cocna.pdffilereader.ui.home.adapter.RecycledPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.imagepicker.model.Image
import com.theartofdev.edmodo.cropper.CropImageView
import kotlin.collections.ArrayList

class ViewPagerRecyclerAdapter(var mContext: Context?, arr: ArrayList<Image>) : RecycledPagerAdapter<RecycledPagerAdapter.ViewHolder?>() {
    var mLayoutInflater: LayoutInflater
    private var lstImage: ArrayList<Image>? = null
    private var layoutInflater: LayoutInflater? = null
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent!!.context)
        }
        val v = layoutInflater!!.inflate(R.layout.item_image_pdf_selected, parent, false)
        // Return view holder
        return ImageViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, position: Int) {
        if (viewHolder is ImageViewHolder) {
            val image = lstImage?.get(position)
            image?.let {

                viewHolder.imgTest.setImageURI(it.uri)
                viewHolder.img.setImageUriAsync(it.uri)
                setCropImage(viewHolder.img, false)
                val rotate = image.rotate
                Logger.showLog("Thuytv---init---rotate: $rotate ---name: " + image.name)
                if(rotate > 0) {
                    viewHolder.imgTest.rotation = rotate.toFloat()
                }
//                if (image.rotate == 90) {
//                    Logger.showLog("Thuytv---init---rotate: ${image.rotate}")
//                    viewHolder.img.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
//                } else if (image.rotate == 180) {
//                    viewHolder.img.rotateImage(CropImageView.RotateDegrees.ROTATE_180D)
//                } else if (image.rotate == 270) {
//                    viewHolder.img.rotateImage(CropImageView.RotateDegrees.ROTATE_270D)
//                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getCount(): Int {
        return lstImage?.size ?: 0
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {}
    class ImageViewHolder(v: View?) : ViewHolder(v!!) {
        var img: CropImageView
        var imgTest: ImageView

        init {
            img = itemView.findViewById<View>(R.id.imv_item_selected) as CropImageView
            imgTest = itemView.findViewById<ImageView>(R.id.imv_item_selected_test)
        }
    }

    init {
        this.lstImage = arr
        mLayoutInflater = mContext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun deleteImage(position: Int) {
        lstImage?.let {
            it.removeAt(position)
            notifyDataSetChanged()
        }

    }

    fun rotateImage(itemView: View?, position: Int) {
//        imageList.removeAt(position)
        lstImage?.let {
//            val imageView: CropImageView? = itemView?.findViewById<View>(R.id.imv_item_selected) as? CropImageView
            val image = it[position]
            if (image != null) {
                val rotate = image.rotate + 90
                Logger.showLog("Thuytv------rotate: $rotate---name:" + image.name)
//                imageView?.rotateImage(com.isseiaoki.simplecropview.CropImageView.RotateDegrees.ROTATE_90D)
                image.rotate = rotate
                notifyDataSetChanged()
//        imageView?.saveCroppedImageAsync(imageList[position].uri)
            }
        }

    }

    fun cropImage(itemView: View?, position: Int) {
        val imageView: CropImageView? = itemView?.findViewById<View>(R.id.imv_item_selected) as? CropImageView
        imageView?.let {
//            it.rotateImage(180)
            setCropImage(imageView, true)
        }
    }

    fun saveCropImage(itemView: View?, position: Int) {
        val imageView: CropImageView? = itemView?.findViewById<View>(R.id.imv_item_selected) as? CropImageView
        imageView?.let {
//            it.saveCroppedImageAsync(imageList[position].uri)
            setCropImage(imageView, false)

        }

    }

    fun updateImage(lstImage: ArrayList<Image>) {
        this.lstImage?.let {
            it.clear()
            it.addAll(lstImage)
            notifyDataSetChanged()
        }

    }

    private fun setCropImage(imageView: CropImageView, isCrop: Boolean) {
//        imageView.isShowCropOverlay = isCrop
//        imageView.isAutoZoomEnabled = isCrop
//        imageView.isFlippedVertically = isCrop
//        imageView.isFlippedHorizontally = isCrop
        imageView.isEnabled = isCrop
    }

}