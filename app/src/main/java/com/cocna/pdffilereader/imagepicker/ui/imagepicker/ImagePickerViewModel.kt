package com.cocna.pdffilereader.imagepicker.ui.imagepicker

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.cocna.pdffilereader.imagepicker.helper.ImageHelper
import com.cocna.pdffilereader.imagepicker.model.CallbackStatus
import com.cocna.pdffilereader.imagepicker.model.Image
import com.cocna.pdffilereader.imagepicker.model.ImagePickerConfig
import com.cocna.pdffilereader.imagepicker.model.Result
import androidx.lifecycle.viewModelScope
import com.cocna.pdffilereader.common.Common
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat

class ImagePickerViewModel(application: Application) : AndroidViewModel(application) {

    private val contextRef = WeakReference(application.applicationContext)
    private lateinit var config: ImagePickerConfig
    private var job: Job? = null

    lateinit var selectedImages: MutableLiveData<ArrayList<Image>>
    val result = MutableLiveData(Result(CallbackStatus.IDLE, arrayListOf()))

    fun setConfig(config: ImagePickerConfig) {
        this.config = config
        selectedImages = MutableLiveData(config.selectedImages)
    }

    fun getConfig() = config

    fun fetchImages() {
        if (job != null) return

        result.postValue(Result(CallbackStatus.FETCHING, arrayListOf()))
        job = viewModelScope.launch() {
            try {
                val images = fetchImagesFromExternalStorage()
                result.postValue(Result(CallbackStatus.SUCCESS, images))
            } catch (e: Exception) {
                result.postValue(Result(CallbackStatus.SUCCESS, arrayListOf()))
            } finally {
                job = null
            }
        }
    }

    private suspend fun fetchImagesFromExternalStorage(): ArrayList<Image> {
        if (contextRef.get() == null) return arrayListOf()

        return withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED
            )

            val imageCollectionUri = ImageHelper.getImageCollectionUri()

            contextRef.get()!!.contentResolver.query(
                imageCollectionUri,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
            )?.use { cursor ->
                val images = arrayListOf<Image>()

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val bucketIdColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val dateModifiedColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val bucketId = cursor.getLong(bucketIdColumn)
                    val bucketName = cursor.getStringOrNull(bucketNameColumn) ?: ""
                    val dateModified = cursor.getStringOrNull(dateModifiedColumn) ?: ""

                    val uri = ContentUris.withAppendedId(imageCollectionUri, id)
                    val strDate = Common.covertDateLongToString(dateModified.toLongOrNull())

                    val image = Image(uri = uri,name=  name, bucketId = bucketId, bucketName = bucketName, dateModified = strDate)
                    images.add(image)
                }
                cursor.close()
                images
            } ?: throw IOException()
        }
    }
}