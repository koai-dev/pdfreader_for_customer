package com.cocna.pdffilereader.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.imagepicker.model.Image
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*
import kotlin.collections.ArrayList

class ViewPagerImageAdapter(val context: Context?, val manager: FragmentManager, private val imageList: ArrayList<Image>) : PagerAdapter() {
    // on below line we are creating a method 
    // as get count to return the size of the list.
    override fun getCount(): Int {
        return imageList.size
    }

    // on below line we are returning the object
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ConstraintLayout
    }

    // on below line we are initializing 
    // our item and inflating our layout file
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // on below line we are initializing 
        // our layout inflater.
        val mLayoutInflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater

        // on below line we are inflating our custom 
        // layout file which we have created.
        val itemView: View? = mLayoutInflater?.inflate(R.layout.item_image_pdf_selected, container, false)
        itemView?.tag = "VIEW$position"
        // on below line we are initializing 
        // our image view with the id.
        val imageView: CropImageView = itemView?.findViewById<View>(R.id.imv_item_selected) as CropImageView
        setCropImage(imageView, true)

//        imageView.setOnCropImageCompleteListener { view, result ->
//            Logger.showLog("Thuytv----onCropImageComplete: " + result?.isSuccessful)
//            notifyDataSetChanged()
//        }
        // on below line we are setting
        // image resource for image view.
        imageList[position].let {
//            Logger.showLog("Thuytv---init---rotate: ${it.rotate}")
//            imageView.setImageURI(it.uri)
//            imageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
//            GlideHelper.loadImage(imageView, it)
        }

        // on the below line we are adding this
        // item view to the container.
        itemView.apply {
            Objects.requireNonNull(container).addView(itemView)
        }


        // on below line we are simply 
        // returning our item view.
        return itemView
    }

    private fun setCropImage(imageView: CropImageView, isCrop: Boolean) {
//        imageView.isShowCropOverlay = isCrop
//        imageView.isAutoZoomEnabled = isCrop
//        imageView.isFlippedVertically = isCrop
//        imageView.isFlippedHorizontally = isCrop
        imageView.isEnabled = isCrop
    }

    // on below line we are creating a destroy item method.
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        // on below line we are removing view
        container.removeView(`object` as ConstraintLayout)
    }

    override fun getItemPosition(`object`: Any): Int {
        if (manager.fragments.contains(`object`)) {
            return POSITION_NONE
        }
        return POSITION_UNCHANGED
    }

    fun deleteImage(position: Int) {
        imageList.removeAt(position)
        notifyDataSetChanged()
    }

    fun rotateImage(itemView: View?, position: Int) {
//        imageList.removeAt(position)
        val imageView: CropImageView? = itemView?.findViewById<View>(R.id.imv_item_selected) as? CropImageView
        val image = imageList[position]
        if(image != null) {
            val rotate = image.rotate + 90
            Logger.showLog("Thuytv------rotate: $rotate")
//            imageView?.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
            image.rotate = rotate
//        imageView?.saveCroppedImageAsync(imageList[position].uri)
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
        imageList.clear()
        imageList.addAll(lstImage)
        notifyDataSetChanged()
    }
}