package com.cocna.pdffilereader.ui.home.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.canhub.cropper.CropImageView
import com.cocna.pdffilereader.R
import com.cocna.pdffilereader.common.Logger
import com.cocna.pdffilereader.common.gone
import com.cocna.pdffilereader.common.invisible
import com.cocna.pdffilereader.common.visible
import com.cocna.pdffilereader.databinding.ItemImagePdfSelectedBinding
import com.cocna.pdffilereader.imagepicker.helper.ImageHelper
import com.cocna.pdffilereader.imagepicker.model.Image
import kotlin.collections.ArrayList

class ViewPagerRecyclerAdapter(private val mContext: Context?, arr: ArrayList<Image>) : RecycledPagerAdapter<RecycledPagerAdapter.ViewHolder?>() {
    var mLayoutInflater: LayoutInflater
    private var lstImage: ArrayList<Image>? = null
    private var layoutInflater: LayoutInflater? = null
    private var mOnCropImageAdapter: OnCropImageAdapter? = null

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
            viewHolder.bindData(mContext,image)
//            image?.let {
//                viewHolder.mCropImageView.tag = "VIEW$position"
//                viewHolder.mImageView.setImageURI(it.uri)
//                viewHolder.mCropImageView.setImageUriAsync(it.uri)
//                val rotate = image.rotate
//                Logger.showLog("Thuytv---init---rotate: $rotate ---name: " + image.name)
//                if (rotate > 0) {
//                    viewHolder.mImageView.rotation = rotate.toFloat()
//                }
////                viewHolder.mCropImageView.setOnCropImageCompleteListener { _, result ->
////                    Logger.showLog("Thuytv----onCropImageComplete: " + result?.isSuccessful)
////                    viewHolder.mImageView.setImageURI(result.uri)
////
////                }
//                Logger.showLog("Thuytv----isCrop: " + image.isCrop + " ---name: " + image.name)
//                if (image.isCrop == 2) {
////                    Handler(Looper.getMainLooper()).post {
////                        viewHolder.mCropImageView.cro(image.uri)
////                    }
//                    viewHolder.mCropImageView.croppedImage?.let {
//                        Logger.showLog("Thuytv--------bitmap-----11111")
//                        viewHolder.mImageView.setImageBitmap(it)
//                    }
//                    this.mOnCropImageAdapter?.onCropImage(viewHolder.mCropImageView, viewHolder.mImageView, image)
//
//                    viewHolder.mCropImageView.gone()
//                    viewHolder.mImageView.visible()
//                    image.isCrop = 0
//                } else if (image.isCrop == 1) {
//                    viewHolder.mCropImageView.visible()
//                    viewHolder.mImageView.gone()
//                } else {
//                    viewHolder.mCropImageView.gone()
//                    viewHolder.mImageView.visible()
//                }
//            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getCount(): Int {
        return lstImage?.size ?: 0
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {}
    class ImageViewHolder(itemView: View) : ViewHolder(itemView) {
        //        var mCropImageView: CropImageView = itemView.findViewById(R.id.imv_item_selected)
//        var mImageView: ImageView = itemView.findViewById(R.id.imv_item_selected_test)
        fun bindData(mContext: Context?, image: Image?) {

            image?.let {
                val binding = ItemImagePdfSelectedBinding.bind(itemView)
//                viewHolder.mCropImageView.tag = "VIEW$position"
                binding.imvItemSelectedImage.setImageURI(it.uri)

//                binding.imvCropImageView.setImageUriAsync(it.uri)
//                ImageHelper.uriToBitmap(mContext, image.uri)?.let {
//                    binding.imvCropImageView.setImageBitmap(it)
//                }

//                binding.imvCropImageView.load(it.uri).initialFrameRect(binding.imvCropImageView.actualCropRect).execute(object : LoadCallback {
//                    override fun onSuccess() {}
//                    override fun onError(e: Throwable) {}
//                })
                val rotate = image.rotate
                Logger.showLog("Thuytv---init---rotate: $rotate ---name: " + image.name)
                if (rotate > 0) {
                    binding.imvItemSelectedImage.rotation = rotate.toFloat()
                }

                Logger.showLog("Thuytv----isCrop: " + image.isCrop + " ---name: " + image.uri?.path)
//                if (image.isCrop == 2) {
////                    binding.imvCropImageView.crop(image.uri).execute( object : CropCallback{
////                        override fun onError(e: Throwable?) {
////                            e?.printStackTrace()
////                        }
////
////                        override fun onSuccess(cropped: Bitmap?) {
////                            Logger.showLog("Thuytv--------bitmap-----11111")
////                            binding.imvItemSelectedImage.setImageBitmap(cropped)
////                        }
////
////                    })
////                    binding.imvCropImageView.croppedImage?.let {
////                        Logger.showLog("Thuytv--------bitmap-----11111")
////                        binding.imvItemSelectedImage.setImageBitmap(it)
////                    }
////                    mOnCropImageAdapter?.onCropImage(binding.imvItemSelected, binding.imvItemSelectedImage, image)
//
////                    binding.imvCropImageView.invisible()
////                    binding.imvItemSelectedImage.visible()
//                    image.isCrop = 0
//                } else if (image.isCrop == 1) {
////                    binding.imvCropImageView.visible()
////                    binding.imvItemSelectedImage.invisible()
//                } else {
////                    binding.imvCropImageView.invisible()
////                    binding.imvItemSelectedImage.visible()
//                }
            }
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

    fun rotateImage(position: Int) {
        lstImage?.let {
            val image = it[position]
            if (image != null) {
                var rotate = image.rotate + 90
                if (rotate > 270) {
                    rotate = 0
                }
                Logger.showLog("Thuytv------rotate: $rotate---name:" + image.name)
                image.rotate = rotate
                notifyDataSetChanged()
            }
        }

    }

    fun cropImage(position: Int) {
//            it.rotateImage(180)
        lstImage?.let {
            it[position].isCrop = 1
            notifyDataSetChanged()
        }
    }
    fun cropImage(image: Image, position: Int) {
        lstImage?.set(position, image)
        notifyDataSetChanged()
    }

    fun saveCropImage(position: Int) {
        lstImage?.let {
            it[position].isCrop = 2
            notifyDataSetChanged()
        }

//        val mCropImageView: CropImageView? = itemView.findViewById<View>(R.id.imv_item_selected) as? CropImageView
//        val mImageView: ImageView? = itemView.findViewById<View>(R.id.imv_item_selected_test) as? ImageView
//        mCropImageView?.croppedImage?.let {
//            mImageView?.setImageBitmap(it)
//            Logger.showLog("Thuytv--------croppedImage---data")
//            mCropImageView?.gone()
//            mImageView?.visible()
//            notifyDataSetChanged()
//        }
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

    interface OnCropImageAdapter {
        fun onCropImage(mCropImageView: CropImageView, imageView: ImageView, image: Image)
    }

}