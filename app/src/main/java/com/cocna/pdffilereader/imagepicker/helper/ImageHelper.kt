package com.cocna.pdffilereader.imagepicker.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.cocna.pdffilereader.imagepicker.model.Folder
import com.cocna.pdffilereader.imagepicker.model.Image
import java.io.FileDescriptor
import java.io.IOException


object ImageHelper {

    fun singleListFromImage(image: Image): ArrayList<Image> {
        val images = arrayListOf<Image>()
        images.add(image)
        return images
    }

    fun folderListFromImages(images: List<Image>): List<Folder> {
        val folderMap: MutableMap<Long, Folder> = LinkedHashMap()
        for (image in images) {
            val bucketId = image.bucketId
            val bucketName = image.bucketName
            var folder = folderMap[bucketId]
            if (folder == null) {
                bucketId?.let {
                    folder = Folder(bucketId, bucketName)
                    folderMap[bucketId] = folder!!
                }

            }
            folder?.images?.add(image)
        }
        return ArrayList(folderMap.values)
    }

    fun filterImages(images: ArrayList<Image>, bucketId: Long?): ArrayList<Image> {
        if (bucketId == null || bucketId == 0L) return images

        val filteredImages = arrayListOf<Image>()
        for (image in images) {
            if (image.bucketId == bucketId) {
                filteredImages.add(image)
            }
        }
        return filteredImages
    }

    fun findImageIndex(image: Image, images: ArrayList<Image>): Int {
        for (i in images.indices) {
            if (images[i].uri == image.uri) {
                return i
            }
        }
        return -1
    }

    fun findImageIndexes(subImages: ArrayList<Image>, images: ArrayList<Image>): ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        for (image in subImages) {
            for (i in images.indices) {
                if (images[i].uri == image.uri) {
                    indexes.add(i)
                    break
                }
            }
        }
        return indexes
    }


    fun isGifFormat(image: Image): Boolean {
        val fileName = image.name;
        val extension = if (fileName?.contains(".") == true) {
            fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length)
        } else ""

        return extension.equals("gif", ignoreCase = true)
    }

    fun getImageCollectionUri(): Uri {
        return if (DeviceHelper.isMinSdk29) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    fun uriToBitmap(mContext: Context?, selectedFileUri: Uri?): Bitmap? {
        try {
            if (mContext == null || selectedFileUri == null) return null
            val parcelFileDescriptor: ParcelFileDescriptor? = mContext.getContentResolver().openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor

            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}